package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.CardCollection;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
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

    public CollectionScreen(Main main) {
        super(main, "COLLECTION");

        this.uiSkin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.collection = CardCollection.createStandardDeck();

        this.skinFiles = new Array<>();
        loadSkinFiles();

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        updateCurrentSkinTexture();
        rebuildUI();
    }

    private enum Mode {
        SKINS, RANKS, COMBINATIONS
    }

    private static final String[] RANK_NAMES = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
    private static final String[] COMBINATIONS = {
            "One Pair",
            "Two Pair",
            "Black Jack",
            "Three of a Kind",
            "5 Odd",
            "5 Even",
            "Straight",
            "Flush",
            "Full House",
            "Prime 5",
            "Royal House",
            "Four of a Kind",
            "Straight Flush",
            "Royal Flush"
    };

    // Ugyanabban a sorrendben, mint a COMBINATIONS: {rang, szín}
    private static final int[][][] EXAMPLES = {
            // One Pair: 5 5 9 K 2
            { { 3, 0 }, { 3, 1 }, { 7, 2 }, { 11, 3 }, { 0, 0 } },
            // Two Pair: 4 4 9 9 K
            { { 2, 0 }, { 2, 1 }, { 7, 2 }, { 7, 3 }, { 11, 0 } },
            // Black Jack: 10 + Ász
            { { 8, 0 }, { 12, 1 } },
            // Three of a Kind: 7 7 7 K 2
            { { 5, 0 }, { 5, 1 }, { 5, 2 }, { 11, 3 }, { 0, 0 } },
            // 5 Odd: A 3 5 7 9
            { { 12, 0 }, { 1, 1 }, { 3, 2 }, { 5, 3 }, { 7, 0 } },
            // 5 Even: 2 4 6 8 10
            { { 0, 0 }, { 2, 1 }, { 4, 2 }, { 6, 3 }, { 8, 0 } },
            // Straight: 5 6 7 8 9, vegyes színek
            { { 3, 0 }, { 4, 1 }, { 5, 2 }, { 6, 3 }, { 7, 0 } },
            // Flush: 2 5 8 J K, egy szín
            { { 0, 2 }, { 3, 2 }, { 6, 2 }, { 9, 2 }, { 11, 2 } },
            // Full House: 8 8 8 K K
            { { 6, 0 }, { 6, 1 }, { 6, 2 }, { 11, 0 }, { 11, 1 } },
            // Prime 5: 2 3 5 7 7 (csak prím rangok, ismétlődhetnek)
            { { 0, 0 }, { 1, 1 }, { 3, 2 }, { 5, 3 }, { 5, 0 } },
            // Royal House: 5 arcos lap (J Q K Q J)
            { { 9, 0 }, { 10, 1 }, { 11, 2 }, { 10, 3 }, { 9, 1 } },
            // Four of a Kind: 9 9 9 9 2
            { { 7, 0 }, { 7, 1 }, { 7, 2 }, { 7, 3 }, { 0, 0 } },
            // Straight Flush: 5 6 7 8 9, egy szín
            { { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 } },
            // Royal Flush: 10 J Q K A, egy szín
            { { 8, 0 }, { 9, 0 }, { 10, 0 }, { 11, 0 }, { 12, 0 } }
    };
    private static final String HIDDEN_NAME = "---Find this in a run---";

    // Ha a képen az ász az első oszlop (A,2,3...K), hagyd true-n.
    // Ha a sorrend 2,3...K,A, állítsd false-ra.
    private static final boolean ACE_FIRST_IN_SHEET = true;

    private Mode mode = Mode.SKINS;
    private final boolean[] comboHidden = new boolean[COMBINATIONS.length];

    private void loadSkinFiles() {
        String[] skinFileNames = {
                "black_blue.png",
                "black_red.png",
                "black_white.png",
                "white_black.png",
                "white_blue.png",
                "white_red.png"
        };

        for (String fileName : skinFileNames) {
            FileHandle file = Gdx.files.internal(SKINS_DIR + "/" + fileName);
            if (file.exists()) {
                skinFiles.add(file);
                Gdx.app.log("CollectionScreen", "Skin sikeresen betöltve: " + file.name());
            } else {
                Gdx.app.error("CollectionScreen", "Skin fájl nem található: " + file.path());
            }
        }

        if (skinFiles.size == 0) {
            Gdx.app.error("CollectionScreen", "Nincsenek érvényes kártya skinek!");
        }
    }

    private void updateCurrentSkinTexture() {
        if (skinFiles.size == 0)
            return;

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
            Gdx.app.error("CollectionScreen", "Hiba a skin textúra betöltése közben: " + e.getMessage());
            cardRegions = null;
        }
    }

    private void changeSkin(int delta) {
        if (skinFiles.size == 0)
            return;

        currentSkinIndex += delta;
        if (currentSkinIndex < 0)
            currentSkinIndex = skinFiles.size - 1;
        if (currentSkinIndex >= skinFiles.size)
            currentSkinIndex = 0;

        updateCurrentSkinTexture();
        if (skinNameLabel != null) {
            skinNameLabel.setText(skinFiles.get(currentSkinIndex).nameWithoutExtension().toUpperCase());
        }
        rebuildUI();
    }

    private void rebuildUI() {
        rootTable.clearChildren();
        rootTable.center();

        float screenWidth = stage.getWidth();
        float screenHeight = stage.getHeight();
        float uiScale = getUiScale(screenWidth, screenHeight);

        // 1) felső váltósor
        rootTable.add(buildTabBar(uiScale, screenHeight)).center().expandX().row();

        // 2) tartalom a kiválasztott módnak megfelelően
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

        // 3) alsó skinválasztó (az eredeti kódod, változtatás nélkül)
        Table selectorTable = new Table();
        selectorTable.defaults().pad(15f * uiScale).space(20f * uiScale);

        TextButton leftArrow = new TextButton("<", uiSkin);
        TextButton rightArrow = new TextButton(">", uiSkin);
        leftArrow.getLabel().setFontScale(uiScale);
        rightArrow.getLabel().setFontScale(uiScale);

        String currentSkinName = (skinFiles.size > 0 && currentSkinIndex < skinFiles.size)
                ? skinFiles.get(currentSkinIndex).nameWithoutExtension().toUpperCase()
                : "NO SKINS";

        skinNameLabel = new Label(currentSkinName, uiSkin);
        skinNameLabel.setFontScale(uiScale);
        skinNameLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        leftArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeSkin(-1);
            }
        });
        rightArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeSkin(1);
            }
        });

        float labelWidth = screenWidth * 0.25f;
        float buttonHeight = Math.max(60f * uiScale, screenHeight * 0.09f);

        selectorTable.add(leftArrow).size(buttonHeight * 1.2f, buttonHeight).fill();
        selectorTable.add(skinNameLabel).width(labelWidth).height(buttonHeight).center();
        selectorTable.add(rightArrow).size(buttonHeight * 1.2f, buttonHeight).fill();

        float selectorHeight = Math.max(70f * uiScale, screenHeight * 0.1f);
        rootTable.add(selectorTable).center().expandX().height(selectorHeight).padBottom(20f * uiScale);
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
    }

    private void setMode(Mode newMode) {
        if (mode == newMode)
            return;
        mode = newMode;
        rebuildUI();
    }

    /** 0..12 = 2..Ász -> oszlopindex a skin képen. */
    private int rankToColumn(int rankIndex) {
        return ACE_FIRST_IN_SHEET ? (rankIndex + 1) % 13 : rankIndex;
    }

    private Table buildTabBar(float uiScale, float screenHeight) {
        Table tabs = new Table();
        tabs.defaults().pad(8f * uiScale).space(10f * uiScale);

        TextButton skinsTab = new TextButton("SKINS", uiSkin);
        TextButton ranksTab = new TextButton("Card Upgrades", uiSkin);
        skinsTab.getLabel().setFontScale(uiScale);
        ranksTab.getLabel().setFontScale(uiScale);

        // Az aktív fül le van tiltva (és szürkén látszik), a másik kattintható
        skinsTab.setDisabled(mode == Mode.SKINS);
        ranksTab.setDisabled(mode == Mode.RANKS);

        skinsTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(Mode.SKINS);
            }
        });
        ranksTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(Mode.RANKS);
            }
        });
        TextButton combosTab = new TextButton("Combinations", uiSkin);
        combosTab.getLabel().setFontScale(uiScale);
        combosTab.setDisabled(mode == Mode.COMBINATIONS);
        combosTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setMode(Mode.COMBINATIONS);
            }
        });

        float h = Math.max(45f * uiScale, screenHeight * 0.07f);
        tabs.add(skinsTab).size(h * 3f, h);
        tabs.add(ranksTab).size(h * 3f, h);
        tabs.add(combosTab).size(h * 3f, h);
        return tabs;
    }

    /** Az eredeti "összes kártya" nézet. */
    private Actor buildSkinsContent(float uiScale, float screenWidth, float screenHeight) {
        Table cardsTable = new Table();
        float dynamicPadding = Math.min(screenWidth, screenHeight) * 0.006f;
        cardsTable.defaults().pad(dynamicPadding);

        float cardWidth, cardHeight;
        int cardsPerRow;
        if (screenHeight <= screenWidth) {
            cardsPerRow = 13;
            cardHeight = screenHeight * 0.16f;
            cardWidth = cardHeight / CARD_ASPECT_RATIO;
        } else {
            cardsPerRow = 4;
            cardWidth = screenWidth * 0.16f;
            cardHeight = cardWidth * CARD_ASPECT_RATIO;
        }

        if (cardRegions != null && collection != null && collection.getCards() != null) {
            int counter = 0;
            for (Card card : collection.getCards()) {
                int suitIndex = counter / 13;
                int rankIndex = counter % 13;

                if (suitIndex < cardRegions.length && rankIndex < cardRegions[suitIndex].length) {
                    TextureRegion region = cardRegions[suitIndex][rankIndex];
                    card.setTextureRegion(region);
                    cardsTable.add(new Image(region)).size(cardWidth, cardHeight);
                }

                counter++;
                if (counter % cardsPerRow == 0) {
                    cardsTable.row();
                }
            }
        } else {
            Label noSkinsLabel = new Label("Nincsenek betölthető kártya skinek!", uiSkin);
            noSkinsLabel.setFontScale(uiScale);
            cardsTable.add(noSkinsLabel).expand().center();
        }

        ScrollPane scrollPane = new ScrollPane(cardsTable, uiSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(screenHeight > screenWidth, screenHeight <= screenWidth);
        return scrollPane;
    }

    /** 2-től ászig, egy színből, középen felsorolva, alattuk a fejlődési sáv. */
    private Actor buildRanksContent(float uiScale, float screenWidth, float screenHeight) {
        Table ranksTable = new Table();
        float pad = Math.min(screenWidth, screenHeight) * 0.008f;
        ranksTable.defaults().pad(pad);

        if (cardRegions == null || cardRegions.length == 0) {
            Label noSkinsLabel = new Label("Nincsenek betölthető kártya skinek!", uiSkin);
            noSkinsLabel.setFontScale(uiScale);
            ranksTable.add(noSkinsLabel).center();
            return ranksTable;
        }

        boolean landscape = screenHeight <= screenWidth;
        int perRow = landscape ? 7 : 4;
        float cardWidth = landscape ? screenWidth * 0.09f : screenWidth * 0.18f;
        float cardHeight = cardWidth * CARD_ASPECT_RATIO;

        TextureRegion[] suitRow = cardRegions[0];

        for (int i = 0; i < RANK_NAMES.length; i++) {
            int col = rankToColumn(i);
            if (col >= suitRow.length)
                continue;

            Table cell = new Table();
            cell.add(new Image(suitRow[col])).size(cardWidth, cardHeight).row();

            ProgressBar bar = new ProgressBar(0f, 100f, 1f, false, uiSkin);
            bar.setValue(RankProgress.getInstance().get(i));
            cell.add(bar).width(cardWidth).padTop(4f * uiScale);

            ranksTable.add(cell);
            if ((i + 1) % perRow == 0) {
                ranksTable.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(ranksTable, uiSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    /** A kártyakombinációk középre igazított listája. */
    /** Kombináció neve, mellette egy gomb (ki/be), és a példa lapokból kirakva. */
    private Actor buildCombinationsContent(float uiScale, float screenWidth, float screenHeight) {
        Table list = new Table();

        float nameWidth = screenWidth * 0.22f;
        float cardWidth = Math.min(screenWidth * 0.06f, screenHeight * 0.1f);
        float cardHeight = cardWidth * CARD_ASPECT_RATIO;
        float pad = Math.min(screenWidth, screenHeight) * 0.006f;
        float buttonHeight = Math.max(40f * uiScale, screenHeight * 0.06f);

        for (int i = 0; i < COMBINATIONS.length; i++) {
            final int index = i;

            final Label label = new Label(comboHidden[i] ? HIDDEN_NAME : COMBINATIONS[i], uiSkin);
            label.setFontScale(uiScale * 1.2f);
            label.setAlignment(com.badlogic.gdx.utils.Align.left);
            list.add(label).width(nameWidth).left().padRight(20f * uiScale);

            final Table cards = new Table();
            if (cardRegions != null) {
                for (int[] c : EXAMPLES[i]) {
                    int col = rankToColumn(c[0]);
                    int suit = c[1];
                    if (suit < cardRegions.length && col < cardRegions[suit].length) {
                        cards.add(new Image(cardRegions[suit][col]))
                                .size(cardWidth, cardHeight).pad(pad);
                    }
                }
            }
            cards.setVisible(!comboHidden[i]);

            final TextButton toggle = new TextButton(comboHidden[i] ? "SHOW" : "HIDE", uiSkin);
            toggle.getLabel().setFontScale(uiScale);
            toggle.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    comboHidden[index] = !comboHidden[index];
                    boolean hidden = comboHidden[index];
                    cards.setVisible(!hidden);
                    label.setText(hidden ? HIDDEN_NAME : COMBINATIONS[index]);
                    toggle.setText(hidden ? "SHOW" : "HIDE");
                }
            });

            list.add(toggle).size(buttonHeight * 2f, buttonHeight).padRight(20f * uiScale);
            list.add(cards).left().padTop(pad).padBottom(pad).row();
        }

        Table wrapper = new Table();
        wrapper.add(list).expandX().center().top();

        ScrollPane scrollPane = new ScrollPane(wrapper, uiSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }
}
