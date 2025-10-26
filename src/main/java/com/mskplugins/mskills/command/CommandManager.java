package com.mskplugins.mskills.command;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.manager.*;
import com.mskplugins.mskills.util.TextUtils;
import com.mskplugins.mskills.util.TimeFormatter;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final EffectManager effectManager;
    private final SettingsManager settingsManager;

    public CommandManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.effectManager = plugin.getEffectManager();
        this.settingsManager = new SettingsManager(plugin);
    }

    public void registerCommands() {
        plugin.getCommand("reward").setExecutor(this);
        plugin.getCommand("reward").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reward.admin")) {
            TextUtils.sendMessage(sender, messageManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                reloadConfig(sender);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "test":
                handleTest(sender, args);
                break;
            case "clearcache":
                clearCache(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "language":
                changeLanguage(sender, args);
                break;
            case "message":
                handleMessage(sender, args);
                break;
            case "help":
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void reloadConfig(CommandSender sender) {
        plugin.reload();
        TextUtils.sendMessage(sender, messageManager.getMessage("config-reloaded"));
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtils.sendMessage(sender, "&cUsage: /reward toggle <feature>");
            TextUtils.sendMessage(sender, "&7Available features: &erewards, ipcheck, cooldown, sound, actionbar, debug");
            return;
        }

        String feature = args[1].toLowerCase();
        boolean newState;
        String messageKey;

        switch (feature) {
            case "rewards":
                newState = settingsManager.toggleRewards();
                messageKey = newState ? "rewards-enabled" : "rewards-disabled";
                break;
            case "ipcheck":
                newState = settingsManager.toggleIPCheck();
                messageKey = newState ? "ip-check-enabled" : "ip-check-disabled";
                break;
            case "cooldown":
                newState = settingsManager.toggleCooldown();
                messageKey = newState ? "cooldown-enabled" : "cooldown-disabled";
                break;
            case "sound":
                newState = settingsManager.toggleSound();
                messageKey = newState ? "sound-enabled" : "sound-disabled";
                break;
            case "actionbar":
                newState = settingsManager.toggleActionBar();
                messageKey = newState ? "actionbar-enabled" : "actionbar-disabled";
                break;
            case "debug":
                newState = settingsManager.toggleDebug();
                messageKey = newState ? "debug-enabled" : "debug-disabled";
                break;
            default:
                TextUtils.sendMessage(sender, "&cUnknown feature: &e" + feature);
                TextUtils.sendMessage(sender, "&7Available features: &erewards, ipcheck, cooldown, sound, actionbar, debug");
                return;
        }

        TextUtils.sendMessage(sender, messageManager.getMessage(messageKey));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TextUtils.sendMessage(sender, "&cUsage: /reward set <setting> <value>");
            TextUtils.sendMessage(sender, "&7Available settings: &ecooldown, globalcooldown, rewardsound, deniedsound, rewardmessage, deniedmessage, timeformat");
            return;
        }

        String setting = args[1].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        switch (setting) {
            case "cooldown":
                try {
                    long cooldown = Long.parseLong(value);
                    settingsManager.setCooldownTime(cooldown);
                    TextUtils.sendMessage(sender, messageManager.getMessage("cooldown-set", "%time%", TextUtils.formatTime(cooldown)));
                } catch (NumberFormatException e) {
                    TextUtils.sendMessage(sender, messageManager.getMessage("invalid-number"));
                }
                break;

            case "globalcooldown":
                try {
                    long globalCooldown = Long.parseLong(value);
                    settingsManager.setGlobalCooldownTime(globalCooldown);
                    TextUtils.sendMessage(sender, messageManager.getMessage("global-cooldown-set", "%time%", TextUtils.formatTime(globalCooldown)));
                } catch (NumberFormatException e) {
                    TextUtils.sendMessage(sender, messageManager.getMessage("invalid-number"));
                }
                break;

            case "rewardsound":
                settingsManager.setRewardSound(value);
                TextUtils.sendMessage(sender, messageManager.getMessage("sound-set", "%sound%", value));
                break;

            case "deniedsound":
                settingsManager.setDeniedSound(value);
                TextUtils.sendMessage(sender, messageManager.getMessage("sound-set", "%sound%", value));
                break;

            case "rewardmessage":
                settingsManager.setRewardActionBarMessage(value);
                TextUtils.sendMessage(sender, messageManager.getMessage("actionbar-message-set"));
                break;

            case "deniedmessage":
                settingsManager.setDeniedActionBarMessage(value);
                TextUtils.sendMessage(sender, messageManager.getMessage("actionbar-message-set"));
                break;

            case "timeformat":
                if (value.equalsIgnoreCase("default") || value.equalsIgnoreCase("compact") || value.equalsIgnoreCase("detailed")) {
                    settingsManager.setTimeFormat(value.toLowerCase());
                    TextUtils.sendMessage(sender, messageManager.getMessage("time-format-set", "%format%", value));
                } else {
                    TextUtils.sendMessage(sender, messageManager.getMessage("invalid-time-format"));
                }
                break;

            default:
                TextUtils.sendMessage(sender, "&cUnknown setting: &e" + setting);
                TextUtils.sendMessage(sender, "&7Available settings: &ecooldown, globalcooldown, rewardsound, deniedsound, rewardmessage, deniedmessage, timeformat");
                break;
        }
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            TextUtils.sendMessage(sender, "&cThis command can only be used by players!");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            TextUtils.sendMessage(sender, "&cUsage: /reward test <sound|actionbar> [value]");
            return;
        }

        String testType = args[1].toLowerCase();
        String value = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";

        switch (testType) {
            case "sound":
                effectManager.testSound(player, value);
                break;
            case "actionbar":
                effectManager.testActionBar(player, value);
                break;
            default:
                TextUtils.sendMessage(sender, "&cInvalid test type. Use: sound, actionbar");
                break;
        }
    }

    private void clearCache(CommandSender sender) {
        plugin.getCooldownManager().clearAllCache();
        TextUtils.sendMessage(sender, "&aCooldown cache cleared successfully!");
    }

    private void showStatus(CommandSender sender) {
        TextUtils.sendMessage(sender, settingsManager.getStatus());
    }

    private void changeLanguage(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtils.sendMessage(sender, "&cUsage: /reward language <en|tr>");
            TextUtils.sendMessage(sender, "&7Current language: &e" + configManager.getLanguage().toUpperCase());
            return;
        }

        String language = args[1].toLowerCase();
        if (language.equals("en") || language.equals("tr")) {
            settingsManager.setLanguage(language);
            TextUtils.sendMessage(sender, messageManager.getMessage("language-changed", "%language%", language.toUpperCase()));
        } else {
            TextUtils.sendMessage(sender, messageManager.getMessage("invalid-language"));
        }
    }

    private void handleMessage(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TextUtils.sendMessage(sender, "&cUsage: /reward message <key> <value>");
            TextUtils.sendMessage(sender, "&7Example: /reward message reward-given \"&aYou killed &e%victim%&a!\"");
            return;
        }

        String key = args[1].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        messageManager.setMessage(key, value);
        TextUtils.sendMessage(sender, "&aMessage &e" + key + " &aset to: &f" + value);
    }

    private void showHelp(CommandSender sender) {
        TextUtils.sendMessage(sender, messageManager.getMessage("help-header"));
        TextUtils.sendMessage(sender, formatHelp("/reward help", "Show this help menu"));
        TextUtils.sendMessage(sender, formatHelp("/reward status", "Show current plugin status"));
        TextUtils.sendMessage(sender, formatHelp("/reward reload", "Reload configuration"));
        TextUtils.sendMessage(sender, formatHelp("/reward toggle <feature>", "Toggle features on/off"));
        TextUtils.sendMessage(sender, formatHelp("/reward set <setting> <value>", "Change settings"));
        TextUtils.sendMessage(sender, formatHelp("/reward test sound [sound]", "Test reward sounds"));
        TextUtils.sendMessage(sender, formatHelp("/reward test actionbar [message]", "Test actionbar messages"));
        TextUtils.sendMessage(sender, formatHelp("/reward clearcache", "Clear cooldown cache"));
        TextUtils.sendMessage(sender, formatHelp("/reward language <en|tr>", "Change language"));
        TextUtils.sendMessage(sender, formatHelp("/reward message <key> <value>", "Edit messages"));
    }

    private String formatHelp(String command, String description) {
        return messageManager.getMessage("help-line")
                .replace("%command%", command)
                .replace("%description%", description);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("reward.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "toggle", "set", "test", "clearcache", "status", "language", "message", "help"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "toggle":
                    completions.addAll(Arrays.asList("rewards", "ipcheck", "cooldown", "sound", "actionbar", "debug"));
                    break;
                case "set":
                    completions.addAll(Arrays.asList("cooldown", "globalcooldown", "rewardsound", "deniedsound", "rewardmessage", "deniedmessage", "timeformat"));
                    break;
                case "test":
                    completions.addAll(Arrays.asList("sound", "actionbar"));
                    break;
                case "language":
                    completions.addAll(Arrays.asList("en", "tr"));
                    break;
                case "message":
                    // Show available message keys
                    completions.addAll(messageManager.getMessages().keySet());
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                switch (args[1].toLowerCase()) {
                    case "timeformat":
                        completions.addAll(Arrays.asList("default", "compact", "detailed"));
                        break;
                    case "rewardsound":
                    case "deniedsound":
                        // Add common sound suggestions
                        completions.addAll(Arrays.asList(
                                "entity.player.levelup",
                                "entity.villager.no",
                                "entity.experience_orb.pickup",
                                "block.note_block.pling",
                                "ui.toast.challenge.complete"
                        ));
                        break;
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}