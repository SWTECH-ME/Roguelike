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
import java.util.List;

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

    private Label scoreLabel;
    private Label targetLabel;
    private Label playsLabel;
    private Label discardsLabel;
    private Label deckCountLabel;
    private Table handTable;
    private Skin skin;
    private boolean isGameOver = false;

    private Texture deckTexture;
    private TextureRegion[][] cardRegions;
    private Array<Texture> generatedTextures;
    private TextureRegionDrawable whiteTintableBg;

    private TextureRegionDrawable cardBgNormal;
    private TextureRegionDrawable cardBgSelected;

    private boolean isScoring = false;
    private float scoringTimer = 0f;
    private int scoringCardIndex = 0;
    private int currentHandPoints = 0;
    private int currentMultiplier = 1;
    private List<Card> scoringCards = new ArrayList<>();
    private List<Group> scoringCardGroups = new ArrayList<>();

    private float uiScale;

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

        Color c1 = Color.valueOf("4C9A7F");
        Color c2 = Color.valueOf("2D6A54");
        if (enemy.isBoss()) {
            c1 = new Color(0.4f, 0.1f, 0.15f, 1f);
            c2 = new Color(0.25f, 0.05f, 0.1f, 1f);
        }

        float overscan = 150f;
        TrippyBackground bgImg = new TrippyBackground(c1, c2);
        bgImg.setSize(width + (overscan * 2), height + (overscan * 2));
        bgImg.setPosition(-overscan, -overscan);
        bgImg.setTouchable(Touchable.disabled);
        stage.addActor(bgImg);

        assignTextureRegionsToDeck();
        drawInitialHand();
        buildUI();
    }

    private void loadDeckTexture() {
        try {
            FileHandle file = Gdx.files.internal("deck_skins/black_blue.png");
            if (file.exists()) {
                deckTexture = new Texture(file);
                int cols = 13;
                int rows = 5;
                int tileWidth = deckTexture.getWidth() / cols;
                int tileHeight = deckTexture.getHeight() / rows;
                cardRegions = TextureRegion.split(deckTexture, tileWidth, tileHeight);
            }
        } catch (Exception e) {
            Gdx.app.error("CombatScreen", "Nem sikerült betölteni a deck skint: " + e.getMessage());
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

    private void drawInitialHand() {
        for (int i = 0; i < GameConfig.STARTING_HAND_SIZE; i++) {
            if (!drawPile.isEmpty()) {
                hand.add(drawPile.remove(0));
            }
        }
    }

    private TextureRegionDrawable createRoundedRect(int width, int height, float r, float g, float b, float a, int radius) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(r, g, b, a);
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
        Table root = new Table();
        root.setFillParent(true);

        float cardWidth = 100 * uiScale;
        float cardHeight = 140 * uiScale;
        int cornerRadius = (int)(10 * uiScale);
        
        cardBgNormal = createRoundedRect((int)cardWidth, (int)cardHeight, 0.95f, 0.95f, 0.98f, 1f, cornerRadius);
        cardBgSelected = createRoundedRect((int)cardWidth, (int)cardHeight, 0.8f, 0.9f, 1.0f, 1f, cornerRadius);

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

        handTable = new Table();
        updateHandUI();

        Table actionTable = new Table();

        Label playBtn = createHoverLabel("PLAY HAND", uiScale * 1.3f, () -> playSelectedCards());
        Label discardBtn = createHoverLabel("DISCARD", uiScale * 1.3f, () -> discardSelectedCards());

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

        Table handAndActions = new Table();
        handAndActions.add(handTable).padBottom(40 * uiScale).row();
        handAndActions.add(actionTable);

        bottomSection.add().uniformX().expandX();
        bottomSection.add(handAndActions).center();
        
        Table deckWrapper = new Table();
        deckWrapper.add(deckTable).expand().right().bottom().padRight(60 * uiScale).padBottom(50 * uiScale);
        bottomSection.add(deckWrapper).uniformX().expandX().fill();

        root.add(topArea).fillX().top().row();
        root.add(middleTable).expand().center().row();
        root.add(bottomSection).fillX().padBottom(40 * uiScale).bottom();

        stage.addActor(root);
    }

    private Label createHoverLabel(String text, float scale, Runnable onClick) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        label.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        label.setTouchable(Touchable.enabled);
        label.setOrigin(Align.center);
        label.setAlignment(Align.center);

        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isGameOver && !isScoring) {
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
                if (!isGameOver && !isScoring) {
                    SoundManager.getInstance().play(SoundManager.SoundEvent.BUTTON_CLICK);
                    
                    label.clearActions();
                    label.addAction(Actions.parallel(
                        Actions.color(new Color(0.65f, 0.65f, 0.65f, 1f), 0.1f, Interpolation.fade),
                        Actions.scaleTo(scale, scale, 0.1f, Interpolation.fade)
                    ));
                    
                    if (onClick != null) onClick.run();
                }
            }
        });

        return label;
    }

    private void updateHandUI() {
        handTable.clear();
        selectedCards.clear();
        scoringCardGroups.clear();

        float cardWidth = 100 * uiScale;
        float cardHeight = 140 * uiScale; 
        float hoverOffset = 15 * uiScale;
        float selectOffset = 30 * uiScale;

        for (Card card : hand) {
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

            cardWrapper.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (isGameOver || isScoring) return;
                    boolean isSelected = selectedCards.contains(card);
                    cardBg.clearActions();
                    cardBg.addAction(Actions.moveTo(0, isSelected ? selectOffset + hoverOffset : hoverOffset, 0.15f, Interpolation.smooth));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (isGameOver || isScoring) return;
                    boolean isSelected = selectedCards.contains(card);
                    cardBg.clearActions();
                    cardBg.addAction(Actions.moveTo(0, isSelected ? selectOffset : 0, 0.15f, Interpolation.smooth));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isGameOver || isScoring) return;
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
                            cardBg.addAction(Actions.moveTo(0, selectOffset + hoverOffset, 0.2f, Interpolation.exp10Out));
                            cardBg.setBackground(cardBgSelected);
                        }
                    }
                }
            });

            handTable.add(cardWrapper).size(cardWidth, cardHeight).padLeft(4 * uiScale).padRight(4 * uiScale).align(Align.bottom);
            scoringCardGroups.add(cardWrapper);
        }
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
        if (isGameOver || isScoring || selectedCards.isEmpty() || playsLeft <= 0) return;

        playsLeft--;

        scoringCards.clear();
        scoringCards.addAll(selectedCards);

        scoringCardGroups.clear();
        for (Card sc : scoringCards) {
            int index = hand.indexOf(sc);
            if (index >= 0 && index < handTable.getChildren().size) {
                scoringCardGroups.add((Group) handTable.getChildren().get(index));
            }
        }

        hand.removeAll(selectedCards);

        isScoring = true;
        scoringTimer = 0f;
        scoringCardIndex = 0;
        currentHandPoints = 0;
        currentMultiplier = scoringCards.size();

        updateTopBar();
    }

    private void discardSelectedCards() {
        if (isGameOver || isScoring || selectedCards.isEmpty() || discardsLeft <= 0) return;

        discardsLeft--;
        hand.removeAll(selectedCards);
        drawUpToMax();

        updateTopBar();
        updateHandUI();
    }

    private void drawUpToMax() {
        while (hand.size() < GameConfig.STARTING_HAND_SIZE && !drawPile.isEmpty()) {
            hand.add(drawPile.remove(0));
        }
        if (deckCountLabel != null) {
            deckCountLabel.setText(String.valueOf(drawPile.size()));
        }
    }

    private void updateTopBar() {
        playsLabel.setText("Plays: " + playsLeft);
        discardsLabel.setText("Discards: " + discardsLeft);
    }

    private void showFailedScreen() {
        isGameOver = true;
        Table failedOverlay = new Table();
        failedOverlay.setFillParent(true);
        failedOverlay.setBackground(whiteTintableBg.tint(new Color(0f, 0f, 0f, 0.9f)));

        Label failedLabel = new Label("GAME OVER", skin);
        failedLabel.setFontScale(uiScale * 4f);
        failedLabel.setColor(new Color(0.9f, 0.2f, 0.2f, 1f));

        Label menuBtn = createHoverLabel("MAIN MENU", uiScale * 1.5f, () -> {
            main.switchScreen(new MainMenuScreen(main));
        });

        failedOverlay.add(failedLabel).padBottom(80 * uiScale).row();
        failedOverlay.add(menuBtn).center();

        stage.addActor(failedOverlay);
        failedOverlay.getColor().a = 0f;
        failedOverlay.addAction(Actions.fadeIn(0.5f, Interpolation.fade));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

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
                    popLabel.setColor(doubled ? Color.valueOf("FF66FF") : Color.valueOf("FFD700"));
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
                } else {
                    int finalHandScore = currentHandPoints * currentMultiplier;
                    targetDisplayScore += (finalHandScore - currentHandPoints);
                    RankProgress.getInstance().save();

                    triggerShake(GameConfig.SCREEN_SHAKE_INTENSITY * 1.5f * uiScale);

                    isScoring = false;
                    drawUpToMax();
                    updateHandUI();
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

        if (!isScoring && currentScore == targetDisplayScore && !isGameOver) {
            if (currentScore >= enemy.getTargetScore()) {
                isGameOver = true;
                runState.addMoney(GameConfig.REWARD_MONEY);
                runState.registerVictory();
                main.switchScreen(new RunMapScreen(main, runState));
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