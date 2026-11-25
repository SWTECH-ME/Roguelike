package io.github.some_rougelike.save;

import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.model.RankProgress;

public class GameSaveData {
    private String username = GameConfig.getDefaultUsername();
    private float[] rankProgress = new float[RankProgress.RANK_COUNT];
    private UserSettingsData settings = new UserSettingsData();
    private int highScore = 0;
    private boolean hasActiveRun = false;
    private RunSaveData activeRun = null;
    private String[] discoveredCombos = new String[0];

    /** 
     * @return String
     */
    public String getUsername() {
        if (username == null || username.trim().isEmpty()) {
            username = GameConfig.getDefaultUsername();
        }
        return username;
    }

    /** 
     * @param username
     */
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.username = GameConfig.getDefaultUsername();
        } else {
            String trimmed = username.trim();
            this.username = trimmed.length() > GameConfig.PROFILE_MAX_USERNAME_LENGTH
                ? trimmed.substring(0, GameConfig.PROFILE_MAX_USERNAME_LENGTH)
                : trimmed;
        }
    }

    /** 
     * @return String[]
     */
    public String[] getDiscoveredCombos() {
        if (discoveredCombos == null) {
            discoveredCombos = new String[0];
        }
        return discoveredCombos;
    }

    /** 
     * @param discoveredCombos
     */
    public void setDiscoveredCombos(String[] discoveredCombos) {
        this.discoveredCombos = discoveredCombos;
    }

    public GameSaveData() {
    }

    /** 
     * @return float[]
     */
    public float[] getRankProgress() {
        return rankProgress;
    }

    /** 
     * @param rankProgress
     */
    public void setRankProgress(float[] rankProgress) {
        this.rankProgress = rankProgress;
    }

    /** 
     * @return UserSettingsData
     */
    public UserSettingsData getSettings() {
        if (settings == null) {
            settings = new UserSettingsData();
        }
        return settings;
    }

    /** 
     * @param settings
     */
    public void setSettings(UserSettingsData settings) {
        this.settings = settings;
    }

    /** 
     * @return int
     */
    public int getHighScore() {
        return Math.max(0, highScore);
    }

    /** 
     * @param highScore
     */
    public void setHighScore(int highScore) {
        this.highScore = Math.max(0, highScore);
    }

    /** 
     * @return boolean
     */
    public boolean isHasActiveRun() {
        return hasActiveRun && activeRun != null;
    }

    /** 
     * @param hasActiveRun
     */
    public void setHasActiveRun(boolean hasActiveRun) {
        this.hasActiveRun = hasActiveRun;
    }

    /** 
     * @return RunSaveData
     */
    public RunSaveData getActiveRun() {
        return activeRun;
    }

    /** 
     * @param activeRun
     */
    public void setActiveRun(RunSaveData activeRun) {
        this.activeRun = activeRun;
    }
}
