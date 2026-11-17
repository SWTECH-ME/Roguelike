package io.github.some_rougelike.logic;

import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.model.Suit;

public enum BossAbilityType {
    NONE(""),
    HIDDEN_SUITS("Card colors are hidden."),
    NO_DISCARDS("No discards allowed."),
    MAX_SELECT_FOUR("Max 4 cards can be played per hand."),
    SMALLER_HAND("Hand size reduced by 2 cards."),
    ONE_LESS_PLAY("1 fewer play available."),
    RED_CARDS_HALVED("Red cards score half chips."),
    BLACK_CARDS_HALVED("Black cards score half chips."),
    FACE_CARDS_NERFED("Face cards (J, Q, K) only give 5 chips."),
    NUMBER_CARDS_PENALTY("Number cards (2-9) give -2 chips."),
    CAP_MULTIPLIER("Hand multiplier is capped at 3x."),
    NO_DOUBLE_ROLLS("Critical x2 chip rolls are disabled."),
    FORCED_RANDOM_DISCARD("Playing a hand discards 1 random card.");

    private final String description;

    BossAbilityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int modifyMaxPlays(int basePlays) {
        if (this == ONE_LESS_PLAY) {
            return Math.max(1, basePlays - GameConfig.BOSS_PLAYS_PENALTY);
        }
        return basePlays;
    }

    public int modifyMaxDiscards(int baseDiscards) {
        if (this == NO_DISCARDS) {
            return 0;
        }
        return baseDiscards;
    }

    public int modifyHandSize(int baseHandSize) {
        if (this == SMALLER_HAND) {
            return Math.max(1, baseHandSize - GameConfig.BOSS_HAND_SIZE_PENALTY);
        }
        return baseHandSize;
    }

    public int getMaxSelectableCards() {
        if (this == MAX_SELECT_FOUR) {
            return GameConfig.BOSS_REDUCED_MAX_SELECT_CARDS;
        }
        return GameConfig.DEFAULT_MAX_SELECTABLE_CARDS;
    }

    public boolean isDoubleRollAllowed() {
        return this != NO_DOUBLE_ROLLS;
    }

    public boolean areCardColorsHidden() {
        return this == HIDDEN_SUITS;
    }

    public boolean triggersRandomDiscardOnPlay() {
        return this == FORCED_RANDOM_DISCARD;
    }

    public int modifyCardChips(Card card, int baseChips) {
        if (card == null) {
            return baseChips;
        }
        switch (this) {
            case RED_CARDS_HALVED:
                if (card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.DIAMONDS) {
                    return Math.max(1, baseChips / 2);
                }
                return baseChips;
            case BLACK_CARDS_HALVED:
                if (card.getSuit() == Suit.CLUBS || card.getSuit() == Suit.SPADES) {
                    return Math.max(1, baseChips / 2);
                }
                return baseChips;
            case FACE_CARDS_NERFED:
                Rank r = card.getRank();
                if (r == Rank.JACK || r == Rank.QUEEN || r == Rank.KING) {
                    return GameConfig.BOSS_FACE_CARD_NERF_CHIPS;
                }
                return baseChips;
            case NUMBER_CARDS_PENALTY:
                Rank rank = card.getRank();
                if (rank != Rank.TEN && rank != Rank.JACK && rank != Rank.QUEEN && rank != Rank.KING && rank != Rank.ACE) {
                    return Math.max(1, baseChips - GameConfig.BOSS_NUMBER_CARD_CHIP_PENALTY);
                }
                return baseChips;
            default:
                return baseChips;
        }
    }

    public int modifyHandMultiplier(int baseMultiplier) {
        if (this == CAP_MULTIPLIER) {
            return Math.min(baseMultiplier, GameConfig.BOSS_MAX_MULTIPLIER_CAP);
        }
        return baseMultiplier;
    }

    public static BossAbilityType fromNameOrDescription(String abilityTypeName, String specialAbilityText) {
        if (abilityTypeName != null && !abilityTypeName.isEmpty()) {
            try {
                return BossAbilityType.valueOf(abilityTypeName);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (specialAbilityText != null && !specialAbilityText.isEmpty()) {
            for (BossAbilityType type : values()) {
                if (type != NONE && type.description.equalsIgnoreCase(specialAbilityText)) {
                    return type;
                }
            }
        }
        return NONE;
    }
}
