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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
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
    private Array<Texture> generatedTextures = new Array<>();

    public MainMenuScreen(Main main) {
        this(main, false, false);
    }

    public MainMenuScreen(Main main, boolean withCardRain, boolean animateUI) {
        stage = new Stage(new ScreenViewport());
        Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        
        trippyBackground = new TrippyBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2);
        trippyBackground.setTouchable(Touchable.disabled);
        stage.addActor(trippyBackground);
        
        new MainMenuUIBuilder().build(stage, skin, main, animateUI);

        if (withCardRain) {
            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

            Texture deckTexture = null;
            TextureRegion[][] cardRegions = null;
            try {
                FileHandle file = Gdx.files.internal("deck_skins/white_black.png");
                if (file.exists()) {
                    deckTexture = new Texture(file);
                    generatedTextures.add(deckTexture);
                    int cols = 13;
                    int rows = 5;
                    cardRegions = TextureRegion.split(deckTexture, deckTexture.getWidth() / cols, deckTexture.getHeight() / rows);
                }
            } catch (Exception e) {}

            Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            whitePix.setColor(Color.WHITE);
            whitePix.fill();
            Texture whiteTex = new Texture(whitePix);
            generatedTextures.add(whiteTex);
            TextureRegionDrawable whiteTintableBg = new TextureRegionDrawable(whiteTex);

            java.util.Random rng = new java.util.Random(888);
            int cardCount = GameConfig.TRANSITION_CARD_COUNT;
            
            for (int i = 0; i < cardCount; i++) {
                int r = rng.nextInt(4);
                int c = rng.nextInt(13);
                float finalScale = GameConfig.TRANSITION_CARD_MIN_SCALE + rng.nextFloat() * GameConfig.TRANSITION_CARD_SCALE_VAR;
                float startX = -250f + rng.nextFloat() * (width + 500f);
                float startY = -250f + rng.nextFloat() * (height + 500f);
                float startRot = rng.nextFloat() * 360f;
                
                Image cardImg;
                if (cardRegions != null) {
                    cardImg = new Image(cardRegions[r][c]);
                } else {
                    cardImg = new Image(whiteTintableBg);
                    cardImg.setColor(GameConfig.COMBAT_CARD_BG_NORMAL);
                }
                
                cardImg.setSize(GameConfig.COMBAT_CARD_WIDTH * scale * finalScale, GameConfig.COMBAT_CARD_HEIGHT * scale * finalScale);
                cardImg.setOrigin(Align.center);
                cardImg.setPosition(startX, startY);
                cardImg.setRotation(startRot);
                
                float targetX = startX + (rng.nextFloat() * 800f - 400f);
                float targetY = -600f - rng.nextFloat() * 1000f;
                
                float progress = (float) i / cardCount;
                float delay = (1f - progress) * (GameConfig.SCREEN_TRANSITION_DURATION * 0.5f);
                float duration = GameConfig.SCREEN_TRANSITION_DURATION * 0.5f;
                
                cardImg.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        Actions.moveTo(targetX, targetY, duration, Interpolation.pow2In),
                        Actions.rotateBy(rng.nextFloat() * 360f - 180f, duration, Interpolation.pow2In)
                    ),
                    Actions.removeActor()
                ));
                
                stage.addActor(cardImg);
            }
        } else if (animateUI) {
            stage.getRoot().getColor().a = 0f;
            stage.getRoot().addAction(Actions.fadeIn(GameConfig.MAIN_MENU_FADE_DURATION, Interpolation.fade));
        }
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