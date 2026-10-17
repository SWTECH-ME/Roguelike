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
}