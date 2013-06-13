package fi.vm.sade.generic.rest;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Helper class to start embedded jetty + jersey for tests.
 *
 * @author Antti Salonen
 */
public class JettyJersey {
    static Server server;

    public static void startServer(int port, String packageContainingJerseyRestResources, String jerseyFilterClasses) throws Exception {
        server = new Server(port);
        Context root = new Context(server, "/", Context.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        servletHolder.setInitParameter("com.sun.jersey.config.property.packages", packageContainingJerseyRestResources);
//        servletHolder.setInitParameter("com.sun.jersey.config.feature.Debug", "true");
//        servletHolder.setInitParameter("com.sun.jersey.config.feature.Trace", "true");
//        servletHolder.setInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.LoggingFilter");
        servletHolder.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters", /*"com.sun.jersey.api.container.filter.LoggingFilter,"*/""+(jerseyFilterClasses != null ? jerseyFilterClasses : ""));
        root.addServlet(servletHolder, "/*");
        server.start();
    }

    public static void stopServer() throws Exception {
        server.stop();
    }
}
