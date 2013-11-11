package fi.vm.sade.support.selenium;

import com.bsb.common.vaadin.embed.EmbedVaadinServer;
import com.bsb.common.vaadin.embed.EmbedVaadinServerBuilder;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.generic.ui.blackboard.SimpleBlackboardProvider;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

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
 * - remember @ContextConfiguration and to override initPageObjects -method
 *
 * @author Antti Salonen
 */
public abstract class AbstractEmbedVaadinTest<COMPONENT extends Component> extends SeleniumTestCaseSupport {

    protected EmbedVaadinServer server;
    protected COMPONENT component;
    protected Application application;
    private boolean createComponent;
    private boolean startVaadinOnSetup;

    public AbstractEmbedVaadinTest() {
        initBlackboardContext();
    }

    public AbstractEmbedVaadinTest(WebDriver driver) {
        super(driver);
        initBlackboardContext();
    }

    public AbstractEmbedVaadinTest(boolean createComponent, boolean startVaadinOnSetup) {
        this();
        this.createComponent = createComponent;
        this.startVaadinOnSetup = startVaadinOnSetup;
    }

    private void initBlackboardContext() {
        BlackboardContext.setBlackboardProvider(new SimpleBlackboardProvider());
    }

    @Before
    public void setUp() throws Exception {
        startSelenium();

        if (createComponent) {
            component = createComponent();
            initComponent(component);
        }
        if (startVaadinOnSetup) {
            EmbedVaadinServerBuilder builder = createEmbedVaadinBuilder();
            startEmbedVaadin(builder);
        }

        initPageObjects();
    }

    protected COMPONENT createComponent() throws Exception {
        return (COMPONENT) findGenericType().newInstance();
    }

    /**
     * Meant for overriding if component initializing needs to be customized
     */
    protected void initComponent(COMPONENT component) {
    }

    @SuppressWarnings("unchecked")
    protected Class findGenericType() {
        try {
            Class clazz = (Class) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
            return clazz;
        } catch (ClassCastException e) {
            throw new RuntimeException("Application class is null. Please define valid class on overridden generic class.", e);
        }
    }

     @Override
    public void tearDown() throws Exception {
        stopEmbedVaadin();
    }

    public COMPONENT startEmbedVaadin(COMPONENT component) {
        this.component = component;
        EmbedVaadinServerBuilder builder = createEmbedVaadinBuilder();
        startEmbedVaadin(builder);
        return component;
    }

    public void startEmbedVaadin(EmbedVaadinServerBuilder builder) {
        long t0 = System.currentTimeMillis();

        // first stop vaadin if it's running
        stopEmbedVaadin();

        // generate debugId's for vaadin components that don't have it already
        TestUtils.generateIds(component);

        // use specific port?
        String overridePort = TestUtils.getEnvOrSystemProperty(null, "EMBED_VAADIN_PORT", "embedVaadinPort");
        if (overridePort != null) {
            builder.withHttpPort(Integer.parseInt(overridePort));
        }

        // start embed vaadin, set port+baseurl, navigate to first page with browser
        server = builder.wait(false).start();
        //SeleniumContext.setHttpPort(server.getConfig().getPort());
        SeleniumContext.setBaseUrl("http://localhost:"+server.getConfig().getPort());
        if (driver != null) {
            driver.get(SeleniumContext.getBaseUrl());
        }
        log.info("started EmbedVaadin, baseUrl: "+SeleniumContext.getBaseUrl()+", took: "+(System.currentTimeMillis()-t0)+"ms");
    }


    protected EmbedVaadinServerBuilder createEmbedVaadinBuilder() {
        return EmbedVaadin.forComponent(component);
    }

    public void stopEmbedVaadin() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    protected  <T extends Component> T getComponentByType(Class<T> clazz) {
        return TestUtils.getComponentsByType(component, clazz).get(0);
    }

}