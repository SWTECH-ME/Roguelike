package io.github.some_rougelike.save;

import io.github.some_rougelike.model.Card;
import io.github.some_rougelike.model.Rank;
import io.github.some_rougelike.model.Suit;

public class CardSaveData {
    private String suit;
    private String rank;
    private int chips;
    private int multiplier;

    public CardSaveData() {
    }

    public CardSaveData(String suit, String rank, int chips, int multiplier) {
        this.suit = suit;
        this.rank = rank;
        this.chips = chips;
        this.multiplier = multiplier;
    }

    /** 
     * @param card
     * @return CardSaveData
     */
    public static CardSaveData fromCard(Card card) {
        return new CardSaveData(
            card.getSuit().name(),
            card.getRank().name(),
            card.getChips(),
            card.getmultiplier()
        );
    }

    /** 
     * @return Card
     */
    public Card toCard() {
        Suit cardSuit = Suit.valueOf(suit);
        Rank cardRank = Rank.valueOf(rank);
        Card card = new Card(cardSuit, cardRank);
        card.setChips(chips);
        card.setmultiplier(multiplier);
        return card;
    }

    /** 
     * @return String
     */
    public String getSuit() {
        return suit;
    }

    /** 
     * @param suit
     */
    public void setSuit(String suit) {
        this.suit = suit;
    }

    /** 
     * @return String
     */
    public String getRank() {
        return rank;
    }

    /** 
     * @param rank
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /** 
     * @return int
     */
    public int getChips() {
        return chips;
    }

    /** 
     * @param chips
     */
    public void setChips(int chips) {
        this.chips = chips;
    }

    /** 
     * @return int
     */
    public int getMultiplier() {
        return multiplier;
    }

    /** 
     * @param multiplier
     */
    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
}
