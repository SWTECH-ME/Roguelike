package io.github.some_rougelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import io.github.some_rougelike.ui.TrippyBackground;

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

        Color c1 = Color.valueOf("4C9A7F");
        Color c2 = Color.valueOf("2D6A54");
        if (enemy.isBoss()) {
            c1 = new Color(0.4f, 0.1f, 0.15f, 1f);
            c2 = new Color(0.25f, 0.05f, 0.1f, 1f);
        }
        
        float overscan = 150f;
        TrippyBackground bgImg = new TrippyBackground(c1, c2);
        bgImg.setSize(Gdx.graphics.getWidth() + (overscan * 2), Gdx.graphics.getHeight() + (overscan * 2));
        bgImg.setPosition(-overscan, -overscan);
        bgImg.setTouchable(Touchable.disabled);
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
        return new TextureRegionDrawable(tex);
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        
        Table leftScorePanel = new Table();
        leftScorePanel.setBackground(createRoundedRect(320, 160, 0.1f, 0.1f, 0.12f, 0.75f, 25));
        leftScorePanel.pad(20);

        Label scoreTitle = new Label("ROUND SCORE", skin);
        scoreTitle.setFontScale(1.1f);
        scoreTitle.setColor(Color.valueOf("BBBBBB"));
        
        scoreLabel = new Label(String.valueOf(currentScore), skin);
        scoreLabel.setFontScale(3.8f);
        scoreLabel.setColor(Color.WHITE);

        targetLabel = new Label("Target: " + enemy.getTargetScore(), skin);
        targetLabel.setFontScale(1.4f);
        targetLabel.setColor(Color.valueOf("88AAFF"));

        leftScorePanel.add(scoreTitle).row();
        leftScorePanel.add(scoreLabel).padTop(5).padBottom(5).row();
        leftScorePanel.add(targetLabel).padTop(5);

        Table rightStatsPanel = new Table();
        rightStatsPanel.setBackground(createRoundedRect(220, 140, 0.1f, 0.1f, 0.12f, 0.75f, 25));
        rightStatsPanel.pad(20);

        playsLabel = new Label("Plays: " + playsLeft, skin);
        playsLabel.setFontScale(1.5f);
        playsLabel.setColor(Color.valueOf("FFD700"));
        
        discardsLabel = new Label("Discards: " + discardsLeft, skin);
        discardsLabel.setFontScale(1.5f);
        discardsLabel.setColor(Color.valueOf("FF9999"));

        rightStatsPanel.add(playsLabel).padBottom(15).row();
        rightStatsPanel.add(discardsLabel);

        Table topArea = new Table();
        topArea.add(leftScorePanel).expandX().left().padLeft(40).padTop(40);
        topArea.add(rightStatsPanel).expandX().right().padRight(40).padTop(40).top();

        Table middleTable = new Table();
        middleTable.setBackground(createRoundedRect(450, 130, 0.05f, 0.05f, 0.08f, 0.8f, 30));
        middleTable.pad(25);

        Label enemyName = new Label(enemy.getName(), skin);
        enemyName.setFontScale(2.2f);
        enemyName.setColor(enemy.isBoss() ? Color.valueOf("FF5555") : Color.WHITE);
        
        Label enemyType = new Label(enemy.isBoss() ? "BOSS BLIND" : "NORMAL BLIND", skin);
        enemyType.setFontScale(1.1f);
        enemyType.setColor(Color.valueOf("AAAAAA"));
        
        middleTable.add(enemyType).row();
        middleTable.add(enemyName).padTop(5).row();
        
        if (enemy.isBoss()) {
            Label abilityLabel = new Label(enemy.getSpecialAbility(), skin);
            abilityLabel.setColor(Color.valueOf("FF8888"));
            middleTable.add(abilityLabel).padTop(5);
        }

        Table bottomSection = new Table();
        
        handTable = new Table();
        updateHandUI();

        Table actionTable = new Table();
        
        TextButton.TextButtonStyle originalStyle = skin.get(TextButton.TextButtonStyle.class);
        
        TextButton.TextButtonStyle playStyle = new TextButton.TextButtonStyle(originalStyle);
        playStyle.up = createRoundedRect(200, 70, 0.1f, 0.4f, 0.9f, 1.0f, 25);
        playStyle.down = createRoundedRect(200, 70, 0.05f, 0.25f, 0.7f, 1.0f, 25);
        TextButton playBtn = new TextButton("PLAY HAND", playStyle);
        playBtn.getLabel().setFontScale(1.2f);
        
        TextButton.TextButtonStyle discardStyle = new TextButton.TextButtonStyle(originalStyle);
        discardStyle.up = createRoundedRect(200, 70, 0.9f, 0.2f, 0.2f, 1.0f, 25);
        discardStyle.down = createRoundedRect(200, 70, 0.7f, 0.1f, 0.1f, 1.0f, 25);
        TextButton discardBtn = new TextButton("DISCARD", discardStyle);
        discardBtn.getLabel().setFontScale(1.2f);
        
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
        
        actionTable.add(playBtn).size(200, 70).padRight(30);
        actionTable.add(discardBtn).size(200, 70);

        Table deckTable = new Table();
        deckTable.setBackground(createRoundedRect(120, 160, 0.12f, 0.12f, 0.15f, 0.85f, 20));
        deckTable.pad(15);
        
        Label deckTitle = new Label("DECK", skin);
        deckTitle.setColor(Color.GRAY);
        deckCountLabel = new Label(String.valueOf(drawPile.size()), skin);
        deckCountLabel.setFontScale(2f);
        deckTable.add(deckTitle).row();
        deckTable.add(deckCountLabel).padTop(15).row();

        Table handAndActions = new Table();
        handAndActions.add(handTable).padBottom(30).row();
        handAndActions.add(actionTable);

        bottomSection.add(handAndActions).expandX().center();
        bottomSection.add(deckTable).right().padRight(40).bottom().padBottom(30);

        root.add(topArea).fillX().top().row();
        root.add(middleTable).expand().center().row();
        root.add(bottomSection).fillX().padBottom(40).bottom();

        stage.addActor(root);
    }

    private void updateHandUI() {
        handTable.clear();
        selectedCards.clear();
        scoringCardTables.clear();

        for (Card card : hand) {
            Table cardBg = new Table();
            cardBg.setBackground(createRoundedRect(120, 170, 0.95f, 0.95f, 0.98f, 1f, 15));
            cardBg.setTouchable(Touchable.enabled);
            
            Label cardLbl = new Label(card.getRank().name() + "\n\n" + card.getSuit().name(), skin);
            cardLbl.setAlignment(Align.center);
            cardLbl.setTouchable(Touchable.disabled);
            
            Color textColor;
            if (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) {
                textColor = Color.valueOf("DD3333");
            } else {
                textColor = Color.valueOf("222233");
            }
            cardLbl.setColor(textColor);
            
            cardBg.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (isGameOver || isScoring || selectedCards.contains(card)) return;
                    cardBg.clearActions();
                    cardBg.addAction(Actions.moveTo(cardBg.getX(), 15, 0.15f, Interpolation.smooth));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (isGameOver || isScoring || selectedCards.contains(card)) return;
                    cardBg.clearActions();
                    cardBg.addAction(Actions.moveTo(cardBg.getX(), 0, 0.15f, Interpolation.smooth));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isGameOver || isScoring) return;
                    if (selectedCards.contains(card)) {
                        selectedCards.remove(card);
                        cardBg.clearActions();
                        cardBg.addAction(Actions.moveTo(cardBg.getX(), 0, 0.2f, Interpolation.exp10Out));
                        cardBg.setBackground(createRoundedRect(120, 170, 0.95f, 0.95f, 0.98f, 1f, 15));
                    } else {
                        if (selectedCards.size() < 5) {
                            selectedCards.add(card);
                            cardBg.clearActions();
                            cardBg.addAction(Actions.moveTo(cardBg.getX(), 40, 0.2f, Interpolation.exp10Out));
                            cardBg.setBackground(createRoundedRect(120, 170, 0.8f, 0.9f, 1.0f, 1f, 15));
                        }
                    }
                }
            });
            
            cardBg.add(cardLbl).expand().fill();
            handTable.add(cardBg).size(120, 170).pad(10).align(Align.bottom);
            scoringCardTables.add(cardBg);
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
        failedOverlay.setBackground(createRoundedRect(1, 1, 0f, 0f, 0f, 0.9f, 0));

        Label failedLabel = new Label("GAME OVER", skin);
        failedLabel.setFontScale(4f);
        failedLabel.setColor(Color.RED);
        
        TextButton.TextButtonStyle originalStyle = skin.get(TextButton.TextButtonStyle.class);
        TextButton.TextButtonStyle menuStyle = new TextButton.TextButtonStyle(originalStyle);
        menuStyle.up = createRoundedRect(250, 60, 0.2f, 0.2f, 0.22f, 1.0f, 25);
        menuStyle.down = createRoundedRect(250, 60, 0.15f, 0.15f, 0.17f, 1.0f, 25);
        
        TextButton menuBtn = new TextButton("MAIN MENU", menuStyle);
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
                    currentHandPoints += c.getValue();
                    targetDisplayScore += c.getValue(); 
                    
                    triggerShake(GameConfig.SCREEN_SHAKE_INTENSITY / 2f);

                    Label popLabel = new Label("+" + c.getValue(), skin);
                    popLabel.setFontScale(4f);
                    popLabel.setColor(Color.valueOf("FFD700"));
                    popLabel.setAlignment(Align.center);
                    
                    if (scoringCardIndex < scoringCardTables.size()) {
                        Table cardTable = scoringCardTables.get(scoringCardIndex);
                        popLabel.pack();
                        
                        com.badlogic.gdx.math.Vector2 pos = new com.badlogic.gdx.math.Vector2(cardTable.getWidth() / 2f - popLabel.getWidth() / 2f, cardTable.getHeight() + 20f);
                        cardTable.localToStageCoordinates(pos);
                        
                        popLabel.setPosition(pos.x, pos.y);
                        stage.addActor(popLabel);
                        
                        popLabel.addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.moveBy(0, 80, 0.7f, Interpolation.circleOut),
                                Actions.fadeOut(0.7f, Interpolation.fade)
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