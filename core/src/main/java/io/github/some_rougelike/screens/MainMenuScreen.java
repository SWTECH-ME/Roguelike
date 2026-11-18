package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.ui.CardTransition;
import io.github.some_rougelike.ui.MainMenuUIBuilder;
import io.github.some_rougelike.ui.TrippyBackground;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;
    private TrippyBackground trippyBackground;
    private Array<Texture> generatedTextures = new Array<>();
    private int debugCodeIndex = 0;
    private boolean debugSequenceFailed = false;
    private boolean debugTransitioning = false;

    public MainMenuScreen(Main main) {
        this(main, false, false);
    }

    public MainMenuScreen(Main main, boolean withCardRain, boolean animateUI) {
        stage = new Stage(new ScreenViewport());
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);

        trippyBackground = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        trippyBackground.setTouchable(Touchable.disabled);
        stage.addActor(trippyBackground);

        new MainMenuUIBuilder().build(stage, skin, main, animateUI, trippyBackground, generatedTextures);
        registerDebugKeyListener(main);

        if (withCardRain) {
            Texture deckTexture = null;
            TextureRegion[][] cardRegions = null;
            try {
                FileHandle file = Gdx.files.internal(SaveManager.getInstance().getSelectedDeckSkinPath());
                if (!file.exists()) {
                    file = Gdx.files.internal(GameConfig.DECK_SKINS_DIR + "/" + GameConfig.DEFAULT_DECK_SKIN);
                }
                if (file.exists()) {
                    deckTexture = new Texture(file);
                    generatedTextures.add(deckTexture);
                    int cols = GameConfig.DECK_SHEET_COLS;
                    int rows = GameConfig.DECK_SHEET_ROWS;
                    cardRegions = TextureRegion.split(deckTexture, deckTexture.getWidth() / cols, deckTexture.getHeight() / rows);
                }
            } catch (Exception e) {}

            Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            whitePix.setColor(Color.WHITE);
            whitePix.fill();
            Texture whiteTex = new Texture(whitePix);
            whitePix.dispose();
            generatedTextures.add(whiteTex);
            TextureRegionDrawable whiteTintableBg = new TextureRegionDrawable(whiteTex);

            CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, true);

        } else if (animateUI) {
            stage.getRoot().getColor().a = 0f;
            stage.getRoot().addAction(Actions.fadeIn(GameConfig.MAIN_MENU_FADE_DURATION, Interpolation.fade));
        }
    }

    private void registerDebugKeyListener(Main main) {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (debugTransitioning || debugSequenceFailed) {
                    return false;
                }
                if (!Character.isLetterOrDigit(character)) {
                    return false;
                }
                String code = GameConfig.DEBUG_BOSS_TEST_CODE;
                if (code == null || code.isEmpty()) {
                    return false;
                }
                char lower = Character.toLowerCase(character);
                if (lower == Character.toLowerCase(code.charAt(debugCodeIndex))) {
                    debugCodeIndex++;
                    if (debugCodeIndex >= code.length()) {
                        debugCodeIndex = 0;
                        launchBossDebugLevel(main);
                    }
                } else {
                    debugSequenceFailed = true;
                }
                return false;
            }
        });
    }

    private void launchBossDebugLevel(Main main) {
        if (debugTransitioning) {
            return;
        }
        debugTransitioning = true;
        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);

        TextureRegion[][] cardRegions = null;
        try {
            FileHandle file = Gdx.files.internal(SaveManager.getInstance().getSelectedDeckSkinPath());
            if (!file.exists()) {
                file = Gdx.files.internal(GameConfig.DECK_SKINS_DIR + "/" + GameConfig.DEFAULT_DECK_SKIN);
            }
            if (file.exists()) {
                Texture deckTexture = new Texture(file);
                generatedTextures.add(deckTexture);
                cardRegions = TextureRegion.split(
                    deckTexture,
                    deckTexture.getWidth() / GameConfig.DECK_SHEET_COLS,
                    deckTexture.getHeight() / GameConfig.DECK_SHEET_ROWS
                );
            }
        } catch (Exception e) {}

        Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePix.setColor(Color.WHITE);
        whitePix.fill();
        Texture whiteTex = new Texture(whitePix);
        whitePix.dispose();
        generatedTextures.add(whiteTex);
        TextureRegionDrawable whiteTintableBg = new TextureRegionDrawable(whiteTex);

        CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.run(() -> main.switchScreen(new RunMapScreen(main, RunState.createBossDebugRun(), true)))
        ));
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
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
    }
}
