package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class CooldownManager {

    private final KillRewardPlugin plugin;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    // Separate caches for per-victim and global player cooldowns
    private final ConcurrentMap<String, Long> perVictimCooldownCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Long> globalPlayerCooldownCache = new ConcurrentHashMap<>();

    public CooldownManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.configManager = plugin.getConfigManager();
    }

    /**
     * Get per-victim cooldown time (killer-victim specific)
     */
    public Long getPerVictimCooldownTime(UUID killerUUID, UUID victimUUID) {
        String cacheKey = killerUUID.toString() + "_" + victimUUID.toString();

        // Check memory cache first
        Long cachedTime = perVictimCooldownCache.get(cacheKey);
        if (cachedTime != null) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("💾 Per-victim cooldown cache hit for " + killerUUID + "->" + victimUUID + ": " + cachedTime);
            }
            return cachedTime;
        }

        // Query database if not in cache
        String query = "SELECT last_kill_time FROM " + configManager.getMySQLTablePrefix() + "per_victim_cooldowns WHERE killer_uuid = ? AND victim_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, killerUUID.toString());
            stmt.setString(2, victimUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                long lastKillTime = rs.getLong("last_kill_time");
                // Cache the result
                perVictimCooldownCache.put(cacheKey, lastKillTime);

                if (configManager.isDebug()) {
                    plugin.getLogger().info("💾 Loaded per-victim cooldown from DB for " + killerUUID + "->" + victimUUID + ": " + lastKillTime);
                }

                return lastKillTime;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "❌ Error getting per-victim cooldown time for " + killerUUID + "->" + victimUUID, e);
        }

        if (configManager.isDebug()) {
            plugin.getLogger().info("💾 No per-victim cooldown found for " + killerUUID + "->" + victimUUID);
        }

        return null;
    }

    /**
     * Get global player cooldown time (per-killer regardless of victim)
     */
    public Long getGlobalPlayerCooldownTime(UUID playerUUID) {
        // Check memory cache first
        Long cachedTime = globalPlayerCooldownCache.get(playerUUID);
        if (cachedTime != null) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("💾 Global player cooldown cache hit for " + playerUUID + ": " + cachedTime);
            }
            return cachedTime;
        }

        // Query database if not in cache
        String query = "SELECT last_global_kill_time FROM " + configManager.getMySQLTablePrefix() + "global_player_cooldowns WHERE player_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                long lastGlobalKillTime = rs.getLong("last_global_kill_time");
                // Cache the result
                globalPlayerCooldownCache.put(playerUUID, lastGlobalKillTime);

                if (configManager.isDebug()) {
                    plugin.getLogger().info("💾 Loaded global player cooldown from DB for " + playerUUID + ": " + lastGlobalKillTime);
                }

                return lastGlobalKillTime;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "❌ Error getting global player cooldown time for " + playerUUID, e);
        }

        if (configManager.isDebug()) {
            plugin.getLogger().info("💾 No global player cooldown found for " + playerUUID);
        }

        return null;
    }

    /**
     * Update both per-victim and global player cooldowns
     */
    public CompletableFuture<Boolean> updateCooldowns(UUID killerUUID, UUID victimUUID, long currentTime) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isSQLite = configManager.getStorageType().equals("sqlite");

            String perVictimCooldownQuery;
            String globalPlayerCooldownQuery;

            if (isSQLite) {
                // SQLite syntax - use INSERT OR REPLACE
                perVictimCooldownQuery = "INSERT OR REPLACE INTO " + configManager.getMySQLTablePrefix() + "per_victim_cooldowns (killer_uuid, victim_uuid, last_kill_time) VALUES (?, ?, ?)";
                globalPlayerCooldownQuery = "INSERT OR REPLACE INTO " + configManager.getMySQLTablePrefix() + "global_player_cooldowns (player_uuid, last_global_kill_time) VALUES (?, ?)";
            } else {
                // MySQL syntax
                perVictimCooldownQuery = "INSERT INTO " + configManager.getMySQLTablePrefix() + "per_victim_cooldowns (killer_uuid, victim_uuid, last_kill_time) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE last_kill_time = ?";
                globalPlayerCooldownQuery = "INSERT INTO " + configManager.getMySQLTablePrefix() + "global_player_cooldowns (player_uuid, last_global_kill_time) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE last_global_kill_time = ?";
            }

            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    // Update per-victim cooldown
                    try (PreparedStatement stmt = conn.prepareStatement(perVictimCooldownQuery)) {
                        stmt.setString(1, killerUUID.toString());
                        stmt.setString(2, victimUUID.toString());
                        stmt.setLong(3, currentTime);
                        if (!isSQLite) {
                            stmt.setLong(4, currentTime); // For MySQL ON DUPLICATE KEY
                        }
                        stmt.executeUpdate();
                    }

                    // Update global player cooldown
                    try (PreparedStatement stmt = conn.prepareStatement(globalPlayerCooldownQuery)) {
                        stmt.setString(1, killerUUID.toString());
                        stmt.setLong(2, currentTime);
                        if (!isSQLite) {
                            stmt.setLong(3, currentTime); // For MySQL ON DUPLICATE KEY
                        }
                        stmt.executeUpdate();
                    }

                    conn.commit();

                    // Update cache
                    String cacheKey = killerUUID.toString() + "_" + victimUUID.toString();
                    perVictimCooldownCache.put(cacheKey, currentTime);
                    globalPlayerCooldownCache.put(killerUUID, currentTime);

                    if (configManager.isDebug()) {
                        plugin.getLogger().info("💾 Updated cooldowns - Killer: " + killerUUID + ", Victim: " + victimUUID + ", Time: " + currentTime);
                    }

                    return true;

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "❌ Error updating cooldowns", e);
                return false;
            }
        }, plugin.getAsyncExecutor());
    }

    public void clearCache(UUID playerUUID) {
        // Remove all cached entries for this player (both as killer and victim)
        perVictimCooldownCache.keySet().removeIf(key -> key.startsWith(playerUUID.toString() + "_") || key.endsWith("_" + playerUUID.toString()));
        globalPlayerCooldownCache.remove(playerUUID);
    }

    public void clearAllCache() {
        perVictimCooldownCache.clear();
        globalPlayerCooldownCache.clear();
    }
}