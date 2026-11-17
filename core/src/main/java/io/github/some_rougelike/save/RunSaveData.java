package io.github.some_rougelike.save;

import java.util.ArrayList;
import java.util.List;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.model.Card;

public class RunSaveData {
    private int money;
    private int chapterIndex;
    private int maxChapters = GameConfig.MAX_CHAPTERS;
    private int totalRunScore;
    private ArrayList<CardSaveData> deck = new ArrayList<>();
    private ArrayList<EnemySaveData> availableNormals = new ArrayList<>();
    private EnemySaveData bossEnemy;
    private boolean inCombat;
    private int activeNormalIndex = -1;
    private boolean activeIsBoss;
    private CombatSaveData combatState;

    public RunSaveData() {
    }

    public static RunSaveData fromRunState(RunState runState, CombatSaveData combatSaveData) {
        RunSaveData data = new RunSaveData();
        data.money = runState.getMoney();
        data.chapterIndex = runState.getChapterIndex();
        data.maxChapters = runState.getMaxChapters();
        data.totalRunScore = runState.getTotalRunScore();

        for (Card card : runState.getDeck()) {
            data.deck.add(CardSaveData.fromCard(card));
        }

        List<Enemy> normals = runState.getAvailableNormals();
        Enemy currentActive = runState.getCurrentEnemy();

        for (int i = 0; i < normals.size(); i++) {
            Enemy normal = normals.get(i);
            data.availableNormals.add(EnemySaveData.fromEnemy(normal));
            if (currentActive != null && currentActive == normal) {
                data.activeNormalIndex = i;
            }
        }

        data.bossEnemy = EnemySaveData.fromEnemy(runState.getBossEnemy());
        if (currentActive != null && currentActive == runState.getBossEnemy()) {
            data.activeIsBoss = true;
        }

        data.inCombat = currentActive != null && combatSaveData != null;
        data.combatState = combatSaveData;
        return data;
    }

    public RunState toRunState() {
        List<Card> restoredDeck = new ArrayList<>();
        if (deck != null) {
            for (CardSaveData cardData : deck) {
                restoredDeck.add(cardData.toCard());
            }
        }

        List<Enemy> restoredNormals = new ArrayList<>();
        if (availableNormals != null) {
            for (EnemySaveData enemyData : availableNormals) {
                restoredNormals.add(enemyData.toEnemy());
            }
        }

        Enemy restoredBoss = bossEnemy != null ? bossEnemy.toEnemy() : null;
        Enemy restoredActive = null;

        if (inCombat) {
            if (activeIsBoss) {
                restoredActive = restoredBoss;
            } else if (activeNormalIndex >= 0 && activeNormalIndex < restoredNormals.size()) {
                restoredActive = restoredNormals.get(activeNormalIndex);
            }
        }

        return new RunState(money, chapterIndex, totalRunScore, restoredDeck, restoredNormals, restoredBoss, restoredActive);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public int getMaxChapters() {
        return maxChapters > 0 ? maxChapters : GameConfig.MAX_CHAPTERS;
    }

    public void setMaxChapters(int maxChapters) {
        this.maxChapters = maxChapters;
    }

    public int getTotalRunScore() {
        return totalRunScore;
    }

    public void setTotalRunScore(int totalRunScore) {
        this.totalRunScore = totalRunScore;
    }

    public ArrayList<CardSaveData> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<CardSaveData> deck) {
        this.deck = deck;
    }

    public ArrayList<EnemySaveData> getAvailableNormals() {
        return availableNormals;
    }

    public void setAvailableNormals(ArrayList<EnemySaveData> availableNormals) {
        this.availableNormals = availableNormals;
    }

    public EnemySaveData getBossEnemy() {
        return bossEnemy;
    }

    public void setBossEnemy(EnemySaveData bossEnemy) {
        this.bossEnemy = bossEnemy;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public int getActiveNormalIndex() {
        return activeNormalIndex;
    }

    public void setActiveNormalIndex(int activeNormalIndex) {
        this.activeNormalIndex = activeNormalIndex;
    }

    public boolean isActiveIsBoss() {
        return activeIsBoss;
    }

    public void setActiveIsBoss(boolean activeIsBoss) {
        this.activeIsBoss = activeIsBoss;
    }

    public CombatSaveData getCombatState() {
        return combatState;
    }

    public void setCombatState(CombatSaveData combatState) {
        this.combatState = combatState;
    }
}
