package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.audio.SoundManager;

public class PauseMenu {
    private static final float FADE = 0.2f;

    private final Stage stage;
    private final Stage pauseStage;
    private final Skin skin;
    private final float scale;
    private final Runnable onResume;
    private final Runnable onMainMenu;
    private final Runnable onSave;
    private final Texture whiteTex;

    private Table overlay;
    private boolean visible = false;
    private boolean closing = false;

    public PauseMenu(Stage stage, Skin skin, float scale, Runnable onResume, Runnable onSave, Runnable onMainMenu) {
        this.stage = stage;
        this.pauseStage = new Stage(new ScreenViewport());
        this.pauseStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.skin = skin;
        this.scale = scale;
        this.onResume = onResume;
        this.onSave = onSave;
        this.onMainMenu = onMainMenu;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        whiteTex = new Texture(pm);
        pm.dispose();
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        if (visible) return;
        visible = true;
        closing = false;
        TrippyBackground.setFrozen(true);

        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(new TextureRegionDrawable(whiteTex).tint(new Color(0f, 0f, 0f, 0.65f)));
        overlay.setTouchable(Touchable.enabled); // elnyeli az alatta lévő kattintásokat

        Label title = new Label("PAUSED", skin);
        title.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * scale);
        title.setColor(Color.WHITE);
        title.setAlignment(Align.center);
        overlay.add(title).padBottom(60 * scale).row();

        Label resumeBtn = createButton("RESUME", GameConfig.MAIN_MENU_BUTTON_SIZE * scale, this::resume);
        final Table currentOverlay = overlay;
        final Label[] saveRef = new Label[1];
        saveRef[0] = createButton("SAVE", GameConfig.MAIN_MENU_BUTTON_SIZE * scale, () -> {
            if (onSave != null) onSave.run();
            saveRef[0].setText("SAVED!");
            currentOverlay.addAction(Actions.sequence(
                Actions.delay(1.2f),
                Actions.run(() -> saveRef[0].setText("SAVE"))));
        });
        Label menuBtn = createButton("MAIN MENU", GameConfig.MAIN_MENU_BUTTON_SIZE * scale, () -> {
            if (onMainMenu != null) onMainMenu.run();
        });

        overlay.add(resumeBtn).padBottom(25 * scale).row();
        overlay.add(saveRef[0]).padBottom(25 * scale).row();
        overlay.add(menuBtn);

        pauseStage.addActor(overlay);
        Gdx.input.setInputProcessor(pauseStage);
        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(FADE, Interpolation.fade));
    }

    public void resume() {
        if (!visible || closing) return;
        hide();
        if (onResume != null) onResume.run();
    }

    public void hide() {
        if (!visible || overlay == null) return;
        closing = true;
        visible = false;
        TrippyBackground.setFrozen(false);
        Gdx.input.setInputProcessor(stage);
        final Table o = overlay;
        overlay = null;
        o.setTouchable(Touchable.disabled);
        o.clearActions();
        o.addAction(Actions.sequence(Actions.fadeOut(FADE, Interpolation.fade), Actions.removeActor()));
    }

    public void render(float delta) {
        if (pauseStage.getActors().size == 0) return;
        pauseStage.act(delta);
        pauseStage.draw();
    }

    public void resize(int width, int height) {
        pauseStage.getViewport().update(width, height, true);
    }

    public void dispose() {
        TrippyBackground.setFrozen(false);
        pauseStage.dispose();
        whiteTex.dispose();
    }

    private Label createButton(String text, float fontScale, Runnable onClick) {
        final Label label = new Label(text, skin);
        label.setFontScale(fontScale);
        label.setColor(GameConfig.BUTTON_TEXT_COLOR);
        label.setTouchable(Touchable.enabled);
        label.setOrigin(Align.center);
        label.setAlignment(Align.center);

        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                        Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.fade)));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade),
                        Actions.scaleTo(1f, 1f, 0.15f, Interpolation.fade)));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                if (onClick != null) onClick.run();
            }
        });
        return label;
    }
}
