package io.github.some_rougelike.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // --- Cheat lapok tartós hatásai ---
    public static final int DEFAULT_SHOP_CARD_SLOTS = 3;
    public static final int MAX_SHOP_CARD_SLOTS = 5;
    private final Map<HandEvaluator.Combo, Double> comboBonuses = new EnumMap<>(HandEvaluator.Combo.class);
    private boolean luckySeven = false;
    private int shopCardSlots = DEFAULT_SHOP_CARD_SLOTS;
    public static final int BASE_REROLL_PRICE = 5;
    public static final int GOLD_RUSH_PER_ROUND = 2;
    private int rerollDiscount = 0;
    private int goldRushCount = 0;
    private int cheatCardsBought = 0;
    /** A bolt blindonként egyszer látogatható: győzelemkor nyílik, belépéskor bezárul. */
    private boolean shopAvailable = false;
    /** A bolt bal oldali cheat fája (0-3: felső sor, 4-5: alsó sor); null, amíg még nem generáltuk. */
    private CheatCard[] shopCheatTree = null;
    /** A játékosnál lévő hot cardok azonosítói (legfeljebb MAX_HOT_CARDS). */
    public static final int MAX_HOT_CARDS = 5;
    private final List<String> hotCards = new ArrayList<>();
    private final Map<String, Integer> hotCardPurchasePrices = new LinkedHashMap<>();
    private HotCardState hotState = new HotCardState();
    /** Rangonkénti plusz chip (Hearts cheat lapok), Rank.ordinal() szerint indexelve. */
    private final int[] rankChipBonus = new int[Rank.values().length];
    /** Késleltetett pénzmozgások: [hátralévő körök, összeg] (Pawn Ticket, Time Is Money). */
    private final List<int[]> delayedMoney = new ArrayList<>();

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

    /** 
     * @return RunState
     */
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

    /** 
     * @return List<Card>
     */
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
        availableNormals = ChapterGenerator.generateNormalEnemies(chapterIndex);
        bossEnemy = ChapterGenerator.generateUniqueBoss(usedBossNames, chapterIndex);
        activeEnemy = null;
    }

    /** 
     * @param enemy
     */
    public void setActiveEnemy(Enemy enemy) {
        this.activeEnemy = enemy;
    }

    /** 
     * @return Enemy
     */
    public Enemy getCurrentEnemy() {
        return activeEnemy;
    }

    public void registerVictory() {
        if (!bossDebugMode) {
            onRoundEnd();
            shopAvailable = true;
        }
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

    /** 
     * @return boolean
     */
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
    /** Levonja az összeget, ha van rá elég pénz; különben nem változtat és false-t ad. */
    public boolean spendMoney(int amount) {
        if (amount < 0 || money < amount) return false;
        money -= amount;
        return true;
    }
    /** A kombináció alap szorzója + a cheat lapokból (Royal Flush) kapott bónusz. */
    public double getComboMultiplier(HandEvaluator.Combo combo) {
        if (combo == null) return 1;
        return combo.getMultiplier() + getComboBonus(combo);
    }
    /** 
     * @param combo
     * @return double
     */
    public double getComboBonus(HandEvaluator.Combo combo) {
        Double bonus = comboBonuses.get(combo);
        return bonus != null ? bonus : 0;
    }
    /** 
     * @param combo
     * @param bonus
     */
    public void addComboBonus(HandEvaluator.Combo combo, double bonus) {
        comboBonuses.put(combo, getComboBonus(combo) + bonus);
    }
    public Map<HandEvaluator.Combo, Double> getComboBonuses() { return comboBonuses; }
    /** Kör (megnyert harc) végi cheat hatások: Gold Rush, és a lejárt késleltetett pénzmozgások. */
    private void onRoundEnd() {
        HotCardEffects.onBlindWon(this);
        money += goldRushCount * GOLD_RUSH_PER_ROUND;
        for (int i = delayedMoney.size() - 1; i >= 0; i--) {
            int[] entry = delayedMoney.get(i);
            entry[0]--;
            if (entry[0] <= 0) {
                money = Math.max(0, money + entry[1]);
                delayedMoney.remove(i);
            }
        }
    }
    public void addDelayedMoney(int rounds, int amount) { delayedMoney.add(new int[] { rounds, amount }); }
    public List<int[]> getDelayedMoney() { return delayedMoney; }
    public int getRerollDiscount() { return rerollDiscount; }
    public void addRerollDiscount(int amount) { rerollDiscount = Math.min(BASE_REROLL_PRICE, rerollDiscount + amount); }
    public int getRerollPrice() { return Math.max(0, BASE_REROLL_PRICE - rerollDiscount); }
    public int getGoldRushCount() { return goldRushCount; }
    public void addGoldRush() { goldRushCount++; }
    public int getRankChipBonus(Rank rank) { return rank == null ? 0 : rankChipBonus[rank.ordinal()]; }
    public void addRankChipBonus(Rank rank, int amount) { rankChipBonus[rank.ordinal()] += amount; }
    public int[] getRankChipBonuses() { return rankChipBonus.clone(); }
    /** 
     * @param values
     */
    public void setRankChipBonuses(int[] values) {
        if (values == null) return;
        System.arraycopy(values, 0, rankChipBonus, 0, Math.min(values.length, rankChipBonus.length));
    }
    public CheatCard[] getShopCheatTree() { return shopCheatTree == null ? null : shopCheatTree.clone(); }
    /** 
     * @param tree
     */
    public void setShopCheatTree(CheatCard[] tree) {
        this.shopCheatTree = (tree != null && tree.length == SHOP_CHEAT_TREE_SIZE) ? tree.clone() : null;
    }
    public static final int SHOP_CHEAT_TREE_SIZE = 6;
    public List<String> getHotCards() { return Collections.unmodifiableList(hotCards); }
    public boolean hasFreeHotCardSlot() { return hotCards.size() < MAX_HOT_CARDS; }
    public boolean hasHotCard(String id) { return hotCards.contains(id); }
    public HotCardState getHotState() { return hotState; }
    public void setHotState(HotCardState state) { this.hotState = state != null ? state : new HotCardState(); }
    /** Hozzáadja a lapot az alapértelmezett árával, ha van hely és még nincs meg; true, ha sikerült. */
    public boolean addHotCard(String id) {
        HotCard card = HotCard.byId(id);
        return addHotCard(id, card != null ? card.getPrice() : 0);
    }
    /** Hozzáadja a lapot a megadott vételárral, ha van hely és még nincs meg; true, ha sikerült. */
    public boolean addHotCard(String id, int purchasePrice) {
        if (id == null || !hasFreeHotCardSlot() || hotCards.contains(id) || HotCard.byId(id) == null) return false;
        hotCards.add(id);
        hotCardPurchasePrices.put(id, Math.max(0, purchasePrice));
        return true;
    }
    /** 
     * @param id
     * @return int
     */
    public int getHotCardPurchasePrice(String id) {
        if (id == null) return 0;
        Integer price = hotCardPurchasePrices.get(id);
        if (price != null) return price;
        HotCard card = HotCard.byId(id);
        return card != null ? card.getPrice() : 0;
    }
    /** Vételár 50%-a (kerekítve). */
    public int getHotCardSellPrice(String id) {
        int purchasePrice = getHotCardPurchasePrice(id);
        return Math.max(1, Math.round(purchasePrice * 0.5f));
    }
    /** Eladja a hot cardot 50%-os áron: levonja a lapot, jóváírja a pénzt, törli a számlálóit; visszaadja a kapott összeget (0 ha sikertelen). */
    public int sellHotCard(String id) {
        if (id == null || !hotCards.contains(id)) return 0;
        int sellPrice = getHotCardSellPrice(id);
        hotCards.remove(id);
        hotCardPurchasePrices.remove(id);
        addMoney(sellPrice);
        if (hotState != null) {
            hotState.resetCardState(id);
        }
        return sellPrice;
    }
    /** 
     * @param id
     * @return boolean
     */
    public boolean removeHotCard(String id) {
        if (id == null || !hotCards.contains(id)) return false;
        hotCards.remove(id);
        hotCardPurchasePrices.remove(id);
        if (hotState != null) {
            hotState.resetCardState(id);
        }
        return true;
    }
    public boolean isShopAvailable() { return shopAvailable; }
    public void setShopAvailable(boolean shopAvailable) { this.shopAvailable = shopAvailable; }
    public int getCheatCardsBought() { return cheatCardsBought; }
    public void incrementCheatCardsBought() { cheatCardsBought++; }
    /** Mentésből visszatöltéshez. */
    public void restoreCheatState(int rerollDiscount, int goldRushCount, int cheatCardsBought) {
        this.rerollDiscount = Math.max(0, rerollDiscount);
        this.goldRushCount = Math.max(0, goldRushCount);
        this.cheatCardsBought = Math.max(0, cheatCardsBought);
    }
    public boolean hasLuckySeven() { return luckySeven; }
    public void setLuckySeven(boolean luckySeven) { this.luckySeven = luckySeven; }
    public int getShopCardSlots() { return shopCardSlots; }
    /** 
     * @param slots
     */
    public void setShopCardSlots(int slots) {
        this.shopCardSlots = Math.max(DEFAULT_SHOP_CARD_SLOTS, Math.min(MAX_SHOP_CARD_SLOTS, slots));
    }
    public int getTotalRunScore() { return totalRunScore; }
    public void setTotalRunScore(int totalRunScore) { this.totalRunScore = Math.max(0, totalRunScore); }
    /** 
     * @param points
     */
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
