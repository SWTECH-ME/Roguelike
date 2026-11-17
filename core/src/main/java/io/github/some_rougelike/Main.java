package io.github.some_rougelike;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.screens.SplashScreen;
import io.github.some_rougelike.model.RankProgress;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.save.UserSettingsData;

public class Main extends Game {

    // Stores the last known windowed resolution so fullscreen toggle can restore it
    private int windowedWidth = GameConfig.DEFAULT_WINDOW_WIDTH;
    private int windowedHeight = GameConfig.DEFAULT_WINDOW_HEIGHT;
    private io.github.some_rougelike.ui.WindowModeController windowModeController;

    public void setWindowModeController(io.github.some_rougelike.ui.WindowModeController controller) {
        this.windowModeController = controller;
    }

    public io.github.some_rougelike.ui.WindowModeController getWindowModeController() {
        return windowModeController;
    }

    @Override
    public void create() {
        SaveManager.getInstance().load();
        RankProgress.getInstance().load();

        // Load all game assets at startup
        Assets.getInstance().load();
        Assets.getInstance().finishLoading();
        SoundManager.getInstance().load();

        UserSettingsData settings = SaveManager.getInstance().getSettings();
        GameConfig.applyBackgroundTheme(settings.getBackgroundTheme());
        SoundManager.getInstance().setMasterVolume(settings.getMasterVolume());
        SoundManager.getInstance().setMenuMusicVolume(settings.getMusicVolume());
        SoundManager.getInstance().setSfxVolume(settings.getSfxVolume());

        applySavedDisplaySettings(settings);

        // Set the initial screen (main menu)
        setScreen(new SplashScreen(this));
    }

    private void applySavedDisplaySettings(UserSettingsData settings) {
        if (settings.getResolutionWidth() > 0 && settings.getResolutionHeight() > 0) {
            windowedWidth = settings.getResolutionWidth();
            windowedHeight = settings.getResolutionHeight();
        }
        Gdx.graphics.setVSync(settings.isVsync());

        String mode = settings.getDisplayMode();
        if ("Windowed".equals(mode)) {
            if (windowModeController != null) {
                windowModeController.setBorderless(false);
            }
            Gdx.graphics.setUndecorated(false);
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
        } else if ("Borderless Windowed".equals(mode)) {
            Gdx.graphics.setUndecorated(true);
            Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
            if (windowModeController != null) {
                windowModeController.setBorderless(true);
            }
        } else {
            Gdx.graphics.setUndecorated(false);
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    /**
     * Switches to a new screen.
     * The old screen is disposed AFTER the new one is set,
     * using postRunnable to avoid disposing during rendering.
     */
    public void switchScreen(Screen next) {
        Gdx.app.postRunnable(() -> {
            Screen old = getScreen();
            setScreen(next);
            if (old != null) {
                old.dispose();
            }
        });
    }

    @Override
    public void render() {
        // Toggle fullscreen when F11 is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        // Continue normal LibGDX rendering flow
        super.render();
    }

    /**
     * Toggles between fullscreen and windowed mode.
     * When switching to fullscreen, the current window size is saved.
     */
    private void toggleFullscreen() {
        UserSettingsData settings = SaveManager.getInstance().getSettings();
        if (Gdx.graphics.isFullscreen()) {
            // Restore previous windowed resolution
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
            settings.setDisplayMode("Windowed");
        } else {
            // Save current window size before going fullscreen
            windowedWidth = Gdx.graphics.getWidth();
            windowedHeight = Gdx.graphics.getHeight();
            settings.setResolutionWidth(windowedWidth);
            settings.setResolutionHeight(windowedHeight);

            // Switch to the monitor's native fullscreen mode
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            settings.setDisplayMode("Fullscreen");
        }
        SaveManager.getInstance().save();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
        RankProgress.getInstance().save();
        SaveManager.getInstance().save();

        // Dispose all loaded assets
        Assets.getInstance().dispose();
        SoundManager.getInstance().dispose();
    }
}
