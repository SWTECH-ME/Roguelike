package io.github.some_rougelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Sound startupSound;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("lwjgl3/bin/main/609019193_2648602242189483_7703116507261013376_n.jpg");
        
        startupSound = Gdx.audio.newSound(Gdx.files.internal("startup.mp"));
        startupSound.play(1.0f);
        
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 0);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        startupSound.dispose();
    }
}
