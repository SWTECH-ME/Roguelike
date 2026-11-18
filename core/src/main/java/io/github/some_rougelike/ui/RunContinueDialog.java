package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.save.CombatSaveData;
import io.github.some_rougelike.save.EnemySaveData;
import io.github.some_rougelike.save.RunSaveData;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.screens.CombatScreen;
import io.github.some_rougelike.screens.RunMapScreen;

public class RunContinueDialog {
    private final Stage stage;
    private final Skin skin;
    private final Main main;
    private final Table parentMenu;
    private final TrippyBackground trippyBackground;
    private final Array<Texture> generatedTextures;
    private boolean isTransitioning = false;

    public RunContinueDialog(Stage stage, Skin skin, Main main, Table parentMenu, TrippyBackground trippyBackground, Array<Texture> generatedTextures) {
        this.stage = stage;
        this.skin = skin;
        this.main = main;
        this.parentMenu = parentMenu;
        this.trippyBackground = trippyBackground;
        this.generatedTextures = generatedTextures;
    }

    public void show() {
        if (isTransitioning) {
            return;
        }

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        parentMenu.setTouchable(Touchable.disabled);
        parentMenu.clearActions();
        parentMenu.addAction(Actions.parallel(
            Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade),
            Actions.moveBy(-GameConfig.CONTINUE_DIALOG_SLIDE_OFFSET * scale, 0f, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.pow2In)
        ));

        lerpBackground(GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2, GameConfig.CONTINUE_BG_COLOR_1, GameConfig.CONTINUE_BG_COLOR_2, GameConfig.CONTINUE_BG_LERP_DURATION);

        TextureRegionDrawable overlayBg = createSolidDrawable(GameConfig.CONTINUE_OVERLAY_TINT);
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(overlayBg);
        overlay.setTouchable(Touchable.enabled);

        int cardW = (int) (GameConfig.CONTINUE_DIALOG_WIDTH * scale);
        int cardH = (int) (GameConfig.CONTINUE_DIALOG_HEIGHT * scale);
        int radius = (int) (GameConfig.CONTINUE_DIALOG_RADIUS * scale);
        int border = (int) (GameConfig.CONTINUE_DIALOG_BORDER * scale);

        TextureRegionDrawable panelBg = createOutlinedRoundedRect(
            cardW,
            cardH,
            GameConfig.CONTINUE_DIALOG_BG,
            GameConfig.CONTINUE_DIALOG_BORDER_COLOR,
            border,
            radius
        );

        Group panelGroup = new Group();
        panelGroup.setSize(cardW, cardH);
        panelGroup.setOrigin(Align.center);

        Table panelTable = new Table();
        panelTable.setSize(cardW, cardH);
        panelTable.setBackground(panelBg);
        panelTable.center().pad(30f * scale);

        Label titleLbl = new Label("RUN IN PROGRESS", skin);
        titleLbl.setFontScale(scale * 1.45f);
        titleLbl.setColor(Color.WHITE);
        titleLbl.setAlignment(Align.center);
        panelTable.add(titleLbl).padBottom(12f * scale).row();

        RunSaveData runData = SaveManager.getInstance().getActiveRunData();
        String chapterSummary = buildRunSummary(runData);
        String stageSummary = buildStageSummary(runData);

        Label summaryLbl = new Label(chapterSummary, skin);
        summaryLbl.setFontScale(scale * 0.9f);
        summaryLbl.setColor(GameConfig.CONTINUE_SUBTITLE_COLOR);
        summaryLbl.setAlignment(Align.center);
        panelTable.add(summaryLbl).padBottom(6f * scale).row();

        Label stageLbl = new Label(stageSummary, skin);
        stageLbl.setFontScale(scale * 0.85f);
        stageLbl.setColor(new Color(0.8f, 0.8f, 0.85f, 1f));
        stageLbl.setAlignment(Align.center);
        panelTable.add(stageLbl).padBottom(32f * scale).row();

        Label continueBtn = createDialogButton("CONTINUE", scale * 1.3f, () -> startContinueRun(overlay, panelGroup, scale));
        Label newRunBtn = createDialogButton("NEW RUN", scale * 1.3f, () -> startNewRun(overlay, panelGroup, scale));
        Label backBtn = createDialogButton("BACK", scale * 1.05f, () -> closeDialog(overlay, panelGroup, scale));

        panelTable.add(continueBtn).padBottom(18f * scale).row();
        panelTable.add(newRunBtn).padBottom(24f * scale).row();
        panelTable.add(backBtn).row();

        panelGroup.addActor(panelTable);
        overlay.add(panelGroup).size(cardW, cardH).center();
        stage.addActor(overlay);

        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade));

        panelGroup.setScale(0.85f);
        panelGroup.getColor().a = 0f;
        panelGroup.addAction(Actions.parallel(
            Actions.fadeIn(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade),
            Actions.scaleTo(1.0f, 1.0f, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.swingOut)
        ));
    }

    public void startDirectNewRun() {
        if (isTransitioning) {
            return;
        }
        isTransitioning = true;
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        parentMenu.setTouchable(Touchable.disabled);
        parentMenu.clearActions();
        parentMenu.addAction(Actions.parallel(
            Actions.moveBy(-GameConfig.CONTINUE_DIALOG_SLIDE_OFFSET * scale, 0f, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.pow2In),
            Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade)
        ));

        triggerCardRainAndSwitch(() -> {
            SaveManager.getInstance().clearActiveRun();
            RunState newRun = new RunState();
            SaveManager.getInstance().saveRunMapState(newRun);
            main.switchScreen(new RunMapScreen(main, newRun, true));
        });
    }

    private void startContinueRun(Table overlay, Group panelGroup, float scale) {
        if (isTransitioning) {
            return;
        }
        isTransitioning = true;
        overlay.setTouchable(Touchable.disabled);

        animateDialogOut(overlay, panelGroup, scale);

        triggerCardRainAndSwitch(() -> {
            RunSaveData runData = SaveManager.getInstance().getActiveRunData();
            if (runData == null) {
                RunState fallbackRun = new RunState();
                SaveManager.getInstance().saveRunMapState(fallbackRun);
                main.switchScreen(new RunMapScreen(main, fallbackRun, true));
                return;
            }

            RunState restoredRun = runData.toRunState();
            CombatSaveData combatData = runData.getCombatState();
            if (runData.isInCombat() && restoredRun.getCurrentEnemy() != null && combatData != null) {
                main.switchScreen(new CombatScreen(main, restoredRun, combatData));
            } else {
                main.switchScreen(new RunMapScreen(main, restoredRun, true));
            }
        });
    }

    private void startNewRun(Table overlay, Group panelGroup, float scale) {
        if (isTransitioning) {
            return;
        }
        isTransitioning = true;
        overlay.setTouchable(Touchable.disabled);

        animateDialogOut(overlay, panelGroup, scale);

        triggerCardRainAndSwitch(() -> {
            SaveManager.getInstance().clearActiveRun();
            RunState newRun = new RunState();
            SaveManager.getInstance().saveRunMapState(newRun);
            main.switchScreen(new RunMapScreen(main, newRun, true));
        });
    }

    private void closeDialog(Table overlay, Group panelGroup, float scale) {
        if (isTransitioning) {
            return;
        }
        overlay.setTouchable(Touchable.disabled);

        lerpBackground(GameConfig.CONTINUE_BG_COLOR_1, GameConfig.CONTINUE_BG_COLOR_2, GameConfig.BG_COLOR_1, GameConfig.BG_COLOR_2, GameConfig.CONTINUE_BG_LERP_DURATION);

        panelGroup.clearActions();
        panelGroup.addAction(Actions.parallel(
            Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade),
            Actions.scaleTo(0.85f, 0.85f, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.pow2In)
        ));

        overlay.clearActions();
        overlay.addAction(Actions.sequence(
            Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade),
            Actions.removeActor()
        ));

        parentMenu.clearActions();
        parentMenu.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade),
                Actions.moveBy(GameConfig.CONTINUE_DIALOG_SLIDE_OFFSET * scale, 0f, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.exp10Out)
            ),
            Actions.run(() -> parentMenu.setTouchable(Touchable.enabled))
        ));
    }

    private void animateDialogOut(Table overlay, Group panelGroup, float scale) {
        panelGroup.clearActions();
        panelGroup.addAction(Actions.parallel(
            Actions.moveBy(0f, -GameConfig.CONTINUE_DIALOG_SLIDE_OFFSET * scale, GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.pow2In),
            Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade)
        ));
        overlay.clearActions();
        overlay.addAction(Actions.fadeOut(GameConfig.CONTINUE_DIALOG_FADE_DURATION, Interpolation.fade));
    }

    private void triggerCardRainAndSwitch(Runnable onComplete) {
        TextureRegion[][] cardRegions = loadSelectedSkinRegions();
        TextureRegionDrawable whiteBg = createSolidDrawable(Color.WHITE);

        CardTransition.applyTransition(stage, cardRegions, whiteBg, false);

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.run(onComplete)
        ));
    }

    private TextureRegion[][] loadSelectedSkinRegions() {
        try {
            FileHandle file = Gdx.files.internal(SaveManager.getInstance().getSelectedDeckSkinPath());
            if (!file.exists()) {
                file = Gdx.files.internal(GameConfig.DECK_SKINS_DIR + "/" + GameConfig.DEFAULT_DECK_SKIN);
            }
            if (file.exists()) {
                Texture deckTexture = new Texture(file);
                generatedTextures.add(deckTexture);
                return TextureRegion.split(
                    deckTexture,
                    deckTexture.getWidth() / GameConfig.DECK_SHEET_COLS,
                    deckTexture.getHeight() / GameConfig.DECK_SHEET_ROWS
                );
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void lerpBackground(Color from1, Color from2, Color to1, Color to2, float duration) {
        if (trippyBackground == null) {
            return;
        }
        final Color start1 = new Color(from1);
        final Color start2 = new Color(from2);
        final Color target1 = new Color(to1);
        final Color target2 = new Color(to2);
        final Color temp1 = new Color(from1);
        final Color temp2 = new Color(from2);

        FloatAction lerpAction = new FloatAction(0f, 1f, duration, Interpolation.smooth) {
            @Override
            protected void update(float percent) {
                super.update(percent);
                temp1.set(start1).lerp(target1, percent);
                temp2.set(start2).lerp(target2, percent);
                trippyBackground.setColors(temp1, temp2);
            }
        };
        stage.addAction(lerpAction);
    }

    private String buildRunSummary(RunSaveData runData) {
        if (runData == null) {
            return "CHAPTER 1 / " + GameConfig.MAX_CHAPTERS + "   |   $" + GameConfig.STARTING_MONEY + "   |   SCORE: 0";
        }
        return "CHAPTER " + runData.getChapterIndex() + " / " + runData.getMaxChapters()
            + "   |   $" + runData.getMoney()
            + "   |   SCORE: " + runData.getTotalRunScore();
    }

    private String buildStageSummary(RunSaveData runData) {
        if (runData == null) {
            return "STAGE: RUN MAP";
        }
        if (runData.isInCombat() && runData.getCombatState() != null) {
            String enemyName = "ENEMY";
            int targetScore = 0;
            if (runData.isActiveIsBoss() && runData.getBossEnemy() != null) {
                enemyName = runData.getBossEnemy().getName();
                targetScore = runData.getBossEnemy().getTargetScore();
            } else if (runData.getActiveNormalIndex() >= 0 && runData.getActiveNormalIndex() < runData.getAvailableNormals().size()) {
                EnemySaveData normal = runData.getAvailableNormals().get(runData.getActiveNormalIndex());
                enemyName = normal.getName();
                targetScore = normal.getTargetScore();
            }
            int currentScore = runData.getCombatState().getCurrentScore();
            return "COMBAT: " + enemyName.toUpperCase() + " (" + currentScore + "/" + targetScore + ")";
        }
        return "STAGE: RUN MAP";
    }

    private Label createDialogButton(String text, float scale, Runnable onClick) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(GameConfig.BUTTON_TEXT_COLOR);
        label.setTouchable(Touchable.enabled);
        label.setOrigin(Align.center);
        label.setAlignment(Align.center);

        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale * 1.1f, scale * 1.1f, 0.15f, Interpolation.fade)
                    ));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.15f, Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isTransitioning) {
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            }
        });

        return label;
    }

    private TextureRegionDrawable createSolidDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        generatedTextures.add(tex);
        return new TextureRegionDrawable(tex);
    }

    private TextureRegionDrawable createOutlinedRoundedRect(int width, int height, Color bgColor, Color borderColor, int borderThickness, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        pixmap.setColor(borderColor);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        pixmap.setColor(bgColor);
        int innerWidth = width - 2 * borderThickness;
        int innerHeight = height - 2 * borderThickness;
        int innerRadius = Math.max(0, radius - borderThickness);
        int offset = borderThickness;

        pixmap.fillRectangle(offset + innerRadius, offset, innerWidth - 2 * innerRadius, innerHeight);
        pixmap.fillRectangle(offset, offset + innerRadius, innerWidth, innerHeight - 2 * innerRadius);
        pixmap.fillCircle(offset + innerRadius, offset + innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerWidth - innerRadius, offset + innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerRadius, offset + innerHeight - innerRadius, innerRadius);
        pixmap.fillCircle(offset + innerWidth - innerRadius, offset + innerHeight - innerRadius, innerRadius);

        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        generatedTextures.add(tex);

        return new TextureRegionDrawable(tex);
    }
}
