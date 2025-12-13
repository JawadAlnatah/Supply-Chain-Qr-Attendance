package com.team.supplychain.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetAddress;

/**
 * Embedded Jetty Web Server Launcher for QR Code Attendance Scanner
 *
 * This launches a lightweight web server that:
 * 1. Serves the QR scanner HTML interface (static files)
 * 2. Provides REST API endpoint for attendance operations
 *
 * Default URL: http://localhost:8080/scanner.html
 * API Endpoint: http://localhost:8080/api/attendance/scan
 *
 * Usage:
 *   java -cp target/classes com.team.supplychain.api.WebServerLauncher
 *   OR
 *   Run this class directly from your IDE
 */
public class WebServerLauncher {

    private static final int PORT = 8080;
    private static final int HTTPS_PORT = 8443;
    private Server server;

    /**
     * Start the embedded web server
     */
    public void start() throws Exception {
        server = new Server();

        // HTTP Configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(HTTPS_PORT);

        // HTTPS Configuration
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // SSL Context Factory
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.p12");
        sslContextFactory.setKeyStorePassword("changeit");
        sslContextFactory.setKeyManagerPassword("changeit");

        // HTTP Connector (port 8080) - keep for backward compatibility
        ServerConnector httpConnector = new ServerConnector(server,
            new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(PORT);

        // HTTPS Connector (port 8443) - main connector for mobile camera access
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConfig));
        httpsConnector.setPort(HTTPS_PORT);

        // Add both connectors to server
        server.addConnector(httpConnector);
        server.addConnector(httpsConnector);

        // Create servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Register AttendanceServlet for API endpoint
        ServletHolder attendanceServlet = new ServletHolder("attendance", new AttendanceServlet());
        context.addServlet(attendanceServlet, "/api/attendance/scan");

        // Serve static files (HTML, CSS, JS) from src/main/webapp
        String resourceBase = getWebappResourceBase();
        System.out.println("Serving static files from: " + resourceBase);

        ServletHolder staticServlet = new ServletHolder("static", DefaultServlet.class);
        staticServlet.setInitParameter("resourceBase", resourceBase);
        staticServlet.setInitParameter("dirAllowed", "false");
        staticServlet.setInitParameter("pathInfoOnly", "false");
        staticServlet.setInitParameter("welcomeServlets", "false");
        context.addServlet(staticServlet, "/*");

        server.setHandler(context);

        // Start server
        server.start();

        // Print access information
        printStartupInfo();

        // Wait for server to be stopped
        server.join();
    }

    /**
     * Stop the web server
     */
    public void stop() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
            System.out.println("Web server stopped");
        }
    }

    /**
     * Get the webapp resource base directory
     */
    private String getWebappResourceBase() {
        try {
            // First try to find webapp in the file system (development mode)
            java.io.File webappDir = new java.io.File("src/main/webapp");
            if (webappDir.exists() && webappDir.isDirectory()) {
                return webappDir.getAbsolutePath();
            }

            // Try classpath resource (when packaged)
            Resource webappResource = Resource.newClassPathResource("webapp");
            if (webappResource != null && webappResource.exists()) {
                return webappResource.toString();
            }

            // Fallback
            return new java.io.File("src/main/webapp").getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.io.File("src/main/webapp").getAbsolutePath();
        }
    }

    /**
     * Print server startup information
     */
    private void printStartupInfo() {
        try {
            String hostname = InetAddress.getLocalHost().getHostAddress();

            System.out.println("\n" + "=".repeat(70));
            System.out.println("  QR CODE ATTENDANCE SCANNER - WEB SERVER STARTED");
            System.out.println("=".repeat(70));
            System.out.println();
            System.out.println("  Server is running on ports: " + PORT + " (HTTP) & " + HTTPS_PORT + " (HTTPS)");
            System.out.println();
            System.out.println("  📱 Access the QR Scanner:");
            System.out.println("     Local HTTP:    http://localhost:" + PORT + "/scanner.html");
            System.out.println("     Local HTTPS:   https://localhost:" + HTTPS_PORT + "/scanner.html");
            System.out.println("     Network HTTPS: https://" + hostname + ":" + HTTPS_PORT + "/scanner.html");
            System.out.println();
            System.out.println("  🔌 API Endpoints:");
            System.out.println("     POST https://localhost:" + HTTPS_PORT + "/api/attendance/scan");
            System.out.println("     POST http://localhost:" + PORT + "/api/attendance/scan");
            System.out.println();
            System.out.println("  ⚠️  MOBILE DEVICE USERS:");
            System.out.println("     - Use HTTPS URL for camera access: https://" + hostname + ":" + HTTPS_PORT + "/scanner.html");
            System.out.println("     - Accept the security warning (self-signed certificate)");
            System.out.println("     - Camera permissions will then work correctly");
            System.out.println();
            System.out.println("  💡 Tips:");
            System.out.println("     - Make sure your phone and PC are on the same WiFi");
            System.out.println("     - HTTP works for desktop, but camera needs HTTPS on mobile");
            System.out.println("     - Press Ctrl+C to stop the server");
            System.out.println();
            System.out.println("=".repeat(70));
            System.out.println();

        } catch (Exception e) {
            System.out.println("Server started on ports: " + PORT + " (HTTP) & " + HTTPS_PORT + " (HTTPS)");
            System.out.println("Access at: https://localhost:" + HTTPS_PORT + "/scanner.html");
        }
    }

    /**
     * Main method to launch the server
     */
    public static void main(String[] args) {
        WebServerLauncher launcher = new WebServerLauncher();

        try {
            System.out.println("Starting QR Code Attendance Scanner Web Server...");
            launcher.start();

        } catch (Exception e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
