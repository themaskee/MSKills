package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.util.SoundResolver;
import com.mskplugins.mskills.util.TextUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectManager {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;
    private final SoundResolver soundResolver;

    public EffectManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.soundResolver = new SoundResolver();
    }

    public void playRewardSound(Player player) {
        if (!configManager.isSoundEnabled() || !configManager.isRewardSoundEnabled()) return;
        playSound(player, configManager.getRewardSound(), configManager.getRewardSoundVolume(), configManager.getRewardSoundPitch());
    }

    public void playDeniedSound(Player player) {
        if (!configManager.isSoundEnabled() || !configManager.isDeniedSoundEnabled()) return;
        playSound(player, configManager.getDeniedSound(), configManager.getDeniedSoundVolume(), configManager.getDeniedSoundPitch());
    }

    private void playSound(Player player, String soundName, float volume, float pitch) {
        try {
            Sound sound = soundResolver.resolve(soundName);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, volume, pitch);

                if (configManager.isDebug()) {
                    plugin.getLogger().info("🔊 Played sound '" + soundName + "' to " + player.getName());
                }
            } else {
                plugin.getLogger().warning("🔇 Sound not found: " + soundName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("❌ Error playing sound to " + player.getName() + ": " + e.getMessage());
        }
    }

    public void sendRewardActionBar(Player player, String victimName) {
        if (!configManager.isActionBarEnabled() || !configManager.isRewardActionBarEnabled()) return;
        String message = configManager.getRewardActionBarMessage().replace("%victim%", victimName);
        sendActionBar(player, message, configManager.getRewardActionBarDuration(), "reward");
    }

    public void sendDeniedActionBar(Player player, String time) {
        if (!configManager.isActionBarEnabled() || !configManager.isDeniedActionBarEnabled()) return;
        String message = configManager.getDeniedActionBarMessage().replace("%time%", time);
        sendActionBar(player, message, configManager.getDeniedActionBarDuration(), "denied");
    }

    private void sendActionBar(Player player, String message, int duration, String type) {
        try {
            // Replace placeholders
            message = message.replace("%player%", player.getName());

            // Send actionbar
            TextUtils.sendActionBar(player, message);

            if (configManager.isDebug()) {
                plugin.getLogger().info("📱 Sent " + type + " actionbar to " + player.getName() + ": " + message);
            }

            // Clear after duration
            if (duration > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            TextUtils.sendActionBar(player, "");
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    }
                }.runTaskLater(plugin, duration);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("❌ Error sending actionbar to " + player.getName() + ": " + e.getMessage());

            // Fallback: send as normal message
            try {
                String fallbackMessage = "⚔️ " + message.replace("%player%", player.getName());
                TextUtils.sendMessage(player, fallbackMessage);
            } catch (Exception ex) {
                plugin.getLogger().severe("❌ Failed to send fallback message: " + ex.getMessage());
            }
        }
    }

    /**
     * Apply all reward effects (sound + actionbar)
     */
    public void applyRewardEffects(Player player, String victimName) {
        playRewardSound(player);
        sendRewardActionBar(player, victimName);
    }

    /**
     * Apply all denied effects (sound + actionbar)
     */
    public void applyDeniedEffects(Player player, String time) {
        playDeniedSound(player);
        sendDeniedActionBar(player, time);
    }

    /**
     * Test sound for a player (for debugging)
     */
    public void testSound(Player player, String soundName) {
        try {
            if (soundName == null || soundName.isEmpty()) {
                playRewardSound(player);
                TextUtils.sendMessage(player, "&aTesting reward sound...");
            } else {
                playSound(player, soundName, 1.0f, 1.0f);
                TextUtils.sendMessage(player, "&aTesting sound: &e" + soundName);
            }
        } catch (Exception e) {
            TextUtils.sendMessage(player, "&cError testing sound: " + e.getMessage());
            plugin.getLogger().warning("Error testing sound: " + e.getMessage());
        }
    }

    /**
     * Test actionbar for a player (for debugging)
     */
    public void testActionBar(Player player, String message) {
        try {
            if (message == null || message.isEmpty()) {
                sendRewardActionBar(player, "TestVictim");
                TextUtils.sendMessage(player, "&aTesting reward actionbar...");
            } else {
                sendActionBar(player, message, 60, "test");
                TextUtils.sendMessage(player, "&aTesting actionbar: &e" + message);
            }
        } catch (Exception e) {
            TextUtils.sendMessage(player, "&cError testing actionbar: " + e.getMessage());
            plugin.getLogger().warning("Error testing actionbar: " + e.getMessage());
        }
    }
}