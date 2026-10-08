package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.some_rougelike.Main;
import io.github.some_rougelike.screens.MainMenuScreen;
import io.github.some_rougelike.audio.SoundManager;

public class SettingsUIBuilder {

    public void build(Stage stage, Skin skin, Main main) {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(2f);
        rootTable.add(titleLabel).padBottom(40).colspan(2).center();
        rootTable.row();

        // --- VIDEO SETTINGS ---
        String[] resolutions = {"1280x720", "1600x900", "1920x1080", "2560x1440"};
        String[] windowModes = {"Windowed", "Full Screen"};

        rootTable.add(createArrowSelector("Resolution:", resolutions, skin, new SelectorListener() {
            @Override
            public void onSelectionChanged(String selection) {
                String[] dims = selection.split("x");
                int width = Integer.parseInt(dims[0]);
                int height = Integer.parseInt(dims[1]);
                Gdx.graphics.setWindowedMode(width, height);
            }
        })).padBottom(15).fillX();
        rootTable.row();

        rootTable.add(createArrowSelector("Display:", windowModes, skin, new SelectorListener() {
            @Override
            public void onSelectionChanged(String selection) {
                if (selection.equals("Full Screen")) {
                    Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                    Gdx.graphics.setFullscreenMode(mode);
                } else {
                    Gdx.graphics.setWindowedMode(1920, 1080);
                }
            }
        })).padBottom(30).fillX();
        rootTable.row();


        // --- SOUND SETTINGS ---
        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(SoundManager.getInstance().getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMasterVolume(masterSlider.getValue());
            }
        });
        rootTable.add(createSliderRow("Master:", masterSlider, skin)).padBottom(15).fillX();
        rootTable.row();

        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(SoundManager.getInstance().getMenuMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMenuMusicVolume(musicSlider.getValue());
            }
        });
        rootTable.add(createSliderRow("Music:", musicSlider, skin)).padBottom(15).fillX();
        rootTable.row();

        Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxSlider.setValue(SoundManager.getInstance().getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSfxVolume(sfxSlider.getValue());
            }
        });
        rootTable.add(createSliderRow("Effects:", sfxSlider, skin)).padBottom(30).fillX();
        rootTable.row();


        // --- EXTRAS ---
        CheckBox vSyncCheck = new CheckBox(" V-Sync", skin);
        vSyncCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.graphics.setVSync(vSyncCheck.isChecked());
            }
        });
        rootTable.add(vSyncCheck).padBottom(10).left();
        rootTable.row();


        // --- BACK BUTTON ---
        TextButton backButton = new TextButton("BACK", skin);
        rootTable.add(backButton).size(200, 50).center();


        addSoundListeners(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new MainMenuScreen(main));
            }
        });
        
        stage.addActor(rootTable);
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

    private interface SelectorListener {
        void onSelectionChanged(String selection);
    }

    private Table createArrowSelector(String labelText, String[] options, Skin skin, SelectorListener listener) {
        Table table = new Table();
        Label nameLabel = new Label(labelText, skin);
        
        TextButton leftBtn = new TextButton("<", skin);
        Label valueLabel = new Label(options[0], skin);
        valueLabel.setAlignment(Align.center);
        TextButton rightBtn = new TextButton(">", skin);

        final int[] currentIndex = {0}; 

        leftBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndex[0]--;
                if (currentIndex[0] < 0) currentIndex[0] = options.length - 1; 
                valueLabel.setText(options[currentIndex[0]]);
                listener.onSelectionChanged(options[currentIndex[0]]);
            }
        });

        rightBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndex[0]++;
                if (currentIndex[0] >= options.length) currentIndex[0] = 0; 
                valueLabel.setText(options[currentIndex[0]]);
                listener.onSelectionChanged(options[currentIndex[0]]);
            }
        });

        addSoundListeners(leftBtn);
        addSoundListeners(rightBtn);

        table.add(nameLabel).width(150).left();
        table.add(leftBtn).size(40, 40).padRight(10);
        table.add(valueLabel).width(150).center();
        table.add(rightBtn).size(40, 40).padLeft(10);

        return table;
    }

    private Table createSliderRow(String labelText, Slider slider, Skin skin) {
        Table table = new Table();
        Label nameLabel = new Label(labelText, skin);
        table.add(nameLabel).width(150).left();
        table.add(slider).width(240).center();
        return table;
    }
}