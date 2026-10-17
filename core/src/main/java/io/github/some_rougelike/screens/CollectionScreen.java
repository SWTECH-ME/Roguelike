package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.audio.SoundManager;
import io.github.some_rougelike.model.CardCollection;
import io.github.some_rougelike.model.RankProgress;

public class CollectionScreen extends PageScreen {
    private static final float CARD_ASPECT_RATIO = 1.4f;
    private static final float REFERENCE_SCREEN_AREA = 1280f * 720f;
    private static final float MIN_UI_SCALE = 0.65f;
    private static final float MAX_UI_SCALE = 1.75f;
    private static final String SKINS_DIR = "deck_skins";

    private final Table rootTable;
    private final Skin uiSkin;
    private final CardCollection collection;

    private final Array<FileHandle> skinFiles;
    private int currentSkinIndex = 0;
    private Texture currentDeckTexture;
    private TextureRegion[][] cardRegions;

    private Label skinNameLabel;

    private Texture whiteTex;
    private Texture darkGrayTex;

    public CollectionScreen(Main main) {
        super(main, "COLLECTION");

        this.uiSkin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.collection = CardCollection.createStandardDeck();

        initTextures();

        this.skinFiles = new Array<>();
        loadSkinFiles();

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        updateCurrentSkinTexture();
        rebuildUI();
    }

    private void initTextures() {
        Pixmap wp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        wp.setColor(Color.WHITE);
        wp.fill();
        whiteTex = new Texture(wp);
        wp.dispose();

        Pixmap gp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        gp.setColor(0.2f, 0.2f, 0.2f, 1f);
        gp.fill();
        darkGrayTex = new Texture(gp);
        gp.dispose();
    }

    private enum Mode {
        SKINS, RANKS, COMBINATIONS
    }

    private static final String[] RANK_NAMES = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
    private static final String[] COMBINATIONS = {
            "One Pair", "Two Pair", "Black Jack", "Three of a Kind", "5 Odd",
            "5 Even", "Straight", "Flush", "Full House", "Prime 5",
            "Royal House", "Four of a Kind", "Straight Flush", "Royal Flush"
    };

    private static final int[][][] EXAMPLES = {
            { { 3, 0 }, { 3, 1 }, { 7, 2 }, { 11, 3 }, { 0, 0 } },
            { { 2, 0 }, { 2, 1 }, { 7, 2 }, { 7, 3 }, { 11, 0 } },
            { { 8, 0 }, { 12, 1 } },
            { { 5, 0 }, { 5, 1 }, { 5, 2 }, { 11, 3 }, { 0, 0 } },
            { { 12, 0 }, { 1, 1 }, { 3, 2 }, { 5, 3 }, { 7, 0 } },
            { { 0, 0 }, { 2, 1 }, { 4, 2 }, { 6, 3 }, { 8, 0 } },
            { { 3, 0 }, { 4, 1 }, { 5, 2 }, { 6, 3 }, { 7, 0 } },
            { { 0, 2 }, { 3, 2 }, { 6, 2 }, { 9, 2 }, { 11, 2 } },
            { { 6, 0 }, { 6, 1 }, { 6, 2 }, { 11, 0 }, { 11, 1 } },
            { { 0, 0 }, { 1, 1 }, { 3, 2 }, { 5, 3 }, { 5, 0 } },
            { { 9, 0 }, { 10, 1 }, { 11, 2 }, { 10, 3 }, { 9, 1 } },
            { { 7, 0 }, { 7, 1 }, { 7, 2 }, { 7, 3 }, { 0, 0 } },
            { { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 } },
            { { 8, 0 }, { 9, 0 }, { 10, 0 }, { 11, 0 }, { 12, 0 } }
    };
    private static final String HIDDEN_NAME = "---Find this in a run---";
    private static final boolean ACE_FIRST_IN_SHEET = true;

    private Mode mode = Mode.SKINS;
    private final boolean[] comboHidden = new boolean[COMBINATIONS.length];

    private void loadSkinFiles() {
        String[] skinFileNames = {
                "black_blue.png", "black_red.png", "black_white.png",
                "white_black.png", "white_blue.png", "white_red.png"
        };

        for (String fileName : skinFileNames) {
            FileHandle file = Gdx.files.internal(SKINS_DIR + "/" + fileName);
            if (file.exists()) {
                skinFiles.add(file);
            }
        }
    }

    private void updateCurrentSkinTexture() {
        if (skinFiles.size == 0) return;

        if (currentDeckTexture != null) {
            currentDeckTexture.dispose();
            currentDeckTexture = null;
        }

        try {
            FileHandle currentSkinFile = skinFiles.get(currentSkinIndex);
            currentDeckTexture = new Texture(currentSkinFile);
            int cols = 13;
            int rows = 5;
            int tileWidth = currentDeckTexture.getWidth() / cols;
            int tileHeight = currentDeckTexture.getHeight() / rows;
            cardRegions = TextureRegion.split(currentDeckTexture, tileWidth, tileHeight);
        } catch (Exception e) {
            cardRegions = null;
        }
    }

    private void changeSkin(int delta) {
        if (skinFiles.size == 0) return;
        currentSkinIndex += delta;
        if (currentSkinIndex < 0) currentSkinIndex = skinFiles.size - 1;
        if (currentSkinIndex >= skinFiles.size) currentSkinIndex = 0;
        updateCurrentSkinTexture();
        rebuildUI();
    }

    private void rebuildUI() {
        rootTable.clearChildren();
        rootTable.center();

        float screenWidth = stage.getWidth();
        float screenHeight = stage.getHeight();
        float uiScale = getUiScale(screenWidth, screenHeight);

        Label titleLabel = new Label("COLLECTION", uiSkin);
        titleLabel.setFontScale(GameConfig.MAIN_MENU_TITLE_SIZE * uiScale);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).center().padTop(20f * uiScale).padBottom(30f * uiScale).row();

        rootTable.add(buildTabBar(uiScale)).center().padBottom(20f * uiScale).row();

        Actor content;
        switch (mode) {
            case RANKS:
                content = buildRanksContent(uiScale, screenWidth, screenHeight);
                break;
            case COMBINATIONS:
                content = buildCombinationsContent(uiScale, screenWidth, screenHeight);
                break;
            default:
                content = buildSkinsContent(uiScale, screenWidth, screenHeight);
                break;
        }
        rootTable.add(content).expand().fill().row();

        Table selectorTable = new Table();
        Label leftArrow = createHoverAction("<", uiScale * 1.5f, () -> changeSkin(-1));
        Label rightArrow = createHoverAction(">", uiScale * 1.5f, () -> changeSkin(1));

        String currentSkinName = (skinFiles.size > 0 && currentSkinIndex < skinFiles.size)
                ? skinFiles.get(currentSkinIndex).nameWithoutExtension().toUpperCase()
                : "NO SKINS";

        skinNameLabel = new Label(currentSkinName, uiSkin);
        skinNameLabel.setFontScale(uiScale * 1.1f);
        skinNameLabel.setColor(Color.WHITE);
        skinNameLabel.setAlignment(Align.center);

        float labelWidth = screenWidth * 0.25f;
        selectorTable.add(leftArrow).padRight(20f * uiScale);
        selectorTable.add(skinNameLabel).width(labelWidth).center();
        selectorTable.add(rightArrow).padLeft(20f * uiScale);

        rootTable.add(selectorTable).center().padTop(20f * uiScale).padBottom(20f * uiScale).row();

        Label backBtn = createHoverAction("BACK", GameConfig.MAIN_MENU_BUTTON_SIZE * uiScale, () -> goBack());
        rootTable.add(backBtn).center().padBottom(20f * uiScale);
    }

    private float getUiScale(float screenWidth, float screenHeight) {
        float scale = (float) Math.sqrt((screenWidth * screenHeight) / REFERENCE_SCREEN_AREA);
        return Math.max(MIN_UI_SCALE, Math.min(MAX_UI_SCALE, scale));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        rebuildUI();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (currentDeckTexture != null) {
            currentDeckTexture.dispose();
        }
        if (whiteTex != null) {
            whiteTex.dispose();
        }
        if (darkGrayTex != null) {
            darkGrayTex.dispose();
        }
    }

    private void setMode(Mode newMode) {
        if (mode == newMode) return;
        mode = newMode;
        rebuildUI();
    }

    private int rankToColumn(int rankIndex) {
        return ACE_FIRST_IN_SHEET ? (rankIndex + 1) % 13 : rankIndex;
    }

    private ScrollPane createModernScrollPane(Actor content, float uiScale) {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        
        TextureRegionDrawable knob = new TextureRegionDrawable(new TextureRegion(whiteTex));
        knob.setMinWidth(4 * uiScale);
        style.vScrollKnob = knob;

        ScrollPane scrollPane = new ScrollPane(content, style);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    private Table buildTabBar(float uiScale) {
        Table tabs = new Table();

        Label skinsTab = createTabLabel("SKINS", uiScale, mode == Mode.SKINS, () -> setMode(Mode.SKINS));
        Label ranksTab = createTabLabel("CARD UPGRADES", uiScale, mode == Mode.RANKS, () -> setMode(Mode.RANKS));
        Label combosTab = createTabLabel("COMBINATIONS", uiScale, mode == Mode.COMBINATIONS, () -> setMode(Mode.COMBINATIONS));

        tabs.add(skinsTab).padRight(40f * uiScale);
        tabs.add(ranksTab).padRight(40f * uiScale);
        tabs.add(combosTab);

        return tabs;
    }

    private Actor buildSkinsContent(float uiScale, float screenWidth, float screenHeight) {
        Table cardsTable = new Table();
        float padX = screenWidth * 0.003f;
        float padY = screenHeight * 0.015f;

        float cardHeight = screenHeight * 0.13f;
        float cardWidth = cardHeight / CARD_ASPECT_RATIO;

        if (cardRegions != null && collection != null && collection.getCards() != null) {
            for (int suit = 0; suit < 4; suit++) {
                Table suitRow = new Table();
                for (int rank = 0; rank < 13; rank++) {
                    if (suit < cardRegions.length && rank < cardRegions[suit].length) {
                        TextureRegion region = cardRegions[suit][rank];
                        Image img = new Image(region);
                        suitRow.add(img).size(cardWidth, cardHeight).padLeft(padX).padRight(padX);
                    }
                }
                cardsTable.add(suitRow).padBottom(padY).row();
            }
        } else {
            Label noSkinsLabel = new Label("Nincsenek betölthető kártya skinek!", uiSkin);
            noSkinsLabel.setFontScale(uiScale);
            noSkinsLabel.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
            cardsTable.add(noSkinsLabel).expand().center();
        }

        return createModernScrollPane(cardsTable, uiScale);
    }

    private Actor buildRanksContent(float uiScale, float screenWidth, float screenHeight) {
        Table ranksTable = new Table();
        float pad = Math.min(screenWidth, screenHeight) * 0.008f;
        ranksTable.defaults().pad(pad);

        if (cardRegions == null || cardRegions.length == 0) {
            Label noSkinsLabel = new Label("Nincsenek betölthető kártya skinek!", uiSkin);
            noSkinsLabel.setFontScale(uiScale);
            noSkinsLabel.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
            ranksTable.add(noSkinsLabel).center();
            return ranksTable;
        }

        boolean landscape = screenHeight <= screenWidth;
        int perRow = landscape ? 7 : 4;
        float cardWidth = landscape ? screenWidth * 0.08f : screenWidth * 0.15f;
        float cardHeight = cardWidth * CARD_ASPECT_RATIO;

        TextureRegion[] suitRow = cardRegions[0];
        
        TextureRegionDrawable bg = new TextureRegionDrawable(new TextureRegion(darkGrayTex));
        bg.setMinHeight(4 * uiScale);
        
        TextureRegionDrawable knobBefore = new TextureRegionDrawable(new TextureRegion(whiteTex));
        knobBefore.setMinHeight(4 * uiScale);
        
        ProgressBar.ProgressBarStyle modernStyle = new ProgressBar.ProgressBarStyle();
        modernStyle.background = bg;
        modernStyle.knobBefore = knobBefore;

        for (int i = 0; i < RANK_NAMES.length; i++) {
            int col = rankToColumn(i);
            if (col >= suitRow.length) continue;

            Table cell = new Table();
            cell.add(new Image(suitRow[col])).size(cardWidth, cardHeight).row();

            ProgressBar bar = new ProgressBar(0f, 100f, 1f, false, modernStyle);
            bar.setValue(RankProgress.getInstance().get(i));
            cell.add(bar).width(cardWidth).padTop(10f * uiScale);

            ranksTable.add(cell);
            if ((i + 1) % perRow == 0) {
                ranksTable.row();
            }
        }

        return createModernScrollPane(ranksTable, uiScale);
    }

    private Actor buildCombinationsContent(float uiScale, float screenWidth, float screenHeight) {
        Table list = new Table();

        float nameWidth = screenWidth * 0.22f;
        float cardWidth = Math.min(screenWidth * 0.05f, screenHeight * 0.09f);
        float cardHeight = cardWidth * CARD_ASPECT_RATIO;
        float padY = screenHeight * 0.015f;
        float padX = screenWidth * 0.005f;

        for (int i = 0; i < COMBINATIONS.length; i++) {
            final int index = i;
            boolean isHidden = GameConfig.ALLOW_HIDING_COMBINATIONS && comboHidden[i];

            final Label label = new Label(isHidden ? HIDDEN_NAME : COMBINATIONS[i], uiSkin);
            label.setFontScale(uiScale * 1.15f);
            label.setColor(Color.WHITE);
            label.setAlignment(Align.left);
            list.add(label).width(nameWidth).left().padRight(30f * uiScale).padTop(padY).padBottom(padY);

            final Table cards = new Table();
            if (cardRegions != null) {
                for (int[] c : EXAMPLES[i]) {
                    int col = rankToColumn(c[0]);
                    int suit = c[1];
                    if (suit < cardRegions.length && col < cardRegions[suit].length) {
                        cards.add(new Image(cardRegions[suit][col]))
                                .size(cardWidth, cardHeight).padRight(padX);
                    }
                }
            }
            cards.setVisible(!isHidden);

            if (GameConfig.ALLOW_HIDING_COMBINATIONS) {
                final Label toggle = new Label(isHidden ? "SHOW" : "HIDE", uiSkin);
                toggle.setFontScale(uiScale * 0.9f);
                toggle.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
                toggle.setOrigin(Align.center);
                toggle.setAlignment(Align.center);
                toggle.setTouchable(Touchable.enabled);
                toggle.addListener(new ClickListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (pointer == -1) {
                            toggle.clearActions();
                            toggle.addAction(Actions.parallel(
                                Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                                Actions.scaleTo(uiScale * 1f, uiScale * 1f, 0.15f, Interpolation.fade)
                            ));
                            SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (pointer == -1) {
                            toggle.clearActions();
                            toggle.addAction(Actions.parallel(
                                Actions.color(new Color(0.65f, 0.65f, 0.65f, 1f), 0.15f, Interpolation.fade),
                                Actions.scaleTo(uiScale * 0.9f, uiScale * 0.9f, 0.15f, Interpolation.fade)
                            ));
                        }
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                        comboHidden[index] = !comboHidden[index];
                        boolean hidden = comboHidden[index];
                        cards.setVisible(!hidden);
                        label.setText(hidden ? HIDDEN_NAME : COMBINATIONS[index]);
                        toggle.setText(hidden ? "SHOW" : "HIDE");
                    }
                });

                list.add(toggle).width(80 * uiScale).left().padRight(30f * uiScale).padTop(padY).padBottom(padY);
            }

            list.add(cards).left().padTop(padY).padBottom(padY).row();
        }

        Table wrapper = new Table();
        wrapper.add(list).expandX().center().top().padTop(20f * uiScale);

        return createModernScrollPane(wrapper, uiScale);
    }

    private Label createTabLabel(String text, float scale, boolean isActive, Runnable onClick) {
        Label label = new Label(text, uiSkin);
        label.setOrigin(Align.center);
        label.setAlignment(Align.center);

        if (isActive) {
            label.setFontScale(scale * 1.1f);
            label.setColor(Color.WHITE);
            label.setTouchable(Touchable.disabled);
        } else {
            label.setFontScale(scale);
            label.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
            label.setTouchable(Touchable.enabled);
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
                    if (onClick != null) onClick.run();
                }
            });
        }
        return label;
    }

    private Label createHoverAction(String text, float scale, Runnable onClick) {
        return createTabLabel(text, scale, false, onClick);
    }
}