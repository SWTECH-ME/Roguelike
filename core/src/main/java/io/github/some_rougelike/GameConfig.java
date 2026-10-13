package io.github.some_rougelike;

public class GameConfig {

    // Backgrounds, images
    public static final String UI_SKIN = "ui/uiskin.json";
    public static final String MAIN_STATIC_BACKGROUND = "ui/background.jpg";
    public static final String MAIN_BACKGROUND = "ui/background-spritesheet.png";
    public static final boolean USE_ANIMATED_BACKGROUND = true;
    public static final float ANIMATED_BACKGROUND_SPEED = 0.125f;

    public static final String BG_ENEMY_1 = "ui/background.jpg";
    public static final String BG_ENEMY_2 = "ui/background.jpg";
    public static final String BG_BOSS = "ui/background.jpg";
    

    // Animated background properties
    public static final int BG_FRAME_COLS = 5;
    public static final int BG_FRAME_ROWS = 2;
    public static final int BG_TOTAL_FRAMES = 7;


    // Sounds
    public static final String MENU_MUSIC = "ui/uisounds/menu_theme_loopable.mp3";
    public static final String SOUND_INTRO = "ui/uisounds/before_menu_intro_sound.mp3";
    public static final String SOUND_HOVER = "ui/uisounds/hover_sound.mp3";
    public static final String SOUND_CLICK = "ui/uisounds/dragon-studio-button-press-382713.mp3";
    public static final String SOUND_NOTIFICATION = "ui/uisounds/vaghato_notification_sounds.mp3";


    // --- CARDS AND DISCARDS (FUTURE REFERENCE, NOT CURRENTLY USED) ---
    public static final String CARD_BACK_IMAGE = "ui/cards/card_back.png"; 
    public static final String CARD_FRONT_ATLAS = "ui/cards/cards.atlas"; 


    // Default gameplay values
    public static final int STARTING_MONEY = 10;
    public static final int STARTING_HAND_SIZE = 8;
    public static final int REWARD_MONEY = 15;
    public static final int DEFAULT_MAX_PLAYS = 4;
    public static final int DEFAULT_MAX_DISCARDS = 3;

    public static final float SCREEN_SHAKE_DURATION = 0.2f;
    public static final float SCREEN_SHAKE_INTENSITY = 12f;
    public static final float SCORING_STEP_DELAY = 0.6f;
    public static final String SCORING_NUMBER_COLOR = "FFD700";
}