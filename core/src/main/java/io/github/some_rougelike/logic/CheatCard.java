package io.github.some_rougelike.logic;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.save.SaveManager;

/**
 * A boltban megvásárolható cheat lapok (Cold clubs és diamonds 2-9).
 * Az apply azonnal érvényesíti a hatást; a "kör" egy megnyert harcot jelent.
 */
public enum CheatCard {
    // --- Clubs ---
    SNAKE_EYES("Snake Eyes", "Instantly get a random +$1-6", 3, "cold_clubs_2.png"),
    FIND_THE_LADY("Find the Lady", "Double or nothing on your money (max 20)", 3, "cold_clubs_3.png"),
    HORSESHOE("Horseshoe", "Skip, does nothing", 0, "cold_clubs_4.png"),
    ROYAL_FLUSH("Royal Flush", "A random combination gets +0.5x multiplier", 6, "cold_clubs_5.png"),
    LOTTERY_DRAW("Lottery Draw", "1 in 5 chance to win $20", 3, "cold_clubs_6.png"),
    LUCKY_SEVEN("Lucky Seven", "The first reroll in every shop is free", 10, "cold_clubs_7.png"),
    PRIZE_WHEEL("Prize Wheel", "Instantly get a random Hot card", 5, "cold_clubs_8_prize_wheel.png"),
    SCRATCH_AND_WIN("Scratch & Win", "The Hot card shop gets +1 slot (max 5)", 10, "cold_clubs_9.png"),

    // --- Diamonds ---
    RIGHT_EARRING("Right Earring", "One free reroll of the right side", 2, "cold_diamonds_2.png"),
    PAWN_TICKET("Pawn Ticket", "+$20 now, but -$25 after 2 rounds", 0, "cold_diamonds_3.png"),
    TIME_IS_MONEY("Time Is Money", "Put all your money aside, get it back +50% after 2 rounds", 4, "cold_diamonds_4.png"),
    COSTUME_JEWELRY("Costume Jewelry", "Everything in this shop is 25% cheaper", 4, "cold_diamonds_5.png"),
    GOLD_RUSH("Gold Rush", "+$2 at the end of every round", 6, "cold_diamonds_6.png"),
    MAGPIES_NEST("Magpie's Nest", "+$3 for every Hot card you have", 4, "cold_diamonds_7.png"),
    LAST_GRAINS("Last Grains", "If you have less than $3, get +$10", 4, "cold_diamonds_8.png"),
    GRAND_CHANDELIER("Grand Chandelier", "Rerolls cost $2 less", 8, "cold_diamonds_9.png"),

    // --- Spades: egy adott kombináció tartós szorzó-bónuszt kap ---
    PAIR_OF_EYES("Pair of Eyes", HandEvaluator.Combo.ONE_PAIR, 0.3, 3, "cold_spades_2.png"),
    THREE_FLAMES("Three Flames", HandEvaluator.Combo.THREE_OF_A_KIND, 0.5, 4, "cold_spades_3.png"),
    IRON_PAIRS("Iron Pairs", HandEvaluator.Combo.TWO_PAIR, 0.4, 3, "cold_spades_4.png"),
    FLUSH_THE_BATS("Flush the Bats", HandEvaluator.Combo.FLUSH, 0.5, 4, "cold_spades_5.png"),
    SIX_FEET_DOWN("Six Feet Down", HandEvaluator.Combo.STRAIGHT, 0.5, 4, "cold_spades_6.png"),
    SKELETON_KEYS("Skeleton Keys", HandEvaluator.Combo.FOUR_OF_A_KIND, 1.0, 5, "cold_spades_7.png"),
    STAINED_GLASS("Stained Glass", HandEvaluator.Combo.STRAIGHT_FLUSH, 1.5, 6, "cold_spades_8.png"),
    FULL_GRAVEYARD("Full Graveyard", HandEvaluator.Combo.FULL_HOUSE, 0.75, 5, "cold_spades_9.png"),

    // --- Hearts: egy adott rang lapjai tartósan +10 chipet érnek ---
    HEARTBREAK("Heartbreak", Rank.TWO, 5, "cold_hearts_2.png"),
    BALLOON_BUNCH("Balloon Bunch", Rank.THREE, 5, "cold_hearts_3.png"),
    HEARTBEAT("Heartbeat", Rank.FOUR, 5, "cold_hearts_4.png"),
    FIREFLY_JAR("Firefly Jar", Rank.FIVE, 5, "cold_hearts_5.png"),
    LOADED_DIE("Loaded Die", Rank.SIX, 5, "cold_hearts_6.png"),
    COR_SEPTEM("Cor Septem", Rank.SEVEN, 5, "cold_hearts_7.png"),
    OCTOHEART("Octoheart", Rank.EIGHT, 5, "cold_hearts_8.png"),
    NINE_LIVES("Nine Lives", Rank.NINE, 5, "cold_hearts_9.png");

    public static final int HEART_CHIP_BONUS = 10;

    /** A bolt állapota, amire néhány lap hat (ingyen reroll, kedvezmény). */
    public interface ShopContext {
        void addFreeReroll();
        void activateDiscount();
        boolean isDiscountActive();
        void onHotCardsChanged();
    }

    public static final String IMAGE_DIR = "Cheat Deck/Cold cards/";
    public static final double ROYAL_FLUSH_BONUS = 0.5;
    public static final int FIND_THE_LADY_MAX_STAKE = 20;
    public static final int LOTTERY_PRIZE = 20;
    public static final int PAWN_LOAN = 20, PAWN_REPAY = 25;
    public static final int DELAY_ROUNDS = 2;
    public static final int MAGPIE_PER_HOT_CARD = 3;
    public static final int LAST_GRAINS_LIMIT = 3, LAST_GRAINS_PRIZE = 10;

    private final String displayName;
    private final String description;
    private final int price;
    private final String imageFile;
    private final HandEvaluator.Combo comboTarget;
    private final double comboBonus;
    private final Rank rankTarget;

    CheatCard(String displayName, String description, int price, String imageFile) {
        this(displayName, description, price, imageFile, null, 0, null);
    }

    CheatCard(String displayName, HandEvaluator.Combo combo, double bonus, int price, String imageFile) {
        this(displayName, null, price, imageFile, combo, bonus, null);
    }

    CheatCard(String displayName, Rank rank, int price, String imageFile) {
        this(displayName, rank.getSymbol() + " cards give +" + HEART_CHIP_BONUS + " chips", price, imageFile, null, 0, rank);
    }

    CheatCard(String displayName, String description, int price, String imageFile,
              HandEvaluator.Combo comboTarget, double comboBonus, Rank rankTarget) {
        this.displayName = displayName;
        this.description = description;
        this.price = price;
        this.imageFile = imageFile;
        this.comboTarget = comboTarget;
        this.comboBonus = comboBonus;
        this.rankTarget = rankTarget;
    }

    public String getDisplayName() { return displayName; }

    /** Spades lapoknál a még fel nem fedezett kombináció neve rejtve marad. */
    public String getDescription() {
        if (comboTarget != null) {
            return comboName(comboTarget) + " gets +" + comboBonus + "x multiplier";
        }
        return description;
    }

    private static String comboName(HandEvaluator.Combo combo) {
        return SaveManager.getInstance().isComboDiscovered(combo) ? combo.getDisplayName() : "????";
    }
    public int getPrice() { return price; }
    public String getImagePath() { return IMAGE_DIR + imageFile; }

    /** Van-e még értelme felkínálni (egyszeri vagy limites hatások). */
    public boolean isAvailable(RunState run, ShopContext shop) {
        switch (this) {
            case LUCKY_SEVEN: return !run.hasLuckySeven();
            case SCRATCH_AND_WIN: return run.getShopCardSlots() < RunState.MAX_SHOP_CARD_SLOTS;
            case COSTUME_JEWELRY: return !shop.isDiscountActive();
            case GRAND_CHANDELIER: return run.getRerollDiscount() < RunState.BASE_REROLL_PRICE;
            case PRIZE_WHEEL: return run.hasFreeHotCardSlot();
            default: return true;
        }
    }

    public static CheatCard randomAvailable(RunState run, ShopContext shop) {
        List<CheatCard> pool = new ArrayList<>();
        for (CheatCard c : values()) {
            if (c.isAvailable(run, shop)) pool.add(c);
        }
        return pool.get(MathUtils.random(pool.size() - 1));
    }

    private static String formatMult(double m) {
        double r = Math.round(m * 100) / 100.0;
        return r == Math.floor(r) ? String.valueOf((int) r) : String.valueOf(r);
    }

    /** Érvényesíti a hatást (az ár már le van vonva), és visszaadja a kiírandó eredményt. */
    public String apply(RunState run, ShopContext shop) {
        if (comboTarget != null) {
            run.addComboBonus(comboTarget, comboBonus);
            return comboName(comboTarget) + " now x" + formatMult(run.getComboMultiplier(comboTarget));
        }
        if (rankTarget != null) {
            run.addRankChipBonus(rankTarget, HEART_CHIP_BONUS);
            return rankTarget.getSymbol() + " cards now give +" + run.getRankChipBonus(rankTarget) + " bonus chips";
        }
        switch (this) {
            case SNAKE_EYES: {
                int roll = MathUtils.random(1, 6);
                run.addMoney(roll);
                return "Snake Eyes rolled " + roll + ": +$" + roll;
            }
            case FIND_THE_LADY: {
                int stake = Math.min(run.getMoney(), FIND_THE_LADY_MAX_STAKE);
                if (stake <= 0) return "Find the Lady: nothing to bet";
                if (MathUtils.randomBoolean()) {
                    run.addMoney(stake);
                    return "Found the lady! +$" + stake;
                }
                run.spendMoney(stake);
                return "Wrong card... -$" + stake;
            }
            case HORSESHOE:
                return "Horseshoe: skipped";
            case ROYAL_FLUSH: {
                HandEvaluator.Combo[] combos = HandEvaluator.Combo.values();
                HandEvaluator.Combo combo = combos[MathUtils.random(combos.length - 1)];
                run.addComboBonus(combo, ROYAL_FLUSH_BONUS);
                String name = SaveManager.getInstance().isComboDiscovered(combo) ? combo.getDisplayName() : "????";
                return name + " gets +" + ROYAL_FLUSH_BONUS + "x multiplier";
            }
            case LOTTERY_DRAW:
                if (MathUtils.random(4) == 0) {
                    run.addMoney(LOTTERY_PRIZE);
                    return "Lottery jackpot! +$" + LOTTERY_PRIZE;
                }
                return "Lottery Draw: no luck this time";
            case LUCKY_SEVEN:
                run.setLuckySeven(true);
                return "Lucky Seven: first reroll is free in every shop";
            case PRIZE_WHEEL: {
                if (!run.hasFreeHotCardSlot()) return "Prize Wheel: your Hot card slots are full";
                HotCard prize = HotCard.randomExcluding(run.getHotCards());
                if (prize == null) return "Prize Wheel: no Hot cards left";
                run.addHotCard(prize.getId());
                shop.onHotCardsChanged();
                return "Prize Wheel: you got " + prize.getDisplayName();
            }
            case SCRATCH_AND_WIN:
                run.setShopCardSlots(run.getShopCardSlots() + 1);
                return "Scratch & Win: the Hot card shop now has " + run.getShopCardSlots() + " slots";

            case RIGHT_EARRING:
                shop.addFreeReroll();
                return "Right Earring: next reroll is free";
            case PAWN_TICKET:
                run.addMoney(PAWN_LOAN);
                run.addDelayedMoney(DELAY_ROUNDS, -PAWN_REPAY);
                return "Pawn Ticket: +$" + PAWN_LOAN + ", pay back $" + PAWN_REPAY + " in " + DELAY_ROUNDS + " rounds";
            case TIME_IS_MONEY: {
                int saved = run.getMoney();
                if (saved <= 0) return "Time Is Money: nothing to put aside";
                int payout = (int) Math.round(saved * 1.5);
                run.spendMoney(saved);
                run.addDelayedMoney(DELAY_ROUNDS, payout);
                return "Time Is Money: $" + saved + " put aside, $" + payout + " back in " + DELAY_ROUNDS + " rounds";
            }
            case COSTUME_JEWELRY:
                shop.activateDiscount();
                return "Costume Jewelry: everything here is 25% cheaper";
            case GOLD_RUSH:
                run.addGoldRush();
                return "Gold Rush: +$" + RunState.GOLD_RUSH_PER_ROUND * run.getGoldRushCount() + " every round";
            case MAGPIES_NEST: {
                int owned = run.getHotCards().size();
                int gain = MAGPIE_PER_HOT_CARD * owned;
                run.addMoney(gain);
                return "Magpie's Nest: " + owned + " Hot cards, +$" + gain;
            }
            case LAST_GRAINS:
                if (run.getMoney() < LAST_GRAINS_LIMIT) {
                    run.addMoney(LAST_GRAINS_PRIZE);
                    return "Last Grains: +$" + LAST_GRAINS_PRIZE;
                }
                return "Last Grains: you still have $" + LAST_GRAINS_LIMIT + " or more";
            case GRAND_CHANDELIER:
                run.addRerollDiscount(2);
                return "Grand Chandelier: rerolls now cost $" + run.getRerollPrice();
            default:
                return "";
        }
    }
}
