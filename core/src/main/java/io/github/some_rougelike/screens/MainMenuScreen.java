package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.ui.MainMenuUIBuilder;
import io.github.some_rougelike.ui.MenuBackgroundRenderer;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;
    private MenuBackgroundRenderer backgroundRenderer;

    public MainMenuScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = Assets.getInstance().getSkin("ui/uiskin.json");
        Texture bgTexture = Assets.getInstance().getTexture("ui/background.jpg"); // CHANGE BACKGROUND HERE

        backgroundRenderer = new MenuBackgroundRenderer(bgTexture);

        MainMenuUIBuilder uiBuilder = new MainMenuUIBuilder();
        uiBuilder.build(stage, skin);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background first, the UI layer (stage) must be drawn on top of it
        backgroundRenderer.render();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        backgroundRenderer.resize(width, height);
    }

    @Override
    public void dispose() {
        backgroundRenderer.dispose();
        stage.dispose();
    }
}