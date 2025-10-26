package com.mskplugins.mskills.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class TextUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static boolean hasPapi = false;

    /**
     * Initialize and check for PlaceholderAPI
     */
    public static void initialize() {
        hasPapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Check if PlaceholderAPI is available
     */
    public static boolean hasPlaceholderAPI() {
        return hasPapi;
    }

    /**
     * Convert legacy formats to MiniMessage format
     */
    public static String toMiniMessage(String text) {
        if (text == null) return "";

        // Convert legacy hex codes &#RRGGBB to <#RRGGBB>
        text = LEGACY_HEX_PATTERN.matcher(text).replaceAll("<#$1>");

        // Convert legacy color codes to MiniMessage tags
        text = text.replace("&0", "<black>");
        text = text.replace("&1", "<dark_blue>");
        text = text.replace("&2", "<dark_green>");
        text = text.replace("&3", "<dark_aqua>");
        text = text.replace("&4", "<dark_red>");
        text = text.replace("&5", "<dark_purple>");
        text = text.replace("&6", "<gold>");
        text = text.replace("&7", "<gray>");
        text = text.replace("&8", "<dark_gray>");
        text = text.replace("&9", "<blue>");
        text = text.replace("&a", "<green>");
        text = text.replace("&b", "<aqua>");
        text = text.replace("&c", "<red>");
        text = text.replace("&d", "<light_purple>");
        text = text.replace("&e", "<yellow>");
        text = text.replace("&f", "<white>");
        text = text.replace("&k", "<obfuscated>");
        text = text.replace("&l", "<bold>");
        text = text.replace("&m", "<strikethrough>");
        text = text.replace("&n", "<underline>");
        text = text.replace("&o", "<italic>");
        text = text.replace("&r", "<reset>");

        return text;
    }

    /**
     * Clean text from any legacy formatting codes before parsing with MiniMessage
     */
    public static String cleanForMiniMessage(String text) {
        if (text == null) return "";

        // Remove any remaining legacy codes that might cause issues
        text = text.replaceAll("§[0-9a-fk-or]", "");
        text = text.replaceAll("&[0-9a-fk-or]", "");

        return text;
    }

    /**
     * Parse MiniMessage string to Component with proper error handling
     */
    public static Component parseMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        try {
            String miniMessageText = toMiniMessage(text);
            // Clean any remaining legacy codes
            miniMessageText = cleanForMiniMessage(miniMessageText);
            return miniMessage.deserialize(miniMessageText);
        } catch (Exception e) {
            // Fallback to legacy parsing if MiniMessage fails
            try {
                return legacySerializer.deserialize(text);
            } catch (Exception e2) {
                // Final fallback - plain text
                return Component.text(text);
            }
        }
    }

    /**
     * Backwards compatible colorize method - converts to MiniMessage and back to legacy
     */
    public static String colorize(String text) {
        if (text == null) return "";

        Component component = parseMiniMessage(text);
        return legacySerializer.serialize(component);
    }

    /**
     * Create a Component from text (backwards compatibility)
     */
    public static Component component(String text) {
        return parseMiniMessage(text);
    }

    /**
     * Apply PlaceholderAPI placeholders to a string for a player
     */
    public static String setPlaceholders(Player player, String text) {
        if (!hasPapi || player == null || text == null) {
            return text;
        }
        try {
            Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Object result = papiClass.getMethod("setPlaceholders", Player.class, String.class)
                    .invoke(null, player, text);
            return result.toString();
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Send a MiniMessage formatted message to a player with PlaceholderAPI support
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;

        try {
            String processedMessage = hasPapi ? setPlaceholders(player, message) : message;
            Component component = parseMiniMessage(processedMessage);
            player.sendMessage(component);
        } catch (Exception e) {
            // Fallback to legacy sending
            player.sendMessage(colorize(message));
        }
    }

    /**
     * Send a MiniMessage formatted message to a command sender with PlaceholderAPI support
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) return;

        try {
            if (sender instanceof Player) {
                sendMessage((Player) sender, message);
            } else {
                // Console - convert to legacy and send
                Component component = parseMiniMessage(message);
                String legacyText = legacySerializer.serialize(component);
                sender.sendMessage(legacyText);
            }
        } catch (Exception e) {
            // Fallback to legacy sending
            sender.sendMessage(colorize(message));
        }
    }

    /**
     * Send a MiniMessage formatted action bar to a player with PlaceholderAPI support
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;

        try {
            String processedMessage = hasPapi ? setPlaceholders(player, message) : message;
            Component component = parseMiniMessage(processedMessage);
            player.sendActionBar(component);
        } catch (Exception e) {
            // Fallback to legacy action bar
            try {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
            } catch (Exception e2) {
                // Final fallback - normal message
                player.sendMessage(colorize(message));
            }
        }
    }

    /**
     * Remove color codes from text
     */
    public static String stripColor(String text) {
        if (text == null) return "";

        try {
            // Convert to legacy and strip
            Component component = parseMiniMessage(text);
            String legacyText = legacySerializer.serialize(component);
            return legacyText.replaceAll("§[0-9a-fk-or]", "");
        } catch (Exception e) {
            // Fallback to simple stripping
            return text.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
        }
    }

    /**
     * Format time in seconds to human readable format
     */
    public static String formatTime(long seconds) {
        return TimeFormatter.formatTime(seconds);
    }

    /**
     * Format a number with commas for thousands
     */
    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    /**
     * Format a percentage value
     */
    public static String formatPercentage(double value) {
        return String.format("%.1f%%", value);
    }

    /**
     * Create a progress bar with MiniMessage
     */
    public static String createProgressBar(int current, int max, int length) {
        return createProgressBar(current, max, length, "green", "gray");
    }

    /**
     * Create a progress bar with custom colors using MiniMessage
     */
    public static String createProgressBar(int current, int max, int length, String completedColor, String remainingColor) {
        if (max <= 0) return "<gray>[</gray>" + " ".repeat(length) + "<gray>]</gray>";

        float percentage = (float) current / max;
        int progress = (int) (length * percentage);

        StringBuilder bar = new StringBuilder("<gray>[</gray>");
        for (int i = 0; i < length; i++) {
            if (i < progress) {
                bar.append("<").append(completedColor).append(">|</").append(completedColor).append(">");
            } else {
                bar.append("<").append(remainingColor).append(">-</").append(remainingColor).append(">");
            }
        }
        bar.append("<gray>]</gray>");

        return bar.toString();
    }

    /**
     * Center text in the chat (basic implementation)
     */
    public static String centerText(String text, int lineLength) {
        if (text == null || text.isEmpty()) return "";

        String stripped = stripColor(text);
        int textLength = stripped.length();

        if (textLength >= lineLength) return text;

        int padding = (lineLength - textLength) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    /**
     * Create gradient text using MiniMessage
     */
    public static String gradientText(String text, String startColor, String endColor) {
        return "<gradient:" + startColor + ":" + endColor + ">" + text + "</gradient>";
    }

    /**
     * Create rainbow text using MiniMessage
     */
    public static String rainbowText(String text) {
        return "<rainbow>" + text + "</rainbow>";
    }

    /**
     * Check if a string is empty or null
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Capitalize first letter of each word
     */
    public static String capitalizeWords(String text) {
        if (isEmpty(text)) return text;

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Close method for compatibility
     */
    public static void close() {
        // No resources to close
    }
}