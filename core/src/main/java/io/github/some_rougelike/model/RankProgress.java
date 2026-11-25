package io.github.some_rougelike.model;

import io.github.some_rougelike.save.SaveManager;

/** Rangonkénti fejlődés (0..100). Index: 0 = "2", 1 = "3", ... 12 = "A". */
public class RankProgress {
    public static final int RANK_COUNT = 13;

    private static final RankProgress INSTANCE = new RankProgress();

    private final float[] values = new float[RANK_COUNT];

    private RankProgress() {
        load();
    }

    /** 
     * @return RankProgress
     */
    public static RankProgress getInstance() {
        return INSTANCE;
    }

    /** 
     * @param rankIndex
     * @return float
     */
    public float get(int rankIndex) {
        return values[rankIndex];
    }

    /** 
     * @param rankIndex
     * @param value
     */
    public void set(int rankIndex, float value) {
        values[rankIndex] = Math.max(0f, Math.min(100f, value));
    }

    /** 
     * @param rankIndex
     * @param amount
     */
    public void add(int rankIndex, float amount) {
        set(rankIndex, values[rankIndex] + amount);
    }

    public void load() {
        float[] savedValues = SaveManager.getInstance().getRankProgressValues();
        for (int i = 0; i < RANK_COUNT; i++) {
            float val = i < savedValues.length ? savedValues[i] : 0f;
            values[i] = Math.max(0f, Math.min(100f, val));
        }
    }

    public void save() {
        SaveManager.getInstance().updateRankProgressValues(values);
    }
}
