package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final KillRewardPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        loadConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }
    }

    // Basic settings
    public boolean isRewardEnabled() {
        return config.getBoolean("reward.enabled", true);
    }

    public void setRewardEnabled(boolean enabled) {
        config.set("reward.enabled", enabled);
        saveConfig();
    }

    public boolean isIPCheckEnabled() {
        return config.getBoolean("security.ip-check", true);
    }

    public void setIPCheckEnabled(boolean enabled) {
        config.set("security.ip-check", enabled);
        saveConfig();
    }

    public boolean isCooldownEnabled() {
        return config.getBoolean("cooldown.enabled", true);
    }

    public void setCooldownEnabled(boolean enabled) {
        config.set("cooldown.enabled", enabled);
        saveConfig();
    }

    public long getCooldown() {
        return config.getLong("cooldown.time", 300);
    }

    public void setCooldown(long cooldown) {
        config.set("cooldown.time", cooldown);
        saveConfig();
    }

    public long getGlobalCooldown() {
        return config.getLong("cooldown.global", 0);
    }

    public void setGlobalCooldown(long cooldown) {
        config.set("cooldown.global", cooldown);
        saveConfig();
    }

    // Sound settings
    public boolean isSoundEnabled() {
        return config.getBoolean("sounds.enabled", true);
    }

    public void setSoundEnabled(boolean enabled) {
        config.set("sounds.enabled", enabled);
        saveConfig();
    }

    public boolean isRewardSoundEnabled() {
        return config.getBoolean("sounds.reward.enabled", true);
    }

    public void setRewardSoundEnabled(boolean enabled) {
        config.set("sounds.reward.enabled", enabled);
        saveConfig();
    }

    public String getRewardSound() {
        return config.getString("sounds.reward.sound", "entity.player.levelup");
    }

    public void setRewardSound(String sound) {
        config.set("sounds.reward.sound", sound);
        saveConfig();
    }

    public float getRewardSoundVolume() {
        return (float) config.getDouble("sounds.reward.volume", 1.0);
    }

    public void setRewardSoundVolume(float volume) {
        config.set("sounds.reward.volume", volume);
        saveConfig();
    }

    public float getRewardSoundPitch() {
        return (float) config.getDouble("sounds.reward.pitch", 1.0);
    }

    public void setRewardSoundPitch(float pitch) {
        config.set("sounds.reward.pitch", pitch);
        saveConfig();
    }

    public boolean isDeniedSoundEnabled() {
        return config.getBoolean("sounds.denied.enabled", true);
    }

    public void setDeniedSoundEnabled(boolean enabled) {
        config.set("sounds.denied.enabled", enabled);
        saveConfig();
    }

    public String getDeniedSound() {
        return config.getString("sounds.denied.sound", "entity.villager.no");
    }

    public void setDeniedSound(String sound) {
        config.set("sounds.denied.sound", sound);
        saveConfig();
    }

    public float getDeniedSoundVolume() {
        return (float) config.getDouble("sounds.denied.volume", 1.0);
    }

    public void setDeniedSoundVolume(float volume) {
        config.set("sounds.denied.volume", volume);
        saveConfig();
    }

    public float getDeniedSoundPitch() {
        return (float) config.getDouble("sounds.denied.pitch", 1.0);
    }

    public void setDeniedSoundPitch(float pitch) {
        config.set("sounds.denied.pitch", pitch);
        saveConfig();
    }

    // ActionBar settings
    public boolean isActionBarEnabled() {
        return config.getBoolean("actionbar.enabled", true);
    }

    public void setActionBarEnabled(boolean enabled) {
        config.set("actionbar.enabled", enabled);
        saveConfig();
    }

    public boolean isRewardActionBarEnabled() {
        return config.getBoolean("actionbar.reward.enabled", true);
    }

    public void setRewardActionBarEnabled(boolean enabled) {
        config.set("actionbar.reward.enabled", enabled);
        saveConfig();
    }

    public String getRewardActionBarMessage() {
        return config.getString("actionbar.reward.message", "&a⚔️ You killed &e%victim%&a!");
    }

    public void setRewardActionBarMessage(String message) {
        config.set("actionbar.reward.message", message);
        saveConfig();
    }

    public int getRewardActionBarDuration() {
        return config.getInt("actionbar.reward.duration", 60);
    }

    public void setRewardActionBarDuration(int duration) {
        config.set("actionbar.reward.duration", duration);
        saveConfig();
    }

    public boolean isDeniedActionBarEnabled() {
        return config.getBoolean("actionbar.denied.enabled", true);
    }

    public void setDeniedActionBarEnabled(boolean enabled) {
        config.set("actionbar.denied.enabled", enabled);
        saveConfig();
    }

    public String getDeniedActionBarMessage() {
        return config.getString("actionbar.denied.message", "&c⏰ Cooldown: &e%time%");
    }

    public void setDeniedActionBarMessage(String message) {
        config.set("actionbar.denied.message", message);
        saveConfig();
    }

    public int getDeniedActionBarDuration() {
        return config.getInt("actionbar.denied.duration", 60);
    }

    public void setDeniedActionBarDuration(int duration) {
        config.set("actionbar.denied.duration", duration);
        saveConfig();
    }

    // Reward commands
    public List<String> getRewardCommands() {
        return config.getStringList("reward.commands");
    }

    public void setRewardCommands(List<String> commands) {
        config.set("reward.commands", commands);
        saveConfig();
    }

    // Database settings
    public String getStorageType() {
        return config.getString("storage.type", "sqlite").toLowerCase();
    }

    public void setStorageType(String type) {
        config.set("storage.type", type);
        saveConfig();
    }

    public String getMySQLHost() {
        return config.getString("storage.mysql.host", "localhost");
    }

    public void setMySQLHost(String host) {
        config.set("storage.mysql.host", host);
        saveConfig();
    }

    public int getMySQLPort() {
        return config.getInt("storage.mysql.port", 3306);
    }

    public void setMySQLPort(int port) {
        config.set("storage.mysql.port", port);
        saveConfig();
    }

    public String getMySQLDatabase() {
        return config.getString("storage.mysql.database", "killrewards");
    }

    public void setMySQLDatabase(String database) {
        config.set("storage.mysql.database", database);
        saveConfig();
    }

    public String getMySQLUsername() {
        return config.getString("storage.mysql.username", "root");
    }

    public void setMySQLUsername(String username) {
        config.set("storage.mysql.username", username);
        saveConfig();
    }

    public String getMySQLPassword() {
        return config.getString("storage.mysql.password", "");
    }

    public void setMySQLPassword(String password) {
        config.set("storage.mysql.password", password);
        saveConfig();
    }

    public String getMySQLTablePrefix() {
        return config.getString("storage.mysql.table-prefix", "killreward_");
    }

    public void setMySQLTablePrefix(String prefix) {
        config.set("storage.mysql.table-prefix", prefix);
        saveConfig();
    }

    // Language settings
    public String getLanguage() {
        return config.getString("language", "en");
    }

    public void setLanguage(String language) {
        config.set("language", language);
        saveConfig();
    }

    // Debug settings
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public void setDebug(boolean debug) {
        config.set("debug", debug);
        saveConfig();
    }

    // Time format settings
    public String getTimeFormat() {
        return config.getString("time-format", "default");
    }

    public void setTimeFormat(String format) {
        config.set("time-format", format);
        saveConfig();
    }

    // Command settings
    public List<String> getCommandAliases() {
        return config.getStringList("commands.aliases");
    }

    public void setCommandAliases(List<String> aliases) {
        config.set("commands.aliases", aliases);
        saveConfig();
    }

    public String getAdminPermission() {
        return config.getString("commands.permissions.admin", "reward.admin");
    }

    public void setAdminPermission(String permission) {
        config.set("commands.permissions.admin", permission);
        saveConfig();
    }

    public String getBypassPermission() {
        return config.getString("commands.permissions.bypass", "reward.bypass");
    }

    public void setBypassPermission(String permission) {
        config.set("commands.permissions.bypass", permission);
        saveConfig();
    }

    public String getUsePermission() {
        return config.getString("commands.permissions.use", "reward.use");
    }

    public void setUsePermission(String permission) {
        config.set("commands.permissions.use", permission);
        saveConfig();
    }

    public boolean isTabCompleteEnabled() {
        return config.getBoolean("commands.tab-complete", true);
    }

    public void setTabCompleteEnabled(boolean enabled) {
        config.set("commands.tab-complete", enabled);
        saveConfig();
    }

    // Feature names
    public String getFeatureName(String feature) {
        return config.getString("features." + feature, feature);
    }

    public void setFeatureName(String feature, String name) {
        config.set("features." + feature, name);
        saveConfig();
    }

    // Settings names
    public String getSettingName(String setting) {
        return config.getString("settings." + setting, setting);
    }

    public void setSettingName(String setting, String name) {
        config.set("settings." + setting, name);
        saveConfig();
    }

    // Time units
    public String getTimeUnit(String language, String unit) {
        return config.getString("time-units." + language + "." + unit, unit);
    }

    public void setTimeUnit(String language, String unit, String value) {
        config.set("time-units." + language + "." + unit, value);
        saveConfig();
    }

    // Status display
    public String getStatusHeader() {
        return config.getString("status.header", "&6&lMSKills Status");
    }

    public void setStatusHeader(String header) {
        config.set("status.header", header);
        saveConfig();
    }

    public String getStatusLineFormat() {
        return config.getString("status.line-format", "&7%feature%: %status%");
    }

    public void setStatusLineFormat(String format) {
        config.set("status.line-format", format);
        saveConfig();
    }

    public String getStatusEnabledText() {
        return config.getString("status.enabled-text", "&aEnabled");
    }

    public void setStatusEnabledText(String text) {
        config.set("status.enabled-text", text);
        saveConfig();
    }

    public String getStatusDisabledText() {
        return config.getString("status.disabled-text", "&cDisabled");
    }

    public void setStatusDisabledText(String text) {
        config.set("status.disabled-text", text);
        saveConfig();
    }

    public List<String> getStatusItems() {
        return config.getStringList("status.items");
    }

    public void setStatusItems(List<String> items) {
        config.set("status.items", items);
        saveConfig();
    }

    // Help display
    public String getHelpHeader() {
        return config.getString("help.header", "&6&lMSKills Commands");
    }

    public void setHelpHeader(String header) {
        config.set("help.header", header);
        saveConfig();
    }

    public String getHelpLineFormat() {
        return config.getString("help.line-format", "&e%command% &7- %description%");
    }

    public void setHelpLineFormat(String format) {
        config.set("help.line-format", format);
        saveConfig();
    }

    public String getHelpCommand(String command) {
        return config.getString("help.commands." + command + ".command", "/reward " + command);
    }

    public void setHelpCommand(String command, String value) {
        config.set("help.commands." + command + ".command", value);
        saveConfig();
    }

    public String getHelpDescription(String command) {
        return config.getString("help.commands." + command + ".description", "No description available");
    }

    public void setHelpDescription(String command, String description) {
        config.set("help.commands." + command + ".description", description);
        saveConfig();
    }

    public List<String> getHelpCommands() {
        if (config.getConfigurationSection("help.commands") != null) {
            return new ArrayList<>(config.getConfigurationSection("help.commands").getKeys(false));
        }
        return new ArrayList<>();
    }

    public void setHelpCommands(List<String> commands) {
        // This would need more complex implementation to set all commands
        // For now, we'll just save the config
        saveConfig();
    }

    // Performance settings
    public int getAsyncThreads() {
        return config.getInt("performance.async-threads", 2);
    }

    public void setAsyncThreads(int threads) {
        config.set("performance.async-threads", threads);
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}