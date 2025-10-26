package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.util.TextUtils;

public class SettingsManager {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;

    public SettingsManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    // Feature toggles
    public boolean toggleRewards() {
        boolean newState = !configManager.isRewardEnabled();
        configManager.setRewardEnabled(newState);
        return newState;
    }

    public boolean toggleIPCheck() {
        boolean newState = !configManager.isIPCheckEnabled();
        configManager.setIPCheckEnabled(newState);
        return newState;
    }

    public boolean toggleCooldown() {
        boolean newState = !configManager.isCooldownEnabled();
        configManager.setCooldownEnabled(newState);
        return newState;
    }

    public boolean toggleSound() {
        boolean newState = !configManager.isSoundEnabled();
        configManager.setSoundEnabled(newState);
        return newState;
    }

    public boolean toggleActionBar() {
        boolean newState = !configManager.isActionBarEnabled();
        configManager.setActionBarEnabled(newState);
        return newState;
    }

    public boolean toggleDebug() {
        boolean newState = !configManager.isDebug();
        configManager.setDebug(newState);
        return newState;
    }

    // Set specific states
    public void setRewards(boolean state) {
        configManager.setRewardEnabled(state);
    }

    public void setIPCheck(boolean state) {
        configManager.setIPCheckEnabled(state);
    }

    public void setCooldown(boolean state) {
        configManager.setCooldownEnabled(state);
    }

    public void setSound(boolean state) {
        configManager.setSoundEnabled(state);
    }

    public void setActionBar(boolean state) {
        configManager.setActionBarEnabled(state);
    }

    public void setDebug(boolean state) {
        configManager.setDebug(state);
    }

    // Cooldown settings
    public void setCooldownTime(long seconds) {
        configManager.setCooldown(seconds);
    }

    public void setGlobalCooldownTime(long seconds) {
        configManager.setGlobalCooldown(seconds);
    }

    // Sound settings
    public void setRewardSound(String sound) {
        configManager.setRewardSound(sound);
    }

    public void setDeniedSound(String sound) {
        configManager.setDeniedSound(sound);
    }

    // ActionBar settings
    public void setRewardActionBarMessage(String message) {
        configManager.setRewardActionBarMessage(message);
    }

    public void setDeniedActionBarMessage(String message) {
        configManager.setDeniedActionBarMessage(message);
    }

    // Language settings
    public void setLanguage(String language) {
        configManager.setLanguage(language);
        plugin.getMessageManager().reload();
    }

    // Time format settings
    public void setTimeFormat(String format) {
        configManager.setTimeFormat(format);
    }

    // Get current status
    public String getStatus() {
        return "&6&lMSKills Status\n" +
                "&7Rewards: " + getStatus(configManager.isRewardEnabled()) + "\n" +
                "&7IP Check: " + getStatus(configManager.isIPCheckEnabled()) + "\n" +
                "&7Cooldown: " + getStatus(configManager.isCooldownEnabled()) + "\n" +
                "&7Sounds: " + getStatus(configManager.isSoundEnabled()) + "\n" +
                "&7ActionBar: " + getStatus(configManager.isActionBarEnabled()) + "\n" +
                "&7Debug: " + getStatus(configManager.isDebug()) + "\n" +
                "&7Language: &f" + configManager.getLanguage().toUpperCase() + "\n" +
                "&7Storage: &f" + configManager.getStorageType().toUpperCase() + "\n" +
                "&7Cooldown: &f" + TextUtils.formatTime(configManager.getCooldown()) + "\n" +
                "&7Global Cooldown: &f" + TextUtils.formatTime(configManager.getGlobalCooldown());
    }

    private String getStatus(boolean enabled) {
        return enabled ? "&aEnabled" : "&cDisabled";
    }
}