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
    
    private List<Card> deck;
    
    private List<Enemy> availableNormals;
    private Enemy bossEnemy;
    private Enemy activeEnemy;

    public RunState() {
        this.money = GameConfig.STARTING_MONEY;
        this.chapterIndex = 1;
        this.deck = generateStandardDeck();
        generateMockChapter();
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
        availableNormals.add(normalPool.get(0));
        availableNormals.add(normalPool.get(1));
        
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
            chapterIndex++;
            generateMockChapter();
        } else {
            availableNormals.remove(activeEnemy);
        }
        activeEnemy = null;
    }

    public boolean isBossUnlocked() {
        return availableNormals.isEmpty();
    }

    public List<Enemy> getAvailableNormals() { return availableNormals; }
    public Enemy getBossEnemy() { return bossEnemy; }
    public int getChapterIndex() { return chapterIndex; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { this.money += amount; }
    public List<Card> getDeck() { return deck; }
}