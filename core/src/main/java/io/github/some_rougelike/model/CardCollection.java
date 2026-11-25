package io.github.some_rougelike.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardCollection {
    private final List<Card> cards;

    public CardCollection() {
        this.cards = new ArrayList<>();
    }

    /** 
     * @return CardCollection
     */
    public static CardCollection createStandardDeck() {
        CardCollection collection = new CardCollection();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                collection.addCard(new Card(suit, rank));
            }
        }
        return collection;
    }

    /** 
     * @param card
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    /** 
     * @return List<Card>
     */
    public List<Card> getCards() {
        return cards;
    }
    
}