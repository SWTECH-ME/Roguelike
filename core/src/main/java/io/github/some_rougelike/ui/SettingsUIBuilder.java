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
import com.badlogic.gdx.utils.Scaling;

import io.github.some_rougelike.Main;
import io.github.some_rougelike.screens.MainMenuScreen;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;

public class SettingsUIBuilder {

    public void build(Stage stage, Skin skin, Main main, float scale, WindowModeController windowModeController) {
        build(stage, skin, main, scale, windowModeController, true);
    }

    public void build(Stage stage, Skin skin, Main main, float scale, WindowModeController windowModeController,
            boolean isBorderless) {
        // Main container to center everything on the stage
        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.center();

        // Inner table holding the title and the two-column alignment together
        Table rootTable = new Table();

        float labelWidth = 200 * scale;
        float btnSize = 40 * scale;
        float rowHeight = 55 * scale;
        float paddingBetweenRows = 15 * scale;
        float titleBottomPadding = 30 * scale;
        float fontScale = Math.max(0.6f, 1.1f * scale);

        // Title row
        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(Math.max(0.5f, 2f * scale));
        rootTable.add(titleLabel).colspan(2).center().padBottom(titleBottomPadding);
        rootTable.row().height(rowHeight);

        // --- VIDEO SETTINGS ---
        Graphics.DisplayMode nativeMode = Gdx.graphics.getDisplayMode();

        java.util.Set<String> resolutionSet = new java.util.LinkedHashSet<>();
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width <= nativeMode.width && mode.height <= nativeMode.height) {
                resolutionSet.add(mode.width + "x" + mode.height);
            }
        }

        String[] tempResolutions = resolutionSet.toArray(new String[0]);
        final String[] resolutions = tempResolutions.length == 0
                ? new String[] { nativeMode.width + "x" + nativeMode.height }
                : tempResolutions;

        String[] windowModes = { "Borderless Windowed", "Windowed" };
        String defaultWindowMode = isBorderless ? "Borderless Windowed" : "Windowed";

        Label displayLabel = new Label("Display mode:", skin);
        displayLabel.setFontScale(fontScale);

        Label resolutionLabel = new Label("Resolution:", skin);
        resolutionLabel.setFontScale(fontScale);

        final Table[] resolutionTableHolder = {
                createResolutionWidget(resolutions, skin, scale, fontScale, isBorderless, nativeMode, stage, main) };
        final Cell<?>[] resolutionCellHolder = new Cell[1];

        Table displayTable = createArrowSelectorWidget(windowModes, skin, scale, fontScale, defaultWindowMode,
                new SelectorListener() {
                    @Override
                    public void onSelectionChanged(String selection) {
                        boolean borderless = selection.equals("Borderless Windowed");
                        if (windowModeController != null) {
                            windowModeController.setBorderless(borderless);
                        }

                        int width = Gdx.graphics.getWidth();
                        int height = Gdx.graphics.getHeight();
                        stage.getViewport().update(width, height, true);

                        for (int i = stage.getActors().size - 1; i >= 0; i--) {
                            Actor actor = stage.getActors().get(i);
                            if (actor instanceof Table) {
                                actor.remove();
                            }
                        }

                        float newScale = Math.min(width / 1920f, height / 1080f) * 1.5f;
                        build(stage, skin, main, newScale, windowModeController, borderless);
                    }
                });

        // 1. Display mode row
        rootTable.add(displayLabel).width(labelWidth).left();
        rootTable.add(displayTable).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // 2. Resolution row
        rootTable.add(resolutionLabel).width(labelWidth).left();
        resolutionCellHolder[0] = rootTable.add(resolutionTableHolder[0]).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // 3. V-Sync row
        Label vSyncLabel = new Label("V-Sync", skin);
        vSyncLabel.setFontScale(fontScale);

        CheckBox vSyncCheck = new CheckBox("", skin);
        vSyncCheck.setChecked(true);
        vSyncCheck.getImageCell().size(btnSize, btnSize);

        vSyncCheck.getImage().setScaling(Scaling.fit);
        vSyncCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.graphics.setVSync(vSyncCheck.isChecked());
            }
        });

        Table vSyncTable = new Table();
        float valueWidth = 220 * scale;
        float padding = 10 * scale;

        vSyncTable.add().size(btnSize, btnSize).padRight(padding);
        vSyncTable.add().width(valueWidth);
        vSyncTable.add(vSyncCheck).size(btnSize, btnSize).padLeft(padding);

        rootTable.add(vSyncLabel).width(labelWidth).left();
        rootTable.add(vSyncTable).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // --- SOUND SETTINGS ---
        // 4. Master volume
        Label masterLabel = new Label("Master volume", skin);
        masterLabel.setFontScale(fontScale);
        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(SoundManager.getInstance().getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMasterVolume(masterSlider.getValue());
            }
        });
        rootTable.add(masterLabel).width(labelWidth).left();
        rootTable.add(masterSlider).width(280 * scale).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // 5. Music volume
        Label musicLabel = new Label("Music volume", skin);
        musicLabel.setFontScale(fontScale);
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(SoundManager.getInstance().getMenuMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMenuMusicVolume(musicSlider.getValue());
            }
        });
        rootTable.add(musicLabel).width(labelWidth).left();
        rootTable.add(musicSlider).width(280 * scale).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // 6. Effects volume
        Label sfxLabel = new Label("Effects volume", skin);
        sfxLabel.setFontScale(fontScale);
        Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxSlider.setValue(SoundManager.getInstance().getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSfxVolume(sfxSlider.getValue());
            }
        });
        rootTable.add(sfxLabel).width(labelWidth).left();
        rootTable.add(sfxSlider).width(280 * scale).left();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        // --- BACK BUTTON ---
        TextButton backButton = new TextButton("BACK", skin);
        backButton.getLabel().setFontScale(fontScale);
        rootTable.add(backButton).size(200 * scale, 50 * scale).colspan(2).center();
        rootTable.row().height(rowHeight).padBottom(paddingBetweenRows);

        addSoundListeners(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new MainMenuScreen(main));
            }
        });

        // Add the unified rootTable to the centered mainContainer
        mainContainer.add(rootTable).center();

        stage.addActor(mainContainer);
    }

    private Table createArrowSelectorWidget(String[] options, Skin skin, float scale, float fontScale,
            String defaultSelection,
            SelectorListener listener) {
        Table table = new Table();
        TextButton leftBtn = new TextButton("<", skin);
        leftBtn.getLabel().setFontScale(fontScale);

        int defaultIndex = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(defaultSelection)) {
                defaultIndex = i;
                break;
            }
        }

        Label valueLabel = new Label(options[defaultIndex], skin);
        valueLabel.setAlignment(Align.center);
        valueLabel.setFontScale(fontScale);
        TextButton rightBtn = new TextButton(">", skin);
        rightBtn.getLabel().setFontScale(fontScale);

        final int[] currentIndex = { defaultIndex };

        leftBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndex[0]--;
                if (currentIndex[0] < 0)
                    currentIndex[0] = options.length - 1;
                valueLabel.setText(options[currentIndex[0]]);
                listener.onSelectionChanged(options[currentIndex[0]]);
            }
        });

        rightBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndex[0]++;
                if (currentIndex[0] >= options.length)
                    currentIndex[0] = 0;
                valueLabel.setText(options[currentIndex[0]]);
                listener.onSelectionChanged(options[currentIndex[0]]);
            }
        });

        addSoundListeners(leftBtn);
        addSoundListeners(rightBtn);

        float btnSize = 40 * scale;
        float valueWidth = 220 * scale;
        float padding = 10 * scale;

        table.add(leftBtn).size(btnSize, btnSize).padRight(padding);
        table.add(valueLabel).width(valueWidth).center();
        table.add(rightBtn).size(btnSize, btnSize).padLeft(padding);

        return table;
    }

    private Table createResolutionWidget(String[] options, Skin skin, float scale, float fontScale,
            boolean isBorderless,
            Graphics.DisplayMode nativeMode, Stage stage, Main main) {
        Table table = new Table();
        float btnSize = 40 * scale;
        float valueWidth = 220 * scale;
        float padding = 10 * scale;

        if (isBorderless) {
            Label valueLabel = new Label(nativeMode.width + "x" + nativeMode.height, skin);
            valueLabel.setAlignment(Align.center);
            valueLabel.setFontScale(fontScale);

            table.add().size(btnSize, btnSize).padRight(padding);
            table.add(valueLabel).width(valueWidth).center();
            table.add().size(btnSize, btnSize).padLeft(padding);
        } else {
            TextButton leftBtn = new TextButton("<", skin);
            leftBtn.getLabel().setFontScale(fontScale);

            int defaultIndex = 0;
            String currentRes = Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(currentRes)) {
                    defaultIndex = i;
                    break;
                }
            }

            Label valueLabel = new Label(options[defaultIndex], skin);
            valueLabel.setAlignment(Align.center);
            valueLabel.setFontScale(fontScale);
            TextButton rightBtn = new TextButton(">", skin);
            rightBtn.getLabel().setFontScale(fontScale);

            final int[] currentIndex = { defaultIndex };

            leftBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    currentIndex[0]--;
                    if (currentIndex[0] < 0)
                        currentIndex[0] = options.length - 1;
                    valueLabel.setText(options[currentIndex[0]]);
                    applyResolution(options[currentIndex[0]], nativeMode, stage, main);
                }
            });

            rightBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    currentIndex[0]++;
                    if (currentIndex[0] >= options.length)
                        currentIndex[0] = 0;
                    valueLabel.setText(options[currentIndex[0]]);
                    applyResolution(options[currentIndex[0]], nativeMode, stage, main);
                }
            });

            addSoundListeners(leftBtn);
            addSoundListeners(rightBtn);

            table.add(leftBtn).size(btnSize, btnSize).padRight(padding);
            table.add(valueLabel).width(valueWidth).center();
            table.add(rightBtn).size(btnSize, btnSize).padLeft(padding);
        }

        return table;
    }

    private void applyResolution(String selection, Graphics.DisplayMode nativeMode, Stage stage, Main main) {
        String[] dims = selection.split("x");
        int width = Integer.parseInt(dims[0]);
        int height = Integer.parseInt(dims[1]);
        if (width <= nativeMode.width && height <= nativeMode.height) {
            Gdx.graphics.setWindowedMode(width, height);
            stage.getViewport().update(width, height, true);

            for (int i = stage.getActors().size - 1; i >= 0; i--) {
                Actor actor = stage.getActors().get(i);
                if (actor instanceof Table) {
                    actor.remove();
                }
            }

            float newScale = Math.min(width / 1920f, height / 1080f) * 1.5f;
            Skin skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
            build(stage, skin, main, newScale, main.getWindowModeController(), false);
        }
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
}