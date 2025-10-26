package com.mskplugins.mskills;

import com.mskplugins.mskills.command.CommandManager;
import com.mskplugins.mskills.listener.PlayerDeathListener;
import com.mskplugins.mskills.manager.*;
import com.mskplugins.mskills.util.TextUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KillRewardPlugin extends JavaPlugin {

    private static KillRewardPlugin instance;

    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CooldownManager cooldownManager;
    private MessageManager messageManager;
    private EffectManager effectManager;
    private CommandManager commandManager;
    private SettingsManager settingsManager;

    // Thread pool for async operations
    private ExecutorService asyncExecutor;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize TextUtils with MiniMessage support
        TextUtils.initialize();

        // Save default config first
        saveDefaultConfig();

        // Initialize thread pool
        int threadCount = getConfig().getInt("performance.async-threads", 2);
        this.asyncExecutor = Executors.newFixedThreadPool(threadCount);

        // Initialize managers in correct order
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.effectManager = new EffectManager(this);
        this.commandManager = new CommandManager(this);
        this.settingsManager = new SettingsManager(this);

        // Load configuration and messages
        configManager.loadConfig();
        messageManager.loadMessages();

        // Initialize database async to not block main thread
        asyncExecutor.submit(() -> {
            try {
                databaseManager.initialize();
                getLogger().info("✓ Database initialized successfully");
            } catch (Exception e) {
                getLogger().severe("✗ Failed to initialize database: " + e.getMessage());
            }
        });

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        // Register commands
        commandManager.registerCommands();

        getLogger().info("✓ MSKills v" + getDescription().getVersion() + " enabled successfully!");
        getLogger().info("✓ Language: " + messageManager.getLanguage().toUpperCase());
        getLogger().info("✓ Time Format: " + configManager.getTimeFormat());
        getLogger().info("✓ MiniMessage: Enabled");
        getLogger().info("✓ PlaceholderAPI: " + (TextUtils.hasPlaceholderAPI() ? "Enabled" : "Not found"));
        getLogger().info("✓ Features: Rewards=" + configManager.isRewardEnabled() +
                ", IP Check=" + configManager.isIPCheckEnabled() +
                ", Cooldown=" + configManager.isCooldownEnabled() +
                ", Sound=" + configManager.isSoundEnabled() +
                ", ActionBar=" + configManager.isActionBarEnabled());
    }

    @Override
    public void onDisable() {
        // Shutdown thread pool gracefully
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Close database connection
        if (databaseManager != null) {
            databaseManager.close();
        }

        // Close TextUtils
        TextUtils.close();

        getLogger().info("✓ MSKills disabled successfully!");
    }

    /**
     * Reload the entire plugin
     */
    public void reload() {
        // Reload configuration
        configManager.reload();

        // Reload messages
        messageManager.reload();

        // Clear cache
        cooldownManager.clearAllCache();

        getLogger().info("✓ Plugin reloaded successfully!");
        getLogger().info("✓ Language: " + messageManager.getLanguage().toUpperCase());
        getLogger().info("✓ Time Format: " + configManager.getTimeFormat());
    }

    public static KillRewardPlugin getInstance() {
        return instance;
    }

    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public SettingsManager getSettingsManager() { return settingsManager; }
    public ExecutorService getAsyncExecutor() { return asyncExecutor; }
}