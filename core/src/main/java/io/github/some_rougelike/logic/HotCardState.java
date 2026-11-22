package io.github.some_rougelike.logic;

/**
 * A hot cardok futás közben gyűjtött számlálói. Egyszerű mezők, hogy a mentés (libGDX Json) közvetlenül kezelje.
 */
public class HotCardState {
    // --- blindonként nullázódik ---
    /** Crossed Blades: ebben a blindban kijátszott heartok. */
    public int heartsPlayedThisBlind;
    /** Chip Stack: ebben a blindban kapott plusz kézméret. */
    public int blindHandSizeBonus;
    /** One-Armed Bandit: ebben a blindban ki van-e kapcsolva a boss képessége. */
    public boolean bossAbilityDisabled;

    // --- a run végéig gyűlik (csak amíg a lap a játékosnál van) ---
    /** Dig Deep: kijátszott pikkek. */
    public int digDeepSpades;
    /** Open Grave: kijátszott pikkek. */
    public int openGraveSpades;
    /** Grave Lantern: pikk flushok. */
    public int graveLanternFlushes;
    /** Reaper's Crown: pikk flushok. */
    public int reaperFlushes;
    /** Black Widow: megnyert blindok. */
    public int blackWidowBlinds;
    /** Money Tree: kijátszott kezek. */
    public int moneyTreePlays;

    public HotCardState() {
    }

    public HotCardState copy() {
        HotCardState c = new HotCardState();
        c.heartsPlayedThisBlind = heartsPlayedThisBlind;
        c.blindHandSizeBonus = blindHandSizeBonus;
        c.bossAbilityDisabled = bossAbilityDisabled;
        c.digDeepSpades = digDeepSpades;
        c.openGraveSpades = openGraveSpades;
        c.graveLanternFlushes = graveLanternFlushes;
        c.reaperFlushes = reaperFlushes;
        c.blackWidowBlinds = blackWidowBlinds;
        c.moneyTreePlays = moneyTreePlays;
        return c;
    }
}
