package io.github.some_rougelike.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.model.RankProgress;
import io.github.some_rougelike.save.RunSaveData;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.screens.MainMenuScreen;

public class ProfileUIBuilder {

    private String currentUsernameText;
    private boolean usernameHovered = false;

    public void build(Stage stage, Skin skin, Main main, float scale) {
        SaveManager saveManager = SaveManager.getInstance();
        currentUsernameText = saveManager.getUsername();

        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.center();

        Table rootTable = new Table();

        float rowPadding = GameConfig.PROFILE_ROW_PADDING * scale;
        float titleBottomPadding = GameConfig.PROFILE_TITLE_BOTTOM_PADDING * scale;

        Label titleLabel = new Label("PROFILE", skin);
        titleLabel.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * scale);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).center().padBottom(titleBottomPadding).row();

        Table usernameRow = createEditableUsernameRow(stage, skin, scale);
        rootTable.add(usernameRow).left().padBottom(rowPadding).row();

        String highScoreText = String.valueOf(saveManager.getHighScore());
        Table highScoreRow = createStatRow("High score:", highScoreText, skin, scale, GameConfig.RUN_MAP_HIGH_SCORE_COLOR);
        rootTable.add(highScoreRow).left().padBottom(rowPadding).row();

        String combosText = saveManager.getDiscoveredCombosCount() + " / " + saveManager.getTotalCombosCount();
        Table combosRow = createStatRow("Discovered combos:", combosText, skin, scale, GameConfig.PROFILE_VALUE_COLOR);
        rootTable.add(combosRow).left().padBottom(rowPadding).row();

        String upgradesText = saveManager.getAverageRankProgressPercent() + "% ("
            + saveManager.getMaxedRanksCount() + "/" + RankProgress.RANK_COUNT + " MAX)";
        Table upgradesRow = createStatRow("Card upgrades:", upgradesText, skin, scale, GameConfig.PROFILE_VALUE_COLOR);
        rootTable.add(upgradesRow).left().padBottom(rowPadding).row();

        String deckSkinName = GameConfig.getDeckSkinName(saveManager.getSettings().getSelectedDeckSkin());
        Table deckRow = createStatRow("Selected deck:", deckSkinName, skin, scale, GameConfig.PROFILE_VALUE_COLOR);
        rootTable.add(deckRow).left().padBottom(rowPadding).row();

        String activeRunText = formatActiveRunText(saveManager);
        Color activeRunColor = saveManager.hasActiveRun() ? GameConfig.RUN_MAP_SUBHEADER_COLOR : GameConfig.PROFILE_HINT_COLOR;
        Table activeRunRow = createStatRow("Active run:", activeRunText, skin, scale, activeRunColor);
        rootTable.add(activeRunRow).left().padBottom(titleBottomPadding).row();

        Label backButton = createHoverAction("BACK", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                commitUsername();
                main.switchScreen(new MainMenuScreen(main));
            }
        });
        rootTable.add(backButton).center();

        mainContainer.add(rootTable).center();
        stage.addActor(mainContainer);
    }

    private Table createEditableUsernameRow(Stage stage, Skin skin, float scale) {
        Table table = new Table();

        Label nameLabel = new Label("Username:", skin);
        nameLabel.setFontScale(scale * GameConfig.PROFILE_ROW_FONT_SCALE);
        nameLabel.setColor(GameConfig.PROFILE_LABEL_COLOR);

        Table valueContainer = new Table();
        valueContainer.setTouchable(Touchable.enabled);

        final Label valueLabel = new Label(currentUsernameText, skin);
        valueLabel.setAlignment(Align.center);
        valueLabel.setOrigin(Align.center);
        valueLabel.setFontScale(scale * GameConfig.PROFILE_ROW_FONT_SCALE);
        valueLabel.setColor(GameConfig.PROFILE_VALUE_COLOR);

        final Label hintLabel = new Label("(Hover to edit)", skin);
        hintLabel.setAlignment(Align.center);
        hintLabel.setFontScale(scale * GameConfig.PROFILE_HINT_FONT_SCALE);
        hintLabel.setColor(GameConfig.PROFILE_HINT_COLOR);

        valueContainer.add(valueLabel).width(GameConfig.PROFILE_VALUE_WIDTH * scale).center().row();
        valueContainer.add(hintLabel).padTop(4 * scale).center();

        valueContainer.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    usernameHovered = true;
                    stage.setKeyboardFocus(valueContainer);
                    updateUsernameDisplay(valueLabel, scale);
                    hintLabel.setText("(Type to edit)");
                    hintLabel.setColor(GameConfig.PROFILE_USERNAME_HOVER_COLOR);
                    valueLabel.clearActions();
                    valueLabel.addAction(Actions.color(GameConfig.PROFILE_USERNAME_HOVER_COLOR, 0.15f, Interpolation.fade));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    usernameHovered = false;
                    if (stage.getKeyboardFocus() == valueContainer) {
                        stage.setKeyboardFocus(null);
                    }
                    commitUsername();
                    updateUsernameDisplay(valueLabel, scale);
                    hintLabel.setText("(Hover to edit)");
                    hintLabel.setColor(GameConfig.PROFILE_HINT_COLOR);
                    valueLabel.clearActions();
                    valueLabel.addAction(Actions.color(GameConfig.PROFILE_VALUE_COLOR, 0.15f, Interpolation.fade));
                }
            }
        });

        valueContainer.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (!usernameHovered) {
                    return false;
                }
                if (keycode == Input.Keys.ENTER) {
                    commitUsername();
                    updateUsernameDisplay(valueLabel, scale);
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (!usernameHovered) {
                    return false;
                }
                if (character == '\b' || character == 127) {
                    if (!currentUsernameText.isEmpty()) {
                        currentUsernameText = currentUsernameText.substring(0, currentUsernameText.length() - 1);
                        if (!currentUsernameText.trim().isEmpty()) {
                            SaveManager.getInstance().setUsername(currentUsernameText);
                        }
                        updateUsernameDisplay(valueLabel, scale);
                    }
                    return true;
                }
                if (character >= 32 && currentUsernameText.length() < GameConfig.PROFILE_MAX_USERNAME_LENGTH) {
                    currentUsernameText = currentUsernameText + character;
                    if (!currentUsernameText.trim().isEmpty()) {
                        SaveManager.getInstance().setUsername(currentUsernameText);
                    }
                    updateUsernameDisplay(valueLabel, scale);
                    return true;
                }
                return false;
            }
        });

        table.add(nameLabel).width(GameConfig.PROFILE_LABEL_WIDTH * scale).left();
        table.add(valueContainer).width(GameConfig.PROFILE_VALUE_WIDTH * scale).center();

        return table;
    }

    private void updateUsernameDisplay(Label valueLabel, float scale) {
        String displayed = usernameHovered ? currentUsernameText + "|" : currentUsernameText;
        valueLabel.setText(displayed);
        float fontScale = displayed.length() > 18
            ? scale * (GameConfig.PROFILE_ROW_FONT_SCALE * 0.85f)
            : scale * GameConfig.PROFILE_ROW_FONT_SCALE;
        valueLabel.setFontScale(fontScale);
    }

    private void commitUsername() {
        if (currentUsernameText == null || currentUsernameText.trim().isEmpty()) {
            currentUsernameText = GameConfig.getDefaultUsername();
        } else {
            currentUsernameText = currentUsernameText.trim();
        }
        SaveManager.getInstance().setUsername(currentUsernameText);
    }

    private Table createStatRow(String labelText, String valueText, Skin skin, float scale, Color valueColor) {
        Table table = new Table();

        Label nameLabel = new Label(labelText, skin);
        nameLabel.setFontScale(scale * GameConfig.PROFILE_ROW_FONT_SCALE);
        nameLabel.setColor(GameConfig.PROFILE_LABEL_COLOR);

        Label valueLabel = new Label(valueText, skin);
        valueLabel.setAlignment(Align.center);
        valueLabel.setFontScale(valueText.length() > 22
            ? scale * (GameConfig.PROFILE_ROW_FONT_SCALE * 0.85f)
            : scale * GameConfig.PROFILE_ROW_FONT_SCALE);
        valueLabel.setColor(valueColor);

        table.add(nameLabel).width(GameConfig.PROFILE_LABEL_WIDTH * scale).left();
        table.add(valueLabel).width(GameConfig.PROFILE_VALUE_WIDTH * scale).center();

        return table;
    }

    private String formatActiveRunText(SaveManager saveManager) {
        if (!saveManager.hasActiveRun()) {
            return "No active run";
        }
        RunSaveData activeRun = saveManager.getActiveRunData();
        if (activeRun == null) {
            return "No active run";
        }
        return "Ch. " + activeRun.getChapterIndex() + "/" + GameConfig.MAX_CHAPTERS
            + " | Score: " + activeRun.getTotalRunScore()
            + " | $" + activeRun.getMoney();
    }

    private Label createHoverAction(String text, Skin skin, float scale) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
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
                        Actions.scaleTo(scale * 1.1f, scale * 1.1f, 0.15f, Interpolation.fade)
                    ));
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(new Color(0.65f, 0.65f, 0.65f, 1f), 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.15f, Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                label.clearActions();
                label.addAction(Actions.parallel(
                    Actions.color(new Color(0.65f, 0.65f, 0.65f, 1f), 0.1f, Interpolation.fade),
                    Actions.scaleTo(scale, scale, 0.1f, Interpolation.fade)
                ));
            }
        });

        return label;
    }
}
