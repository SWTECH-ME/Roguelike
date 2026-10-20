package io.github.some_rougelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.some_rougelike.Main;
import io.github.some_rougelike.screens.MainMenuScreen;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.GameConfig;

public class SettingsUIBuilder {

    private static com.badlogic.gdx.graphics.Texture whiteTex;
    private static com.badlogic.gdx.graphics.Texture grayTex;

    private void initTextures() {
        if (whiteTex == null) {
            com.badlogic.gdx.graphics.Pixmap wp = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            wp.setColor(Color.WHITE);
            wp.fill();
            whiteTex = new com.badlogic.gdx.graphics.Texture(wp);
            wp.dispose();

            com.badlogic.gdx.graphics.Pixmap gp = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            gp.setColor(0.3f, 0.3f, 0.3f, 1f);
            gp.fill();
            grayTex = new com.badlogic.gdx.graphics.Texture(gp);
            gp.dispose();
        }
    }

    public void build(Stage stage, Skin skin, Main main, float scale, WindowModeController windowModeController) {
        build(stage, skin, main, scale, windowModeController, false);
    }

    public void build(Stage stage, Skin skin, Main main, float scale, WindowModeController windowModeController, boolean isBorderless) {
        initTextures();

        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.center();

        Table rootTable = new Table();

        float paddingBetweenRows = 15 * scale;
        float titleBottomPadding = 50 * scale;

        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * scale);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).center().padBottom(titleBottomPadding).row();

        Graphics.DisplayMode nativeMode = Gdx.graphics.getDisplayMode();

        java.util.Set<String> resolutionSet = new java.util.LinkedHashSet<>();
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width <= nativeMode.width && mode.height <= nativeMode.height) {
                resolutionSet.add(mode.width + "x" + mode.height);
            }
        }

        String[] tempResolutions = resolutionSet.toArray(new String[0]);
        final String[] resolutions = tempResolutions.length == 0 ? new String[]{nativeMode.width + "x" + nativeMode.height} : tempResolutions;

        String currentRes = Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
        String defaultRes = resolutions[0];
        for (String r : resolutions) {
            if (r.equals(currentRes)) {
                defaultRes = r;
                break;
            }
        }

        String[] windowModes = {"Fullscreen", "Borderless Windowed", "Windowed"};
        String defaultWindowMode = "Windowed";
        
        if (Gdx.graphics.isFullscreen()) {
            defaultWindowMode = "Fullscreen";
        } else if (isBorderless) {
            defaultWindowMode = "Borderless Windowed";
        }

        Table displayTable = createArrowSelector("Display mode:", windowModes, defaultWindowMode, skin, scale, new SelectorListener() {
            @Override
            public void onSelectionChanged(String selection) {
                boolean fullscreen = selection.equals("Fullscreen");
                boolean borderless = selection.equals("Borderless Windowed");

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (fullscreen) {
                            Gdx.graphics.setUndecorated(false);
                            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                            Gdx.graphics.setFullscreenMode(mode);
                        } else if (borderless) {
                            Gdx.graphics.setUndecorated(true);
                            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                            Gdx.graphics.setWindowedMode(mode.width, mode.height);
                        } else {
                            Gdx.graphics.setUndecorated(false);
                            Gdx.graphics.setWindowedMode(1280, 720);
                        }

                        if (windowModeController != null) {
                            windowModeController.setBorderless(borderless || fullscreen);
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
            }
        });

        Table resolutionTable = createArrowSelector("Resolution:", resolutions, defaultRes, skin, scale, new SelectorListener() {
            @Override
            public void onSelectionChanged(String selection) {
                String[] dims = selection.split("x");
                int width = Integer.parseInt(dims[0]);
                int height = Integer.parseInt(dims[1]);
                if (!Gdx.graphics.isFullscreen()) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.graphics.setWindowedMode(width, height);
                            stage.getViewport().update(width, height, true);

                            for (int i = stage.getActors().size - 1; i >= 0; i--) {
                                Actor actor = stage.getActors().get(i);
                                if (actor instanceof Table) {
                                    actor.remove();
                                }
                            }

                            float newScale = Math.min(width / 1920f, height / 1080f) * 1.5f;
                            build(stage, skin, main, newScale, windowModeController, isBorderless);
                        }
                    });
                }
            }
        });

        rootTable.add(displayTable).left().padBottom(paddingBetweenRows).row();
        
        if (isBorderless || Gdx.graphics.isFullscreen()) {
            Table disabledResTable = new Table();
            Label nameLabel = new Label("Resolution:", skin);
            nameLabel.setFontScale(scale * 1.1f);
            nameLabel.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
            
            Label valueLabel = new Label(nativeMode.width + "x" + nativeMode.height, skin);
            valueLabel.setAlignment(Align.center);
            valueLabel.setFontScale(scale * 1.1f);
            valueLabel.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
            
            disabledResTable.add(nameLabel).width(250 * scale).left();
            disabledResTable.add().size(40 * scale, 40 * scale).padRight(10 * scale);
            disabledResTable.add(valueLabel).width(220 * scale).center();
            disabledResTable.add().size(40 * scale, 40 * scale).padLeft(10 * scale);
            
            rootTable.add(disabledResTable).left().padBottom(paddingBetweenRows).row();
        } else {
            rootTable.add(resolutionTable).left().padBottom(paddingBetweenRows).row();
        }

        Table vSyncTable = createArrowSelector("V-Sync:", new String[]{"OFF", "ON"}, Gdx.graphics.getBufferFormat().coverageSampling ? "ON" : "ON", skin, scale, new SelectorListener() {
            @Override
            public void onSelectionChanged(String selection) {
                Gdx.graphics.setVSync(selection.equals("ON"));
            }
        });
        rootTable.add(vSyncTable).left().padBottom(paddingBetweenRows).row();

        Table masterSliderTable = createSliderRow("Master volume:", SoundManager.getInstance().getMasterVolume(), scale, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMasterVolume(((Slider)actor).getValue());
            }
        });
        rootTable.add(masterSliderTable).left().padBottom(paddingBetweenRows).row();

        Table musicSliderTable = createSliderRow("Music volume:", SoundManager.getInstance().getMenuMusicVolume(), scale, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMenuMusicVolume(((Slider)actor).getValue());
            }
        });
        rootTable.add(musicSliderTable).left().padBottom(paddingBetweenRows).row();

        Table sfxSliderTable = createSliderRow("Effects volume:", SoundManager.getInstance().getSfxVolume(), scale, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSfxVolume(((Slider)actor).getValue());
            }
        });
        rootTable.add(sfxSliderTable).left().padBottom(titleBottomPadding).row();

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

    public interface SelectorListener {
        void onSelectionChanged(String selection);
    }

    private Table createArrowSelector(String labelText, String[] options, String defaultSelection, Skin skin, float scale, SelectorListener listener) {
        Table table = new Table();
        Label nameLabel = new Label(labelText, skin);
        nameLabel.setFontScale(scale * 1.1f);
        nameLabel.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        Label leftBtn = createHoverAction("<", skin, scale * 1.3f);
        Label rightBtn = createHoverAction(">", skin, scale * 1.3f);

        int defaultIndex = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(defaultSelection)) {
                defaultIndex = i;
                break;
            }
        }

        Label valueLabel = new Label(options[defaultIndex], skin);
        valueLabel.setAlignment(Align.center);
        valueLabel.setFontScale(scale * 1.1f);
        valueLabel.setColor(Color.WHITE);

        final int[] currentIndex = {defaultIndex};

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

        table.add(nameLabel).width(250 * scale).left();
        table.add(leftBtn).size(40 * scale, 40 * scale).padRight(10 * scale);
        table.add(valueLabel).width(220 * scale).center();
        table.add(rightBtn).size(40 * scale, 40 * scale).padLeft(10 * scale);

        return table;
    }

    private Table createSliderRow(String labelText, float initialValue, float scale, ChangeListener listener) {
        Table table = new Table();
        
        Label nameLabel = new Label(labelText, new Label.LabelStyle(new com.badlogic.gdx.graphics.g2d.BitmapFont(), Color.WHITE));
        nameLabel.setFontScale(scale * 1.1f);
        nameLabel.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable bg = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(grayTex));
        bg.setMinHeight(4 * scale);

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable kb = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(whiteTex));
        kb.setMinHeight(4 * scale);

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable knob = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(whiteTex));
        knob.setMinWidth(12 * scale);
        knob.setMinHeight(24 * scale);

        Slider.SliderStyle modernStyle = new Slider.SliderStyle();
        modernStyle.background = bg;
        modernStyle.knobBefore = kb;
        modernStyle.knob = knob;

        Slider slider = new Slider(0f, 1f, 0.01f, false, modernStyle);
        slider.setValue(initialValue);
        slider.addListener(listener);

        table.add(nameLabel).width(250 * scale).left();
        table.add(slider).width(320 * scale).center();
        
        return table;
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
            }
        });

        return label;
    }
}