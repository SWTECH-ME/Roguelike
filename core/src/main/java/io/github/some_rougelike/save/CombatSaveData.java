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

    /** 
     * @param currentScore
     * @param playsLeft
     * @param discardsLeft
     * @param drawPileCards
     * @param handCards
     * @param selectedCards
     * @return CombatSaveData
     */
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

    /** 
     * @return int
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /** 
     * @param currentScore
     */
    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    /** 
     * @return int
     */
    public int getPlaysLeft() {
        return playsLeft;
    }

    /** 
     * @param playsLeft
     */
    public void setPlaysLeft(int playsLeft) {
        this.playsLeft = playsLeft;
    }

    /** 
     * @return int
     */
    public int getDiscardsLeft() {
        return discardsLeft;
    }

    /** 
     * @param discardsLeft
     */
    public void setDiscardsLeft(int discardsLeft) {
        this.discardsLeft = discardsLeft;
    }

    /** 
     * @return ArrayList<CardSaveData>
     */
    public ArrayList<CardSaveData> getDrawPile() {
        return drawPile;
    }

    /** 
     * @param drawPile
     */
    public void setDrawPile(ArrayList<CardSaveData> drawPile) {
        this.drawPile = drawPile;
    }

    /** 
     * @return ArrayList<CardSaveData>
     */
    public ArrayList<CardSaveData> getHand() {
        return hand;
    }

    /** 
     * @param hand
     */
    public void setHand(ArrayList<CardSaveData> hand) {
        this.hand = hand;
    }

    /** 
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getSelectedHandIndices() {
        return selectedHandIndices;
    }

    /** 
     * @param selectedHandIndices
     */
    public void setSelectedHandIndices(ArrayList<Integer> selectedHandIndices) {
        this.selectedHandIndices = selectedHandIndices;
    }
}
