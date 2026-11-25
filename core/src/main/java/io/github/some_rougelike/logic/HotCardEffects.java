package io.github.some_rougelike.logic;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.model.Suit;

/**
 * A hot cardok hatásai. A harc három ponton hívja: blind elején, minden kijátszott kéznél és győzelemkor.
 * A run-hosszú számlálók csak addig nőnek, amíg a lap a játékosnál van.
 */
public final class HotCardEffects {
    public static final int CROSSED_BLADES_HEARTS = 5;
    public static final int IRON_HEART_CHIPS = 80;
    public static final int HOUSE_OF_CARDS_CHIPS = 30;
    public static final int CARDIAC_KING_CHIPS = 111;
    public static final float STRIKE_GOLD_CHANCE = 0.2f;
    public static final int BULLION_MONEY = 3;
    public static final int DIAMOND_RING_MONEY = 2;
    public static final int VAULT_MONEY = 20;
    public static final int DIG_DEEP_CHIPS = 2;
    public static final int GRAVE_LANTERN_CHIPS = 30;
    public static final int BLACK_WIDOW_CHIPS = 10;
    public static final double REAPER_MULT = 0.5;
    public static final double OPEN_GRAVE_MULT = 0.1;
    public static final int CLOVER_MIN = 10, CLOVER_MAX = 100;
    public static final int MONEY_TREE_EVERY = 4;
    public static final int CHIP_STACK_MAX = 3;
    public static final float BANDIT_CHANCE = 0.4f;
    public static final double ROULETTE_MIN = 0.3, ROULETTE_MAX = 1.3;

    private HotCardEffects() {}

    /** Egy kijátszott kéz hot card eredménye; a pénz és a számlálók már jóvá vannak írva a run-ban. */
    public static final class HandResult {
        /** Lapos plusz chip lapjanként (a kijátszott lapok sorrendjében). */
        public final int[] cardChips;
        /** A kézhez egyszer hozzáadott plusz chip. */
        public int flatChips;
        /** A szorzóhoz hozzáadott plusz. */
        public double multBonus;
        public int money;
        public int extraDiscards;
        /** Kapott-e új hot cardot (Money Tree), a sávot frissíteni kell. */
        public boolean hotCardsChanged;
        public final List<String> notes = new ArrayList<>();

        HandResult(int cards) {
            cardChips = new int[cards];
        }
    }

    /** 
     * @param run
     * @return boolean
     */
    public static boolean allCountAsHearts(RunState run) {
        return run.hasHotCard(HotCard.BLEEDING_HEART);
    }

    /** A kombináció a Bleeding Heartot is figyelembe véve. */
    public static HandEvaluator.Combo evaluate(RunState run, List<Card> cards) {
        return HandEvaluator.evaluate(cards, allCountAsHearts(run));
    }

    /** 
     * @param run
     * @param c
     * @return boolean
     */
    private static boolean isHeart(RunState run, Card c) {
        return c.getSuit() == Suit.HEARTS || allCountAsHearts(run);
    }

    /** 
     * @param c
     * @return boolean
     */
    private static boolean isFace(Card c) {
        return c.getRank() == Rank.JACK || c.getRank() == Rank.QUEEN || c.getRank() == Rank.KING;
    }

    /** 
     * @param combo
     * @return boolean
     */
    private static boolean isFlush(HandEvaluator.Combo combo) {
        return combo == HandEvaluator.Combo.FLUSH
            || combo == HandEvaluator.Combo.STRAIGHT_FLUSH
            || combo == HandEvaluator.Combo.ROYAL_FLUSH;
    }

    // ---------------------------------------------------------------- blind start / end

    /** Új blind kezdetekor (nem mentésből folytatva). Visszaadja a kiírandó üzeneteket. */
    public static List<String> onBlindStart(RunState run, Enemy enemy) {
        HotCardState st = run.getHotState();
        List<String> notes = new ArrayList<>();
        st.heartsPlayedThisBlind = 0;
        st.blindHandSizeBonus = 0;
        st.bossAbilityDisabled = false;

        if (run.hasHotCard(HotCard.CHIP_STACK)) {
            st.blindHandSizeBonus = MathUtils.random(0, CHIP_STACK_MAX);
            notes.add("Chip Stack: +" + st.blindHandSizeBonus + " hand size");
        }
        if (enemy != null && enemy.isBoss() && enemy.getAbilityType() != BossAbilityType.NONE
                && run.hasHotCard(HotCard.ONE_ARMED_BANDIT)) {
            st.bossAbilityDisabled = MathUtils.random() < BANDIT_CHANCE;
            notes.add(st.bossAbilityDisabled ? "One-Armed Bandit: boss ability disabled!" : "One-Armed Bandit: no luck");
        }
        return notes;
    }

    /** Megnyert blind után. */
    public static void onBlindWon(RunState run) {
        if (run.hasHotCard(HotCard.BLACK_WIDOW)) {
            run.getHotState().blackWidowBlinds++;
        }
    }

    /** 
     * @param run
     * @param played
     * @param combo
     * @param cardsLeftInDeck
     * @return HandResult
     */
    // ---------------------------------------------------------------- hand played

    public static HandResult onHandPlayed(RunState run, List<Card> played, HandEvaluator.Combo combo, int cardsLeftInDeck) {
        HotCardState st = run.getHotState();
        HandResult r = new HandResult(played.size());

        int hearts = 0, spades = 0;
        boolean allHearts = !played.isEmpty(), allSpades = !played.isEmpty(), allDiamonds = !played.isEmpty();
        boolean anyHeart = false, aceOfDiamonds = false;
        for (Card c : played) {
            boolean heart = isHeart(run, c);
            if (heart) { hearts++; anyHeart = true; } else { allHearts = false; }
            if (c.getSuit() == Suit.SPADES) spades++; else allSpades = false;
            if (c.getSuit() != Suit.DIAMONDS) allDiamonds = false;
            if (c.getSuit() == Suit.DIAMONDS && c.getRank() == Rank.ACE) aceOfDiamonds = true;
        }
        boolean flush = isFlush(combo);
        boolean heartFlush = flush && allHearts;
        boolean spadeFlush = flush && allSpades;
        boolean diamondFlush = flush && allDiamonds;

        // --- Hearts ---
        if (run.hasHotCard(HotCard.CROSSED_BLADES) && hearts > 0) {
            int before = st.heartsPlayedThisBlind / CROSSED_BLADES_HEARTS;
            st.heartsPlayedThisBlind += hearts;
            int gained = st.heartsPlayedThisBlind / CROSSED_BLADES_HEARTS - before;
            if (gained > 0) {
                r.extraDiscards += gained;
                r.notes.add("Crossed Blades +" + gained + " discard");
            }
        }
        if (run.hasHotCard(HotCard.IRON_HEART) && heartFlush) {
            r.flatChips += IRON_HEART_CHIPS;
            r.notes.add("Iron Heart +" + IRON_HEART_CHIPS);
        }
        if (run.hasHotCard(HotCard.HOUSE_OF_CARDS)) {
            for (int i = 0; i < played.size(); i++) {
                Card c = played.get(i);
                if (isFace(c) && isHeart(run, c)) r.cardChips[i] += HOUSE_OF_CARDS_CHIPS;
            }
        }
        if (run.hasHotCard(HotCard.CARDIAC_KING) && combo == HandEvaluator.Combo.FULL_HOUSE && anyHeart) {
            r.flatChips += CARDIAC_KING_CHIPS;
            r.notes.add("Cardiac King +" + CARDIAC_KING_CHIPS);
        }

        // --- Spades ---
        if (run.hasHotCard(HotCard.BLACK_WIDOW) && st.blackWidowBlinds > 0) {
            for (int i = 0; i < played.size(); i++) {
                if (played.get(i).getSuit() == Suit.SPADES) r.cardChips[i] += BLACK_WIDOW_CHIPS * st.blackWidowBlinds;
            }
        }
        if (run.hasHotCard(HotCard.DIG_DEEP)) {
            st.digDeepSpades += spades;
            if (st.digDeepSpades > 0) {
                r.flatChips += DIG_DEEP_CHIPS * st.digDeepSpades;
                r.notes.add("Dig Deep +" + DIG_DEEP_CHIPS * st.digDeepSpades);
            }
        }
        if (run.hasHotCard(HotCard.GRAVE_LANTERN)) {
            if (spadeFlush) st.graveLanternFlushes++;
            if (st.graveLanternFlushes > 0) {
                r.flatChips += GRAVE_LANTERN_CHIPS * st.graveLanternFlushes;
                r.notes.add("Grave Lantern +" + GRAVE_LANTERN_CHIPS * st.graveLanternFlushes);
            }
        }
        if (run.hasHotCard(HotCard.REAPERS_CROWN)) {
            if (spadeFlush) st.reaperFlushes++;
            if (st.reaperFlushes > 0) {
                r.multBonus += REAPER_MULT * st.reaperFlushes;
                r.notes.add("Reaper's Crown +" + fmt(REAPER_MULT * st.reaperFlushes) + "x");
            }
        }
        if (run.hasHotCard(HotCard.OPEN_GRAVE)) {
            st.openGraveSpades += spades;
            if (st.openGraveSpades > 0) {
                r.multBonus += OPEN_GRAVE_MULT * st.openGraveSpades;
                r.notes.add("Open Grave +" + fmt(OPEN_GRAVE_MULT * st.openGraveSpades) + "x");
            }
        }

        // --- Clubs ---
        if (run.hasHotCard(HotCard.FOUR_LEAF_CLOVER)) {
            int bonus = MathUtils.random(CLOVER_MIN, CLOVER_MAX);
            r.flatChips += bonus;
            r.notes.add("Four-Leaf Clover +" + bonus);
        }
        if (run.hasHotCard(HotCard.ROULETTE)) {
            double bonus = Math.round(MathUtils.random((float) ROULETTE_MIN, (float) ROULETTE_MAX) * 10) / 10.0;
            r.multBonus += bonus;
            r.notes.add("Roulette +" + fmt(bonus) + "x");
        }
        if (run.hasHotCard(HotCard.MONEY_TREE)) {
            st.moneyTreePlays++;
            if (st.moneyTreePlays % MONEY_TREE_EVERY == 0) {
                HotCard gift = run.hasFreeHotCardSlot() ? HotCard.randomExcluding(run.getHotCards()) : null;
                if (gift != null && run.addHotCard(gift.getId())) {
                    r.hotCardsChanged = true;
                    r.notes.add("Money Tree: " + gift.getDisplayName());
                } else {
                    r.notes.add("Money Tree: no free Hot card slot");
                }
            }
        }

        // --- Diamonds (pénz; a Flawless Cut a legvégén, hogy a többi pénz is duplázódjon) ---
        if (run.hasHotCard(HotCard.STRIKE_GOLD)) {
            for (Card c : played) {
                if (MathUtils.random() < STRIKE_GOLD_CHANCE) r.money += c.getRank().getBaseValue();
            }
            if (r.money > 0) r.notes.add("Strike Gold +$" + r.money);
        }
        if (run.hasHotCard(HotCard.BULLION)) {
            int gain = 0;
            for (Card c : played) {
                if (c.getSuit() == Suit.DIAMONDS && isFace(c)) gain += BULLION_MONEY;
            }
            if (gain > 0) { r.money += gain; r.notes.add("Bullion +$" + gain); }
        }
        if (run.hasHotCard(HotCard.DIAMOND_RING)) {
            int gain = 0;
            for (Card c : played) {
                if (c.getSuit() == Suit.DIAMONDS) gain += DIAMOND_RING_MONEY;
            }
            if (gain > 0) { r.money += gain; r.notes.add("Diamond Ring +$" + gain); }
        }
        if (run.hasHotCard(HotCard.THE_VAULT) && diamondFlush) {
            r.money += VAULT_MONEY;
            r.notes.add("The Vault +$" + VAULT_MONEY);
        }
        run.addMoney(r.money);

        if (run.hasHotCard(HotCard.FLAWLESS_CUT) && aceOfDiamonds) {
            int chance = Math.max(0, Math.min(100, cardsLeftInDeck));
            if (MathUtils.random(99) < chance) {
                int doubled = run.getMoney();
                run.addMoney(doubled);
                r.money += doubled;
                r.notes.add("Flawless Cut (" + chance + "%): money doubled!");
            } else {
                r.notes.add("Flawless Cut (" + chance + "%): no luck");
            }
        }
        return r;
    }

    /** 
     * @param m
     * @return String
     */
    private static String fmt(double m) {
        double r = Math.round(m * 100) / 100.0;
        return r == Math.floor(r) ? String.valueOf((int) r) : String.valueOf(r);
    }
}
