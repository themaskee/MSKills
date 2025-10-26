package com.mskplugins.mskills.manager;

import com.mskplugins.mskills.KillRewardPlugin;
import com.mskplugins.mskills.util.TextUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final KillRewardPlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, String> messages;
    private String language;

    public MessageManager(KillRewardPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messages = new HashMap<>();
    }

    public void loadMessages() {
        messages.clear();

        language = configManager.getLanguage();
        FileConfiguration config = configManager.getConfig();

        // Load messages for selected language
        String messagePath = "messages." + language + ".";

        if (config.getConfigurationSection("messages." + language) != null) {
            for (String key : config.getConfigurationSection("messages." + language).getKeys(false)) {
                String message = config.getString(messagePath + key);
                if (message != null) {
                    messages.put(key, TextUtils.colorize(message));
                }
            }
        }

        // Load default messages if some are missing
        loadDefaultMessages();

        plugin.getLogger().info("✓ Loaded " + messages.size() + " messages for language: " + language);
    }

    private void loadDefaultMessages() {
        if (language.equals("tr")) {
            loadTurkishMessages();
        } else {
            loadEnglishMessages();
        }
    }

    private void loadEnglishMessages() {
        // Time unit messages
        putIfMissing("time.second", "second");
        putIfMissing("time.second_short", "s");
        putIfMissing("time.second_plural", "seconds");
        putIfMissing("time.minute", "minute");
        putIfMissing("time.minute_short", "m");
        putIfMissing("time.minute_plural", "minutes");
        putIfMissing("time.hour", "hour");
        putIfMissing("time.hour_short", "h");
        putIfMissing("time.hour_plural", "hours");
        putIfMissing("time.day", "day");
        putIfMissing("time.day_short", "d");
        putIfMissing("time.day_plural", "days");

        // Command messages
        putIfMissing("no-permission", "&cYou don't have permission to use this command!");
        putIfMissing("config-reloaded", "&aConfiguration reloaded successfully!");
        putIfMissing("feature-toggled", "&aFeature &e%feature% &ahas been %state%&a!");
        putIfMissing("language-changed", "&aLanguage changed to &e%language%&a!");
        putIfMissing("debug-enabled", "&aDebug mode enabled!");
        putIfMissing("debug-disabled", "&cDebug mode disabled!");
        putIfMissing("cooldown-set", "&aCooldown set to &e%time%&a!");
        putIfMissing("global-cooldown-set", "&aGlobal cooldown set to &e%time%&a!");
        putIfMissing("sound-set", "&aSound set to &e%sound%&a!");
        putIfMissing("actionbar-message-set", "&aActionBar message set!");
        putIfMissing("time-format-set", "&aTime format set to &e%format%&a!");

        // Feature toggle messages
        putIfMissing("rewards-enabled", "&aRewards enabled!");
        putIfMissing("rewards-disabled", "&cRewards disabled!");
        putIfMissing("ip-check-enabled", "&aIP Check enabled!");
        putIfMissing("ip-check-disabled", "&cIP Check disabled!");
        putIfMissing("cooldown-enabled", "&aCooldown system enabled!");
        putIfMissing("cooldown-disabled", "&cCooldown system disabled!");
        putIfMissing("sound-enabled", "&aSounds enabled!");
        putIfMissing("sound-disabled", "&cSounds disabled!");
        putIfMissing("actionbar-enabled", "&aActionBar messages enabled!");
        putIfMissing("actionbar-disabled", "&cActionBar messages disabled!");

        // Kill event messages
        putIfMissing("same-ip", "&cYou cannot receive rewards for killing players with the same IP!");
        putIfMissing("cooldown", "&cYou must wait &e%time% &cbefore killing this player again!");
        putIfMissing("global-cooldown", "&cYou must wait &e%time% &cbefore killing anyone again!");
        putIfMissing("reward-given", "&aYou received rewards for killing &e%victim%&a!");
        putIfMissing("self-kill", "&cYou cannot receive rewards for killing yourself!");

        // Status messages
        putIfMissing("status-header", "&6&lMSKills Status");
        putIfMissing("status-line", "&7%feature%: %status%");

        // Help messages
        putIfMissing("help-header", "&6&lMSKills Commands");
        putIfMissing("help-line", "&e%command% &7- %description%");

        // Error messages
        putIfMissing("player-not-found", "&cPlayer not found!");
        putIfMissing("invalid-number", "&cInvalid number!");
        putIfMissing("invalid-time-format", "&cInvalid time format! Use: default, compact, or detailed");
        putIfMissing("invalid-sound", "&cInvalid sound!");
        putIfMissing("invalid-language", "&cInvalid language! Available: en, tr");

        // Add any missing messages that might be causing issues
        putIfMissing("enabled", "&aenabled");
        putIfMissing("disabled", "&cdisabled");
        putIfMissing("on", "&aon");
        putIfMissing("off", "&coff");
    }

    private void loadTurkishMessages() {
        // Time unit messages
        putIfMissing("time.second", "saniye");
        putIfMissing("time.second_short", "sn");
        putIfMissing("time.second_plural", "saniye");
        putIfMissing("time.minute", "dakika");
        putIfMissing("time.minute_short", "dk");
        putIfMissing("time.minute_plural", "dakika");
        putIfMissing("time.hour", "saat");
        putIfMissing("time.hour_short", "sa");
        putIfMissing("time.hour_plural", "saat");
        putIfMissing("time.day", "gün");
        putIfMissing("time.day_short", "g");
        putIfMissing("time.day_plural", "gün");

        // Command messages
        putIfMissing("no-permission", "&cBu komutu kullanma izniniz yok!");
        putIfMissing("config-reloaded", "&aYapılandırma başarıyla yeniden yüklendi!");
        putIfMissing("feature-toggled", "&aÖzellik &e%feature% &a%state%&a!");
        putIfMissing("language-changed", "&aDil &e%language% &aolarak değiştirildi!");
        putIfMissing("debug-enabled", "&aHata ayıklama modu etkinleştirildi!");
        putIfMissing("debug-disabled", "&cHata ayıklama modu devre dışı bırakıldı!");
        putIfMissing("cooldown-set", "&aBekleme süresi &e%time% &aolarak ayarlandı!");
        putIfMissing("global-cooldown-set", "&aGlobal bekleme süresi &e%time% &aolarak ayarlandı!");
        putIfMissing("sound-set", "&aSes &e%sound% &aolarak ayarlandı!");
        putIfMissing("actionbar-message-set", "&aActionBar mesajı ayarlandı!");
        putIfMissing("time-format-set", "&aZaman formatı &e%format% &aolarak ayarlandı!");

        // Feature toggle messages
        putIfMissing("rewards-enabled", "&aÖdüller etkinleştirildi!");
        putIfMissing("rewards-disabled", "&cÖdüller devre dışı bırakıldı!");
        putIfMissing("ip-check-enabled", "&aIP Kontrolü etkinleştirildi!");
        putIfMissing("ip-check-disabled", "&cIP Kontrolü devre dışı bırakıldı!");
        putIfMissing("cooldown-enabled", "&aBekleme süresi etkinleştirildi!");
        putIfMissing("cooldown-disabled", "&cBekleme süresi devre dışı bırakıldı!");
        putIfMissing("sound-enabled", "&aSesler etkinleştirildi!");
        putIfMissing("sound-disabled", "&cSesler devre dışı bırakıldı!");
        putIfMissing("actionbar-enabled", "&aActionBar mesajları etkinleştirildi!");
        putIfMissing("actionbar-disabled", "&cActionBar mesajları devre dışı bırakıldı!");

        // Kill event messages
        putIfMissing("same-ip", "&cAynı IP'ye sahip oyuncuları öldürerek ödül alamazsınız!");
        putIfMissing("cooldown", "&cBu oyuncuyu tekrar öldürmeden önce &e%time% &cbeklemelisiniz!");
        putIfMissing("global-cooldown", "&cBaşka birini öldürmeden önce &e%time% &cbeklemelisiniz!");
        putIfMissing("reward-given", "&a&e%victim%&a adlı oyuncuyu öldürerek ödül aldınız!");
        putIfMissing("self-kill", "&cKendinizi öldürerek ödül alamazsınız!");

        // Status messages
        putIfMissing("status-header", "&6&lMSKills Durum");
        putIfMissing("status-line", "&7%feature%: %status%");

        // Help messages
        putIfMissing("help-header", "&6&lMSKills Komutları");
        putIfMissing("help-line", "&e%command% &7- %description%");

        // Error messages
        putIfMissing("player-not-found", "&cOyuncu bulunamadı!");
        putIfMissing("invalid-number", "&cGeçersiz sayı!");
        putIfMissing("invalid-time-format", "&cGeçersiz zaman formatı! Kullanın: default, compact, detailed");
        putIfMissing("invalid-sound", "&cGeçersiz ses!");
        putIfMissing("invalid-language", "&cGeçersiz dil! Mevcut: en, tr");

        // Add any missing messages that might be causing issues
        putIfMissing("enabled", "&aetkinleştirildi");
        putIfMissing("disabled", "&cdevre dışı bırakıldı");
        putIfMissing("on", "&aaçık");
        putIfMissing("off", "&ckapalı");
    }

    private void putIfMissing(String key, String defaultValue) {
        if (!messages.containsKey(key)) {
            messages.put(key, TextUtils.colorize(defaultValue));
        }
    }

    public String getMessage(String key) {
        String message = messages.get(key);
        if (message == null) {
            plugin.getLogger().warning("Message key not found: " + key);
            return "&cMessage not found: " + key;
        }
        return message;
    }

    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);

        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("Uneven number of placeholders for key: " + key);
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return message;
    }

    public void reload() {
        loadMessages();
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, String> getMessages() {
        return new HashMap<>(messages);
    }

    // Method to update a message in config
    public void setMessage(String key, String value) {
        String path = "messages." + language + "." + key;
        configManager.getConfig().set(path, value);
        configManager.saveConfig();
        messages.put(key, TextUtils.colorize(value));
    }

    // Helper method to check if a message exists
    public boolean hasMessage(String key) {
        return messages.containsKey(key);
    }
}