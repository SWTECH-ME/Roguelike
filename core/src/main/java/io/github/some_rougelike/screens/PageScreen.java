package io.github.some_rougelike.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.ui.AnimatedImage;

public abstract class PageScreen extends ScreenAdapter {

    protected final Main main;
    protected final Stage stage;

    protected PageScreen(Main main, String title) {
        this.main = main;
        this.stage = new Stage(new ScreenViewport());

        Texture sheetTexture = Assets.getInstance().getTexture(GameConfig.MAIN_BACKGROUND);

        int frameCols = GameConfig.BG_FRAME_COLS; 
        int frameRows = GameConfig.BG_FRAME_ROWS;  
        int totalRealFrames = GameConfig.BG_TOTAL_FRAMES; 

        int frameWidth = sheetTexture.getWidth() / frameCols;
        int frameHeight = sheetTexture.getHeight() / frameRows;

        TextureRegion[][] tmp = TextureRegion.split(sheetTexture, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[totalRealFrames];
        int index = 0;
        
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                if (index < totalRealFrames) {
                    frames[index++] = tmp[i][j];
                }
            }
        }

        Animation<TextureRegion> backgroundAnimation = new Animation<TextureRegion>(GameConfig.ANIMATED_BACKGROUND_SPEED, frames);
        AnimatedImage animatedBackground = new AnimatedImage(backgroundAnimation);
        animatedBackground.setFillParent(true);
        animatedBackground.setScaling(Scaling.fill);
        
        stage.addActor(animatedBackground); 
    }

    public void goBack() {
        main.switchScreen(new MainMenuScreen(main));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}