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
    private boolean bossDebugMode;

    private List<Card> deck;
    private List<String> usedBossNames;

    private List<Enemy> availableNormals;
    private Enemy bossEnemy;
    private Enemy activeEnemy;

    public RunState() {
        this.money = GameConfig.STARTING_MONEY;
        this.chapterIndex = 1;
        this.totalRunScore = 0;
        this.runCompleted = false;
        this.bossDebugMode = false;
        this.usedBossNames = new ArrayList<>();
        this.deck = generateStandardDeck();
        generateMockChapter();
    }

    public static RunState createBossDebugRun() {
        RunState debugState = new RunState();
        debugState.bossDebugMode = true;
        debugState.availableNormals = ChapterGenerator.createAllBosses();
        debugState.bossEnemy = debugState.availableNormals.get(debugState.availableNormals.size() - 1);
        debugState.activeEnemy = null;
        return debugState;
    }

    public RunState(int money, int chapterIndex, int totalRunScore, List<Card> deck, List<Enemy> availableNormals, Enemy bossEnemy, Enemy activeEnemy) {
        this(money, chapterIndex, totalRunScore, deck, availableNormals, bossEnemy, activeEnemy, null);
    }

    public RunState(int money, int chapterIndex, int totalRunScore, List<Card> deck, List<Enemy> availableNormals, Enemy bossEnemy, Enemy activeEnemy, List<String> usedBossNames) {
        this.money = money;
        this.chapterIndex = Math.max(1, Math.min(chapterIndex, GameConfig.MAX_CHAPTERS));
        this.totalRunScore = Math.max(0, totalRunScore);
        this.runCompleted = false;
        this.bossDebugMode = false;
        this.usedBossNames = usedBossNames != null ? new ArrayList<>(usedBossNames) : new ArrayList<>();
        this.deck = (deck != null && !deck.isEmpty()) ? new ArrayList<>(deck) : generateStandardDeck();
        if (availableNormals != null && !availableNormals.isEmpty() && bossEnemy != null) {
            this.availableNormals = new ArrayList<>(availableNormals);
            this.bossEnemy = bossEnemy;
            this.activeEnemy = activeEnemy;
            if (bossEnemy.getName() != null && !this.usedBossNames.contains(bossEnemy.getName())) {
                this.usedBossNames.add(bossEnemy.getName());
            }
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
        availableNormals = ChapterGenerator.generateNormalEnemies();
        bossEnemy = ChapterGenerator.generateUniqueBoss(usedBossNames);
        activeEnemy = null;
    }

    public void setActiveEnemy(Enemy enemy) {
        this.activeEnemy = enemy;
    }

    public Enemy getCurrentEnemy() {
        return activeEnemy;
    }

    public void registerVictory() {
        if (bossDebugMode) {
            if (activeEnemy != null) {
                activeEnemy.setDefeated(true);
            }
            boolean allDefeated = true;
            for (Enemy e : availableNormals) {
                if (!e.isDefeated()) {
                    allDefeated = false;
                    break;
                }
            }
            runCompleted = allDefeated;
            activeEnemy = null;
            return;
        }
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
        if (bossDebugMode) {
            return true;
        }
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
    public boolean isBossDebugMode() { return bossDebugMode; }
    public List<String> getUsedBossNames() { return usedBossNames; }
    public List<Card> getDeck() { return deck; }
}
