package com.mskplugins.mskills.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SoundResolver {

    private final Map<String, Sound> soundCache;
    private final Logger logger;

    public SoundResolver() {
        this.soundCache = new HashMap<>();
        this.logger = JavaPlugin.getProvidingPlugin(SoundResolver.class).getLogger();
        initializeCache();
    }

    /**
     * Pre-cache common sounds for better performance
     */
    private void initializeCache() {
        // Reward sounds
        cacheSound("entity.player.levelup", Sound.ENTITY_PLAYER_LEVELUP);
        cacheSound("entity.experience_orb.pickup", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        cacheSound("block.note_block.pling", Sound.BLOCK_NOTE_BLOCK_PLING);
        cacheSound("ui.toast.challenge.complete", Sound.UI_TOAST_CHALLENGE_COMPLETE);
        cacheSound("entity.player.attack.strong", Sound.ENTITY_PLAYER_ATTACK_STRONG);

        // Denied sounds
        cacheSound("entity.villager.no", Sound.ENTITY_VILLAGER_NO);
        cacheSound("block.anvil.land", Sound.BLOCK_ANVIL_LAND);
        cacheSound("entity.ender_dragon.growl", Sound.ENTITY_ENDER_DRAGON_GROWL);
        cacheSound("entity.iron_golem.hurt", Sound.ENTITY_IRON_GOLEM_HURT);
        cacheSound("entity.zombie.attack_iron_door", Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR);

        // Ambient sounds
        cacheSound("ambient.cave", Sound.AMBIENT_CAVE);
        cacheSound("music_disc.11", Sound.MUSIC_DISC_11);

        // Legacy name support
        cacheSound("ENTITY_PLAYER_LEVELUP", Sound.ENTITY_PLAYER_LEVELUP);
        cacheSound("ENTITY_EXPERIENCE_ORB_PICKUP", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        cacheSound("BLOCK_NOTE_BLOCK_PLING", Sound.BLOCK_NOTE_BLOCK_PLING);
        cacheSound("UI_TOAST_CHALLENGE_COMPLETE", Sound.UI_TOAST_CHALLENGE_COMPLETE);
        cacheSound("ENTITY_VILLAGER_NO", Sound.ENTITY_VILLAGER_NO);
        cacheSound("BLOCK_ANVIL_LAND", Sound.BLOCK_ANVIL_LAND);
    }

    private void cacheSound(String key, Sound sound) {
        soundCache.put(key.toLowerCase(), sound);
    }

    /**
     * Resolve a sound name to a Sound object
     */
    public Sound resolve(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return Sound.ENTITY_PLAYER_LEVELUP; // Default fallback
        }

        String lowerName = soundName.toLowerCase();

        // Check cache first
        Sound cached = soundCache.get(lowerName);
        if (cached != null) {
            return cached;
        }

        // Try registry lookup with different formats
        Sound sound = tryRegistryLookup(soundName);
        if (sound != null) {
            soundCache.put(lowerName, sound); // Cache for next time
            return sound;
        }

        // Try with minecraft: namespace
        if (!lowerName.startsWith("minecraft:")) {
            sound = tryRegistryLookup("minecraft:" + lowerName);
            if (sound != null) {
                soundCache.put(lowerName, sound);
                return sound;
            }
        }

        // Log warning for unknown sounds
        logger.warning("Unknown sound: " + soundName + ". Using default sound.");

        // Cache the default to avoid repeated lookups
        soundCache.put(lowerName, Sound.ENTITY_PLAYER_LEVELUP);
        return Sound.ENTITY_PLAYER_LEVELUP;
    }

    private Sound tryRegistryLookup(String soundName) {
        try {
            NamespacedKey key = NamespacedKey.fromString(soundName);
            if (key != null) {
                return Registry.SOUNDS.get(key);
            }
        } catch (Exception e) {
            // Ignore and try other methods
        }
        return null;
    }

    /**
     * Get all available sound names (for debugging/autocomplete)
     */
    public java.util.List<String> getAvailableSounds() {
        java.util.List<String> sounds = new java.util.ArrayList<>();
        for (Sound sound : Registry.SOUNDS) {
            sounds.add(sound.getKey().toString());
        }
        return sounds;
    }

    /**
     * Check if a sound exists
     */
    public boolean soundExists(String soundName) {
        return resolve(soundName) != Sound.ENTITY_PLAYER_LEVELUP ||
                soundName.equalsIgnoreCase("entity.player.levelup");
    }

    /**
     * Clear the sound cache
     */
    public void clearCache() {
        soundCache.clear();
        initializeCache(); // Re-initialize with common sounds
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return "Sound cache: " + soundCache.size() + " entries";
    }
}