package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws the menu background with a SpriteBatch.
 * Call render() before stage.draw() so the UI layer ends up on top.
 */
public class MenuBackgroundRenderer implements Disposable {
    private final SpriteBatch batch;
    private final Texture texture;

    public MenuBackgroundRenderer(Texture texture) {
        this.texture = texture;
        this.batch = new SpriteBatch();
    }

    public void render() {
        batch.begin();
        batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    /**
     * Only the batch is disposed here, the texture is owned by Assets.
     */
    @Override
    public void dispose() {
        batch.dispose();
    }
}
