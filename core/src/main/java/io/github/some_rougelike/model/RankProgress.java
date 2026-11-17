package io.github.some_rougelike.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Rangonkénti fejlődés (0..100). Index: 0 = "2", 1 = "3", ... 12 = "A". */
public class RankProgress {
    public static final int RANK_COUNT = 13;

    private static final String PREFS_NAME = "rougelike_progress";
    private static final String KEY_INITIALIZED = "initialized_v2";
    private static final String KEY_PREFIX = "rank_";

    private static final RankProgress INSTANCE = new RankProgress();

    private final float[] values = new float[RANK_COUNT];

    private RankProgress() {
        load();
    }

    public static RankProgress getInstance() {
        return INSTANCE;
    }

    public float get(int rankIndex) {
        return values[rankIndex];
    }

    public void set(int rankIndex, float value) {
        values[rankIndex] = Math.max(0f, Math.min(100f, value));
    }

    public void add(int rankIndex, float amount) {
        set(rankIndex, values[rankIndex] + amount);
    }

    private void load() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        if (prefs.getBoolean(KEY_INITIALIZED, false)) {
            for (int i = 0; i < RANK_COUNT; i++) {
                values[i] = prefs.getFloat(KEY_PREFIX + i, 0f);
            }
        } else {
            // Első indítás (vagy nullázás után): minden sáv 0%-on
            for (int i = 0; i < RANK_COUNT; i++) {
                values[i] = 0f;
            }
            save();
        }
    }

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        for (int i = 0; i < RANK_COUNT; i++) {
            prefs.putFloat(KEY_PREFIX + i, values[i]);
        }
        prefs.putBoolean(KEY_INITIALIZED, true);
        prefs.flush();
    }
}