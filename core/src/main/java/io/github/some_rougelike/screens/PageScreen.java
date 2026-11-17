package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.ui.TrippyBackground;

public abstract class PageScreen extends ScreenAdapter {
    protected final Main main;
    protected final Stage stage;
    private TrippyBackground trippyBackground;

    private static final float REFERENCE_WIDTH = 1920f;
    private static final float REFERENCE_HEIGHT = 1080f;

    protected PageScreen(Main main, String title) {
        this.main = main;
        this.stage = new Stage(new ScreenViewport());

        trippyBackground = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        trippyBackground.setTouchable(Touchable.disabled);
        stage.addActor(trippyBackground);
    }

    public float getScaleFactor() {
        float currentWidth = Gdx.graphics.getWidth();
        float currentHeight = Gdx.graphics.getHeight();
        return Math.min(currentWidth / REFERENCE_WIDTH, currentHeight / REFERENCE_HEIGHT) * 1.5f;
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
        if (trippyBackground != null) {
            trippyBackground.setSize(stage.getWidth(), stage.getHeight());
            trippyBackground.setPosition(0, 0);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}