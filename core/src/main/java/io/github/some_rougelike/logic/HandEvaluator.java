package io.github.some_rougelike.logic;

import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.model.Suit;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A kijátszott lapokból meghatározza a kombinációt.
 * Ha egy kéz egyszerre több kombinációnak is megfelel, mindig a legerősebb számít
 * (a {@link Combo} felsorolás sorrendje az erősségi sorrend).
 */
public final class HandEvaluator {

    /** Gyengétől az erősig; a sorszám adja az erősséget. */
    public enum Combo {
        HIGH_CARD("High Card", 1),
        ONE_PAIR("One Pair", 1.5),
        TWO_PAIR("Two Pair", 2),
        BLACK_JACK("Black Jack", 2.5),
        THREE_OF_A_KIND("Three of a Kind", 3),
        FIVE_ODD("5 Odd", 3.5),
        FIVE_EVEN("5 Even", 3.5),
        STRAIGHT("Straight", 4),
        FLUSH("Flush", 4.5),
        FULL_HOUSE("Full House", 5),
        PRIME_FIVE("Prime 5", 5.5),
        ROYAL_HOUSE("Royal House", 6),
        FOUR_OF_A_KIND("Four of a Kind", 6.5),
        STRAIGHT_FLUSH("Straight Flush", 7),
        ROYAL_FLUSH("Royal Flush", 7.5);

        private final String displayName;
        private final double multiplier;

        Combo(String displayName, double multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }

        public String getDisplayName() { return displayName; }
        public double getMultiplier() { return multiplier; }
    }

    private static final Set<Rank> ODD = EnumSet.of(Rank.ACE, Rank.THREE, Rank.FIVE, Rank.SEVEN, Rank.NINE);
    private static final Set<Rank> EVEN = EnumSet.of(Rank.TWO, Rank.FOUR, Rank.SIX, Rank.EIGHT, Rank.TEN);
    private static final Set<Rank> PRIME = EnumSet.of(Rank.TWO, Rank.THREE, Rank.FIVE, Rank.SEVEN);
    private static final Set<Rank> FACE = EnumSet.of(Rank.JACK, Rank.QUEEN, Rank.KING);

    private HandEvaluator() {}

    /** Üres kézre null-t ad. */
    public static Combo evaluate(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return null;

        int n = cards.size();
        Map<Rank, Integer> counts = new EnumMap<>(Rank.class);
        for (Card c : cards) counts.merge(c.getRank(), 1, Integer::sum);

        int maxSame = 0, pairs = 0, triples = 0;
        for (int v : counts.values()) {
            maxSame = Math.max(maxSame, v);
            if (v == 2) pairs++;
            if (v == 3) triples++;
        }

        boolean five = n == 5;
        boolean sameSuit = five && allSameSuit(cards);
        boolean straight = five && isStraight(counts);
        boolean royalStraight = straight && counts.containsKey(Rank.TEN) && counts.containsKey(Rank.ACE);

        Combo best = Combo.HIGH_CARD;
        best = max(best, pairs >= 1 || maxSame >= 2 ? Combo.ONE_PAIR : null);
        best = max(best, pairs >= 2 || (maxSame >= 4 && n >= 4) ? Combo.TWO_PAIR : null);
        best = max(best, n == 2 && isBlackJack(cards) ? Combo.BLACK_JACK : null);
        best = max(best, maxSame >= 3 ? Combo.THREE_OF_A_KIND : null);
        best = max(best, five && onlyRanks(counts, ODD) ? Combo.FIVE_ODD : null);
        best = max(best, five && onlyRanks(counts, EVEN) ? Combo.FIVE_EVEN : null);
        best = max(best, straight ? Combo.STRAIGHT : null);
        best = max(best, sameSuit ? Combo.FLUSH : null);
        best = max(best, five && triples == 1 && pairs == 1 ? Combo.FULL_HOUSE : null);
        best = max(best, five && onlyRanks(counts, PRIME) ? Combo.PRIME_FIVE : null);
        best = max(best, five && onlyRanks(counts, FACE) ? Combo.ROYAL_HOUSE : null);
        best = max(best, maxSame >= 4 ? Combo.FOUR_OF_A_KIND : null);
        best = max(best, straight && sameSuit ? Combo.STRAIGHT_FLUSH : null);
        best = max(best, royalStraight && sameSuit ? Combo.ROYAL_FLUSH : null);
        return best;
    }

    private static Combo max(Combo a, Combo b) {
        if (b == null) return a;
        return b.ordinal() > a.ordinal() ? b : a;
    }

    private static boolean allSameSuit(List<Card> cards) {
        Suit s = cards.get(0).getSuit();
        for (Card c : cards) if (c.getSuit() != s) return false;
        return true;
    }

    private static boolean onlyRanks(Map<Rank, Integer> counts, Set<Rank> allowed) {
        return allowed.containsAll(counts.keySet());
    }

    private static boolean isBlackJack(List<Card> cards) {
        Card a = cards.get(0), b = cards.get(1);
        return (a.getRank() == Rank.ACE && b.getRank().getBaseValue() == 10)
            || (b.getRank() == Rank.ACE && a.getRank().getBaseValue() == 10);
    }

    /** 5 különböző rang egymás után; az Ász alacsony (A-2-3-4-5) és magas (10-J-Q-K-A) is lehet. */
    private static boolean isStraight(Map<Rank, Integer> counts) {
        if (counts.size() != 5) return false;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Rank r : counts.keySet()) {
            min = Math.min(min, r.ordinal());
            max = Math.max(max, r.ordinal());
        }
        if (max - min == 4) return true;
        return counts.containsKey(Rank.ACE) && counts.containsKey(Rank.TWO) && counts.containsKey(Rank.THREE)
            && counts.containsKey(Rank.FOUR) && counts.containsKey(Rank.FIVE);
    }
}