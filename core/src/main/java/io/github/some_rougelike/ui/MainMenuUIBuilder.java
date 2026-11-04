package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
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

import io.github.some_rougelike.Main;
import io.github.some_rougelike.screens.CollectionScreen;
import io.github.some_rougelike.screens.FriendsScreen;
import io.github.some_rougelike.screens.ProfileScreen;
import io.github.some_rougelike.screens.RunMapScreen;
import io.github.some_rougelike.screens.SettingsScreen;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.GameConfig;

public class MainMenuUIBuilder {

    public void build(Stage stage, Skin skin, Main main) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        Table leftMenu = new Table();
        leftMenu.setFillParent(true);
        leftMenu.left().padLeft(120 * scale);

        Label titleLabel = new Label("ROUGELIKE", skin);
        titleLabel.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * scale);
        titleLabel.setColor(Color.WHITE);
        leftMenu.add(titleLabel).padBottom(60 * scale).left();
        leftMenu.row();

        Label playButton = createMenuItem("PLAY", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        Label collectionButton = createMenuItem("COLLECTION", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        Label profileBtn = createMenuItem("PROFILE", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        Label settingsBtn = createMenuItem("SETTINGS", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        Label friendsBtn = createMenuItem("FRIENDS", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);
        Label exitButton = createMenuItem("EXIT", skin, GameConfig.MAIN_MENU_BUTTON_SIZE * scale);

        leftMenu.add(playButton).padBottom(20 * scale).left();
        leftMenu.row();
        leftMenu.add(collectionButton).padBottom(35 * scale).left();
        leftMenu.row();
        
        leftMenu.add(profileBtn).padBottom(15 * scale).left();
        leftMenu.row();
        leftMenu.add(settingsBtn).padBottom(15 * scale).left();
        leftMenu.row();
        leftMenu.add(friendsBtn).padBottom(35 * scale).left();
        leftMenu.row();
        
        leftMenu.add(exitButton).left();

        addSoundListeners(playButton);
        addSoundListeners(collectionButton);
        addSoundListeners(profileBtn);
        addSoundListeners(settingsBtn);
        addSoundListeners(friendsBtn);
        addSoundListeners(exitButton);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playButton.addAction(Actions.sequence(
                    Actions.color(Color.GRAY, 0.05f),
                    Actions.color(Color.WHITE, 0.05f),
                    Actions.run(() -> {
                        RunState newRun = new RunState();
                        main.switchScreen(new RunMapScreen(main, newRun));
                    })
                ));
            }
        });

        collectionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new CollectionScreen(main));
            }
        });

        profileBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new ProfileScreen(main));
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new SettingsScreen(main));
            }
        });

        friendsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new FriendsScreen(main));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        stage.addActor(leftMenu);
    }

    private Label createMenuItem(String text, Skin skin, float scale) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        label.setTouchable(Touchable.enabled);
        label.setOrigin(Align.left);

        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale * 1.05f, scale * 1.05f, 0.15f, Interpolation.fade)
                    ));
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
        });

        return label;
    }

    private void addSoundListeners(Actor actor) {
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
            }
        });
    }
}