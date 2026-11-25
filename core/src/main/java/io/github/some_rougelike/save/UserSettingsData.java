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
    private String backgroundTheme = GameConfig.DEFAULT_BACKGROUND_THEME;

    public UserSettingsData() {
    }

    /** 
     * @return float
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /** 
     * @param masterVolume
     */
    public void setMasterVolume(float masterVolume) {
        this.masterVolume = masterVolume;
    }

    /** 
     * @return float
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /** 
     * @param musicVolume
     */
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    /** 
     * @return float
     */
    public float getSfxVolume() {
        return sfxVolume;
    }

    /** 
     * @param sfxVolume
     */
    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    /** 
     * @return String
     */
    public String getDisplayMode() {
        return displayMode;
    }

    /** 
     * @param displayMode
     */
    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    /** 
     * @return int
     */
    public int getResolutionWidth() {
        return resolutionWidth;
    }

    /** 
     * @param resolutionWidth
     */
    public void setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    /** 
     * @return int
     */
    public int getResolutionHeight() {
        return resolutionHeight;
    }

    /** 
     * @param resolutionHeight
     */
    public void setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    /** 
     * @return boolean
     */
    public boolean isVsync() {
        return vsync;
    }

    /** 
     * @param vsync
     */
    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    /** 
     * @return String
     */
    public String getSelectedDeckSkin() {
        if (selectedDeckSkin == null || selectedDeckSkin.trim().isEmpty()) {
            return GameConfig.DEFAULT_DECK_SKIN;
        }
        return selectedDeckSkin;
    }

    /** 
     * @param selectedDeckSkin
     */
    public void setSelectedDeckSkin(String selectedDeckSkin) {
        this.selectedDeckSkin = selectedDeckSkin;
    }

    /** 
     * @return String
     */
    public String getBackgroundTheme() {
        if (backgroundTheme == null || backgroundTheme.trim().isEmpty()) {
            return GameConfig.DEFAULT_BACKGROUND_THEME;
        }
        return backgroundTheme;
    }

    /** 
     * @param backgroundTheme
     */
    public void setBackgroundTheme(String backgroundTheme) {
        this.backgroundTheme = backgroundTheme;
    }
}
