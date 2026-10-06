package io.github.some_rougelike.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundManager implements Disposable {
    private static final SoundManager INSTANCE = new SoundManager();
    private static final String MENU_MUSIC_PATH = "ui/uisounds/menu_theme_loopable.mp3";
    private static final float DEFAULT_MUSIC_VOLUME = 0.5f;

    private final AssetManager assetManager = new AssetManager();
    private final Map<SoundEvent, String> soundPaths = new EnumMap<>(SoundEvent.class);
    private boolean loaded;
    private Music menuMusic;
    private float menuMusicVolume = DEFAULT_MUSIC_VOLUME;

    private SoundManager() {
        register(SoundEvent.GAME_OPEN, "ui/uisounds/before_menu_intro_sound.mp3");
        register(SoundEvent.BUTTON_HOVER, "ui/uisounds/hover_sound.mp3");
        register(SoundEvent.BUTTON_CLICK, "ui/uisounds/dragon-studio-button-press-382713.mp3");
        register(SoundEvent.TEXT_POPUP, "audio/text_popup.ogg");
    }
/*Ha új eventet akar valaki hozzaadni/soundeffectet ahhoz registerelni kell az uj eventet, assetpathot kitolteni hogy
game startupon ne crasheljen az egesz, illetve lent az enumoknal boviteni kell a listat. */
    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void register(SoundEvent event, String assetPath) {
        if (loaded) {
            throw new IllegalStateException("Sound events must be registered before loading.");
        }
        if (event == null || assetPath == null || assetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("An event and non-empty asset path are required.");
        }
        soundPaths.put(event, assetPath);
    }

    public void load() {
        if (loaded) {
            return;
        }

        Set<String> queuedPaths = new HashSet<>();
        for (Map.Entry<SoundEvent, String> entry : soundPaths.entrySet()) {
            String assetPath = entry.getValue();
            if (queuedPaths.add(assetPath)) {
                if (Gdx.files.internal(assetPath).exists()) {
                    assetManager.load(assetPath, Sound.class);
                } else {
                    Gdx.app.log("SoundManager", "Skipping missing optional sound asset: " + assetPath);
                }
            }
        }

        if (Gdx.files.internal(MENU_MUSIC_PATH).exists()) {
            assetManager.load(MENU_MUSIC_PATH, Music.class);
        } else {
            Gdx.app.log("SoundManager", "Skipping missing optional menu music: " + MENU_MUSIC_PATH);
        }

        assetManager.finishLoading();
        if (assetManager.isLoaded(MENU_MUSIC_PATH, Music.class)) {
            menuMusic = assetManager.get(MENU_MUSIC_PATH, Music.class);
            menuMusic.setLooping(true);
            menuMusic.setVolume(menuMusicVolume);
        }
        loaded = true;
    }

    public void startMenuMusic() {
        ensureLoaded();
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public void setMenuMusicVolume(float volume) {
        menuMusicVolume = MathUtils.clamp(volume, 0f, 1f);
        if (menuMusic != null) {
            menuMusic.setVolume(menuMusicVolume);
        }
    }

    public float getMenuMusicVolume() {
        return menuMusicVolume;
    }

    public void play(SoundEvent event) {
        ensureLoaded();

        String assetPath = soundPaths.get(event);
        if (assetPath != null && assetManager.isLoaded(assetPath, Sound.class)) {
            assetManager.get(assetPath, Sound.class).play();
        }
    }

    private void ensureLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Load the sound manager before using audio.");
        }
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }

    public enum SoundEvent {
        GAME_OPEN,
        BUTTON_HOVER,
        BUTTON_CLICK,
        TEXT_POPUP
    }
}
