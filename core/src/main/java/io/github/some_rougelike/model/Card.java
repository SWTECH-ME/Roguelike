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

    /** 
     * @return Suit
     */
    public Suit getSuit() { 
        return suit; 
    }

    /** 
     * @return Rank
     */
    public Rank getRank() { 
        return rank; 
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
    public int getmultiplier() { 
        return multiplier; 
    }

    /** 
     * @param multiplier
     */
    public void setmultiplier(int multiplier) { 
        this.multiplier = multiplier; 
    }

    /** 
     * @return boolean
     */
    public boolean isSelected() { 
        return selected; 
    }

    /** 
     * @param selected
     */
    public void setSelected(boolean selected) { 
        this.selected = selected; 
    }

    /** 
     * @return TextureRegion
     */
    public TextureRegion getTextureRegion() { 
        return textureRegion; 
    }

    /** 
     * @param textureRegion
     */
    public void setTextureRegion(TextureRegion textureRegion) { 
        this.textureRegion = textureRegion; 
    }

}