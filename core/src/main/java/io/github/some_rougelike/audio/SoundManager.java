package io.github.some_rougelike.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_rougelike.GameConfig;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundManager implements Disposable {
    private static final SoundManager INSTANCE = new SoundManager();
    // Variables
    private static final float DEFAULT_MUSIC_VOLUME = 1.0f;
    private static final float DEFAULT_SFX_VOLUME = 1.0f;
    private float masterVolume = 1.0f;

    private final AssetManager assetManager = new AssetManager();
    private final Map<SoundEvent, String> soundPaths = new EnumMap<>(SoundEvent.class);
    private boolean loaded;
    private Music menuMusic;
    
    private float menuMusicVolume = DEFAULT_MUSIC_VOLUME;
    private float sfxVolume = DEFAULT_SFX_VOLUME;

    private SoundManager() {
        register(SoundEvent.GAME_OPEN, GameConfig.SOUND_INTRO);
        register(SoundEvent.BUTTON_HOVER, GameConfig.SOUND_HOVER);
        register(SoundEvent.BUTTON_CLICK, GameConfig.SOUND_CLICK);
        register(SoundEvent.TEXT_POPUP, GameConfig.SOUND_NOTIFICATION);
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void register(SoundEvent event, String assetPath) {
        if (loaded) throw new IllegalStateException("[ERROR] Sound events must be registered before loading.");
        if (event == null || assetPath == null || assetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("[ERROR] An event and non-empty asset path are required.");
        }
        soundPaths.put(event, assetPath);
    }

    public void load() {
        if (loaded) return;

        Set<String> queuedPaths = new HashSet<>();
        for (Map.Entry<SoundEvent, String> entry : soundPaths.entrySet()) {
            String assetPath = entry.getValue();
            if (queuedPaths.add(assetPath)) {
                if (Gdx.files.internal(assetPath).exists()) {
                    assetManager.load(assetPath, Sound.class);
                } else {
                    Gdx.app.log("SoundManager", "[ERROR] Skipping missing sound asset: " + assetPath);
                }
            }
        }

        if (Gdx.files.internal(GameConfig.MENU_MUSIC).exists()) {
            assetManager.load(GameConfig.MENU_MUSIC, Music.class);
        } else {
            Gdx.app.log("SoundManager", "[ERROR] Skipping missing menu music: " + GameConfig.MENU_MUSIC);
        }

        assetManager.finishLoading();
        
        if (assetManager.isLoaded(GameConfig.MENU_MUSIC, Music.class)) {
            menuMusic = assetManager.get(GameConfig.MENU_MUSIC, Music.class);
            menuMusic.setLooping(true);
            menuMusic.setVolume(menuMusicVolume);
        }
        loaded = true;
    }

    public void setMasterVolume(float volume) {
        masterVolume = MathUtils.clamp(volume, 0f, 1f);
        if (menuMusic != null) {
            menuMusic.setVolume(menuMusicVolume * masterVolume);
        }
    }

    public float getMasterVolume() {
        return masterVolume;
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
            menuMusic.setVolume(menuMusicVolume * masterVolume);
        }
    }

    public float getMenuMusicVolume() {
        return menuMusicVolume;
    }

    public void setSfxVolume(float volume) {
        sfxVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void play(SoundEvent event) {
        ensureLoaded();

        String assetPath = soundPaths.get(event);
        if (assetPath != null && assetManager.isLoaded(assetPath, Sound.class)) {
            assetManager.get(assetPath, Sound.class).play(sfxVolume * masterVolume);
        }
    }

    private void ensureLoaded() {
        if (!loaded) throw new IllegalStateException("[ERROR] Load the sound manager before using audio.");
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