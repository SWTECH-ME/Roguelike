package io.github.some_rougelike.ui;

import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.Align;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.screens.MainMenuScreen;

public class FriendsUIBuilder {

    /** 
     * @param stage
     * @param skin
     * @param main
     * @param scale
     */
    public void build(Stage stage, Skin skin, Main main, float scale) {
        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.center();

        Table rootTable = new Table();

        float titleBottomPadding = GameConfig.PROFILE_TITLE_BOTTOM_PADDING * scale;

        Label titleLabel = new Label("FRIENDS", skin);
        titleLabel.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * scale);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).center().padBottom(titleBottomPadding).row();

        Label comingSoonLabel = new Label("COMING SOON", skin);
        comingSoonLabel.setFontScale(scale * 1.4f);
        comingSoonLabel.setColor(GameConfig.PROFILE_LABEL_COLOR);
        comingSoonLabel.setAlignment(Align.center);
        rootTable.add(comingSoonLabel).center().padBottom(titleBottomPadding).row();

        Label backButton = createHoverAction("BACK", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new MainMenuScreen(main));
            }
        });
        rootTable.add(backButton).center();

        mainContainer.add(rootTable).center();
        stage.addActor(mainContainer);
    }

    /** 
     * @param text
     * @param skin
     * @param scale
     * @return Label
     */
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
