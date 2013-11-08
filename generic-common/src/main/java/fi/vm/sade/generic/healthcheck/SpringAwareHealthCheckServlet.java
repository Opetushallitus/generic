package fi.vm.sade.generic.healthcheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Health check servlet, joka on tietoinen springistä, ja mm:
 * ==========================================================
 *
 * - ajaa healthcheckit kaikille spring beaneille jotka toteuttavat HealthChecker -rajapinnan
 * - ajaa healthcheckit tietokannalle (kaivaa spring datasourcen)
 * - kertoo kannan data_status -taulun sisällön
 * - ajaa itsensä myös sovelluksen startissa ja kirjoittaa logiin jos ongelmia
 *
 *
 * Käyttöönotto:
 * =============
 *
 * 1. Mäppää tämä (tai tästä peritty) luokka urliin /healthcheck
 *
 *      @WebServlet(urlPatterns = "/healthcheck", loadOnStartup = 9)
 *      public class HealthCheckServlet extends SpringAwareHealthCheckServlet { }
 *
 *      TAI
 *
 *      <servlet>
 *          <servlet-name>healthcheck</servlet-name>
 *          <servlet-class>fi.vm.sade.generic.healthcheck.SpringAwareHealthCheckServlet</servlet-class>
 *      </servlet>
 *      <servlet-mapping>
 *          <servlet-name>healthcheck</servlet-name>
 *          <url-pattern>/healthcheck</url-pattern>
 *      </servlet-mapping>
 *
 *      HUOM:
 *      - Vaatii gson -dependencyn toimiakseen (vaihtoehtoisesti voinee ylikirjoittaa toJson -metodin)
 *
 * 2. Lisää tarvittaessa springin app contexiin uusia beaneja, jotka toteuttaa HealthChecker -interfacen
 *
 *
 * Kustomointi jos tarvetta, esim:
 * ===============================
 *
 * - Ylikirjoita registerHealthCheckers -metodi jos haluat lisätä muita checkereitä
 * - Toteuta afterHealthCheck -metodi, jos haluat tehdä jotain spesifiä kaikkien tarkastuksien jälkeen
 * - Huom, pääset käsiksi spring app ctx:iin, ctx -muuttujan kautta mikäli tarvetta
 *
 *
 * Speksi:
 * =======
 *
 * - https://liitu.hard.ware.fi/confluence/display/PROG/Healthcheck+url
 *
 *
 * TODO: sisäinen cachetus esim 10 sekunniksi
 *
 * @see HealthChecker
 * @author Antti Salonen
 */
public class SpringAwareHealthCheckServlet extends HttpServlet {

    public static final String OK = "OK";
    public static final String STATUS = "status";

    private static final Logger log = LoggerFactory.getLogger(SpringAwareHealthCheckServlet.class);
    protected ApplicationContext ctx;

    @Autowired(required = false)
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        log.info("init healthcheck servlet");

        // autowire
        ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        ctx.getAutowireCapableBeanFactory().autowireBean(this);
        log.info("initial health check:\n" + toJson(doHealthCheck()));
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("application/json");
            Map<String, Object> result = doHealthCheck();
            String resultJson = toJson(result);
            if (result == null || !OK.equals(result.get(STATUS))) { // log status != ok
                log.warn("healthcheck failed:\n" + resultJson);
            }
            resp.getWriter().print(resultJson);
        } catch (Throwable e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(resp.getWriter());
        }
    }

    protected String toJson(Object o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }

    protected Map<String, Object> doHealthCheck() {
        StringBuffer errorString = new StringBuffer("");

        // register healthcheckers
        Map<String, HealthChecker> checkers = registerHealthCheckers();

        // invoke all healthcheckers
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("contextPath", getServletContext().getContextPath());
        result.put("checks", new LinkedHashMap());
        for (String checkerName : checkers.keySet()) {
            HealthChecker healthChecker = checkers.get(checkerName);
            log.debug("healthcheck calling checker: " + checkerName);
            doHealthChecker(errorString, result, checkerName, healthChecker);
        }

        // set app's health check status
        if (errorString.length() == 0) {
            result.put(STATUS, OK);
        } else {
            result.put(STATUS, "ERROR: " + errorString);
        }

        //
        afterHealthCheck(result, checkers);

        return result;
    }

    protected Map<String, HealthChecker> registerHealthCheckers() {
        Map<String, HealthChecker> checkers = ctx.getBeansOfType(HealthChecker.class);
        addDatabaseChecker(checkers); // by default check databases
        return checkers;
    }

    protected void doHealthChecker(StringBuffer errorString, LinkedHashMap<String, Object> result, String checkerName, HealthChecker healthChecker) {
        try {
            Object res = healthChecker.checkHealth();

            // if check ok, put the response into healtcheck results check
            log.debug("healthcheck called healthchecker ok: " + checkerName + ", result: " + res);
            if (res == null || (res instanceof Collection && ((Collection) res).isEmpty())) res = OK;
            if (res instanceof Map) res = new LinkedHashMap((Map)res); // gson ei tykkää sisäkkäisistä normimäpeistä :-o
            ((Map)result.get("checks")).put(checkerName, res);

        } catch (Throwable e) {
            log.warn("error in healthchecker '" + checkerName + "': " + e, e);

            // if check failed, put the error into healtcheck results
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            result.put(checkerName, "ERROR: " + e.getMessage());
            errorString.append(checkerName).append("=").append(e.getMessage()).append(", ");
        }
    }

    protected void addDatabaseChecker(Map<String, HealthChecker> checkers) {
        checkers.put("database", new HealthChecker() {
            @Override
            public Object checkHealth() throws Throwable {
                if (dataSource != null) {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    DatabaseMetaData dbMetaData = dataSource.getConnection().getMetaData();
                    result.put("url", dbMetaData.getURL());
                    ResultSet rs = dbMetaData.getTables(null, null, "DATA_STATUS", null);
                    boolean dataStatusTableExists = rs.next();
                    if (dataStatusTableExists) {
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM data_status ORDER BY muutoshetki");
                        result.put("data_status", list);
                    }

                    //
                    // Get count information from database tables
                    //
                    List<Map<String, Object>> countList = new ArrayList<Map<String, Object>>();
                    rs = dbMetaData.getTables(null, null, "%" ,new String[] {"TABLE"});
                    while(rs.next()) {
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                        String tableName = rs.getString("TABLE_NAME");
                        countList.add(jdbcTemplate.queryForMap("SELECT '" + tableName + "' AS table, COUNT(*) AS count FROM " + tableName));
                    }
                    if (countList.size() != 0) {
                        result.put("counts", countList);
                    }

                    return result;
                } else {
                    return "N/A";
                }
            }
        });
    }

    protected void afterHealthCheck(Map<String, Object> result, Map<String, HealthChecker> checkers) {
    }

}
