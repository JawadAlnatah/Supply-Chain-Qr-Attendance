package com.team.supplychain.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database connection management using HikariCP connection pooling
 * for optimal performance with TiDB Cloud.
 *
 * WHY HikariCP?
 * Creating a new connection to TiDB Cloud takes 500ms+ (network latency + SSL handshake).
 * Connection pooling keeps connections alive and reuses them = 100x faster queries.
 *
 * Key improvements over old singleton pattern:
 * - Connection pooling (10 connections ready to use)
 * - Thread-safe (multiple controllers can query DB simultaneously)
 * - Automatic connection validation (detects dead connections)
 * - Configurable pool size and timeouts via config.properties
 *
 * NOTE: When you close() a connection from this pool, it doesn't actually close -
 * it just returns the connection to the pool for reuse. That's the magic!
 */
public class DatabaseConnection {
    private static HikariDataSource dataSource;
    private static Properties props = new Properties();

    // Static initializer block runs ONCE when class is first loaded
    // This happens before any other code tries to get a connection
    static {
        try {
            // Load database config from src/main/resources/config.properties
            InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input != null) {
                props.load(input);
                System.out.println("✓ Config loaded from config.properties");
            } else {
                // Fallback to hardcoded defaults if file is missing
                // Not ideal for production but useful for quick testing
                System.out.println("⚠ config.properties not found, using default values");
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading config.properties: " + e.getMessage());
            e.printStackTrace();
        }

        // Shutdown hook ensures connections close cleanly when app exits
        // This prevents "connection leak" warnings in TiDB Cloud
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                System.out.println("✓ Connection pool closed on shutdown");
            }
        }));
    }

    /**
     * Initialize the HikariCP connection pool with configuration from properties file.
     * This is called automatically on first getConnection() call.
     *
     * Why synchronized?
     * Multiple threads might call getConnection() at the same time during app startup.
     * We only want to create the pool ONCE, so we synchronize to prevent race conditions.
     */
    private static synchronized void initializePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            return; // Pool already initialized - don't create a second one
        }

        try {
            // Get database configuration from properties file
            // Defaults are TiDB Cloud production credentials (should be in config.properties)
            String url = props.getProperty("db.url",
                    "jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY");
            String username = props.getProperty("db.username", "3uB8fqJmu4peKdN.root");
            String password = props.getProperty("db.password", "46dmNGakAQIh5Q0v");

            // Pool size and timeout configuration
            int maxPoolSize = Integer.parseInt(props.getProperty("db.maxConnections", "10"));  // Max 10 concurrent connections
            long connectionTimeout = Long.parseLong(props.getProperty("db.connectionTimeout", "30000"));  // Wait up to 30s for connection

            // Create HikariCP configuration object
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");  // MySQL driver (TiDB is MySQL-compatible)

            // Connection pool size settings
            config.setMaximumPoolSize(maxPoolSize);  // Maximum 10 connections to avoid overwhelming TiDB
            config.setMinimumIdle(2);  // Always keep 2 connections ready (prevents cold start delays)
            config.setConnectionTimeout(connectionTimeout);  // How long to wait for an available connection
            config.setIdleTimeout(600000);  // Close idle connections after 10 minutes
            config.setMaxLifetime(1800000);  // Refresh connections every 30 minutes (prevents stale connections)

            // Health check query - validates connection is alive before using it
            config.setConnectionTestQuery("SELECT 1");

            // Pool name shows up in logs and monitoring tools
            config.setPoolName("SupplyChainPool");

            // Performance tuning for MySQL/TiDB
            // These properties squeeze extra performance out of the MySQL driver

            // PreparedStatement caching - reuses compiled queries instead of parsing each time
            config.addDataSourceProperty("cachePrepStmts", "true");  // Enable statement caching
            config.addDataSourceProperty("prepStmtCacheSize", "250");  // Cache up to 250 different queries
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");  // Max query length to cache (2KB)

            // Server-side prepared statements - let TiDB compile queries once, reuse many times
            config.addDataSourceProperty("useServerPrepStmts", "true");

            // Session state optimization - reduces back-and-forth with database
            config.addDataSourceProperty("useLocalSessionState", "true");  // Track session state locally

            // Batch optimization - combines multiple INSERT/UPDATE into one network call
            config.addDataSourceProperty("rewriteBatchedStatements", "true");  // e.g., 10 INSERTs → 1 batch INSERT

            // Metadata caching - don't ask TiDB for table structure every query
            config.addDataSourceProperty("cacheResultSetMetadata", "true");  // Cache column names, types, etc.
            config.addDataSourceProperty("cacheServerConfiguration", "true");  // Cache server capabilities

            // Skip redundant operations - don't send same command twice
            config.addDataSourceProperty("elideSetAutoCommits", "true");  // Don't send SET autocommit if already set

            // Disable time tracking - we don't need microsecond precision stats
            config.addDataSourceProperty("maintainTimeStats", "false");  // Saves a bit of CPU

            // Create the data source (connection pool)
            dataSource = new HikariDataSource(config);

            System.out.println("✓ HikariCP connection pool initialized successfully!");
            System.out.println("  Pool name: " + dataSource.getPoolName());
            System.out.println("  Max pool size: " + maxPoolSize);
            System.out.println("  Connection timeout: " + connectionTimeout + "ms");
            System.out.println("  Database: TiDB Cloud");

        } catch (Exception e) {
            System.err.println("✗ Failed to initialize connection pool!");
            e.printStackTrace();
            throw new RuntimeException("Could not initialize database connection pool", e);
        }
    }

    /**
     * Get a connection from the pool.
     * The connection is automatically returned to the pool when closed (via try-with-resources).
     *
     * IMPORTANT: Always use try-with-resources pattern:
     * <pre>
     * try (Connection conn = DatabaseConnection.getConnection()) {
     *     // Use connection here
     * } // Connection returns to pool automatically here
     * </pre>
     *
     * The connection isn't really "closed" - it's returned to the pool for reuse.
     * This is WAY faster than creating a new connection every time (500ms vs 1ms).
     *
     * @return A database connection from the pool
     * @throws SQLException if unable to get a connection (usually means pool is exhausted)
     */
    public static Connection getConnection() throws SQLException {
        // Lazy initialization - pool is created on first call, not at class load
        if (dataSource == null || dataSource.isClosed()) {
            initializePool();
        }

        try {
            // Get a connection from the pool (blocks if all 10 connections are in use)
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("✗ Failed to get connection from pool: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Shutdown the connection pool.
     * Call this when the application is closing.
     * Note: A shutdown hook is already registered to close the pool automatically.
     */
    public static synchronized void shutdownPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Connection pool shut down successfully");
        }
    }

    /**
     * Get pool statistics for monitoring.
     *
     * @return String containing pool statistics
     */
    public static String getPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format(
                    "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool not initialized";
    }

    /**
     * Test method to verify database connectivity.
     * Useful for debugging connection issues.
     */
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connection test successful!");
                System.out.println("  Database: " + conn.getCatalog());
                System.out.println("  URL: " + conn.getMetaData().getURL());
                System.out.println("  " + getPoolStats());
                conn.close(); // Return to pool
            }
        } catch (SQLException e) {
            System.err.println("✗ Connection test failed!");
            e.printStackTrace();
        }
    }
}
