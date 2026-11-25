package io.github.some_rougelike.save;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.logic.CheatCard;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.HandEvaluator;
import io.github.some_rougelike.logic.HotCard;
import io.github.some_rougelike.logic.HotCardState;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.model.Card;

public class RunSaveData {
    private int money;
    private int chapterIndex;
    private int maxChapters = GameConfig.MAX_CHAPTERS;
    private int totalRunScore;
    private ArrayList<String> usedBossNames = new ArrayList<>();
    private ArrayList<CardSaveData> deck = new ArrayList<>();
    private ArrayList<EnemySaveData> availableNormals = new ArrayList<>();
    private EnemySaveData bossEnemy;
    private boolean inCombat;
    private int activeNormalIndex = -1;
    private boolean activeIsBoss;
    private CombatSaveData combatState;
    private ArrayList<String> comboBonusNames = new ArrayList<>();
    private float[] comboBonusValues = new float[0];
    private boolean luckySeven;
    private int shopCardSlots = RunState.DEFAULT_SHOP_CARD_SLOTS;
    private int rerollDiscount;
    private int goldRushCount;
    private int cheatCardsBought;
    private int[] delayedRounds = new int[0];
    private int[] delayedAmounts = new int[0];
    private int[] rankChipBonus = new int[0];
    private boolean shopAvailable;
    private String[] shopCheatTree = new String[0];
    private ArrayList<String> hotCards = new ArrayList<>();
    private int[] hotCardPurchasePrices = new int[0];
    private HotCardState hotState = new HotCardState();

    public RunSaveData() {
    }

    /** 
     * @param runState
     * @param combatSaveData
     * @return RunSaveData
     */
    public static RunSaveData fromRunState(RunState runState, CombatSaveData combatSaveData) {
        RunSaveData data = new RunSaveData();
        data.money = runState.getMoney();
        data.chapterIndex = runState.getChapterIndex();
        data.maxChapters = runState.getMaxChapters();
        data.totalRunScore = runState.getTotalRunScore();
        if (runState.getUsedBossNames() != null) {
            data.usedBossNames.addAll(runState.getUsedBossNames());
        }

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

        data.comboBonusValues = new float[runState.getComboBonuses().size()];
        for (Map.Entry<HandEvaluator.Combo, Double> e : runState.getComboBonuses().entrySet()) {
            data.comboBonusNames.add(e.getKey().name());
            data.comboBonusValues[data.comboBonusNames.size() - 1] = e.getValue().floatValue();
        }
        data.luckySeven = runState.hasLuckySeven();
        data.shopCardSlots = runState.getShopCardSlots();
        data.rerollDiscount = runState.getRerollDiscount();
        data.goldRushCount = runState.getGoldRushCount();
        data.cheatCardsBought = runState.getCheatCardsBought();
        data.rankChipBonus = runState.getRankChipBonuses();
        data.shopAvailable = runState.isShopAvailable();
        List<String> ownedHot = runState.getHotCards();
        data.hotCards.addAll(ownedHot);
        data.hotCardPurchasePrices = new int[ownedHot.size()];
        for (int i = 0; i < ownedHot.size(); i++) {
            data.hotCardPurchasePrices[i] = runState.getHotCardPurchasePrice(ownedHot.get(i));
        }
        data.hotState = runState.getHotState().copy();
        CheatCard[] tree = runState.getShopCheatTree();
        if (tree != null) {
            data.shopCheatTree = new String[tree.length];
            for (int i = 0; i < tree.length; i++) {
                data.shopCheatTree[i] = tree[i].name();
            }
        }
        List<int[]> delayed = runState.getDelayedMoney();
        data.delayedRounds = new int[delayed.size()];
        data.delayedAmounts = new int[delayed.size()];
        for (int i = 0; i < delayed.size(); i++) {
            data.delayedRounds[i] = delayed.get(i)[0];
            data.delayedAmounts[i] = delayed.get(i)[1];
        }
        return data;
    }

    /** 
     * @return RunState
     */
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

        RunState restored = new RunState(money, chapterIndex, totalRunScore, restoredDeck, restoredNormals, restoredBoss, restoredActive, usedBossNames);
        if (comboBonusNames != null && comboBonusValues != null) {
            for (int i = 0; i < Math.min(comboBonusNames.size(), comboBonusValues.length); i++) {
                try {
                    restored.addComboBonus(HandEvaluator.Combo.valueOf(comboBonusNames.get(i)), comboBonusValues[i]);
                } catch (IllegalArgumentException ignored) {
                    // régi/ismeretlen kombináció neve a mentésben
                }
            }
        }
        restored.setLuckySeven(luckySeven);
        restored.setShopCardSlots(shopCardSlots);
        restored.restoreCheatState(rerollDiscount, goldRushCount, cheatCardsBought);
        restored.setRankChipBonuses(rankChipBonus);
        restored.setShopAvailable(shopAvailable);
        if (hotCards != null) {
            for (int i = 0; i < hotCards.size(); i++) {
                String id = hotCards.get(i);
                if (hotCardPurchasePrices != null && i < hotCardPurchasePrices.length) {
                    restored.addHotCard(id, hotCardPurchasePrices[i]);
                } else {
                    restored.addHotCard(id);
                }
            }
        }
        restored.setHotState(hotState != null ? hotState.copy() : null);
        if (shopCheatTree != null && shopCheatTree.length == RunState.SHOP_CHEAT_TREE_SIZE) {
            try {
                CheatCard[] tree = new CheatCard[shopCheatTree.length];
                for (int i = 0; i < tree.length; i++) {
                    tree[i] = CheatCard.valueOf(shopCheatTree[i]);
                }
                restored.setShopCheatTree(tree);
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // ismeretlen lapnév: a bolt újat generál
            }
        }
        if (delayedRounds != null && delayedAmounts != null) {
            for (int i = 0; i < Math.min(delayedRounds.length, delayedAmounts.length); i++) {
                restored.addDelayedMoney(delayedRounds[i], delayedAmounts[i]);
            }
        }
        return restored;
    }

    /** 
     * @return int
     */
    public int getMoney() {
        return money;
    }

    /** 
     * @param money
     */
    public void setMoney(int money) {
        this.money = money;
    }

    /** 
     * @return int
     */
    public int getChapterIndex() {
        return chapterIndex;
    }

    /** 
     * @param chapterIndex
     */
    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    /** 
     * @return int
     */
    public int getMaxChapters() {
        return maxChapters > 0 ? maxChapters : GameConfig.MAX_CHAPTERS;
    }

    /** 
     * @param maxChapters
     */
    public void setMaxChapters(int maxChapters) {
        this.maxChapters = maxChapters;
    }

    /** 
     * @return int
     */
    public int getTotalRunScore() {
        return totalRunScore;
    }

    /** 
     * @param totalRunScore
     */
    public void setTotalRunScore(int totalRunScore) {
        this.totalRunScore = totalRunScore;
    }

    /** 
     * @return ArrayList<String>
     */
    public ArrayList<String> getUsedBossNames() {
        return usedBossNames;
    }

    /** 
     * @param usedBossNames
     */
    public void setUsedBossNames(ArrayList<String> usedBossNames) {
        this.usedBossNames = usedBossNames;
    }

    /** 
     * @return ArrayList<CardSaveData>
     */
    public ArrayList<CardSaveData> getDeck() {
        return deck;
    }

    /** 
     * @param deck
     */
    public void setDeck(ArrayList<CardSaveData> deck) {
        this.deck = deck;
    }

    /** 
     * @return ArrayList<EnemySaveData>
     */
    public ArrayList<EnemySaveData> getAvailableNormals() {
        return availableNormals;
    }

    /** 
     * @param availableNormals
     */
    public void setAvailableNormals(ArrayList<EnemySaveData> availableNormals) {
        this.availableNormals = availableNormals;
    }

    /** 
     * @return EnemySaveData
     */
    public EnemySaveData getBossEnemy() {
        return bossEnemy;
    }

    /** 
     * @param bossEnemy
     */
    public void setBossEnemy(EnemySaveData bossEnemy) {
        this.bossEnemy = bossEnemy;
    }

    /** 
     * @return boolean
     */
    public boolean isInCombat() {
        return inCombat;
    }

    /** 
     * @param inCombat
     */
    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    /** 
     * @return int
     */
    public int getActiveNormalIndex() {
        return activeNormalIndex;
    }

    /** 
     * @param activeNormalIndex
     */
    public void setActiveNormalIndex(int activeNormalIndex) {
        this.activeNormalIndex = activeNormalIndex;
    }

    /** 
     * @return boolean
     */
    public boolean isActiveIsBoss() {
        return activeIsBoss;
    }

    /** 
     * @param activeIsBoss
     */
    public void setActiveIsBoss(boolean activeIsBoss) {
        this.activeIsBoss = activeIsBoss;
    }

    /** 
     * @return CombatSaveData
     */
    public CombatSaveData getCombatState() {
        return combatState;
    }

    /** 
     * @param combatState
     */
    public void setCombatState(CombatSaveData combatState) {
        this.combatState = combatState;
    }
}
