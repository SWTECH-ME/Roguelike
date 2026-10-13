package io.github.some_rougelike.model;

public enum Suit {
    HEARTS("Hearts", "red"),
    DIAMONDS("Diamonds", "red"),
    CLUBS("Clubs", "black"),
    SPADES("Spades", "black");

    private final String displayName;
    private final String color;

    Suit(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName; 
    }
    
    public String getColor() {
        return color; 
    }

}