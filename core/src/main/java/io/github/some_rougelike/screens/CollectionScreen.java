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

        Table cardsTable = new Table();
        float screenWidth = stage.getWidth();
        float screenHeight = stage.getHeight();
        float uiScale = getUiScale(screenWidth, screenHeight);
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

                    Image cardImage = new Image(region);
                    cardsTable.add(cardImage).size(cardWidth, cardHeight);
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

        if (screenHeight <= screenWidth) {
            scrollPane.setScrollingDisabled(false, true);
        } else {
            scrollPane.setScrollingDisabled(true, false);
        }

        rootTable.add(scrollPane).expand().fill().row();

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
}