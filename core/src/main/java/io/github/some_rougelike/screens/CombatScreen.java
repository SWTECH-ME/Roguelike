package io.github.some_rougelike.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.some_rougelike.Assets;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.logic.Card;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;

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

    private boolean isScoring = false;
    private float scoringTimer = 0f;
    private int scoringCardIndex = 0;
    private int currentHandPoints = 0;
    private int currentMultiplier = 1;
    private List<Card> scoringCards = new ArrayList<>();
    private List<Table> scoringCardTables = new ArrayList<>();

    public CombatScreen(Main main, RunState runState) {
        super(main, "");
        this.runState = runState;
        this.enemy = runState.getCurrentEnemy();
        this.playsLeft = enemy.getMaxPlays();
        this.discardsLeft = enemy.getMaxDiscards();
        this.skin = Assets.getInstance().getSkin(GameConfig.UI_SKIN);

        this.drawPile = new ArrayList<>(runState.getDeck());
        Collections.shuffle(this.drawPile);

        Texture bgTex = Assets.getInstance().getTexture(enemy.getBackgroundPath());
        Image bgImg = new Image(bgTex);
        bgImg.setFillParent(true);
        stage.addActor(bgImg);

        drawInitialHand();
        buildUI();
    }

    private void drawInitialHand() {
        for (int i = 0; i < GameConfig.STARTING_HAND_SIZE; i++) {
            if (!drawPile.isEmpty()) {
                hand.add(drawPile.remove(0));
            }
        }
    }

    private TextureRegionDrawable createColorDrawable(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(r, g, b, a));
        pixmap.fill();
        return new TextureRegionDrawable(new Texture(pixmap));
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        
        Table leftScorePanel = new Table();
        leftScorePanel.setBackground(createColorDrawable(0.1f, 0.15f, 0.3f, 0.85f));
        leftScorePanel.pad(20);

        Label scoreTitle = new Label("ROUND SCORE", skin);
        scoreTitle.setFontScale(1.2f);
        scoreTitle.setColor(Color.LIGHT_GRAY);
        
        scoreLabel = new Label(String.valueOf(currentScore), skin);
        scoreLabel.setFontScale(3.5f);
        scoreLabel.setColor(Color.CYAN);

        targetLabel = new Label("Target: " + enemy.getTargetScore(), skin);
        targetLabel.setFontScale(1.5f);
        targetLabel.setColor(Color.WHITE);

        leftScorePanel.add(scoreTitle).row();
        leftScorePanel.add(scoreLabel).padTop(10).padBottom(10).row();
        leftScorePanel.add(targetLabel).padTop(10);

        Table rightStatsPanel = new Table();
        rightStatsPanel.setBackground(createColorDrawable(0.3f, 0.1f, 0.1f, 0.85f));
        rightStatsPanel.pad(15);

        playsLabel = new Label("Plays: " + playsLeft, skin);
        playsLabel.setFontScale(1.6f);
        playsLabel.setColor(Color.ORANGE);
        
        discardsLabel = new Label("Discards: " + discardsLeft, skin);
        discardsLabel.setFontScale(1.6f);
        discardsLabel.setColor(Color.ORANGE);

        rightStatsPanel.add(playsLabel).padRight(30);
        rightStatsPanel.add(discardsLabel);

        Table topArea = new Table();
        topArea.add(leftScorePanel).expandX().left().padLeft(30).padTop(30);
        topArea.add(rightStatsPanel).expandX().right().padRight(30).padTop(30).top();

        Table middleTable = new Table();
        middleTable.setBackground(createColorDrawable(0f, 0f, 0f, 0.6f));
        middleTable.pad(20);

        Label enemyName = new Label(enemy.getName(), skin);
        enemyName.setFontScale(2f);
        if (enemy.isBoss()) enemyName.setColor(Color.RED);
        
        Label enemyType = new Label(enemy.isBoss() ? "BOSS BLIND" : "NORMAL BLIND", skin);
        enemyType.setColor(Color.GOLD);
        middleTable.add(enemyType).row();
        middleTable.add(enemyName).padBottom(10).row();
        
        if (enemy.isBoss()) {
            Label abilityLabel = new Label(enemy.getSpecialAbility(), skin);
            abilityLabel.setColor(Color.WHITE);
            middleTable.add(abilityLabel);
        }

        Table bottomSection = new Table();
        
        handTable = new Table();
        updateHandUI();

        Table actionTable = new Table();
        TextButton playBtn = new TextButton("PLAY HAND", skin);
        playBtn.setColor(Color.valueOf("4477FF"));
        TextButton discardBtn = new TextButton("DISCARD", skin);
        discardBtn.setColor(Color.valueOf("DD4444"));
        
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playSelectedCards();
            }
        });
        
        discardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                discardSelectedCards();
            }
        });
        
        actionTable.add(playBtn).size(200, 60).padRight(20);
        actionTable.add(discardBtn).size(200, 60);

        Table deckTable = new Table();
        deckTable.setBackground(createColorDrawable(0.1f, 0.1f, 0.1f, 0.9f));
        deckTable.pad(15);
        TextButton deckIcon = new TextButton("DECK", skin);
        deckIcon.setDisabled(true);
        deckCountLabel = new Label(drawPile.size() + " cards left", skin);
        deckTable.add(deckIcon).size(70, 100).row();
        deckTable.add(deckCountLabel).padTop(10);

        Table handAndActions = new Table();
        handAndActions.add(handTable).padBottom(20).row();
        handAndActions.add(actionTable);

        bottomSection.add(handAndActions).expandX().center();
        bottomSection.add(deckTable).right().padRight(30);

        root.add(topArea).fillX().top().row();
        root.add(middleTable).expand().center().row();
        root.add(bottomSection).fillX().padBottom(30).bottom();

        stage.addActor(root);
    }

    private void updateHandUI() {
        handTable.clear();
        selectedCards.clear();
        scoringCardTables.clear();

        for (Card card : hand) {
            Table cardBg = new Table();
            cardBg.setBackground(createColorDrawable(1f, 1f, 1f, 1f));
            
            TextButton cardBtn = new TextButton(card.getRank().name() + "\n\n" + card.getSuit().name(), skin);
            cardBtn.getLabel().setAlignment(Align.center);
            
            Color textColor;
            if (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) {
                textColor = Color.valueOf("DD2222");
            } else {
                textColor = Color.valueOf("222222");
            }
            cardBtn.getLabel().setColor(textColor);
            cardBtn.setColor(Color.WHITE);
            
            cardBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isGameOver || isScoring) return;
                    if (selectedCards.contains(card)) {
                        selectedCards.remove(card);
                        cardBg.addAction(Actions.moveTo(cardBg.getX(), 0, 0.1f));
                        cardBtn.setColor(Color.WHITE);
                    } else {
                        if (selectedCards.size() < 5) {
                            selectedCards.add(card);
                            cardBg.addAction(Actions.moveTo(cardBg.getX(), 30, 0.1f));
                            cardBtn.setColor(Color.valueOf("AAAAFF"));
                        }
                    }
                }
            });
            
            cardBg.add(cardBtn).size(110, 160);
            handTable.add(cardBg).size(110, 160).pad(10).align(Align.bottom);
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
            Actions.moveTo(0, 0, d)
        ));
    }

    private void playSelectedCards() {
        if (isGameOver || isScoring || selectedCards.isEmpty() || playsLeft <= 0) return;

        playsLeft--;
        
        scoringCards.clear();
        scoringCards.addAll(selectedCards);
        
        scoringCardTables.clear();
        for (Card sc : scoringCards) {
            int index = hand.indexOf(sc);
            if (index >= 0 && index < handTable.getChildren().size) {
                scoringCardTables.add((Table) handTable.getChildren().get(index));
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
            deckCountLabel.setText(drawPile.size() + " cards left");
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
        failedOverlay.setBackground(createColorDrawable(0, 0, 0, 0.85f));

        Label failedLabel = new Label("GAME OVER", skin);
        failedLabel.setFontScale(4f);
        failedLabel.setColor(Color.RED);
        
        TextButton menuBtn = new TextButton("MAIN MENU", skin);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.switchScreen(new MainMenuScreen(main));
            }
        });

        failedOverlay.add(failedLabel).padBottom(60).row();
        failedOverlay.add(menuBtn).size(250, 60);

        stage.addActor(failedOverlay);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (isScoring) {
            scoringTimer += delta;
            if (scoringTimer >= GameConfig.SCORING_STEP_DELAY) {
                scoringTimer = 0f;
                
                if (scoringCardIndex < scoringCards.size()) {
                    Card c = scoringCards.get(scoringCardIndex);
                    currentHandPoints += c.getValue();
                    targetDisplayScore += c.getValue(); 
                    
                    triggerShake(GameConfig.SCREEN_SHAKE_INTENSITY / 2f);

                    Label popLabel = new Label("+" + c.getValue(), skin);
                    popLabel.setFontScale(1.5f);
                    popLabel.setColor(Color.YELLOW);
                    
                    if (scoringCardIndex < scoringCardTables.size()) {
                        Table cardTable = scoringCardTables.get(scoringCardIndex);
                        popLabel.setPosition(cardTable.getX() + cardTable.getWidth()/2f - 20, cardTable.getY() + cardTable.getHeight() + 20);
                        stage.addActor(popLabel);
                        
                        popLabel.addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.moveBy(0, 50, 0.5f),
                                Actions.fadeOut(0.5f)
                            ),
                            Actions.removeActor()
                        ));
                    }

                    scoringCardIndex++;
                } else {
                    int finalHandScore = currentHandPoints * currentMultiplier;
                    targetDisplayScore += (finalHandScore - currentHandPoints);
                    
                    triggerShake(GameConfig.SCREEN_SHAKE_INTENSITY * 1.5f);
                    
                    isScoring = false;
                    drawUpToMax();
                    updateHandUI();
                }
            }
        }

        if (currentScore < targetDisplayScore) {
            int diff = targetDisplayScore - currentScore;
            int increment = Math.max(1, (int)(diff * 15 * delta));
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
}