package io.github.some_rougelike.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Card {
    private final Suit suit;
    private final Rank rank;
    private int chips;
    private int multiplier;
    private boolean selected;
    private TextureRegion textureRegion;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.chips = rank.getBaseValue();
        this.multiplier = 0;
        this.selected = false;
    }

    public Suit getSuit() { 
        return suit; 
    }

    public Rank getRank() { 
        return rank; 
    }

    public int getChips() { 
        return chips; 
    }

    public void setChips(int chips) { 
        this.chips = chips; 
    }

    public int getmultiplier() { 
        return multiplier; 
    }

    public void setmultiplier(int multiplier) { 
        this.multiplier = multiplier; 
    }

    public boolean isSelected() { 
        return selected; 
    }

    public void setSelected(boolean selected) { 
        this.selected = selected; 
    }

    public TextureRegion getTextureRegion() { 
        return textureRegion; 
    }

    public void setTextureRegion(TextureRegion textureRegion) { 
        this.textureRegion = textureRegion; 
    }

}