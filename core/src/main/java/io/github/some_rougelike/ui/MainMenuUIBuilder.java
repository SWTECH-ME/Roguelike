package io.github.some_rougelike.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

   import io.github.some_rougelike.Main;
   import io.github.some_rougelike.screens.CollectionScreen;
   import io.github.some_rougelike.screens.FriendsScreen;
   import io.github.some_rougelike.screens.GameScreen;
   import io.github.some_rougelike.screens.ProfileScreen;
   import io.github.some_rougelike.screens.SettingsScreen;
import io.github.some_rougelike.audio.SoundManager;

public class MainMenuUIBuilder {

    public void build(Stage stage, Skin skin, Main main) {
        Label messageLabel = new Label("", skin);
        messageLabel.setFontScale(1.5f);
        messageLabel.setAlignment(Align.center);

        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.center();
        centerTable.add(messageLabel);

        Table leftMenu = new Table();
        leftMenu.setFillParent(true);
        leftMenu.left().padLeft(150);

        Label titleLabel = new Label("ROUGELIKE", skin);
        titleLabel.setFontScale(2.5f);
        leftMenu.add(titleLabel).padBottom(50).left();
        leftMenu.row();

        TextButton playButton = new TextButton("PLAY", skin);
        TextButton collectionButton = new TextButton("COLLECTION", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        leftMenu.add(playButton).size(250, 50).padBottom(15).left();
        leftMenu.row();
        leftMenu.add(collectionButton).size(250, 50).padBottom(25).left();
        leftMenu.row();
        leftMenu.add(exitButton).size(150, 40).left();

        Table topRightMenu = new Table();
        topRightMenu.setFillParent(true);
        topRightMenu.top().right().padTop(30).padRight(50);

        TextButton profileBtn = new TextButton("PROFILE", skin);
        TextButton settingsBtn = new TextButton("SETTINGS", skin);
        TextButton friendsBtn = new TextButton("FRIENDS", skin);

        addSoundListeners(playButton);
        addSoundListeners(collectionButton);
        addSoundListeners(exitButton);
        addSoundListeners(profileBtn);
        addSoundListeners(settingsBtn);
        addSoundListeners(friendsBtn);

        topRightMenu.add(profileBtn).size(100, 40).padRight(15);
        topRightMenu.add(settingsBtn).size(100, 40).padRight(15);
        topRightMenu.add(friendsBtn).size(100, 40);
        topRightMenu.row().padTop(20);
        topRightMenu.add(new Label("MUSIC", skin)).left().padRight(10);
        Slider musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(SoundManager.getInstance().getMenuMusicVolume());
        musicVolumeSlider.addListener(event -> {
            SoundManager.getInstance().setMenuMusicVolume(musicVolumeSlider.getValue());
            return false;
        });
        topRightMenu.add(musicVolumeSlider).width(180).colspan(2).right();


        playButton.addListener(new ClickListener() {
    @Override
    public void clicked(InputEvent event, float x, float y) {
        main.switchScreen(new GameScreen(main));
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

        stage.addActor(centerTable);
        stage.addActor(leftMenu);
        stage.addActor(topRightMenu);
    }

    private void addSoundListeners(TextButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
            }
        });
    }

    private void showPopup(Label label, String message) {
        label.setText(message);
        SoundManager.getInstance().play(SoundManager.SoundEvent.TEXT_POPUP);
    }
}