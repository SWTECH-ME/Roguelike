package io.github.some_rougelike;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class GameConfig {

    // --- SPLASH SCREEN ---
    public static final String SPLASH_LOGO_PATH = "ui/logo.jpg";
    public static final float SPLASH_LOGO_SCALE = 0.12f;
    public static final float SPLASH_FADE_IN_DURATION = 1.0f;
    public static final float SPLASH_HOLD_DURATION = 1.5f;
    public static final float SPLASH_FADE_OUT_DURATION = 1.0f;

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
    public static final float MAIN_MENU_FADE_DURATION = 0.5f;
    // Collections Debug:
    public static final boolean ALLOW_HIDING_COMBINATIONS = false;
    // Secret key sequence on the Main Menu to launch the scrollable 12-Boss test level
    public static final String DEBUG_BOSS_TEST_CODE = "adamdebug";
    public static final int DEBUG_BOSS_GRID_COLS = 3;
    public static final float DEBUG_BOSS_CARD_SCALE = 0.9f;
    public static final float DEBUG_BOSS_SCROLL_KNOB_WIDTH = 4f;

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
    public static final int DEFAULT_MAX_SELECTABLE_CARDS = 5;
    public static final int REWARD_MONEY = 15;
    public static final int DEFAULT_MAX_PLAYS = 4;
    public static final int DEFAULT_MAX_DISCARDS = 3;
    // Maximum number of chapters in a finite run and number of normal enemies per chapter (+1 boss)
    public static final int MAX_CHAPTERS = 12;
    public static final int NORMALS_PER_CHAPTER = 2;

    // Normal enemy pool definitions (names, base target scores, plays, discards)
    public static final String[] NORMAL_ENEMY_NAMES = {
        "Backrooms Obama",
        "360 NoScope",
        "Doge",
        "Trollface",
        "Nyan Cat",
        "Pepe the Frog",
        "Wojak",
        "Cheems"
    };
    public static final int[] NORMAL_ENEMY_BASE_TARGETS = {
        300, 600, 450, 550, 350, 420, 480, 380
    };
    public static final int[] NORMAL_ENEMY_PLAYS = {
        4, 4, 4, 3, 4, 4, 3, 4
    };
    public static final int[] NORMAL_ENEMY_DISCARDS = {
        3, 3, 3, 2, 3, 3, 3, 3
    };

    // 12 unique Boss names and their base target scores (each paired with a unique BossAbilityType, appearing at most once per run)
    public static final String[] BOSS_NAMES = {
        "Triple T",
        "Giga Chad",
        "Skibidi Toilet",
        "Sigma Patrick",
        "Ohio Final Boss",
        "Morbius",
        "Walter White",
        "Saul Goodman",
        "Shrek",
        "Big Chungus",
        "Rick Astley",
        "Amogus Impostor"
    };
    public static final int[] BOSS_BASE_TARGETS = {
        500, 500, 520, 480, 450, 500, 500, 520, 480, 460, 500, 500
    };
    public static final int BOSS_DEFAULT_PLAYS = 4;
    public static final int BOSS_DEFAULT_DISCARDS = 3;

    // Boss special ability tuning constants
    public static final int BOSS_REDUCED_MAX_SELECT_CARDS = 4;
    public static final int BOSS_HAND_SIZE_PENALTY = 2;
    public static final int BOSS_PLAYS_PENALTY = 1;
    public static final int BOSS_FACE_CARD_NERF_CHIPS = 5;
    public static final int BOSS_NUMBER_CARD_CHIP_PENALTY = 2;
    public static final int BOSS_MAX_MULTIPLIER_CAP = 3;
    public static final Color BOSS_HIDDEN_SUIT_TINT = new Color(0.55f, 0.55f, 0.62f, 1.0f);

    // --- RUN MAP SCREEN ---
    public static final float RUN_MAP_CARD_WIDTH = 240f;
    public static final float RUN_MAP_CARD_HEIGHT = 360f;
    public static final int RUN_MAP_CARD_RADIUS = 15;
    public static final int RUN_MAP_BORDER_THICKNESS = 3;
    // Header scales and colors for displaying current chapter, run score, and high score on the Run Map
    public static final float RUN_MAP_CHAPTER_TITLE_SCALE = 1.9f;
    public static final float RUN_MAP_SUBHEADER_SCALE = 1.05f;
    public static final Color RUN_MAP_CHAPTER_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public static final Color RUN_MAP_SUBHEADER_COLOR = new Color(0.75f, 0.85f, 0.95f, 1.0f);
    public static final Color RUN_MAP_HIGH_SCORE_COLOR = new Color(1.0f, 0.84f, 0.28f, 1.0f);
    public static final Color RUN_MAP_ABILITY_TEXT_COLOR = new Color(1.0f, 0.6f, 0.6f, 1.0f);

    public static final Color RUN_MAP_NORMAL_BG = new Color(0.12f, 0.15f, 0.18f, 0.6f);
    public static final Color RUN_MAP_BOSS_BG = new Color(0.18f, 0.10f, 0.10f, 0.6f);
    public static final Color RUN_MAP_LOCKED_BG = new Color(0.08f, 0.08f, 0.08f, 0.4f);

    public static final Color RUN_MAP_NORMAL_PILL = new Color(0.2f, 0.4f, 0.8f, 1f);
    public static final Color RUN_MAP_BOSS_PILL = new Color(0.8f, 0.2f, 0.2f, 1f);
    public static final Color RUN_MAP_LOCKED_PILL = new Color(0.3f, 0.3f, 0.3f, 1f);

    public static final float SCREEN_TRANSITION_DURATION = 1.0f;
    public static final float TRANSITION_CARD_MIN_SCALE = 0.6f;
    public static final float TRANSITION_CARD_SCALE_VAR = 1.1f;
    public static final float TRANSITION_CARD_SCALE_MULTIPLIER = 1.2f;

    // --- COMBAT SCREEN: BUTTONS ---
    public static final float BUTTON_STRIKE_OVERHANG = 20f;
    public static final float BUTTON_STRIKE_THICKNESS = 3f;
    public static final Color BUTTON_TEXT_COLOR = new Color(0.65f, 0.65f, 0.65f, 1f);
    public static final Color BUTTON_ERROR_COLOR = new Color(0.9f, 0.2f, 0.2f, 1f);

    // --- COMBAT SCREEN: CARD PROPERTIES ---
    public static final float COMBAT_CARD_WIDTH = 100f;
    public static final float COMBAT_CARD_HEIGHT = 140f;
    public static final int COMBAT_CARD_RADIUS = 10;
    public static final float COMBAT_CARD_HOVER_OFFSET = 15f;
    public static final float COMBAT_CARD_SELECT_OFFSET = 30f;

    // --- COMBAT SCREEN: LAYOUT & SPACING ---
    public static final float HAND_Y_POS = 100f;
    public static final float HAND_SPACING = 8f;
    public static final float PLAYED_Y_POS = HAND_Y_POS + 20;
    public static final float PLAYED_SPACING = 20f;

    // --- COMBAT SCREEN: ANIMATION TIMINGS ---
    public static final float DRAW_INITIAL_DELAY = SCREEN_TRANSITION_DURATION + 0.1f;
    public static final float DRAW_CARD_DELAY = 0.1f;
    public static final float DRAW_CARD_DURATION = 0.5f;

    public static final float FLYOUT_CARD_DELAY = 0.1f;
    public static final float FLYOUT_PLAY_DURATION = 0.5f;
    public static final float FLYOUT_DISCARD_DURATION = 0.6f;
    public static final float FLYOUT_DISCARD_ROTATION = 180f;
    public static final float FLYOUT_PLAY_ROTATION = 120f;

    public static final float SCREEN_SHAKE_DURATION = 0.2f;
    public static final float SCREEN_SHAKE_INTENSITY = 12f;
    public static final float SCORING_STEP_DELAY = 0.6f;

    public static final float GAMEOVER_FADE_TIME = 0.4f;
    public static final float GAMEOVER_SCREEN_TINT = 0.4f;
    public static final float GAMEOVER_BG_LERP_DURATION = 1.2f;
    public static final Color GAMEOVER_TEXT_COLOR = new Color(0.9f, 0.2f, 0.2f, 1f);
    public static final Color GAMEOVER_BG_1 = new Color(0.3f, 0.0f, 0.05f, 1f);
    public static final Color GAMEOVER_BG_2 = new Color(0.15f, 0.0f, 0.0f, 1f);

    // Run Victory overlay colors when all 12 chapters are beaten
    public static final Color RUN_VICTORY_TEXT_COLOR = new Color(1.0f, 0.84f, 0.25f, 1f);
    public static final Color RUN_VICTORY_BG_1 = new Color(0.08f, 0.32f, 0.18f, 1f);
    public static final Color RUN_VICTORY_BG_2 = new Color(0.04f, 0.16f, 0.10f, 1f);

    public static final float VICTORY_UI_FADE_DURATION = 0.7f;
    public static final float VICTORY_BG_LERP_DELAY = 0.2f;
    public static final float VICTORY_BG_LERP_DURATION = 1.2f;

    // --- COMBAT SCREEN: COLORS ---
    public static final Color COMBAT_CARD_BG_NORMAL = new Color(0.95f, 0.95f, 0.98f, 1f);
    public static final Color COMBAT_CARD_BG_SELECTED = new Color(0.8f, 0.9f, 1.0f, 1f);
    public static final Color COMBAT_BOSS_BG_1 = new Color(0.4f, 0.1f, 0.15f, 1f);
    public static final Color COMBAT_BOSS_BG_2 = new Color(0.25f, 0.05f, 0.1f, 1f);

    public static final String SCORING_NUMBER_COLOR = "FFD700";
    public static final String DOUBLE_SCORING_NUMBER_COLOR = "FF66FF";

    // --- SAVE SYSTEM & ENCRYPTION ---
    // Local path where the JSON save file is stored
    public static final String SAVE_FILE_PATH = "saves/savegame.json";
    // Toggle between readable plain JSON (false, great for debugging) and AES encrypted save (true)
    public static final boolean SAVE_ENCRYPTION_ENABLED = false;
    // AES algorithm and 16-byte secret key used when SAVE_ENCRYPTION_ENABLED is true
    public static final String SAVE_ENCRYPTION_ALGORITHM = "AES";
    public static final String SAVE_ENCRYPTION_KEY = "SwTechRougelike!";

    // --- DEFAULT USER SETTINGS ---
    // Default audio volume levels (0.0 to 1.0)
    public static final float DEFAULT_MASTER_VOLUME = 0.5f;
    public static final float DEFAULT_MUSIC_VOLUME = 0.5f;
    public static final float DEFAULT_SFX_VOLUME = 0.5f;
    // Default display settings
    public static final String DEFAULT_DISPLAY_MODE = "Fullscreen";
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 720;
    public static final boolean DEFAULT_VSYNC = true;
    // Default background theme option
    public static final String DEFAULT_BACKGROUND_THEME = "Random";
    // Default deck skin directory and file name
    public static final String DECK_SKINS_DIR = "deck_skins";
    public static final String DEFAULT_DECK_SKIN = "white_black.png";
    public static final int DECK_SHEET_COLS = 13;
    public static final int DECK_SHEET_ROWS = 5;

    // --- CONTINUE RUN MENU & ANIMATIONS ---
    // Dimensions, colors, and animation timings for the continue/new run prompt in the main menu
    public static final float CONTINUE_DIALOG_WIDTH = 520f;
    public static final float CONTINUE_DIALOG_HEIGHT = 380f;
    public static final int CONTINUE_DIALOG_RADIUS = 18;
    public static final int CONTINUE_DIALOG_BORDER = 3;
    public static final float CONTINUE_DIALOG_FADE_DURATION = 0.35f;
    public static final float CONTINUE_DIALOG_SLIDE_OFFSET = 60f;
    public static final float CONTINUE_BG_LERP_DURATION = 0.45f;
    public static final Color CONTINUE_DIALOG_BG = new Color(0.08f, 0.10f, 0.14f, 0.88f);
    public static final Color CONTINUE_DIALOG_BORDER_COLOR = new Color(0.85f, 0.85f, 0.9f, 1f);
    public static final Color CONTINUE_OVERLAY_TINT = new Color(0f, 0f, 0f, 0.55f);
    public static final Color CONTINUE_SUBTITLE_COLOR = new Color(0.55f, 0.8f, 1.0f, 1f);
    public static final Color CONTINUE_BG_COLOR_1 = new Color(0.08f, 0.22f, 0.30f, 1f);
    public static final Color CONTINUE_BG_COLOR_2 = new Color(0.04f, 0.10f, 0.18f, 1f);

    // --- RANDOMIZED GLOBAL BACKGROUND COLORS ---
    public static Color BG_COLOR_1;
    public static Color BG_COLOR_2;

    // Selectable background theme names in Settings (Random + named color combinations)
    public static final String[] BACKGROUND_THEMES = {
        "Random",
        "Deep Blue & Purple",
        "Retro Green & Teal",
        "Crimson & Charcoal",
        "Golden Orange & Brown",
        "Neon Pink & Dark Violet",
        "Seafoam & Midnight"
    };

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

    // Applies a specific named background theme or rolls a random one if "Random" is selected
    public static void applyBackgroundTheme(String themeName) {
        if (themeName == null || "Random".equalsIgnoreCase(themeName)) {
            rollRandomBackground();
            return;
        }
        switch (themeName) {
            case "Deep Blue & Purple":
                BG_COLOR_1 = Color.valueOf("1a1a3a");
                BG_COLOR_2 = Color.valueOf("4a154b");
                break;
            case "Retro Green & Teal":
                BG_COLOR_1 = Color.valueOf("0f380f");
                BG_COLOR_2 = Color.valueOf("306230");
                break;
            case "Crimson & Charcoal":
                BG_COLOR_1 = Color.valueOf("4a0e17");
                BG_COLOR_2 = Color.valueOf("1a1a1a");
                break;
            case "Golden Orange & Brown":
                BG_COLOR_1 = Color.valueOf("7a4012");
                BG_COLOR_2 = Color.valueOf("381d08");
                break;
            case "Neon Pink & Dark Violet":
                BG_COLOR_1 = Color.valueOf("481045");
                BG_COLOR_2 = Color.valueOf("1c0826");
                break;
            case "Seafoam & Midnight":
                BG_COLOR_1 = Color.valueOf("225555");
                BG_COLOR_2 = Color.valueOf("0f1a24");
                break;
            default:
                rollRandomBackground();
                break;
        }
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
