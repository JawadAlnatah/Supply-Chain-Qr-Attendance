package com.team.supplychain.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

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
    private Server server;

    /**
     * Start the embedded web server
     */
    public void start() throws Exception {
        server = new Server(PORT);

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
            System.out.println("  Server is running on port: " + PORT);
            System.out.println();
            System.out.println("  📱 Access the QR Scanner:");
            System.out.println("     Local:   http://localhost:" + PORT + "/scanner.html");
            System.out.println("     Network: http://" + hostname + ":" + PORT + "/scanner.html");
            System.out.println();
            System.out.println("  🔌 API Endpoint:");
            System.out.println("     POST http://localhost:" + PORT + "/api/attendance/scan");
            System.out.println();
            System.out.println("  📋 Test API:");
            System.out.println("     GET http://localhost:" + PORT + "/api/attendance/scan");
            System.out.println();
            System.out.println("  💡 Tips:");
            System.out.println("     - Use the network URL to access from mobile devices");
            System.out.println("     - Make sure your phone and PC are on the same WiFi");
            System.out.println("     - Press Ctrl+C to stop the server");
            System.out.println();
            System.out.println("=".repeat(70));
            System.out.println();

        } catch (Exception e) {
            System.out.println("Server started on port: " + PORT);
            System.out.println("Access at: http://localhost:" + PORT + "/scanner.html");
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
