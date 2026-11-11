package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;

public class SplashScreen extends ScreenAdapter {
    private Stage stage;
    private Texture logoTexture;

    public SplashScreen(Main main) {
        stage = new Stage(new ScreenViewport());

        try {
            logoTexture = new Texture(Gdx.files.internal(GameConfig.SPLASH_LOGO_PATH));
            logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            
            Image logo = new Image(logoTexture);
            logo.setScaling(Scaling.fit);
            
            float uiScale = Math.min(Gdx.graphics.getWidth() / 1920f, Gdx.graphics.getHeight() / 1080f);
            float finalScale = uiScale * GameConfig.SPLASH_LOGO_SCALE;
            
            Table root = new Table();
            root.setFillParent(true);
            root.add(logo).size(logoTexture.getWidth() * finalScale, logoTexture.getHeight() * finalScale);
            
            logo.getColor().a = 0f;
            
            logo.addAction(Actions.sequence(
                Actions.fadeIn(GameConfig.SPLASH_FADE_IN_DURATION),
                Actions.delay(GameConfig.SPLASH_HOLD_DURATION),
                Actions.fadeOut(GameConfig.SPLASH_FADE_OUT_DURATION),
                Actions.run(() -> main.switchScreen(new MainMenuScreen(main, false, true)))
            ));
            
            stage.addActor(root);
        } catch (Exception e) {
            Gdx.app.error("SplashScreen", e.getMessage());
            main.switchScreen(new MainMenuScreen(main, false, true));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 10 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (logoTexture != null) {
            logoTexture.dispose();
        }
    }
}