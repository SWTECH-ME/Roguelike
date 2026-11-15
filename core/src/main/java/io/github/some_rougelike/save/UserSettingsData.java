package io.github.some_rougelike.save;

import io.github.some_rougelike.GameConfig;

public class UserSettingsData {
    private float masterVolume = GameConfig.DEFAULT_MASTER_VOLUME;
    private float musicVolume = GameConfig.DEFAULT_MUSIC_VOLUME;
    private float sfxVolume = GameConfig.DEFAULT_SFX_VOLUME;
    private String displayMode = GameConfig.DEFAULT_DISPLAY_MODE;
    private int resolutionWidth = GameConfig.DEFAULT_WINDOW_WIDTH;
    private int resolutionHeight = GameConfig.DEFAULT_WINDOW_HEIGHT;
    private boolean vsync = GameConfig.DEFAULT_VSYNC;
    private String selectedDeckSkin = GameConfig.DEFAULT_DECK_SKIN;

    public UserSettingsData() {
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = masterVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public void setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public void setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public String getSelectedDeckSkin() {
        if (selectedDeckSkin == null || selectedDeckSkin.trim().isEmpty()) {
            return GameConfig.DEFAULT_DECK_SKIN;
        }
        return selectedDeckSkin;
    }

    public void setSelectedDeckSkin(String selectedDeckSkin) {
        this.selectedDeckSkin = selectedDeckSkin;
    }
}
