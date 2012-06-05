package fi.vm.sade.support.selenium;

import com.bsb.common.vaadin.embed.EmbedVaadinServer;
import com.bsb.common.vaadin.embed.EmbedVaadinServerBuilder;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Iterator;

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
 * TODO: genericsit kehiin ja automaattinen ylikirjoitettava komponentin luominen setupissa
 * TODO: koodiston puolelta aspect-pohjainen component-pageobject-systeemi tänne
 * TODO: selkeämpi support applicationille ja componentille (nyt ruma ja esim. component/application-fieldit jää nulliksi nyt)
 * TODO: yksinkertaista organisaatio selenium perintärakennetta
 * TODO: testcasereporterin todo-kohta
 *
 * @author Antti Salonen
 */
public abstract class AbstractEmbedVaadinTest extends SeleniumTestCaseSupport {

    protected EmbedVaadinServer server;
    protected Component component;
    protected Application application;

    public AbstractEmbedVaadinTest() {
    }

    public AbstractEmbedVaadinTest(WebDriver driver) {
        super(driver);
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
        generateIds(component);

        // create embed vaadin builder for component
        EmbedVaadinServerBuilder builder = createEmbedVaadin(component);

        // configure embed vaadin builder
        builder
                .withContextRootDirectory(new File(getModuleBaseDir(), "target/organisaatio-app"))
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
        this.component = component;
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

    private static long nextDebugId = 0;
    public void generateIds(Component component) {
        if (component == null) {
            return;
        }

        // generate debugid if not present
        if (component.getDebugId() == null) {
            String id = "generatedId_" + (++nextDebugId);
            component.setDebugId(id);
        }

        // recursion
        if (component instanceof ComponentContainer) {
            ComponentContainer container = (ComponentContainer) component;
            Iterator<Component> iterator = container.getComponentIterator();
            while (iterator.hasNext()) {
                generateIds(iterator.next());
            }
        }
        if (component instanceof Window) {
            Window window = (Window) component;
            for (Window child : window.getChildWindows()) {
                generateIds(child);
            }
        }
        if (component instanceof Form) {
            Form form = (Form) component;
            generateIds(form.getLayout());
        }
    }

    protected File getModuleBaseDir() {
        return new File(".");
    }

}