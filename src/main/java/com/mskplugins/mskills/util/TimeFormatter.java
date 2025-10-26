package com.mskplugins.mskills.util;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.manager.ConfigManager;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {

    private static ConfigManager getConfigManager() {
        return KillRewardPlugin.getInstance().getConfigManager();
    }

    /**
     * Format seconds to human readable time based on configured format and language
     */
    public static String formatTime(long seconds) {
        String format = getConfigManager().getTimeFormat();
        String language = getConfigManager().getLanguage();

        switch (format.toLowerCase()) {
            case "compact":
                return formatCompact(seconds, language);
            case "detailed":
                return formatDetailed(seconds, language);
            case "default":
            default:
                return formatDefault(seconds, language);
        }
    }

    /**
     * Default format: 1h 30m 15s (with configurable units)
     */
    private static String formatDefault(long seconds, String language) {
        if (seconds < 60) {
            return seconds + getTimeUnit("second_short", language, seconds);
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes < 60) {
            if (remainingSeconds > 0) {
                return minutes + getTimeUnit("minute_short", language, minutes) + " " +
                        remainingSeconds + getTimeUnit("second_short", language, remainingSeconds);
            } else {
                return minutes + getTimeUnit("minute_short", language, minutes);
            }
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours < 24) {
            if (remainingMinutes > 0) {
                return hours + getTimeUnit("hour_short", language, hours) + " " +
                        remainingMinutes + getTimeUnit("minute_short", language, remainingMinutes);
            } else {
                return hours + getTimeUnit("hour_short", language, hours);
            }
        }

        long days = hours / 24;
        long remainingHours = hours % 24;

        if (remainingHours > 0) {
            return days + getTimeUnit("day_short", language, days) + " " +
                    remainingHours + getTimeUnit("hour_short", language, remainingHours);
        } else {
            return days + getTimeUnit("day_short", language, days);
        }
    }

    /**
     * Compact format: 1h30m15s (with configurable units)
     */
    private static String formatCompact(long seconds, String language) {
        if (seconds < 60) {
            return seconds + getTimeUnit("second_short", language, seconds);
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes < 60) {
            return remainingSeconds > 0 ?
                    minutes + getTimeUnit("minute_short", language, minutes) +
                            remainingSeconds + getTimeUnit("second_short", language, remainingSeconds) :
                    minutes + getTimeUnit("minute_short", language, minutes);
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours < 24) {
            return remainingMinutes > 0 ?
                    hours + getTimeUnit("hour_short", language, hours) +
                            remainingMinutes + getTimeUnit("minute_short", language, remainingMinutes) :
                    hours + getTimeUnit("hour_short", language, hours);
        }

        long days = hours / 24;
        long remainingHours = hours % 24;

        return remainingHours > 0 ?
                days + getTimeUnit("day_short", language, days) +
                        remainingHours + getTimeUnit("hour_short", language, remainingHours) :
                days + getTimeUnit("day_short", language, days);
    }

    /**
     * Detailed format: 1 hour 30 minutes 15 seconds (with configurable units)
     */
    private static String formatDetailed(long seconds, String language) {
        if (seconds <= 0) {
            return "0 " + getTimeUnit("second_plural", language, 0);
        }

        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
        long secs = seconds - TimeUnit.DAYS.toSeconds(days) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" ").append(getTimeUnit("day", language, days)).append(" ");
        }
        if (hours > 0) {
            sb.append(hours).append(" ").append(getTimeUnit("hour", language, hours)).append(" ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" ").append(getTimeUnit("minute", language, minutes)).append(" ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append(" ").append(getTimeUnit("second", language, secs));
        }

        return sb.toString().trim();
    }

    /**
     * Get time unit from config with proper pluralization
     */
    private static String getTimeUnit(String unitKey, String language, long value) {
        ConfigManager configManager = getConfigManager();
        String unit = configManager.getTimeUnit(language, unitKey);

        // Handle pluralization for detailed format
        if (unitKey.endsWith("_plural")) {
            return unit;
        }

        // For short format, just return the unit
        if (unitKey.endsWith("_short")) {
            return unit;
        }

        // For regular units, handle pluralization
        if (value != 1 && configManager.getConfig().contains("time-units." + language + "." + unitKey + "_plural")) {
            return configManager.getTimeUnit(language, unitKey + "_plural");
        }

        return unit;
    }

    /**
     * Parse time string to seconds (e.g., "1h30m" -> 5400)
     */
    public static long parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return 0;
        }

        long totalSeconds = 0;
        StringBuilder number = new StringBuilder();

        for (char c : timeString.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() > 0) {
                    long value = Long.parseLong(number.toString());
                    switch (c) {
                        case 's': totalSeconds += value; break;
                        case 'm': totalSeconds += value * 60; break;
                        case 'h': totalSeconds += value * 3600; break;
                        case 'd': totalSeconds += value * 86400; break;
                        case 'w': totalSeconds += value * 604800; break;
                    }
                    number.setLength(0);
                }
            }
        }

        return totalSeconds;
    }
}