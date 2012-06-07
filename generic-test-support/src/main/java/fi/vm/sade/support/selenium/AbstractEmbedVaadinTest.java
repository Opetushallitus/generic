package fi.vm.sade.support.selenium;

import com.bsb.common.vaadin.embed.EmbedVaadinServer;
import com.bsb.common.vaadin.embed.EmbedVaadinServerBuilder;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Super class for running embed vaadin selenium tests against some vaadin component.
 * Supports model where you:
 *
 * 1) init application or just single ui component
 * 2) run selenium operations on browser
 * 3) make asserts against backing java objects instead of browser views
 *
 * Features:
 *
 *  - selenium & reporting & single browser window & spring (see SeleniumTestCaseSupport for these)
 *  - embed vaadin for generic component type (tomcat+vaadin started inside the test)
 *  - access selenium page objects AND vaadin/service objects inside same jvm
 *
 * NOTE:
 * - uses random port, unless "EMBED_VAADIN_PORT"-envvar or "embedVaadinPort"-systemproperty is given
 * - access vaadin component/application object via 'component' and 'application' -fields
 *
 * TODO: koodiston puolelta aspect-pohjainen component-pageobject-systeemi tänne
 * TODO: selkeämpi support applicationille ja componentille (nyt ruma ja esim. component/application-fieldit jää nulliksi nyt)
 * TODO: yksinkertaista organisaatio selenium perintärakennetta
 * TODO: testcasereporterin todo-kohta
 *
 * @author Antti Salonen
 */
public abstract class AbstractEmbedVaadinTest<COMPONENT extends Component> extends SeleniumTestCaseSupport {

    protected EmbedVaadinServer server;
    protected COMPONENT component;
    protected Application application;
    private boolean createComponentAndStartVaadinOnSetup;

    public AbstractEmbedVaadinTest() {
    }

    public AbstractEmbedVaadinTest(WebDriver driver) {
        super(driver);
    }

    public AbstractEmbedVaadinTest(boolean createComponentAndStartVaadinOnSetup) {
        this();
        this.createComponentAndStartVaadinOnSetup = createComponentAndStartVaadinOnSetup;
    }

    @Before
    public void setUp() throws Exception {
        startSelenium();

        if (createComponentAndStartVaadinOnSetup) {
            component = createComponent();
            initComponent(component);
            startEmbedVaadin(component);
        }

        initPageObjects();
    }

    protected COMPONENT createComponent() throws Exception {
        return findGenericType().newInstance();
    }

    /**
     * Meant for overriding if component initializing needs to be customized
     */
    protected void initComponent(Component component) {
    }

    @SuppressWarnings("unchecked")
    private Class<COMPONENT> findGenericType() {
        try {
            Class<COMPONENT> clazz = (Class<COMPONENT>) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
            return clazz;
        } catch (ClassCastException e) {
            throw new RuntimeException("Application class is null. Please define valid class on overridden generic class.", e);
        }
    }

     @Override
    public void tearDown() throws Exception {
        stopEmbedVaadin();
    }

    public <T extends Component> T startEmbedVaadin(T component) {
        long t0 = System.currentTimeMillis();

        // first stop vaadin if it's running
        stopEmbedVaadin();

        // generate id's for vaadin components that don't have it already
        TestUtils.generateIds(component);

        // create embed vaadin builder for component
        EmbedVaadinServerBuilder builder = createEmbedVaadin(component);

        // configure embed vaadin builder
        File moduleBaseDir = getModuleBaseDir();
        File rootDir = null;
        try {
            rootDir = new File(moduleBaseDir, "target/" + moduleBaseDir.getCanonicalFile().getName());
            log.info("vaadin root dir: "+rootDir.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        builder
                .withContextRootDirectory(rootDir)
                .withWidgetSet("fi.vm.sade.widgetset.GoogleMapComponent")
                .wait(false);

        // use specific port?
        String overridePort = TestUtils.getEnvOrSystemProperty(null, "EMBED_VAADIN_PORT", "embedVaadinPort");
        if (overridePort != null) {
            builder.withHttpPort(Integer.parseInt(overridePort));
        }

        // start embed vaadin, set port+baseurl, navigate to first page with browser
        server = builder.start();
        SeleniumContext.setHttpPort(server.getConfig().getPort());
        if (driver != null) {
            driver.get(SeleniumContext.getBaseUrl());
        }
        log.info("started EmbedVaadin, baseUrl: "+SeleniumContext.getBaseUrl()+", took: "+(System.currentTimeMillis()-t0)+"ms");

        this.component = (COMPONENT) component;
        if (component != null) {
            this.application = component.getApplication();
        }
        return component;
    }

    protected <T extends Component> EmbedVaadinServerBuilder createEmbedVaadin(T component) {
        return EmbedVaadin.forComponent(component);
    }

    public void stopEmbedVaadin() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    protected File getModuleBaseDir() {
        return new File(".");
    }

    protected  <T extends Component> T getComponentByType(Class<T> clazz) {
        return TestUtils.getComponentsByType(component, clazz).get(0);
    }

}