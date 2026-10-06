package io.github.some_rougelike.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.ui.MainMenuUIBuilder;
import com.badlogic.gdx.Screen;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;

public MainMenuScreen(Main main) {
    stage = new Stage(new ScreenViewport());

    Skin skin = Assets.getInstance().getSkin("ui/uiskin.json");
    Texture bgTexture = Assets.getInstance().getTexture("ui/background.jpg");

    Image background = new Image(bgTexture);
    background.setFillParent(true);
    stage.addActor(background);

    new MainMenuUIBuilder().build(stage, skin, main);
}

    @Override
    public void render(float delta) {
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

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
}
