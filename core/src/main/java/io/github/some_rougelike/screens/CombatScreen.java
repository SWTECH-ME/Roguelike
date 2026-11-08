package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Suit;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.ui.TrippyBackground;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_rougelike.model.RankProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private Map<Card, Group> cardActorMap = new HashMap<>();

    private Label scoreLabel;
    private Label targetLabel;
    private Label playsLabel;
    private Label discardsLabel;
    private Label deckCountLabel;
    
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
    private int currentMultiplier = 1;
    private List<Card> scoringCards = new ArrayList<>();
    private List<Group> scoringCardGroups = new ArrayList<>();

    private float uiScale;
    private TrippyBackground bgImg;
    private Color currentColor1;
    private Color currentColor2;
    private boolean isTransitioningOut = false;

    private Image playStrike;
    private Image discardStrike;
    private Label playBtnLabel;
    private Label discardBtnLabel;
    private boolean playStrikeActive = false;
    private boolean discardStrikeActive = false;

    private interface Condition {
        boolean isTrue();
    }

    public CombatScreen(Main main, RunState runState) {
        super(main, "");
        this.runState = runState;
        this.enemy = runState.getCurrentEnemy();
        this.playsLeft = enemy.getMaxPlays();
        this.discardsLeft = enemy.getMaxDiscards();
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

        this.drawPile = new ArrayList<>(runState.getDeck());
        Collections.shuffle(this.drawPile);

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

        assignTextureRegionsToDeck();
        buildUI();
    }

    private void loadDeckTexture() {
        try {
            FileHandle file = Gdx.files.internal("deck_skins/white_black.png");
            if (file.exists()) {
                deckTexture = new Texture(file);
                int cols = 13;
                int rows = 5;
                int tileWidth = deckTexture.getWidth() / cols;
                int tileHeight = deckTexture.getHeight() / rows;
                cardRegions = TextureRegion.split(deckTexture, tileWidth, tileHeight);
            }
        } catch (Exception e) {
            Gdx.app.error("CombatScreen", e.getMessage());
        }
    }

    private void assignTextureRegionsToDeck() {
        if (cardRegions == null) return;
        for (Card card : runState.getDeck()) {
            int row = getRowForSuit(card.getSuit());
            int col = getColForRank(card.getRank());
            if (row >= 0 && row < cardRegions.length && col >= 0 && col < cardRegions[row].length) {
                card.setTextureRegion(cardRegions[row][col]);
            }
        }
    }

    private int getRowForSuit(Suit suit) {
        switch (suit) {
            case CLUBS: return 0;
            case HEARTS: return 1;
            case SPADES: return 2;
            case DIAMONDS: return 3;
            default: return 0;
        }
    }

    private int getColForRank(Rank rank) {
        switch (rank) {
            case ACE: return 0;
            case TWO: return 1;
            case THREE: return 2;
            case FOUR: return 3;
            case FIVE: return 4;
            case SIX: return 5;
            case SEVEN: return 6;
            case EIGHT: return 7;
            case NINE: return 8;
            case TEN: return 9;
            case JACK: return 10;
            case QUEEN: return 11;
            case KING: return 12;
            default: return 0;
        }
    }

    private int getProgressIndex(Rank rank) {
        return (getColForRank(rank) + 12) % 13;
    }

    private boolean rollDouble(Card card) {
        float chance = RankProgress.getInstance().get(getProgressIndex(card.getRank()));
        return MathUtils.random(100f) < chance;
    }

    private List<Card> drawUpToMax() {
        List<Card> drawn = new ArrayList<>();
        while (hand.size() < GameConfig.STARTING_HAND_SIZE && !drawPile.isEmpty()) {
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

    private void buildUI() {
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
            middleTable.add(abilityLabel).padTop(10 * uiScale);
        }

        Table bottomSection = new Table();
        Table actionTable = new Table();

        Group playBtn = createActionButton("PLAY HAND", uiScale * 1.3f, true, () -> playSelectedCards(), () -> playsLeft <= 0, true);
        Group discardBtn = createActionButton("DISCARD", uiScale * 1.3f, true, () -> discardSelectedCards(), () -> discardsLeft <= 0, false);

        actionTable.add(playBtn).padRight(60 * uiScale);
        actionTable.add(discardBtn);

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

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        
        cardLayer = new Group();
        cardLayer.setSize(width, height);
        cardLayer.setTouchable(Touchable.childrenOnly);
        stage.addActor(cardLayer);

        Actor inputBlocker = new Actor();
        inputBlocker.setSize(width, height);
        inputBlocker.setTouchable(Touchable.enabled);
        inputBlocker.addAction(Actions.sequence(
            Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
            Actions.removeActor()
        ));
        stage.addActor(inputBlocker);

        TextureRegionDrawable fallbackBg = createRoundedRect((int)(100*uiScale), (int)(140*uiScale), GameConfig.COMBAT_CARD_BG_NORMAL, (int)(10*uiScale));
        java.util.Random rng = new java.util.Random(888);
        int cardCount = GameConfig.TRANSITION_CARD_COUNT;
        
        for (int i = 0; i < cardCount; i++) {
            int r = rng.nextInt(4);
            int c = rng.nextInt(13);
            float finalScale = GameConfig.TRANSITION_CARD_MIN_SCALE + rng.nextFloat() * GameConfig.TRANSITION_CARD_SCALE_VAR;
            float startX = -250f + rng.nextFloat() * (width + 500f);
            float startY = -250f + rng.nextFloat() * (height + 500f);
            float startRot = rng.nextFloat() * 360f;
            
            Image cardImg;
            if (cardRegions != null) {
                cardImg = new Image(cardRegions[r][c]);
            } else {
                cardImg = new Image(fallbackBg);
            }
            
            cardImg.setSize(GameConfig.COMBAT_CARD_WIDTH * uiScale * finalScale, GameConfig.COMBAT_CARD_HEIGHT * uiScale * finalScale);
            cardImg.setOrigin(Align.center);
            cardImg.setPosition(startX, startY);
            cardImg.setRotation(startRot);
            
            float targetX = startX + (rng.nextFloat() * 800f - 400f);
            float targetY = -600f - rng.nextFloat() * 1000f;
            
            float progress = (float) i / cardCount;
            float delay = (1f - progress) * (GameConfig.SCREEN_TRANSITION_DURATION * 0.5f);
            float duration = GameConfig.SCREEN_TRANSITION_DURATION * 0.5f;
            
            cardImg.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(targetX, targetY, duration, Interpolation.pow2In),
                    Actions.rotateBy(rng.nextFloat() * 360f - 180f, duration, Interpolation.pow2In)
                ),
                Actions.removeActor()
            ));
            
            stage.addActor(cardImg);
        }

        stage.addAction(Actions.sequence(
            Actions.delay(GameConfig.DRAW_INITIAL_DELAY),
            Actions.run(() -> {
                List<Card> initialCards = drawUpToMax();
                updateHandUI(initialCards);
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
                if (pointer == -1 && (!checkBusy || (!isGameOver && !isScoring))) {
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
                if (pointer == -1) {
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
                if (!checkBusy || (!isGameOver && !isScoring)) {
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
                if (pointer == -1 && (!checkBusy || (!isGameOver && !isScoring))) {
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
                        Actions.color(GameConfig.BUTTON_TEXT_COLOR, 0.15f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.15f, Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!checkBusy || (!isGameOver && !isScoring)) {
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
                cardBg.setBackground(cardBgNormal);
                cardBg.setTouchable(Touchable.disabled);

                Actor cardContent;
                if (card.getTextureRegion() != null) {
                    Image cardImg = new Image(card.getTextureRegion());
                    cardImg.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
                    cardImg.setTouchable(Touchable.disabled);
                    cardContent = cardImg;
                } else {
                    Label fallbackLbl = new Label(card.getRank().getSymbol() + "\n" + card.getSuit().name(), skin);
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
                        if (isGameOver || isScoring || isTransitioningOut) return;
                        if (selectedCards.contains(card)) return;
                        cardBg.clearActions();
                        cardBg.addAction(Actions.moveTo(0, hoverOffset, 0.15f, Interpolation.smooth));
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (isGameOver || isScoring || isTransitioningOut) return;
                        if (selectedCards.contains(card)) return;
                        cardBg.clearActions();
                        cardBg.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.smooth));
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isGameOver || isScoring || isTransitioningOut) return;
                        SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                        
                        if (selectedCards.contains(card)) {
                            selectedCards.remove(card);
                            cardBg.clearActions();
                            cardBg.addAction(Actions.moveTo(0, hoverOffset, 0.2f, Interpolation.exp10Out));
                            cardBg.setBackground(cardBgNormal);
                        } else {
                            if (selectedCards.size() < 5) {
                                selectedCards.add(card);
                                cardBg.clearActions();
                                cardBg.addAction(Actions.moveTo(0, selectOffset, 0.2f, Interpolation.exp10Out));
                                cardBg.setBackground(cardBgSelected);
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
        if (isGameOver || isScoring || selectedCards.isEmpty() || playsLeft <= 0 || isTransitioningOut) return;

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

        isScoring = true;
        scoringTimer = 0f;
        scoringCardIndex = 0;
        currentHandPoints = 0;
        currentMultiplier = scoringCards.size();

        updateTopBar();
    }

    private void discardSelectedCards() {
        if (isGameOver || isScoring || selectedCards.isEmpty() || discardsLeft <= 0 || isTransitioningOut) return;

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
                List<Card> newCards = drawUpToMax();
                updateHandUI(newCards);
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

        Label menuBtn = createHoverLabel("MAIN MENU", uiScale * 1.5f, false, () -> {
            if (isTransitioningOut) return;
            isTransitioningOut = true;
            
            failedOverlay.addAction(Actions.fadeOut(0.4f, Interpolation.fade));
            
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();
            
            java.util.Random rng = new java.util.Random(888);
            int cardCount = GameConfig.TRANSITION_CARD_COUNT;
            
            for (int i = 0; i < cardCount; i++) {
                int r = rng.nextInt(4);
                int c = rng.nextInt(13);
                float finalScale = GameConfig.TRANSITION_CARD_MIN_SCALE + rng.nextFloat() * GameConfig.TRANSITION_CARD_SCALE_VAR;
                float targetX = -250f + rng.nextFloat() * (w + 500f);
                float targetY = -250f + rng.nextFloat() * (h + 500f);
                float targetRot = rng.nextFloat() * 360f;
                
                Image cardImg;
                if (cardRegions != null) {
                    cardImg = new Image(cardRegions[r][c]);
                } else {
                    cardImg = new Image(whiteTintableBg);
                    cardImg.setColor(GameConfig.COMBAT_CARD_BG_NORMAL);
                }
                
                cardImg.setSize(GameConfig.COMBAT_CARD_WIDTH * uiScale * finalScale, GameConfig.COMBAT_CARD_HEIGHT * uiScale * finalScale);
                cardImg.setOrigin(Align.center);
                
                float startX = targetX + (rng.nextFloat() * 800f - 400f);
                float startY = h + 400f + rng.nextFloat() * 1000f;
                float startRot = targetRot + (rng.nextFloat() * 360f - 180f);
                
                cardImg.setPosition(startX, startY);
                cardImg.setRotation(startRot);
                
                float progress = (float) i / cardCount;
                float delay = (float) Math.pow(progress, 3.5) * (GameConfig.SCREEN_TRANSITION_DURATION * 0.5f);
                float duration = GameConfig.SCREEN_TRANSITION_DURATION * 0.5f;
                
                cardImg.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        Actions.moveTo(targetX, targetY, duration, Interpolation.exp10Out),
                        Actions.rotateTo(targetRot, duration, Interpolation.exp10Out)
                    )
                ));
                
                stage.addActor(cardImg);
            }
            
            stage.addAction(Actions.sequence(
                Actions.delay(GameConfig.SCREEN_TRANSITION_DURATION),
                Actions.run(() -> main.switchScreen(new MainMenuScreen(main)))
            ));
        });

        failedOverlay.add(failedLabel).padBottom(80 * uiScale).row();
        failedOverlay.add(menuBtn).center();

        stage.addActor(failedOverlay);
        failedOverlay.getColor().a = 0f;
        failedOverlay.addAction(Actions.fadeIn(0.5f, Interpolation.fade));
    }

    private void executeVictoryTransition() {
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
                runState.addMoney(GameConfig.REWARD_MONEY);
                runState.registerVictory();
                main.switchScreen(new RunMapScreen(main, runState));
            })
        ));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (drawingTimer > 0) {
            drawingTimer -= delta;
        }

        if (isScoring) {
            scoringTimer += delta;
            if (scoringTimer >= GameConfig.SCORING_STEP_DELAY) {
                scoringTimer = 0f;

                if (scoringCardIndex < scoringCards.size()) {
                    Card c = scoringCards.get(scoringCardIndex);
                    boolean doubled = rollDouble(c);
                    int chips = doubled ? c.getChips() * 2 : c.getChips();

                    RankProgress.getInstance().add(getProgressIndex(c.getRank()), 5f);

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
                    
                    int finalHandScore = currentHandPoints * currentMultiplier;
                    targetDisplayScore += (finalHandScore - currentHandPoints);
                    RankProgress.getInstance().save();

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

                    stage.addAction(Actions.sequence(
                        Actions.delay(maxAnimTime),
                        Actions.run(() -> {
                            isAnimatingFlyout = false;
                            isScoring = false;
                            List<Card> newCards = drawUpToMax();
                            updateHandUI(newCards);
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
    public void dispose() {
        super.dispose();
        if (deckTexture != null) {
            deckTexture.dispose();
        }
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
    }
}