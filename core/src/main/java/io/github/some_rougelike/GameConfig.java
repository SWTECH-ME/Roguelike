package io.github.some_rougelike;

public class GameConfig {
    // Global variables for backgrounds, etc.
    // We'll only have to update them here swag

    // Backgrounds, images
    public static final String UI_SKIN = "ui/uiskin.json";
    public static final String MAIN_STATIC_BACKGROUND = "ui/background.jpg";
    public static final String MAIN_BACKGROUND = "ui/background-spritesheet.png";
    public static final boolean USE_ANIMATED_BACKGROUND = true;
    public static final float ANIMATED_BACKGROUND_SPEED = 0.125f;
    
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

    // We can include other game rules here in the future as well
}