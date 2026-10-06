package io.github.some_rougelike;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

public class Assets implements Disposable {
    private static final Assets INSTANCE = new Assets();
    private final AssetManager assetManager;

    private Assets() {
        assetManager = new AssetManager();
    }

    public static Assets getInstance() {
        return INSTANCE;
    }

    /**
     * Queue the assets to be loaded.
     * The LibGDX AssetManager will load these asynchronously.
     */
    public void load() {
        // Example for textures (cards, backgrounds)
        // assetManager.load("cards/card_back.png", Texture.class);
        // assetManager.load("ui/background.png", Texture.class);

        // Example for UI Skin (if using Scene2D UI)
        // assetManager.load("skin/uiskin.json", Skin.class);

        // Example for fonts
        // assetManager.load("fonts/font.fnt", BitmapFont.class);
        assetManager.load("ui/uiskin.json", Skin.class);
        assetManager.load("ui/background.jpg", Texture.class); // CHANGE BACKGROUND HERE
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }
    
    /**
     * Call this inside your render loop while loading is in progress.
     * @return true if all assets are successfully loaded.
     */
    public boolean update() {
        return assetManager.update();
    }

    /**
     * Returns the loading progress as a float between 0.0 and 1.0 (useful for loading screens).
     */
    public float getProgress() {
        return assetManager.getProgress();
    }
    
    // --- Getter methods for various asset types ---

    public Texture getTexture(String fileName) {
        return assetManager.get(fileName, Texture.class);
    }

    public BitmapFont getFont(String fileName) {
        return assetManager.get(fileName, BitmapFont.class);
    }

    public Skin getSkin(String fileName) {
        return assetManager.get(fileName, Skin.class);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }

}