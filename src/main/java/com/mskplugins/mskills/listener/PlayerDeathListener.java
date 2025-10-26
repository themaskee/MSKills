package com.mskplugins.mskills.listener;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.manager.*;
import com.mskplugins.mskills.util.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDeathListener implements Listener {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final CooldownManager cooldownManager;
    private final MessageManager messageManager;
    private final EffectManager effectManager;

    public PlayerDeathListener(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.messageManager = plugin.getMessageManager();
        this.effectManager = plugin.getEffectManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Quick validation checks
        if (killer == null) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("❌ No killer found for victim: " + victim.getName());
            }
            return;
        }

        if (!killer.isOnline()) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("❌ Killer is not online: " + killer.getName());
            }
            return;
        }

        if (killer.equals(victim)) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("❌ Self-kill detected: " + killer.getName());
            }
            handleDenied(killer, "self-kill", "");
            return;
        }

        if (!configManager.isRewardEnabled()) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("❌ Rewards disabled - no reward for " + killer.getName());
            }
            return;
        }

        if (configManager.isDebug()) {
            plugin.getLogger().info("✅ Processing kill: " + killer.getName() + " killed " + victim.getName());
        }

        // Check bypass permission
        if (killer.hasPermission("reward.bypass")) {
            if (configManager.isDebug()) {
                plugin.getLogger().info("✅ Bypass permission - giving reward to " + killer.getName());
            }
            giveReward(killer, victim);
            return;
        }

        // Process kill reward
        processKillReward(killer, victim);
    }

    private void processKillReward(Player killer, Player victim) {
        CompletableFuture.supplyAsync(() -> {
            try {
                UUID killerUUID = killer.getUniqueId();
                UUID victimUUID = victim.getUniqueId();
                String killerName = killer.getName();
                String victimName = victim.getName();

                if (configManager.isDebug()) {
                    plugin.getLogger().info("🔍 Starting checks for " + killerName + " killing " + victimName);
                }

                // IP Check
                if (configManager.isIPCheckEnabled()) {
                    if (configManager.isDebug()) {
                        plugin.getLogger().info("🔍 Checking IP for " + killerName + " and " + victimName);
                    }

                    boolean sameIP = hasSameIP(killer, victim);
                    if (sameIP) {
                        if (configManager.isDebug()) {
                            plugin.getLogger().info("❌ Same IP detected - denying reward for " + killerName);
                        }
                        handleDenied(killer, "same-ip", "");
                        return false;
                    } else {
                        if (configManager.isDebug()) {
                            plugin.getLogger().info("✅ IP check passed for " + killerName);
                        }
                    }
                }

                // Per-Victim Cooldown Check (killer-victim specific)
                if (configManager.isCooldownEnabled()) {
                    if (configManager.isDebug()) {
                        plugin.getLogger().info("🔍 Checking per-victim cooldown for " + killerName + "->" + victimName);
                    }

                    Long lastPerVictimKillTime = cooldownManager.getPerVictimCooldownTime(killerUUID, victimUUID);
                    long perVictimCooldownMs = configManager.getCooldown() * 1000L;
                    long currentTime = System.currentTimeMillis();

                    if (configManager.isDebug()) {
                        plugin.getLogger().info("⏰ Per-victim cooldown data - Last: " + lastPerVictimKillTime + ", Current: " + currentTime + ", Cooldown: " + perVictimCooldownMs + "ms");
                    }

                    if (lastPerVictimKillTime != null) {
                        long timeSinceLastKill = currentTime - lastPerVictimKillTime;
                        long remaining = perVictimCooldownMs - timeSinceLastKill;

                        if (configManager.isDebug()) {
                            plugin.getLogger().info("⏰ Time since last kill (per-victim): " + timeSinceLastKill + "ms, Remaining: " + remaining + "ms");
                        }

                        if (remaining > 0) {
                            String timeFormatted = TimeFormatter.formatTime(remaining / 1000);
                            if (configManager.isDebug()) {
                                plugin.getLogger().info("❌ Per-victim cooldown active - " + killerName + " must wait " + timeFormatted + " to kill " + victimName + " again");
                            }
                            handleDenied(killer, "cooldown", timeFormatted);
                            return false;
                        } else {
                            if (configManager.isDebug()) {
                                plugin.getLogger().info("✅ Per-victim cooldown check passed for " + killerName + "->" + victimName);
                            }
                        }
                    } else {
                        if (configManager.isDebug()) {
                            plugin.getLogger().info("✅ No previous kills found for " + killerName + "->" + victimName + " - per-victim cooldown check passed");
                        }
                    }
                }

                // Global Player Cooldown Check (per-killer regardless of victim)
                if (configManager.getGlobalCooldown() > 0) {
                    if (configManager.isDebug()) {
                        plugin.getLogger().info("🔍 Checking global player cooldown for " + killerName);
                    }

                    Long lastGlobalKillTime = cooldownManager.getGlobalPlayerCooldownTime(killerUUID);
                    long globalCooldownMs = configManager.getGlobalCooldown() * 1000L;
                    long currentTime = System.currentTimeMillis();

                    if (configManager.isDebug()) {
                        plugin.getLogger().info("⏰ Global player cooldown data - Last: " + lastGlobalKillTime + ", Current: " + currentTime + ", Cooldown: " + globalCooldownMs + "ms");
                    }

                    if (lastGlobalKillTime != null) {
                        long timeSinceLastGlobalKill = currentTime - lastGlobalKillTime;
                        long remaining = globalCooldownMs - timeSinceLastGlobalKill;

                        if (configManager.isDebug()) {
                            plugin.getLogger().info("⏰ Time since last global kill: " + timeSinceLastGlobalKill + "ms, Remaining: " + remaining + "ms");
                        }

                        if (remaining > 0) {
                            String timeFormatted = TimeFormatter.formatTime(remaining / 1000);
                            if (configManager.isDebug()) {
                                plugin.getLogger().info("❌ Global player cooldown active - " + killerName + " must wait " + timeFormatted + " to kill anyone");
                            }
                            handleDenied(killer, "global-cooldown", timeFormatted);
                            return false;
                        } else {
                            if (configManager.isDebug()) {
                                plugin.getLogger().info("✅ Global player cooldown check passed for " + killerName);
                            }
                        }
                    } else {
                        if (configManager.isDebug()) {
                            plugin.getLogger().info("✅ No previous global kills found for " + killerName + " - global cooldown check passed");
                        }
                    }
                }

                // All checks passed - give reward
                if (configManager.isDebug()) {
                    plugin.getLogger().info("✅ All checks passed - giving reward to " + killerName + " for killing " + victimName);
                }
                giveReward(killer, victim);

                // Update both cooldowns async
                if (configManager.isCooldownEnabled() || configManager.getGlobalCooldown() > 0) {
                    plugin.getAsyncExecutor().submit(() -> {
                        if (configManager.isDebug()) {
                            plugin.getLogger().info("💾 Updating cooldowns for " + killerName + "->" + victimName);
                        }
                        cooldownManager.updateCooldowns(killerUUID, victimUUID, System.currentTimeMillis());
                    });
                }

                return true;

            } catch (Exception e) {
                plugin.getLogger().severe("❌ Error in processKillReward: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, plugin.getAsyncExecutor()).exceptionally(throwable -> {
            plugin.getLogger().severe("❌ Error processing kill reward: " + throwable.getMessage());
            return false;
        });
    }

    private boolean hasSameIP(Player killer, Player victim) {
        try {
            if (killer.getAddress() == null || victim.getAddress() == null) {
                if (configManager.isDebug()) {
                    plugin.getLogger().info("❌ Could not get IP address for one of the players");
                }
                return false;
            }

            InetAddress killerIP = killer.getAddress().getAddress();
            InetAddress victimIP = victim.getAddress().getAddress();

            if (killerIP == null || victimIP == null) {
                if (configManager.isDebug()) {
                    plugin.getLogger().info("❌ IP address is null for one of the players");
                }
                return false;
            }

            String killerIPString = killerIP.getHostAddress();
            String victimIPString = victimIP.getHostAddress();

            boolean sameIP = killerIP.equals(victimIP);

            if (configManager.isDebug()) {
                plugin.getLogger().info("🌐 IP Check - Killer: " + killerIPString + ", Victim: " + victimIPString + ", Same: " + sameIP);
            }

            return sameIP;
        } catch (Exception e) {
            plugin.getLogger().warning("❌ Error checking IP addresses: " + e.getMessage());
            return false;
        }
    }

    private void handleDenied(Player killer, String messageKey, String time) {
        // Run on main thread for effects
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (configManager.isDebug()) {
                plugin.getLogger().info("🚫 Handling denied reward for " + killer.getName() + ": " + messageKey);
            }

            effectManager.applyDeniedEffects(killer, time);

            String message = messageManager.getMessage(messageKey);
            if (message.contains("%time%")) {
                message = message.replace("%time%", time);
            }

            if (!message.isEmpty()) {
                killer.sendMessage(message);
            }
        });
    }

    private void giveReward(Player killer, Player victim) {
        if (configManager.isDebug()) {
            plugin.getLogger().info("🎁 Giving reward to " + killer.getName() + " for killing " + victim.getName());
        }

        // Execute reward commands
        List<String> rewardCommands = configManager.getRewardCommands();
        for (String command : rewardCommands) {
            String formattedCommand = command
                    .replace("%player%", killer.getName())
                    .replace("%victim%", victim.getName());

            if (configManager.isDebug()) {
                plugin.getLogger().info("⚙️ Executing command: " + formattedCommand);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
                } catch (Exception e) {
                    plugin.getLogger().warning("❌ Failed to execute command: " + formattedCommand + " - " + e.getMessage());
                }
            });
        }

        // Apply effects on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            effectManager.applyRewardEffects(killer, victim.getName());

            String message = messageManager.getMessage("reward-given")
                    .replace("%victim%", victim.getName());
            killer.sendMessage(message);

            if (configManager.isDebug()) {
                plugin.getLogger().info("✅ Reward effects applied to " + killer.getName());
            }
        });
    }
}