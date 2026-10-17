package io.github.some_rougelike;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class GameConfig {

    // --- BACKGROUNDS ---
    public static final String UI_SKIN = "ui/uiskin.json";

    public static final String BG_ENEMY_1 = "ui/background.jpg";
    public static final String BG_ENEMY_2 = "ui/background.jpg";
    public static final String BG_BOSS = "ui/background.jpg";
    
    // --- MENU ---
    // Fonts:
    public static final float MAIN_MENU_TITLE_SIZE = 2.2f;
    public static final float MAIN_MENU_BUTTON_SIZE = 1.3f;
    public static final float MAIN_MENU_SCALE = 1.5f;
    // Collections Debug
    public static final boolean ALLOW_HIDING_COMBINATIONS = false; 

    // --- SOUNDS ---
    public static final String MENU_MUSIC = "ui/uisounds/menu_theme_loopable.mp3";
    public static final String SOUND_INTRO = "ui/uisounds/before_menu_intro_sound.mp3";
    public static final String SOUND_HOVER = "ui/uisounds/hover_sound.mp3";
    public static final String SOUND_CLICK = "ui/uisounds/dragon-studio-button-press-382713.mp3";
    public static final String SOUND_NOTIFICATION = "ui/uisounds/vaghato_notification_sounds.mp3";

    // --- CARDS AND DISCARDS (FUTURE REFERENCE, NOT CURRENTLY USED) ---
    public static final String CARD_BACK_IMAGE = "ui/cards/card_back.png"; 
    public static final String CARD_FRONT_ATLAS = "ui/cards/cards.atlas"; 

    // --- DEFAULT GAMEPLAY VALUES ---
    public static final int STARTING_MONEY = 10;
    public static final int STARTING_HAND_SIZE = 8;
    public static final int REWARD_MONEY = 15;
    public static final int DEFAULT_MAX_PLAYS = 4;
    public static final int DEFAULT_MAX_DISCARDS = 3;

    public static final float SCREEN_SHAKE_DURATION = 0.2f;
    public static final float SCREEN_SHAKE_INTENSITY = 12f;
    public static final float SCORING_STEP_DELAY = 0.6f;
    public static final String SCORING_NUMBER_COLOR = "FFD700";

    // --- RUN MAP SCREEN ---
    public static final float RUN_MAP_CARD_WIDTH = 240f;
    public static final float RUN_MAP_CARD_HEIGHT = 360f;
    public static final int RUN_MAP_CARD_RADIUS = 15;
    public static final int RUN_MAP_BORDER_THICKNESS = 3;
    
    public static final Color RUN_MAP_NORMAL_BG = new Color(0.12f, 0.15f, 0.18f, 0.6f);
    public static final Color RUN_MAP_BOSS_BG = new Color(0.18f, 0.10f, 0.10f, 0.6f);
    public static final Color RUN_MAP_LOCKED_BG = new Color(0.08f, 0.08f, 0.08f, 0.4f);
    
    public static final Color RUN_MAP_NORMAL_PILL = new Color(0.2f, 0.4f, 0.8f, 1f);
    public static final Color RUN_MAP_BOSS_PILL = new Color(0.8f, 0.2f, 0.2f, 1f);
    public static final Color RUN_MAP_LOCKED_PILL = new Color(0.3f, 0.3f, 0.3f, 1f);

    // --- COMBAT SCREEN ---
    public static final float COMBAT_CARD_WIDTH = 100f;
    public static final float COMBAT_CARD_HEIGHT = 140f;
    public static final int COMBAT_CARD_RADIUS = 10;
    public static final float COMBAT_CARD_HOVER_OFFSET = 15f;
    public static final float COMBAT_CARD_SELECT_OFFSET = 30f;
    
    public static final Color COMBAT_CARD_BG_NORMAL = new Color(0.95f, 0.95f, 0.98f, 1f);
    public static final Color COMBAT_CARD_BG_SELECTED = new Color(0.8f, 0.9f, 1.0f, 1f);
    public static final Color COMBAT_BOSS_BG_1 = new Color(0.4f, 0.1f, 0.15f, 1f);
    public static final Color COMBAT_BOSS_BG_2 = new Color(0.25f, 0.05f, 0.1f, 1f);

    // --- RANDOMIZED GLOBAL BACKGROUND COLORS ---
    public static Color BG_COLOR_1;
    public static Color BG_COLOR_2;

    static {
        rollRandomBackground();
    }

    public static void rollRandomBackground() {
        Color[][] colorCombos = {
            { Color.valueOf("1a1a3a"), Color.valueOf("4a154b") }, // Deep Blue & Purple
            { Color.valueOf("0f380f"), Color.valueOf("306230") }, // Retro Green & Teal
            { Color.valueOf("4a0e17"), Color.valueOf("1a1a1a") }, // Crimson & Charcoal
            { Color.valueOf("7a4012"), Color.valueOf("381d08") }, // Golden Orange & Brown
            { Color.valueOf("481045"), Color.valueOf("1c0826") }, // Neon Pink & Dark Violet
            { Color.valueOf("225555"), Color.valueOf("0f1a24") }  // Seafoam & Midnight
        };

        int randomIndex = MathUtils.random(0, colorCombos.length - 1);
        BG_COLOR_1 = colorCombos[randomIndex][0];
        BG_COLOR_2 = colorCombos[randomIndex][1];
    }

    // --- DECK NAMES ---
    public static String getDeckSkinName(String filename) {
        if (filename == null) return "Unknown Deck";
        switch (filename) {
            case "black_blue.png": return "Black & Blue";
            case "black_red.png": return "Black & Red";
            case "black_white.png": return "Black & White";
            case "white_black.png": return "White & Black";
            case "white_blue.png": return "White & Blue";
            case "white_red.png": return "White & Red";
            default: return filename.replace(".png", "").replace("_", " ").toUpperCase();
        }
    }
}