package io.github.some_rougelike.save;

import java.util.ArrayList;
import java.util.List;
import io.github.some_rougelike.model.Card;

public class CombatSaveData {
    private int currentScore;
    private int playsLeft;
    private int discardsLeft;
    private ArrayList<CardSaveData> drawPile = new ArrayList<>();
    private ArrayList<CardSaveData> hand = new ArrayList<>();
    private ArrayList<Integer> selectedHandIndices = new ArrayList<>();

    public CombatSaveData() {
    }

    public static CombatSaveData create(int currentScore, int playsLeft, int discardsLeft, List<Card> drawPileCards, List<Card> handCards, List<Card> selectedCards) {
        CombatSaveData data = new CombatSaveData();
        data.currentScore = currentScore;
        data.playsLeft = playsLeft;
        data.discardsLeft = discardsLeft;

        if (drawPileCards != null) {
            for (Card card : drawPileCards) {
                data.drawPile.add(CardSaveData.fromCard(card));
            }
        }

        if (handCards != null) {
            for (int i = 0; i < handCards.size(); i++) {
                Card card = handCards.get(i);
                data.hand.add(CardSaveData.fromCard(card));
                if (selectedCards != null && selectedCards.contains(card)) {
                    data.selectedHandIndices.add(i);
                }
            }
        }

        return data;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getPlaysLeft() {
        return playsLeft;
    }

    public void setPlaysLeft(int playsLeft) {
        this.playsLeft = playsLeft;
    }

    public int getDiscardsLeft() {
        return discardsLeft;
    }

    public void setDiscardsLeft(int discardsLeft) {
        this.discardsLeft = discardsLeft;
    }

    public ArrayList<CardSaveData> getDrawPile() {
        return drawPile;
    }

    public void setDrawPile(ArrayList<CardSaveData> drawPile) {
        this.drawPile = drawPile;
    }

    public ArrayList<CardSaveData> getHand() {
        return hand;
    }

    public void setHand(ArrayList<CardSaveData> hand) {
        this.hand = hand;
    }

    public ArrayList<Integer> getSelectedHandIndices() {
        return selectedHandIndices;
    }

    public void setSelectedHandIndices(ArrayList<Integer> selectedHandIndices) {
        this.selectedHandIndices = selectedHandIndices;
    }
}
