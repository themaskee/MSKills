package com.mskplugins.mskills.data;

import java.util.UUID;

public class PlayerData {
    private final UUID playerUUID;
    private final String playerName;
    private long lastKillTime;
    private String ipAddress;
    private int totalKills;
    private int rewardsReceived;

    public PlayerData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.lastKillTime = 0;
        this.totalKills = 0;
        this.rewardsReceived = 0;
    }

    public PlayerData(UUID playerUUID, String playerName, long lastKillTime, String ipAddress, int totalKills, int rewardsReceived) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.lastKillTime = lastKillTime;
        this.ipAddress = ipAddress;
        this.totalKills = totalKills;
        this.rewardsReceived = rewardsReceived;
    }

    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public long getLastKillTime() { return lastKillTime; }
    public String getIpAddress() { return ipAddress; }
    public int getTotalKills() { return totalKills; }
    public int getRewardsReceived() { return rewardsReceived; }

    // Setters
    public void setLastKillTime(long lastKillTime) { this.lastKillTime = lastKillTime; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setTotalKills(int totalKills) { this.totalKills = totalKills; }
    public void setRewardsReceived(int rewardsReceived) { this.rewardsReceived = rewardsReceived; }

    // Utility methods
    public void incrementKills() { this.totalKills++; }
    public void incrementRewards() { this.rewardsReceived++; }

    public boolean isOnCooldown(long cooldownDuration) {
        if (lastKillTime == 0) return false;
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastKillTime) < cooldownDuration;
    }

    public long getRemainingCooldown(long cooldownDuration) {
        if (lastKillTime == 0) return 0;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastKillTime;
        return Math.max(0, cooldownDuration - elapsed);
    }

    public double getRewardRate() {
        if (totalKills == 0) return 0.0;
        return (double) rewardsReceived / totalKills * 100;
    }

    @Override
    public String toString() {
        return String.format(
                "PlayerData{uuid=%s, name=%s, lastKill=%d, kills=%d, rewards=%d, rate=%.1f%%}",
                playerUUID, playerName, lastKillTime, totalKills, rewardsReceived, getRewardRate()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerData that = (PlayerData) obj;
        return playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }
}