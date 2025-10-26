package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;

    public DatabaseManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public boolean initialize() {
        String storageType = configManager.getStorageType();

        try {
            if (storageType.equals("mysql")) {
                initializeMySQL();
            } else {
                initializeSQLite();
            }

            createTables();
            plugin.getLogger().info("✓ Database initialized successfully (" + storageType + ")");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Failed to initialize database", e);
            return false;
        }
    }

    private void initializeMySQL() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" +
                configManager.getMySQLHost() + ":" +
                configManager.getMySQLPort() + "/" +
                configManager.getMySQLDatabase());
        config.setUsername(configManager.getMySQLUsername());
        config.setPassword(configManager.getMySQLPassword());

        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // MySQL specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);
    }

    private void initializeSQLite() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/killrewards.db");
        config.setDriverClassName("org.sqlite.JDBC");

        // SQLite specific settings
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    private void createTables() {
        String storageType = configManager.getStorageType();

        // Use different syntax for MySQL vs SQLite
        String perVictimTable;
        String globalCooldownTable;

        if (storageType.equals("mysql")) {
            // MySQL syntax
            perVictimTable = "CREATE TABLE IF NOT EXISTS " + configManager.getMySQLTablePrefix() + "per_victim_cooldowns (" +
                    "killer_uuid VARCHAR(36) NOT NULL," +
                    "victim_uuid VARCHAR(36) NOT NULL," +
                    "last_kill_time BIGINT NOT NULL," +
                    "PRIMARY KEY (killer_uuid, victim_uuid)" +
                    ")";

            globalCooldownTable = "CREATE TABLE IF NOT EXISTS " + configManager.getMySQLTablePrefix() + "global_player_cooldowns (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY," +
                    "last_global_kill_time BIGINT NOT NULL" +
                    ")";
        } else {
            // SQLite syntax
            perVictimTable = "CREATE TABLE IF NOT EXISTS " + configManager.getMySQLTablePrefix() + "per_victim_cooldowns (" +
                    "killer_uuid TEXT NOT NULL," +
                    "victim_uuid TEXT NOT NULL," +
                    "last_kill_time INTEGER NOT NULL," +
                    "PRIMARY KEY (killer_uuid, victim_uuid)" +
                    ")";

            globalCooldownTable = "CREATE TABLE IF NOT EXISTS " + configManager.getMySQLTablePrefix() + "global_player_cooldowns (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "last_global_kill_time INTEGER NOT NULL" +
                    ")";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(perVictimTable);
             PreparedStatement stmt2 = conn.prepareStatement(globalCooldownTable)) {

            stmt1.execute();
            stmt2.execute();

            plugin.getLogger().info("✓ Database tables created successfully");

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Failed to create database tables", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("✓ Database connection closed");
        }
    }

    public boolean isConnected() {
        try {
            return dataSource != null && dataSource.getConnection() != null;
        } catch (SQLException e) {
            return false;
        }
    }
}