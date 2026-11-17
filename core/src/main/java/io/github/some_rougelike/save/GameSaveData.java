package io.github.some_rougelike.save;

import io.github.some_rougelike.model.RankProgress;

public class GameSaveData {
    private float[] rankProgress = new float[RankProgress.RANK_COUNT];
    private UserSettingsData settings = new UserSettingsData();
    private boolean hasActiveRun = false;
    private RunSaveData activeRun = null;
    private String[] discoveredCombos = new String[0];

    public String[] getDiscoveredCombos() {
        if (discoveredCombos == null) {
            discoveredCombos = new String[0];
        }
        return discoveredCombos;
    }

    public void setDiscoveredCombos(String[] discoveredCombos) {
        this.discoveredCombos = discoveredCombos;
    }

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
