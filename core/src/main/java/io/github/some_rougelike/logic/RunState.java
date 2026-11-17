package io.github.some_rougelike.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Suit;
import io.github.some_rougelike.model.Rank;

public class RunState {
    private int money;
    private int chapterIndex;
    private int totalRunScore;
    private boolean runCompleted;

    private List<Card> deck;

    private List<Enemy> availableNormals;
    private Enemy bossEnemy;
    private Enemy activeEnemy;

    public RunState() {
        this.money = GameConfig.STARTING_MONEY;
        this.chapterIndex = 1;
        this.totalRunScore = 0;
        this.runCompleted = false;
        this.deck = generateStandardDeck();
        generateMockChapter();
    }

    public RunState(int money, int chapterIndex, int totalRunScore, List<Card> deck, List<Enemy> availableNormals, Enemy bossEnemy, Enemy activeEnemy) {
        this.money = money;
        this.chapterIndex = Math.max(1, Math.min(chapterIndex, GameConfig.MAX_CHAPTERS));
        this.totalRunScore = Math.max(0, totalRunScore);
        this.runCompleted = false;
        this.deck = (deck != null && !deck.isEmpty()) ? new ArrayList<>(deck) : generateStandardDeck();
        if (availableNormals != null && !availableNormals.isEmpty() && bossEnemy != null) {
            this.availableNormals = new ArrayList<>(availableNormals);
            this.bossEnemy = bossEnemy;
            this.activeEnemy = activeEnemy;
        } else {
            generateMockChapter();
        }
    }

    private List<Card> generateStandardDeck() {
        List<Card> newDeck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                newDeck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(newDeck);
        return newDeck;
    }

    private void generateMockChapter() {
        List<Enemy> normalPool = new ArrayList<>();
        normalPool.add(new Enemy("Backrooms Obama", 300, 4, 3, GameConfig.BG_ENEMY_1));
        normalPool.add(new Enemy("360 NoScope", 600, 4, 3, GameConfig.BG_ENEMY_2));
        normalPool.add(new Enemy("Doge", 450, 4, 3, GameConfig.BG_ENEMY_1));
        normalPool.add(new Enemy("Trollface", 550, 3, 2, GameConfig.BG_ENEMY_2));
        Collections.shuffle(normalPool);

        availableNormals = new ArrayList<>();
        int count = Math.min(GameConfig.NORMALS_PER_CHAPTER, normalPool.size());
        for (int i = 0; i < count; i++) {
            availableNormals.add(normalPool.get(i));
        }

        List<Enemy> bossPool = new ArrayList<>();
        bossPool.add(new Enemy("Triple T", 500, 3, 2, "Card colors are hidden.", GameConfig.BG_BOSS));
        bossPool.add(new Enemy("Giga Chad", 500, 4, 2, "No discards allowed.", GameConfig.BG_BOSS));
        Collections.shuffle(bossPool);

        bossEnemy = bossPool.get(0);
        activeEnemy = null;
    }

    public void setActiveEnemy(Enemy enemy) {
        this.activeEnemy = enemy;
    }

    public Enemy getCurrentEnemy() {
        return activeEnemy;
    }

    public void registerVictory() {
        if (activeEnemy == bossEnemy) {
            if (chapterIndex < GameConfig.MAX_CHAPTERS) {
                chapterIndex++;
                generateMockChapter();
            } else {
                if (bossEnemy != null) {
                    bossEnemy.setDefeated(true);
                }
                runCompleted = true;
            }
        } else {
            if (activeEnemy != null) {
                activeEnemy.setDefeated(true);
            }
        }
        activeEnemy = null;
    }

    public boolean isBossUnlocked() {
        for (Enemy enemy : availableNormals) {
            if (!enemy.isDefeated()) {
                return false;
            }
        }
        return true;
    }

    public List<Enemy> getAvailableNormals() { return availableNormals; }
    public Enemy getBossEnemy() { return bossEnemy; }
    public int getChapterIndex() { return chapterIndex; }
    public int getMaxChapters() { return GameConfig.MAX_CHAPTERS; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { this.money += amount; }
    public int getTotalRunScore() { return totalRunScore; }
    public void setTotalRunScore(int totalRunScore) { this.totalRunScore = Math.max(0, totalRunScore); }
    public void addRunScore(int points) {
        if (points > 0) {
            this.totalRunScore += points;
        }
    }
    public boolean isRunCompleted() { return runCompleted; }
    public List<Card> getDeck() { return deck; }
}
