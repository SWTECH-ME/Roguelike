package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.ui.MainMenuUIBuilder;
import io.github.some_rougelike.ui.TrippyBackground;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;
    private TrippyBackground trippyBackground;

    public MainMenuScreen(Main main) {
        stage = new Stage(new ScreenViewport());
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        
        trippyBackground = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        trippyBackground.setTouchable(Touchable.disabled);
        stage.addActor(trippyBackground);
        
        new MainMenuUIBuilder().build(stage, skin, main);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        SoundManager.getInstance().startMenuMusic();
        SoundManager.getInstance().play(SoundManager.SoundEvent.GAME_OPEN);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 10 / 30f));
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