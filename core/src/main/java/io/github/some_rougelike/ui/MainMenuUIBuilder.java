package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class MainMenuUIBuilder {

    public void build(Stage stage, Skin skin) {
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

        topRightMenu.add(profileBtn).size(100, 40).padRight(15);
        topRightMenu.add(settingsBtn).size(100, 40).padRight(15);
        topRightMenu.add(friendsBtn).size(100, 40);


        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Clicked: PLAY");
            }
        });

        collectionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Clicked: COLLECTION");
            }
        });

        profileBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Clicked: PROFILE");
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Clicked: SETTINGS");
            }
        });

        friendsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Clicked: FRIENDS");
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
}