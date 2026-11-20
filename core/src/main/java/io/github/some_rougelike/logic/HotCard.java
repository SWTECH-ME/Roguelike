package io.github.some_rougelike.logic;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hot cardok (10, J, Q, K, A minden színben): a jobb oldali boltban vehetők,
 * és legfeljebb {@link RunState#MAX_HOT_CARDS} darab lehet a játékosnál. A hatásuk a {@link HotCardEffects}-ben van.
 */
public final class HotCard {
    public static final String IMAGE_DIR = "Cheat Deck/Hot cards/";
    /** A táblázatban megadott árak szorzója. */
    public static final int PRICE_MULTIPLIER = 2;

    // --- Hearts ---
    public static final String CROSSED_BLADES = "hot_hearts_10";
    public static final String IRON_HEART = "hot_hearts_jack";
    public static final String HOUSE_OF_CARDS = "hot_hearts_queen";
    public static final String CARDIAC_KING = "hot_hearts_king";
    public static final String BLEEDING_HEART = "hot_hearts_ace";
    // --- Diamonds ---
    public static final String STRIKE_GOLD = "hot_diamonds_10";
    public static final String BULLION = "hot_diamonds_jack";
    public static final String DIAMOND_RING = "hot_diamonds_queen";
    public static final String THE_VAULT = "hot_diamonds_king";
    public static final String FLAWLESS_CUT = "hot_diamonds_ace";
    // --- Spades ---
    public static final String DIG_DEEP = "hot_spades_10";
    public static final String GRAVE_LANTERN = "hot_spades_jack";
    public static final String BLACK_WIDOW = "hot_spades_queen";
    public static final String REAPERS_CROWN = "hot_spades_king";
    public static final String OPEN_GRAVE = "hot_spades_ace";
    // --- Clubs ---
    public static final String FOUR_LEAF_CLOVER = "hot_clubs_10";
    public static final String MONEY_TREE = "hot_clubs_jack";
    public static final String CHIP_STACK = "hot_clubs_queen";
    public static final String ONE_ARMED_BANDIT = "hot_clubs_king";
    public static final String ROULETTE = "hot_clubs_ace";

    private static final Map<String, HotCard> ALL = new LinkedHashMap<>();

    static {
        add(CROSSED_BLADES, "Crossed Blades", "+1 discard after every 5 hearts played in a blind", 5);
        add(IRON_HEART, "Iron Heart", "Hearts flush: +80 chips", 6);
        add(HOUSE_OF_CARDS, "House of Cards", "Heart face cards give +30 chips", 8);
        add(CARDIAC_KING, "Cardiac King", "Full House with a heart in it: +111 chips", 12);
        add(BLEEDING_HEART, "Bleeding Heart", "Every card also counts as a heart", 15);

        add(STRIKE_GOLD, "Strike Gold", "Played cards have a 20% chance to pay their value in $", 5);
        add(BULLION, "Bullion", "Diamond face cards give +$3", 6);
        add(DIAMOND_RING, "Diamond Ring", "Every diamond card gives +$2", 8);
        add(THE_VAULT, "The Vault", "Diamond flush: +$20", 12);
        add(FLAWLESS_CUT, "Flawless Cut", "Playing the Ace of Diamonds: chance to double your $ (cards left in deck = %)", 15);

        add(DIG_DEEP, "Dig Deep", "+2 chips per hand for every spade played this run", 5);
        add(GRAVE_LANTERN, "Grave Lantern", "Every spade flush gives +30 chips to all future hands", 6);
        add(BLACK_WIDOW, "Black Widow", "Spade cards gain +10 chips after every blind", 8);
        add(REAPERS_CROWN, "Reaper's Crown", "Every spade flush gives +0.5x to all future hands", 12);
        add(OPEN_GRAVE, "Open Grave", "+0.1x per hand for every spade played this run", 15);

        add(FOUR_LEAF_CLOVER, "Four-Leaf Clover", "Every hand gets a random +10 to +100 chips", 5);
        add(MONEY_TREE, "Money Tree", "Every 4th hand played gives a random Hot card", 6);
        add(CHIP_STACK, "Chip Stack", "Every blind: random +0 to +3 hand size", 8);
        add(ONE_ARMED_BANDIT, "One-Armed Bandit", "40% chance to disable the boss ability", 12);
        add(ROULETTE, "Roulette", "Every hand gets a random +0.3x to +1.3x", 15);
    }

    private static void add(String id, String name, String description, int price) {
        ALL.put(id, new HotCard(id, name, description, price * PRICE_MULTIPLIER));
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final int price;

    private HotCard(String id, String displayName, String description, int price) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.price = price;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImagePath() { return IMAGE_DIR + id + ".png"; }

    public static HotCard byId(String id) { return id == null ? null : ALL.get(id); }

    public static Collection<HotCard> all() { return Collections.unmodifiableCollection(ALL.values()); }

    /** Véletlen hot card, ami nincs az excluded azonosítók között; null, ha nincs ilyen. */
    public static HotCard randomExcluding(Collection<String> excludedIds) {
        List<HotCard> pool = new ArrayList<>();
        for (HotCard c : ALL.values()) {
            if (!excludedIds.contains(c.id)) pool.add(c);
        }
        return pool.isEmpty() ? null : pool.get(MathUtils.random(pool.size() - 1));
    }
}
