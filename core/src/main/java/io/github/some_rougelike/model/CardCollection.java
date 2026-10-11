package io.github.some_rougelike.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardCollection {
    private final List<Card> cards;

    public CardCollection() {
        this.cards = new ArrayList<>();
    }

    public static CardCollection createStandardDeck() {
        CardCollection collection = new CardCollection();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                collection.addCard(new Card(suit, rank));
            }
        }
        return collection;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> getCards() {
        return cards;
    }
    
}