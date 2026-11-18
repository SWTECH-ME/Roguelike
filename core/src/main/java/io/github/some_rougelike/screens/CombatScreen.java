package io.github.some_rougelike.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import io.github.some_rougelike.logic.BossAbilityType;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.logic.HandEvaluator;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.model.RankProgress;
import io.github.some_rougelike.model.Suit;
import io.github.some_rougelike.save.CardSaveData;
import io.github.some_rougelike.save.CombatSaveData;
import io.github.some_rougelike.save.SaveManager;
import io.github.some_rougelike.ui.CardTransition;
import io.github.some_rougelike.ui.PauseMenu;
import io.github.some_rougelike.ui.TrippyBackground;

public class CombatScreen extends PageScreen {
    private RunState runState;
    private Enemy enemy;

    private int targetDisplayScore = 0;
    private int currentScore = 0;
    private int playsLeft;
    private int discardsLeft;

    private List<Card> drawPile;
    private List<Card> hand = new ArrayList<>();
    private List<Card> selectedCards = new ArrayList<>();

    private final Map<Card, Group> cardActorMap = new HashMap<>();

    private Label scoreLabel;
    private Label targetLabel;
    private Label playsLabel;
    private Label discardsLabel;
    private Label deckCountLabel;
    private Label comboLabel;
    private String lastComboText = null;

    private Table uiRoot;
    private Group cardLayer;
    private Skin skin;
    private boolean isGameOver = false;

    private Texture deckTexture;
    private TextureRegion[][] cardRegions;
    private Array<Texture> generatedTextures;
    private TextureRegionDrawable whiteTintableBg;

    private TextureRegionDrawable cardBgNormal;
    private TextureRegionDrawable cardBgSelected;

    private boolean isScoring = false;
    private boolean isAnimatingFlyout = false;
    private float scoringTimer = 0f;
    private float drawingTimer = 0f;
    private int scoringCardIndex = 0;
    private int currentHandPoints = 0;
    private double currentMultiplier = 1;
    private final List<Card> scoringCards = new ArrayList<>();
    private final List<Group> scoringCardGroups = new ArrayList<>();
    private final List<Boolean> preRolledDoubles = new ArrayList<>();
    private final List<Integer> preRolledChips = new ArrayList<>();
    private final List<Card> pendingDrawnCards = new ArrayList<>();

    private float uiScale;
    private TrippyBackground bgImg;
    private Color currentColor1;
    private Color currentColor2;
    private boolean isTransitioningOut = false;

    private Image playStrike;
    private Image discardStrike;
    private Label playBtnLabel;
    private Label discardBtnLabel;
    private PauseMenu pauseMenu;
    private boolean playStrikeActive = false;
    private boolean discardStrikeActive = false;

    private interface Condition {
        boolean isTrue();
    }

    public CombatScreen(Main main, RunState runState) {
        this(main, runState, null);
    }

    public CombatScreen(Main main, RunState runState, CombatSaveData savedCombat) {
        super(main, "");
        this.runState = runState;
        this.enemy = runState.getCurrentEnemy();
        this.skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);
        this.generatedTextures = new Array<>();

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        this.uiScale = Math.min(width / 1920f, height / 1080f) * GameConfig.MAIN_MENU_SCALE;

        Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePix.setColor(Color.WHITE);
        whitePix.fill();
        Texture whiteTex = new Texture(whitePix);
        whiteTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        whitePix.dispose();
        generatedTextures.add(whiteTex);
        whiteTintableBg = new TextureRegionDrawable(whiteTex);

        loadDeckTexture();

        if (savedCombat != null) {
            this.currentScore = savedCombat.getCurrentScore();
            this.targetDisplayScore = savedCombat.getCurrentScore();
            this.playsLeft = savedCombat.getPlaysLeft();
            this.discardsLeft = savedCombat.getDiscardsLeft();
            this.drawPile = new ArrayList<>();
            if (savedCombat.getDrawPile() != null) {
                for (CardSaveData csd : savedCombat.getDrawPile()) {
                    this.drawPile.add(csd.toCard());
                }
            }
            this.hand = new ArrayList<>();
            if (savedCombat.getHand() != null) {
                for (CardSaveData csd : savedCombat.getHand()) {
                    this.hand.add(csd.toCard());
                }
            }
            this.selectedCards = new ArrayList<>();
            if (savedCombat.getSelectedHandIndices() != null) {
                for (Integer idx : savedCombat.getSelectedHandIndices()) {
                    if (idx != null && idx >= 0 && idx < this.hand.size()) {
                        Card selected = this.hand.get(idx);
                        selected.setSelected(true);
                        this.selectedCards.add(selected);
                    }
                }
            }
        } else {
            this.playsLeft = enemy.getMaxPlays();
            this.discardsLeft = enemy.getMaxDiscards();
            this.drawPile = new ArrayList<>(runState.getDeck());
            Collections.shuffle(this.drawPile);
        }

        currentColor1 = Color.valueOf("4C9A7F");
        currentColor2 = Color.valueOf("2D6A54");
        if (enemy.isBoss()) {
            currentColor1.set(GameConfig.COMBAT_BOSS_BG_1);
            currentColor2.set(GameConfig.COMBAT_BOSS_BG_2);
        }

        bgImg = new TrippyBackground(currentColor1, currentColor2);
        bgImg.setSize(width, height);
        bgImg.setPosition(0, 0);
        bgImg.setTouchable(Touchable.disabled);
        stage.addActor(bgImg);

        assignTextureRegionsToAllCards();
        buildUI(savedCombat != null);

        pauseMenu = new PauseMenu(stage, skin, uiScale,
            () -> setPaused(false),
            () -> {
                saveCurrentCombatState();
                RankProgress.getInstance().save();
                SaveManager.getInstance().save();
            },
            this::exitToMainMenu);
    }

    private BossAbilityType getActiveBossAbility() {
        if (enemy != null && enemy.isBoss()) {
            return enemy.getAbilityType();
        }
        return BossAbilityType.NONE;
    }

    private int getEffectiveHandSize() {
        return getActiveBossAbility().modifyHandSize(GameConfig.STARTING_HAND_SIZE);
    }

    private void loadDeckTexture() {
        try {
            FileHandle file = Gdx.files.internal(SaveManager.getInstance().getSelectedDeckSkinPath());
            if (!file.exists()) {
                file = Gdx.files.internal(GameConfig.DECK_SKINS_DIR + "/" + GameConfig.DEFAULT_DECK_SKIN);
            }
            if (file.exists()) {
                deckTexture = new Texture(file);
                int cols = GameConfig.DECK_SHEET_COLS;
                int rows = GameConfig.DECK_SHEET_ROWS;
                int tileWidth = deckTexture.getWidth() / cols;
                int tileHeight = deckTexture.getHeight() / rows;
                cardRegions = TextureRegion.split(deckTexture, tileWidth, tileHeight);
            }
        } catch (Exception e) {
            Gdx.app.error("CombatScreen", e.getMessage());
        }
    }

    private void assignTextureRegionsToAllCards() {
        if (cardRegions == null) return;
        assignRegionsToList(runState.getDeck());
        assignRegionsToList(drawPile);
        assignRegionsToList(hand);
    }

    private void assignRegionsToList(List<Card> cards) {
        if (cards == null || cardRegions == null) return;
        for (Card card : cards) {
            int row = getRowForSuit(card.getSuit());
            int col = getColForRank(card.getRank());
            if (row >= 0 && row < cardRegions.length && col >= 0 && col < cardRegions[row].length) {
                card.setTextureRegion(cardRegions[row][col]);
            }
        }
    }

    public void saveCurrentCombatState() {
        if (isGameOver || runState.isBossDebugMode()) return;
        CombatSaveData combatSaveData = CombatSaveData.create(
            targetDisplayScore,
            playsLeft,
            discardsLeft,
            drawPile,
            hand,
            selectedCards
        );
        SaveManager.getInstance().saveCombatState(runState, combatSaveData);
    }

    private void saveProjectedCombatState(int projectedScore, int projectedPlaysLeft, int projectedDiscardsLeft, List<Card> projectedDrawPile, List<Card> projectedHand, List<Card> projectedSelected) {
        if (runState.isBossDebugMode()) {
            return;
        }
        if (projectedScore < enemy.getTargetScore() && projectedPlaysLeft <= 0) {
            SaveManager.getInstance().clearActiveRun();
            return;
        }
        CombatSaveData combatSaveData = CombatSaveData.create(
            projectedScore,
            projectedPlaysLeft,
            projectedDiscardsLeft,
            projectedDrawPile,
            projectedHand,
            projectedSelected
        );
        SaveManager.getInstance().saveCombatState(runState, combatSaveData);
    }

    private static final Map<Suit, Integer> SUIT_ROWS = new EnumMap<>(Suit.class);
    private static final Map<Rank, Integer> RANK_COLS = new EnumMap<>(Rank.class);

    static {
        SUIT_ROWS.put(Suit.CLUBS, 0);
        SUIT_ROWS.put(Suit.HEARTS, 1);
        SUIT_ROWS.put(Suit.SPADES, 2);
        SUIT_ROWS.put(Suit.DIAMONDS, 3);

        RANK_COLS.put(Rank.ACE, 0);
        RANK_COLS.put(Rank.TWO, 1);
        RANK_COLS.put(Rank.THREE, 2);
        RANK_COLS.put(Rank.FOUR, 3);
        RANK_COLS.put(Rank.FIVE, 4);
        RANK_COLS.put(Rank.SIX, 5);
        RANK_COLS.put(Rank.SEVEN, 6);
        RANK_COLS.put(Rank.EIGHT, 7);
        RANK_COLS.put(Rank.NINE, 8);
        RANK_COLS.put(Rank.TEN, 9);
        RANK_COLS.put(Rank.JACK, 10);
        RANK_COLS.put(Rank.QUEEN, 11);
        RANK_COLS.put(Rank.KING, 12);
    }

    private int getRowForSuit(Suit suit) {
        if (getActiveBossAbility().areCardColorsHidden()) {
            if (suit == Suit.HEARTS) {
                return SUIT_ROWS.getOrDefault(Suit.SPADES, 2);
            }
            if (suit == Suit.DIAMONDS) {
                return SUIT_ROWS.getOrDefault(Suit.CLUBS, 0);
            }
        }
        return SUIT_ROWS.getOrDefault(suit, 0);
    }

    private int getColForRank(Rank rank) {
        return RANK_COLS.getOrDefault(rank, 0);
    }

    private int getProgressIndex(Rank rank) {
        return (getColForRank(rank) + 12) % 13;
    }

    private boolean rollDouble(Card card) {
        if (!getActiveBossAbility().isDoubleRollAllowed()) {
            return false;
        }
        float chance = RankProgress.getInstance().get(getProgressIndex(card.getRank()));
        return MathUtils.random(100f) < chance;
    }

    private List<Card> drawUpToMax() {
        List<Card> drawn = new ArrayList<>();
        int maxHand = getEffectiveHandSize();
        while (hand.size() < maxHand && !drawPile.isEmpty()) {
            Card c = drawPile.remove(0);
            hand.add(c);
            drawn.add(c);
        }
        if (deckCountLabel != null) {
            deckCountLabel.setText(String.valueOf(drawPile.size()));
        }
        return drawn;
    }

    private TextureRegionDrawable createRoundedRect(int width, int height, Color color, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(color);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        generatedTextures.add(tex);
        return new TextureRegionDrawable(tex);
    }

    private void buildUI(boolean restoredFromSave) {
        uiRoot = new Table();
        uiRoot.setFillParent(true);

        float cardWidth = GameConfig.COMBAT_CARD_WIDTH * uiScale;
        float cardHeight = GameConfig.COMBAT_CARD_HEIGHT * uiScale;
        int cornerRadius = (int)(GameConfig.COMBAT_CARD_RADIUS * uiScale);

        cardBgNormal = createRoundedRect((int)cardWidth, (int)cardHeight, GameConfig.COMBAT_CARD_BG_NORMAL, cornerRadius);
        cardBgSelected = createRoundedRect((int)cardWidth, (int)cardHeight, GameConfig.COMBAT_CARD_BG_SELECTED, cornerRadius);

        Table leftScorePanel = new Table();
        Label scoreTitle = new Label("ROUND SCORE", skin);
        scoreTitle.setFontScale(uiScale * 0.9f);
        scoreTitle.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));

        scoreLabel = new Label(String.valueOf(currentScore), skin);
        scoreLabel.setFontScale(uiScale * 4.0f);
        scoreLabel.setColor(Color.WHITE);

        targetLabel = new Label("Target: " + enemy.getTargetScore(), skin);
        targetLabel.setFontScale(uiScale * 1.3f);
        targetLabel.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        leftScorePanel.add(scoreTitle).left().row();
        leftScorePanel.add(scoreLabel).left().padTop(5 * uiScale).padBottom(5 * uiScale).row();
        leftScorePanel.add(targetLabel).left();

        Table rightStatsPanel = new Table();
        playsLabel = new Label("Plays: " + playsLeft, skin);
        playsLabel.setFontScale(uiScale * 1.3f);
        playsLabel.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));

        discardsLabel = new Label("Discards: " + discardsLeft, skin);
        discardsLabel.setFontScale(uiScale * 1.3f);
        discardsLabel.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));

        rightStatsPanel.add(playsLabel).right().padBottom(10 * uiScale).row();
        rightStatsPanel.add(discardsLabel).right();

        Table topArea = new Table();
        topArea.add(leftScorePanel).expandX().left().padLeft(60 * uiScale).padTop(50 * uiScale);
        topArea.add(rightStatsPanel).expandX().right().padRight(60 * uiScale).padTop(50 * uiScale).top();

        Table middleTable = new Table();
        Label enemyType = new Label(enemy.isBoss() ? "BOSS BLIND" : "NORMAL BLIND", skin);
        enemyType.setFontScale(uiScale * 1.0f);
        enemyType.setColor(enemy.isBoss() ? new Color(0.9f, 0.3f, 0.3f, 1f) : new Color(0.4f, 0.6f, 0.9f, 1f));

        Label enemyName = new Label(enemy.getName(), skin);
        enemyName.setFontScale(uiScale * 2.5f);
        enemyName.setColor(Color.WHITE);

        middleTable.add(enemyType).row();
        middleTable.add(enemyName).padTop(10 * uiScale).row();

        if (enemy.isBoss()) {
            Label abilityLabel = new Label(enemy.getSpecialAbility(), skin);
            abilityLabel.setFontScale(uiScale * 1.1f);
            abilityLabel.setColor(new Color(0.9f, 0.5f, 0.5f, 1f));
            middleTable.add(abilityLabel).padTop(10 * uiScale).row();
        }

        comboLabel = new Label("", skin);
        comboLabel.setFontScale(uiScale * 2.0f);
        comboLabel.setColor(Color.valueOf("FFD700"));
        comboLabel.setAlignment(Align.center);
        middleTable.add(comboLabel).padTop(30 * uiScale);

        Table bottomSection = new Table();
        Table actionTable = new Table();

        Group playBtn = createActionButton("PLAY HAND", uiScale * 1.3f, true, () -> playSelectedCards(), () -> playsLeft <= 0, true);
        Group discardBtn = createActionButton("DISCARD", uiScale * 1.3f, true, () -> discardSelectedCards(), () -> discardsLeft <= 0, false);

        actionTable.add(playBtn).padRight(60 * uiScale);
        actionTable.add(discardBtn);

        final List<Card> initialHandCards;
        if (!restoredFromSave) {
            initialHandCards = drawUpToMax();
            saveCurrentCombatState();
        } else {
            initialHandCards = new ArrayList<>(hand);
        }

        Table deckTable = new Table();
        Label deckTitle = new Label("DECK", skin);
        deckTitle.setFontScale(uiScale * 0.9f);
        deckTitle.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        deckTitle.setAlignment(Align.right);

        deckCountLabel = new Label(String.valueOf(drawPile.size()), skin);
        deckCountLabel.setFontScale(uiScale * 2.0f);
        deckCountLabel.setColor(Color.WHITE);
        deckCountLabel.setAlignment(Align.right);

        deckTable.add(deckTitle).right().row();
        deckTable.add(deckCountLabel).right().padTop(5 * uiScale).row();

        Table dummyHandSpace = new Table();

        Table handAndActions = new Table();
        handAndActions.add(dummyHandSpace).size(0, GameConfig.COMBAT_CARD_HEIGHT * uiScale).padBottom(40 * uiScale).row();
        handAndActions.add(actionTable);

        bottomSection.add().uniformX().expandX();
        bottomSection.add(handAndActions).center();

        Table deckWrapper = new Table();
        deckWrapper.add(deckTable).expand().right().bottom().padRight(60 * uiScale).padBottom(50 * uiScale);
        bottomSection.add(deckWrapper).uniformX().expandX().fill();

        uiRoot.add(topArea).fillX().top().row();
        uiRoot.add(middleTable).expand().center().row();
        uiRoot.add(bottomSection).fillX().padBottom(40 * uiScale).bottom();

        stage.addActor(uiRoot);

        Actor inputBlocker = new Actor();
        inputBlocker.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        inputBlocker.setTouchable(Touchable.enabled);
        inputBlocker.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.removeActor()
        ));
        stage.addActor(inputBlocker);

        cardLayer = new Group();
        cardLayer.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cardLayer.setTouchable(Touchable.childrenOnly);
        stage.addActor(cardLayer);

        CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, true);

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.DRAW_INITIAL_DELAY),
            Actions.run(() -> {
                updateHandUI(initialHandCards);
            })
        ));

        updateTopBar();
    }

    private Group createActionButton(String text, float scale, boolean checkBusy, Runnable onClick, Condition isZero, boolean isPlayBtn) {
        Group group = new Group();

        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(GameConfig.BUTTON_TEXT_COLOR);
        label.setAlignment(Align.center);
        label.pack();

        float w = label.getWidth();
        float h = label.getHeight();
        group.setSize(w, h);
        group.setOrigin(Align.center);

        label.setPosition(0, 0);
        group.addActor(label);

        float extraWidth = GameConfig.BUTTON_STRIKE_OVERHANG * uiScale;
        float strikeThick = GameConfig.BUTTON_STRIKE_THICKNESS * uiScale;

        Image strikethrough = new Image(whiteTintableBg);
        strikethrough.setColor(GameConfig.BUTTON_TEXT_COLOR);
        strikethrough.setSize(0, strikeThick);
        strikethrough.setPosition(-extraWidth / 2f, h / 2f - (strikeThick / 2f));
        group.addActor(strikethrough);

        if (isPlayBtn) {
            playStrike = strikethrough;
            playBtnLabel = label;
        } else {
            discardStrike = strikethrough;
            discardBtnLabel = label;
        }

        group.setTouchable(Touchable.enabled);

        group.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isPaused && (!checkBusy || (!isGameOver && !isScoring))) {
                    group.clearActions();
                    group.addAction(Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.fade));

                    label.clearActions();
                    label.addAction(Actions.color(Color.WHITE, 0.15f, Interpolation.fade));

                    strikethrough.clearActions();
                    float targetW = isZero.isTrue() ? label.getWidth() + extraWidth : 0f;

                    strikethrough.addAction(Actions.parallel(
                        Actions.color(Color.WHITE, 0.15f, Interpolation.fade),
                        Actions.sizeTo(targetW, strikeThick, 0.15f, Interpolation.smooth)
                    ));

                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isPaused) {
                    group.clearActions();
                    group.addAction(Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.fade));

                    label.clearActions();
                    label.addAction(Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade));

                    strikethrough.clearActions();
                    float targetW = isZero.isTrue() ? label.getWidth() + extraWidth : 0f;

                    strikethrough.addAction(Actions.parallel(
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade),
                        Actions.sizeTo(targetW, strikeThick, 0.15f, Interpolation.smooth)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isPaused && (!checkBusy || (!isGameOver && !isScoring))) {
                    if (isZero.isTrue()) {
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);

                        label.clearActions();
                        label.addAction(Actions.sequence(
                            Actions.color(GameConfig.BUTTON_ERROR_COLOR, 0.1f),
                            Actions.color(Color.WHITE, 0.2f)
                        ));

                        strikethrough.clearActions();
                        float targetW = label.getWidth() + extraWidth;
                        strikethrough.addAction(Actions.parallel(
                            Actions.sizeTo(targetW, strikeThick),
                            Actions.sequence(
                                Actions.color(GameConfig.BUTTON_ERROR_COLOR, 0.1f),
                                Actions.color(Color.WHITE, 0.2f)
                            )
                        ));
                    } else {
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);

                        group.clearActions();
                        group.addAction(Actions.sequence(
                            Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.fade),
                            Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.fade)
                        ));

                        if (onClick != null) onClick.run();
                    }
                }
            }
        });

        return group;
    }

    private Label createHoverLabel(String text, float scale, boolean checkBusy, Runnable onClick) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(GameConfig.BUTTON_TEXT_COLOR);
        label.setTouchable(Touchable.enabled);
        label.setOrigin(Align.center);
        label.setAlignment(Align.center);

        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isPaused && (!checkBusy || (!isGameOver && !isScoring))) {
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
                if (pointer == -1 && !isPaused) {
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.15f, Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isPaused && (!checkBusy || (!isGameOver && !isScoring))) {
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);

                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.1f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.1f, Interpolation.fade)
                    ));

                    if (onClick != null) onClick.run();
                }
            }
        });

        return label;
    }

    private void updateHandUI(List<Card> incomingCards) {
        float cardWidth = GameConfig.COMBAT_CARD_WIDTH * uiScale;
        float cardHeight = GameConfig.COMBAT_CARD_HEIGHT * uiScale;
        float hoverOffset = GameConfig.COMBAT_CARD_HOVER_OFFSET * uiScale;
        float selectOffset = GameConfig.COMBAT_CARD_SELECT_OFFSET * uiScale;
        float spacing = GameConfig.HAND_SPACING * uiScale;
        float targetY = GameConfig.HAND_Y_POS * uiScale;

        float maxHandAnimTime = 0f;
        float baseRepositionTime = 0f;

        for (Card card : hand) {
            if (!cardActorMap.containsKey(card)) {
                Group cardWrapper = new Group();
                cardWrapper.setSize(cardWidth, cardHeight);
                cardWrapper.setTouchable(Touchable.enabled);

                Table cardBg = new Table();
                cardBg.setSize(cardWidth, cardHeight);
                cardBg.setBackground(selectedCards.contains(card) ? cardBgSelected : cardBgNormal);
                cardBg.setTouchable(Touchable.disabled);

                Actor cardContent;
                if (card.getTextureRegion() != null) {
                    Image cardImg = new Image(card.getTextureRegion());
                    cardImg.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
                    if (getActiveBossAbility().areCardColorsHidden()) {
                        cardImg.setColor(GameConfig.BOSS_HIDDEN_SUIT_TINT);
                    }
                    cardImg.setTouchable(Touchable.disabled);
                    cardContent = cardImg;
                } else {
                    String suitText = getActiveBossAbility().areCardColorsHidden() ? "???" : card.getSuit().name();
                    Label fallbackLbl = new Label(card.getRank().getSymbol() + "\n" + suitText, skin);
                    fallbackLbl.setAlignment(Align.center);
                    fallbackLbl.setTouchable(Touchable.disabled);
                    cardContent = fallbackLbl;
                }

                cardBg.add(cardContent).expand().fill();
                cardWrapper.addActor(cardBg);

                cardActorMap.put(card, cardWrapper);
                cardLayer.addActor(cardWrapper);

                if (incomingCards != null && incomingCards.contains(card)) {
                    cardWrapper.setTouchable(Touchable.disabled);
                    cardBg.getColor().a = 0f;
                    cardWrapper.setPosition(Gdx.graphics.getWidth() + 100 * uiScale, -200 * uiScale);
                } else {
                    cardWrapper.setPosition(0, -200 * uiScale);
                }

                cardWrapper.addListener(new ClickListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (isGameOver || isScoring || isTransitioningOut || isPaused) return;
                        if (selectedCards.contains(card)) return;
                        cardBg.clearActions();
                        cardBg.addAction(Actions.moveTo(0, hoverOffset, 0.15f, Interpolation.smooth));
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (isGameOver || isScoring || isTransitioningOut || isPaused) return;
                        if (selectedCards.contains(card)) return;
                        cardBg.clearActions();
                        cardBg.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.smooth));
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isGameOver || isScoring || isTransitioningOut || isPaused) return;
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);

                        if (selectedCards.contains(card)) {
                            selectedCards.remove(card);
                            cardBg.clearActions();
                            cardBg.addAction(Actions.moveTo(0, hoverOffset, 0.2f, Interpolation.exp10Out));
                            cardBg.setBackground(cardBgNormal);
                            saveCurrentCombatState();
                        } else {
                            int maxSelectable = getActiveBossAbility().getMaxSelectableCards();
                            if (selectedCards.size() < maxSelectable) {
                                selectedCards.add(card);
                                cardBg.clearActions();
                                cardBg.addAction(Actions.moveTo(0, selectOffset, 0.2f, Interpolation.exp10Out));
                                cardBg.setBackground(cardBgSelected);
                                saveCurrentCombatState();
                            }
                        }
                    }
                });
            } else if (incomingCards == null || !incomingCards.contains(card)) {
                 baseRepositionTime = 0.35f;
            }
        }

        float totalWidth = hand.size() * cardWidth + (hand.size() - 1) * spacing;
        float startX = (Gdx.graphics.getWidth() - totalWidth) / 2f;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            Group cw = cardActorMap.get(card);
            float currentTargetX = startX + i * (cardWidth + spacing);

            cw.clearActions();
            Actor bg = cw.getChild(0);
            bg.clearActions();

            if (incomingCards != null && incomingCards.contains(card)) {
                float delay = baseRepositionTime + (incomingCards.indexOf(card) * GameConfig.DRAW_CARD_DELAY);
                float animDur = delay + GameConfig.DRAW_CARD_DURATION;
                maxHandAnimTime = Math.max(maxHandAnimTime, animDur);

                bg.getColor().a = 0f;
                bg.setPosition(0, selectedCards.contains(card) ? selectOffset : 0);
                bg.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.fadeIn(0.3f)
                ));
                cw.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.moveTo(currentTargetX, targetY, GameConfig.DRAW_CARD_DURATION, Interpolation.swingOut),
                    Actions.run(() -> cw.setTouchable(Touchable.enabled))
                ));
            } else {
                maxHandAnimTime = Math.max(maxHandAnimTime, 0.35f);
                bg.addAction(Actions.moveTo(0, selectedCards.contains(card) ? selectOffset : 0, 0.2f, Interpolation.smooth));
                cw.addAction(Actions.moveTo(currentTargetX, targetY, 0.35f, Interpolation.smooth));
            }
        }

        drawingTimer = maxHandAnimTime;
    }

    private void triggerShake(float intensity) {
        stage.getRoot().clearActions();
        stage.getRoot().setPosition(0, 0);
        float d = GameConfig.SCREEN_SHAKE_DURATION / 4f;
        stage.getRoot().addAction(Actions.sequence(
                Actions.moveBy(intensity, intensity, d),
                Actions.moveBy(-intensity * 2, -intensity * 2, d),
                Actions.moveBy(intensity * 2, intensity, d),
                Actions.moveTo(0, 0, d)));
    }

    private void playSelectedCards() {
        if (isGameOver || isScoring || selectedCards.isEmpty() || playsLeft <= 0 || isTransitioningOut || isPaused) return;

        playsLeft--;

        scoringCards.clear();
        scoringCards.addAll(selectedCards);
        scoringCardGroups.clear();

        float selectOffset = GameConfig.COMBAT_CARD_SELECT_OFFSET * uiScale;

        for (Card sc : scoringCards) {
            Group cw = cardActorMap.get(sc);
            scoringCardGroups.add(cw);

            com.badlogic.gdx.math.Vector2 pos = cw.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));
            cw.remove();
            stage.addActor(cw);
            cw.setPosition(pos.x, pos.y);
            cardActorMap.remove(sc);

            cw.clearActions();
            Actor bg = cw.getChild(0);
            bg.clearActions();
            bg.addAction(Actions.moveTo(0, selectOffset, 0.2f, Interpolation.smooth));
        }

        hand.removeAll(selectedCards);
        selectedCards.clear();

        BossAbilityType bossAbility = getActiveBossAbility();
        if (bossAbility.triggersRandomDiscardOnPlay() && !hand.isEmpty()) {
            Card forcedDiscard = hand.remove(MathUtils.random(hand.size() - 1));
            Group forcedCw = cardActorMap.remove(forcedDiscard);
            if (forcedCw != null) {
                forcedCw.clearActions();
                forcedCw.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.moveBy(-1000f * uiScale, MathUtils.random(-150f, 150f), GameConfig.FLYOUT_DISCARD_DURATION, Interpolation.pow2In),
                        Actions.rotateBy(MathUtils.random(-GameConfig.FLYOUT_DISCARD_ROTATION, GameConfig.FLYOUT_DISCARD_ROTATION), GameConfig.FLYOUT_DISCARD_DURATION),
                        Actions.fadeOut(0.4f)
                    ),
                    Actions.removeActor()
                ));
            }
        }

        preRolledDoubles.clear();
        preRolledChips.clear();
        int sumChips = 0;
        for (Card sc : scoringCards) {
            boolean doubled = rollDouble(sc);
            int baseChips = bossAbility.modifyCardChips(sc, sc.getChips());
            int chips = doubled ? baseChips * 2 : baseChips;
            RankProgress.getInstance().add(getProgressIndex(sc.getRank()), 5f);
            preRolledDoubles.add(doubled);
            preRolledChips.add(chips);
            sumChips += chips;
        }
        RankProgress.getInstance().save();

        HandEvaluator.Combo playedCombo = HandEvaluator.evaluate(scoringCards);
        double baseMultiplier = playedCombo != null ? playedCombo.getMultiplier() : 1;
        currentMultiplier = bossAbility == BossAbilityType.CAP_MULTIPLIER
                ? Math.min(baseMultiplier, GameConfig.BOSS_MAX_MULTIPLIER_CAP)
                : baseMultiplier;
        SaveManager.getInstance().discoverCombo(playedCombo);
        int projectedHandScore = (int) Math.round(sumChips * currentMultiplier);

        runState.addRunScore(projectedHandScore);
        if (!runState.isBossDebugMode()) {
            SaveManager.getInstance().updateHighScore(runState.getTotalRunScore());
        }

        pendingDrawnCards.clear();
        int needed = getEffectiveHandSize() - hand.size();
        while (pendingDrawnCards.size() < needed && !drawPile.isEmpty()) {
            pendingDrawnCards.add(drawPile.remove(0));
        }

        List<Card> projectedHand = new ArrayList<>(hand);
        projectedHand.addAll(pendingDrawnCards);
        saveProjectedCombatState(projectedTotalScore, playsLeft, discardsLeft, drawPile, projectedHand, Collections.emptyList());

        isScoring = true;
        scoringTimer = 0f;
        scoringCardIndex = 0;
        currentHandPoints = 0;

        updateTopBar();
    }

    private void discardSelectedCards() {
        if (isGameOver || isScoring || selectedCards.isEmpty() || discardsLeft <= 0 || isTransitioningOut || isPaused) return;

        discardsLeft--;

        float maxAnimTime = 0f;
        for (int i = 0; i < selectedCards.size(); i++) {
            Card sc = selectedCards.get(i);
            Group cw = cardActorMap.get(sc);
            cardActorMap.remove(sc);

            cw.clearActions();
            Actor bg = cw.getChild(0);
            bg.clearActions();
            bg.addAction(Actions.moveTo(0, 0, 0.2f, Interpolation.smooth));

            float animDur = i * GameConfig.FLYOUT_CARD_DELAY + GameConfig.FLYOUT_DISCARD_DURATION;
            maxAnimTime = Math.max(maxAnimTime, animDur);

            cw.addAction(Actions.sequence(
                Actions.delay(i * GameConfig.FLYOUT_CARD_DELAY),
                Actions.parallel(
                    Actions.moveBy(-1000f * uiScale, MathUtils.random(-150f, 150f), GameConfig.FLYOUT_DISCARD_DURATION, Interpolation.pow2In),
                    Actions.rotateBy(MathUtils.random(-GameConfig.FLYOUT_DISCARD_ROTATION, GameConfig.FLYOUT_DISCARD_ROTATION), GameConfig.FLYOUT_DISCARD_DURATION),
                    Actions.fadeOut(0.4f)
                ),
                Actions.removeActor()
            ));
        }

        hand.removeAll(selectedCards);
        selectedCards.clear();
        updateTopBar();

        final List<Card> drawnAfterDiscard = new ArrayList<>();
        int needed = getEffectiveHandSize() - hand.size();
        while (drawnAfterDiscard.size() < needed && !drawPile.isEmpty()) {
            drawnAfterDiscard.add(drawPile.remove(0));
        }

        List<Card> projectedHand = new ArrayList<>(hand);
        projectedHand.addAll(drawnAfterDiscard);
        saveProjectedCombatState(targetDisplayScore, playsLeft, discardsLeft, drawPile, projectedHand, Collections.emptyList());

        Actor inputBlocker = new Actor();
        inputBlocker.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        inputBlocker.setTouchable(Touchable.enabled);
        inputBlocker.addAction(Actions.sequence(
            Actions.delay(maxAnimTime),
            Actions.removeActor()
        ));
        stage.addActor(inputBlocker);

        stage.addAction(Actions.sequence(
            Actions.delay(maxAnimTime),
            Actions.run(() -> {
                hand.addAll(drawnAfterDiscard);
                if (deckCountLabel != null) {
                    deckCountLabel.setText(String.valueOf(drawPile.size()));
                }
                updateHandUI(drawnAfterDiscard);
            })
        ));
    }

    private void updateTopBar() {
        playsLabel.setText("Plays: " + playsLeft);
        discardsLabel.setText("Discards: " + discardsLeft);

        float extraWidth = GameConfig.BUTTON_STRIKE_OVERHANG * uiScale;
        float strikeThick = GameConfig.BUTTON_STRIKE_THICKNESS * uiScale;

        if (playsLeft <= 0 && !playStrikeActive && playStrike != null && playBtnLabel != null) {
            playStrikeActive = true;
            playStrike.addAction(Actions.sizeTo(playBtnLabel.getWidth() + extraWidth, strikeThick, 0.3f, Interpolation.smooth));
        } else if (playsLeft > 0 && playStrikeActive && playStrike != null) {
            playStrikeActive = false;
            playStrike.addAction(Actions.sizeTo(0, strikeThick, 0.3f, Interpolation.smooth));
        }

        if (discardsLeft <= 0 && !discardStrikeActive && discardStrike != null && discardBtnLabel != null) {
            discardStrikeActive = true;
            discardStrike.addAction(Actions.sizeTo(discardBtnLabel.getWidth() + extraWidth, strikeThick, 0.3f, Interpolation.smooth));
        } else if (discardsLeft > 0 && discardStrikeActive && discardStrike != null) {
            discardStrikeActive = false;
            discardStrike.addAction(Actions.sizeTo(0, strikeThick, 0.3f, Interpolation.smooth));
        }
    }

    private void showFailedScreen() {
        isGameOver = true;
        if (!runState.isBossDebugMode()) {
            SaveManager.getInstance().updateHighScore(runState.getTotalRunScore());
            SaveManager.getInstance().clearActiveRun();
        }

        if (uiRoot != null) {
            uiRoot.clearActions();
            uiRoot.addAction(Actions.sequence(
                Actions.fadeOut(GameConfig.GAMEOVER_FADE_TIME, Interpolation.fade),
                Actions.run(() -> uiRoot.setVisible(false))
            ));
        }
        cardLayer.addAction(Actions.fadeOut(GameConfig.GAMEOVER_FADE_TIME, Interpolation.fade));

        final Color targetColor1 = GameConfig.GAMEOVER_BG_1;
        final Color targetColor2 = GameConfig.GAMEOVER_BG_2;
        final Color startColor1 = new Color(currentColor1);
        final Color startColor2 = new Color(currentColor2);

        FloatAction lerpAction = new FloatAction(0f, 1f, GameConfig.GAMEOVER_BG_LERP_DURATION, Interpolation.smooth) {
            @Override
            protected void update(float percent) {
                super.update(percent);
                currentColor1.set(startColor1).lerp(targetColor1, percent);
                currentColor2.set(startColor2).lerp(targetColor2, percent);
                if (bgImg != null) {
                    bgImg.setColors(currentColor1, currentColor2);
                }
            }
        };
        stage.addAction(lerpAction);

        final Table failedOverlay = new Table();
        failedOverlay.setFillParent(true);
        failedOverlay.setBackground(whiteTintableBg.tint(new Color(0f, 0f, 0f, GameConfig.GAMEOVER_SCREEN_TINT)));

        Label failedLabel = new Label("GAME OVER", skin);
        failedLabel.setFontScale(uiScale * 4f);
        failedLabel.setColor(GameConfig.GAMEOVER_TEXT_COLOR);

        Label runScoreLabel = new Label("RUN SCORE: " + runState.getTotalRunScore(), skin);
        runScoreLabel.setFontScale(uiScale * 1.4f);
        runScoreLabel.setColor(Color.WHITE);

        Label highScoreLabel = new Label("HIGH SCORE: " + SaveManager.getInstance().getHighScore(), skin);
        highScoreLabel.setFontScale(uiScale * 1.2f);
        highScoreLabel.setColor(GameConfig.RUN_MAP_HIGH_SCORE_COLOR);

        Label menuBtn = createHoverLabel("MAIN MENU", uiScale * 1.5f, false, () -> {
            if (isTransitioningOut) return;
            isTransitioningOut = true;

            failedOverlay.addAction(Actions.fadeOut(0.4f, Interpolation.fade));

            final Color targetMenuC1 = GameConfig.BG_COLOR_1;
            final Color targetMenuC2 = GameConfig.BG_COLOR_2;
            final Color currentC1 = new Color(currentColor1);
            final Color currentC2 = new Color(currentColor2);

            FloatAction backLerp = new FloatAction(0f, 1f, GameConfig.SCREEN_TRANSITION_DURATION, Interpolation.smooth) {
                @Override
                protected void update(float percent) {
                    super.update(percent);
                    currentColor1.set(currentC1).lerp(targetMenuC1, percent);
                    currentColor2.set(currentC2).lerp(targetMenuC2, percent);
                    if (bgImg != null) {
                        bgImg.setColors(currentColor1, currentColor2);
                    }
                }
            };
            stage.addAction(backLerp);

            CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);

            stage.addAction(Actions.sequence(
                Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
                Actions.run(() -> main.switchScreen(new MainMenuScreen(main, true, true)))
            ));
        });

        failedOverlay.add(failedLabel).padBottom(24 * uiScale).row();
        failedOverlay.add(runScoreLabel).padBottom(10 * uiScale).row();
        failedOverlay.add(highScoreLabel).padBottom(45 * uiScale).row();

        if (runState.isBossDebugMode()) {
            Label debugArenaBtn = createHoverLabel("DEBUG ARENA", uiScale * 1.5f, false, () -> {
                if (isTransitioningOut) return;
                isTransitioningOut = true;
                failedOverlay.addAction(Actions.fadeOut(0.4f, Interpolation.fade));
                CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);
                stage.addAction(Actions.sequence(
                    Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
                    Actions.run(() -> main.switchScreen(new RunMapScreen(main, runState, true)))
                ));
            });
            failedOverlay.add(debugArenaBtn).padBottom(20 * uiScale).center().row();
        }

        failedOverlay.add(menuBtn).center();

        stage.addActor(failedOverlay);
        failedOverlay.getColor().a = 0f;
        failedOverlay.addAction(Actions.fadeIn(0.5f, Interpolation.fade));
    }

    private void showRunVictoryScreen() {
        isGameOver = true;
        runState.addMoney(GameConfig.REWARD_MONEY * 2);
        runState.registerVictory();
        if (!runState.isBossDebugMode()) {
            SaveManager.getInstance().updateHighScore(runState.getTotalRunScore());
            SaveManager.getInstance().clearActiveRun();
        }

        if (uiRoot != null) {
            uiRoot.clearActions();
            uiRoot.addAction(Actions.sequence(
                Actions.fadeOut(GameConfig.GAMEOVER_FADE_TIME, Interpolation.fade),
                Actions.run(() -> uiRoot.setVisible(false))
            ));
        }
        cardLayer.addAction(Actions.fadeOut(GameConfig.GAMEOVER_FADE_TIME, Interpolation.fade));

        final Color targetColor1 = GameConfig.RUN_VICTORY_BG_1;
        final Color targetColor2 = GameConfig.RUN_VICTORY_BG_2;
        final Color startColor1 = new Color(currentColor1);
        final Color startColor2 = new Color(currentColor2);

        FloatAction lerpAction = new FloatAction(0f, 1f, GameConfig.GAMEOVER_BG_LERP_DURATION, Interpolation.smooth) {
            @Override
            protected void update(float percent) {
                super.update(percent);
                currentColor1.set(startColor1).lerp(targetColor1, percent);
                currentColor2.set(startColor2).lerp(targetColor2, percent);
                if (bgImg != null) {
                    bgImg.setColors(currentColor1, currentColor2);
                }
            }
        };
        stage.addAction(lerpAction);

        final Table victoryOverlay = new Table();
        victoryOverlay.setFillParent(true);
        victoryOverlay.setBackground(whiteTintableBg.tint(new Color(0f, 0f, 0f, GameConfig.GAMEOVER_SCREEN_TINT)));

        Label victoryLabel = new Label("VICTORY", skin);
        victoryLabel.setFontScale(uiScale * 4f);
        victoryLabel.setColor(GameConfig.RUN_VICTORY_TEXT_COLOR);

        Label chapterClearedLabel = new Label("ALL " + GameConfig.MAX_CHAPTERS + " CHAPTERS COMPLETED", skin);
        chapterClearedLabel.setFontScale(uiScale * 1.3f);
        chapterClearedLabel.setColor(GameConfig.RUN_MAP_SUBHEADER_COLOR);

        Label runScoreLabel = new Label("FINAL RUN SCORE: " + runState.getTotalRunScore(), skin);
        runScoreLabel.setFontScale(uiScale * 1.5f);
        runScoreLabel.setColor(Color.WHITE);

        Label highScoreLabel = new Label("HIGH SCORE: " + SaveManager.getInstance().getHighScore(), skin);
        highScoreLabel.setFontScale(uiScale * 1.25f);
        highScoreLabel.setColor(GameConfig.RUN_MAP_HIGH_SCORE_COLOR);

        Label menuBtn = createHoverLabel("MAIN MENU", uiScale * 1.5f, false, () -> {
            if (isTransitioningOut) return;
            isTransitioningOut = true;

            victoryOverlay.addAction(Actions.fadeOut(0.4f, Interpolation.fade));

            final Color targetMenuC1 = GameConfig.BG_COLOR_1;
            final Color targetMenuC2 = GameConfig.BG_COLOR_2;
            final Color currentC1 = new Color(currentColor1);
            final Color currentC2 = new Color(currentColor2);

            FloatAction backLerp = new FloatAction(0f, 1f, GameConfig.SCREEN_TRANSITION_DURATION, Interpolation.smooth) {
                @Override
                protected void update(float percent) {
                    super.update(percent);
                    currentColor1.set(currentC1).lerp(targetMenuC1, percent);
                    currentColor2.set(currentC2).lerp(targetMenuC2, percent);
                    if (bgImg != null) {
                        bgImg.setColors(currentColor1, currentColor2);
                    }
                }
            };
            stage.addAction(backLerp);

            CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);

            stage.addAction(Actions.sequence(
                Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
                Actions.run(() -> main.switchScreen(new MainMenuScreen(main, true, true)))
            ));
        });

        victoryOverlay.add(victoryLabel).padBottom(16 * uiScale).row();
        victoryOverlay.add(chapterClearedLabel).padBottom(20 * uiScale).row();
        victoryOverlay.add(runScoreLabel).padBottom(10 * uiScale).row();
        victoryOverlay.add(highScoreLabel).padBottom(55 * uiScale).row();
        victoryOverlay.add(menuBtn).center();

        stage.addActor(victoryOverlay);
        victoryOverlay.getColor().a = 0f;
        victoryOverlay.addAction(Actions.fadeIn(0.5f, Interpolation.fade));
    }

    private void executeVictoryTransition() {
        if (!runState.isBossDebugMode() && enemy.isBoss() && runState.getChapterIndex() >= runState.getMaxChapters()) {
            showRunVictoryScreen();
            return;
        }

        isTransitioningOut = true;

        uiRoot.addAction(Actions.fadeOut(GameConfig.VICTORY_UI_FADE_DURATION, Interpolation.fade));
        cardLayer.addAction(Actions.fadeOut(GameConfig.VICTORY_UI_FADE_DURATION, Interpolation.fade));

        final Color targetColor1 = GameConfig.BG_COLOR_1;
        final Color targetColor2 = GameConfig.BG_COLOR_2;
        final Color startColor1 = new Color(currentColor1);
        final Color startColor2 = new Color(currentColor2);

        FloatAction lerpAction = new FloatAction(0f, 1f, GameConfig.VICTORY_BG_LERP_DURATION, Interpolation.smooth) {
            @Override
            protected void update(float percent) {
                super.update(percent);
                currentColor1.set(startColor1).lerp(targetColor1, percent);
                currentColor2.set(startColor2).lerp(targetColor2, percent);
                if (bgImg != null) {
                    bgImg.setColors(currentColor1, currentColor2);
                }
            }
        };

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.VICTORY_BG_LERP_DELAY),
            lerpAction,
            Actions.run(() -> {
                runState.addMoney(enemy.isBoss() ? GameConfig.REWARD_MONEY * 2 : GameConfig.REWARD_MONEY);
                runState.registerVictory();
                SaveManager.getInstance().saveRunMapState(runState);
                main.switchScreen(new RunMapScreen(main, runState));
            })
        ));
    }

    @Override
    public void setPaused(boolean paused) {
        super.setPaused(paused);
        if (uiRoot != null) {
            uiRoot.setTouchable(paused ? Touchable.disabled : Touchable.childrenOnly);
        }
        if (cardLayer != null) {
            cardLayer.setTouchable(paused ? Touchable.disabled : Touchable.childrenOnly);
        }
    }

    public void exitToMainMenu() {
        if (isTransitioningOut) return;
        isTransitioningOut = true;
        saveCurrentCombatState();
        setPaused(false);

        if (uiRoot != null) {
            uiRoot.setTouchable(Touchable.disabled);
            uiRoot.clearActions();
            uiRoot.addAction(Actions.fadeOut(0.4f, Interpolation.fade));
        }
        if (cardLayer != null) {
            cardLayer.setTouchable(Touchable.disabled);
            cardLayer.clearActions();
            cardLayer.addAction(Actions.fadeOut(0.4f, Interpolation.fade));
        }

        final Color targetMenuC1 = GameConfig.BG_COLOR_1;
        final Color targetMenuC2 = GameConfig.BG_COLOR_2;
        final Color currentC1 = new Color(currentColor1);
        final Color currentC2 = new Color(currentColor2);

        FloatAction backLerp = new FloatAction(0f, 1f, GameConfig.SCREEN_TRANSITION_DURATION, Interpolation.smooth) {
            @Override
            protected void update(float percent) {
                super.update(percent);
                currentColor1.set(currentC1).lerp(targetMenuC1, percent);
                currentColor2.set(currentC2).lerp(targetMenuC2, percent);
                if (bgImg != null) {
                    bgImg.setColors(currentColor1, currentColor2);
                }
            }
        };
        stage.addAction(backLerp);

        CardTransition.applyTransition(stage, cardRegions, whiteTintableBg, false);

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.run(() -> main.switchScreen(new MainMenuScreen(main, true, true)))
        ));
    }

    @Override
    protected void onEscapePressed() {
        if (isPaused) {
            pauseMenu.resume();
        } else {
            saveCurrentCombatState();
            setPaused(true);
            pauseMenu.show();
        }
    }

    /** A kijelölt (pontozás közben a kijátszott) lapokból meghatározott kombináció kiírása. */
    private void updateComboLabel() {
        if (comboLabel == null) return;
        List<Card> source = isScoring ? scoringCards : selectedCards;
        HandEvaluator.Combo combo = HandEvaluator.evaluate(source);
        String text;
        if (combo == null) {
            text = "";
        } else if (!SaveManager.getInstance().isComboDiscovered(combo)) {
            text = "????";
        } else {
            text = combo.getDisplayName() + "  x" + formatMult(combo.getMultiplier());
        }
        if (!text.equals(lastComboText)) {
            lastComboText = text;
            comboLabel.setText(text);
        }
    }

    private static String formatMult(double m) {
        return (m == Math.floor(m)) ? String.valueOf((int) m) : String.valueOf(m);
    }

    @Override
    public void render(float delta) {
        updateComboLabel();
        if (!isGameOver && !isScoring && !isTransitioningOut && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onEscapePressed();
            return;
        }

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!isPaused) {
            stage.act(delta);
        }
        stage.draw();
        pauseMenu.render(delta);

        if (isPaused) {
            return;
        }

        if (drawingTimer > 0) {
            drawingTimer -= delta;
        }

        if (isScoring) {
            scoringTimer += delta;
            if (scoringTimer >= GameConfig.SCORING_STEP_DELAY) {
                scoringTimer = 0f;

                if (scoringCardIndex < scoringCards.size()) {
                    boolean doubled = scoringCardIndex < preRolledDoubles.size()
                        && Boolean.TRUE.equals(preRolledDoubles.get(scoringCardIndex));
                    Integer rolledChips = scoringCardIndex < preRolledChips.size()
                        ? preRolledChips.get(scoringCardIndex)
                        : null;
                    int chips = rolledChips != null
                        ? rolledChips
                        : scoringCards.get(scoringCardIndex).getChips();

                    currentHandPoints += chips;
                    targetDisplayScore += chips;

                    triggerShake(doubled
                            ? GameConfig.SCREEN_SHAKE_INTENSITY * uiScale
                            : (GameConfig.SCREEN_SHAKE_INTENSITY / 2f) * uiScale);

                    Label popLabel = new Label((doubled ? "x2! +" : "+") + chips, skin);
                    popLabel.setFontScale(uiScale * 3f);
                    popLabel.setColor(doubled ? Color.valueOf(GameConfig.DOUBLE_SCORING_NUMBER_COLOR) : Color.valueOf(GameConfig.SCORING_NUMBER_COLOR));
                    popLabel.setAlignment(Align.center);

                    if (scoringCardIndex < scoringCardGroups.size()) {
                        Group cardGroup = scoringCardGroups.get(scoringCardIndex);
                        popLabel.pack();

                        com.badlogic.gdx.math.Vector2 pos = new com.badlogic.gdx.math.Vector2(
                                cardGroup.getWidth() / 2f - popLabel.getWidth() / 2f, cardGroup.getHeight() + 30f * uiScale);
                        cardGroup.localToStageCoordinates(pos);

                        popLabel.setPosition(pos.x, pos.y);
                        stage.addActor(popLabel);

                        popLabel.addAction(Actions.sequence(
                                Actions.parallel(
                                        Actions.moveBy(0, 100 * uiScale, 0.7f, Interpolation.circleOut),
                                        Actions.fadeOut(0.7f, Interpolation.fade)),
                                Actions.removeActor()));
                    }

                    scoringCardIndex++;
                } else if (!isAnimatingFlyout) {
                    isAnimatingFlyout = true;

                    int finalHandScore = (int) Math.round(currentHandPoints * currentMultiplier);
                    targetDisplayScore += (finalHandScore - currentHandPoints);

                    triggerShake(GameConfig.SCREEN_SHAKE_INTENSITY * 1.5f * uiScale);

                    float maxAnimTime = 0f;
                    for (int i = 0; i < scoringCardGroups.size(); i++) {
                        Group cardGroup = scoringCardGroups.get(i);
                        cardGroup.clearActions();
                        float animDur = i * GameConfig.FLYOUT_CARD_DELAY + GameConfig.FLYOUT_PLAY_DURATION;
                        maxAnimTime = Math.max(maxAnimTime, animDur);
                        cardGroup.addAction(Actions.sequence(
                            Actions.delay(i * GameConfig.FLYOUT_CARD_DELAY),
                            Actions.parallel(
                                Actions.moveBy(-800f * uiScale, 400f * uiScale, GameConfig.FLYOUT_PLAY_DURATION, Interpolation.pow2In),
                                Actions.rotateBy(MathUtils.random(-GameConfig.FLYOUT_PLAY_ROTATION, GameConfig.FLYOUT_PLAY_ROTATION), GameConfig.FLYOUT_PLAY_DURATION),
                                Actions.fadeOut(0.5f)
                            ),
                            Actions.removeActor()
                        ));
                    }
                    scoringCardGroups.clear();

                    final List<Card> drawnAfterPlay = new ArrayList<>(pendingDrawnCards);
                    pendingDrawnCards.clear();

                    stage.addAction(Actions.sequence(
                        Actions.delay(maxAnimTime),
                        Actions.run(() -> {
                            isAnimatingFlyout = false;
                            isScoring = false;
                            hand.addAll(drawnAfterPlay);
                            if (deckCountLabel != null) {
                                deckCountLabel.setText(String.valueOf(drawPile.size()));
                            }
                            updateHandUI(drawnAfterPlay);
                        })
                    ));
                }
            }
        }

        if (currentScore < targetDisplayScore) {
            int diff = targetDisplayScore - currentScore;
            int increment = Math.max(1, (int) (diff * 15 * delta));
            currentScore += increment;
            if (currentScore > targetDisplayScore) {
                currentScore = targetDisplayScore;
            }
            scoreLabel.setText(String.valueOf(currentScore));
        }

        boolean isDrawing = drawingTimer > 0;
        if (!isScoring && !isDrawing && currentScore == targetDisplayScore && !isGameOver && !isTransitioningOut) {
            if (currentScore >= enemy.getTargetScore()) {
                executeVictoryTransition();
            } else if (playsLeft <= 0) {
                showFailedScreen();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (pauseMenu != null) {
            pauseMenu.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (pauseMenu != null) {
            pauseMenu.dispose();
        }
        if (deckTexture != null) {
            deckTexture.dispose();
        }
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
    }
}
