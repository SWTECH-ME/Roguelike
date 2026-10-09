package io.github.some_rougelike.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.ui.AnimatedImage;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.ui.MainMenuUIBuilder;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;

    public MainMenuScreen(Main main) {
        stage = new Stage(new ScreenViewport());

        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        stage.addActor(GameConfig.USE_ANIMATED_BACKGROUND ? createAnimatedBackground() : createStaticBackground());

        new MainMenuUIBuilder().build(stage, skin, main);
    }

    private Image createStaticBackground() {
        Image background = new Image(Assets.getInstance().getTexture(GameConfig.MAIN_STATIC_BACKGROUND));
        background.setFillParent(true);
        return background;
    }

    private AnimatedImage createAnimatedBackground() {
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

        Animation<TextureRegion> backgroundAnimation = new Animation<>(GameConfig.ANIMATED_BACKGROUND_SPEED, frames);

        AnimatedImage animatedBackground = new AnimatedImage(backgroundAnimation);
        animatedBackground.setFillParent(true);
        animatedBackground.setScaling(Scaling.stretch);
        return animatedBackground;
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
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
