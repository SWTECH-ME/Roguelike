package io.github.some_rougelike;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import io.github.some_rougelike.screens.MainMenuScreen;

public class Main extends Game {

    // Stores the last known windowed resolution so fullscreen toggle can restore it
    private int windowedWidth = 1280;
    private int windowedHeight = 720;

    @Override
    public void create() {
        // Load all game assets at startup
        Assets.getInstance().load();
        Assets.getInstance().finishLoading();

        // Set the initial screen (main menu)
        setScreen(new MainMenuScreen(this));
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
        if (Gdx.graphics.isFullscreen()) {
            // Restore previous windowed resolution
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
        } else {
            // Save current window size before going fullscreen
            windowedWidth = Gdx.graphics.getWidth();
            windowedHeight = Gdx.graphics.getHeight();

            // Switch to the monitor's native fullscreen mode
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    @Override
    public void dispose() {
        // Dispose the active screen if it exists
        if (getScreen() != null) {
            getScreen().dispose();
        }

        // Dispose LibGDX internals
        super.dispose();

        // Dispose all loaded assets
        Assets.getInstance().dispose();
    }
}
