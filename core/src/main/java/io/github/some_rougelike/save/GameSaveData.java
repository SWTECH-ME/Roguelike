package io.github.some_rougelike.save;

import io.github.some_rougelike.model.RankProgress;

public class GameSaveData {
    private float[] rankProgress = new float[RankProgress.RANK_COUNT];
    private UserSettingsData settings = new UserSettingsData();
    private int highScore = 0;
    private boolean hasActiveRun = false;
    private RunSaveData activeRun = null;

    public GameSaveData() {
    }

    public float[] getRankProgress() {
        return rankProgress;
    }

    public void setRankProgress(float[] rankProgress) {
        this.rankProgress = rankProgress;
    }

    public UserSettingsData getSettings() {
        if (settings == null) {
            settings = new UserSettingsData();
        }
        return settings;
    }

    public void setSettings(UserSettingsData settings) {
        this.settings = settings;
    }

    public int getHighScore() {
        return Math.max(0, highScore);
    }

    public void setHighScore(int highScore) {
        this.highScore = Math.max(0, highScore);
    }

    public boolean isHasActiveRun() {
        return hasActiveRun && activeRun != null;
    }

    public void setHasActiveRun(boolean hasActiveRun) {
        this.hasActiveRun = hasActiveRun;
    }

    public RunSaveData getActiveRun() {
        return activeRun;
    }

    public void setActiveRun(RunSaveData activeRun) {
        this.activeRun = activeRun;
    }
}
