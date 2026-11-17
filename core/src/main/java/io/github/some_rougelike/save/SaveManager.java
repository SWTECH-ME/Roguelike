package io.github.some_rougelike.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import io.github.some_rougelike.GameConfig;
import io.github.some_rougelike.logic.HandEvaluator;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.model.RankProgress;

import java.util.Arrays;

public class SaveManager {
    private static final SaveManager INSTANCE = new SaveManager();

    private final Json json;
    private GameSaveData currentSave;
    private boolean loaded = false;

    private SaveManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        currentSave = new GameSaveData();
    }

    public static SaveManager getInstance() {
        return INSTANCE;
    }

    private SaveEncryptionStrategy getStrategy() {
        if (GameConfig.SAVE_ENCRYPTION_ENABLED) {
            return new AesSaveStrategy();
        }
        return new PlainSaveStrategy();
    }

    public void load() {
        if (Gdx.files == null) {
            return;
        }
        FileHandle file = Gdx.files.local(GameConfig.SAVE_FILE_PATH);
        if (file.exists()) {
            try {
                String rawContent = file.readString("UTF-8");
                String decodedJson = getStrategy().decode(rawContent);
                if (decodedJson != null && !decodedJson.trim().isEmpty()) {
                    GameSaveData parsed = json.fromJson(GameSaveData.class, decodedJson);
                    if (parsed != null) {
                        currentSave = parsed;
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("SaveManager", "Failed to load save file: " + e.getMessage());
                currentSave = new GameSaveData();
            }
        } else {
            currentSave = new GameSaveData();
            loaded = true;
            save();
        }
        loaded = true;
    }

    public void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    public void save() {
        if (Gdx.files == null) {
            return;
        }
        ensureLoaded();
        try {
            FileHandle file = Gdx.files.local(GameConfig.SAVE_FILE_PATH);
            String prettyJson = json.prettyPrint(currentSave);
            String encoded = getStrategy().encode(prettyJson);
            file.writeString(encoded, false, "UTF-8");
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to write save file: " + e.getMessage());
        }
    }

    public UserSettingsData getSettings() {
        ensureLoaded();
        return currentSave.getSettings();
    }

    public String getSelectedDeckSkinPath() {
        ensureLoaded();
        return GameConfig.DECK_SKINS_DIR + "/" + currentSave.getSettings().getSelectedDeckSkin();
    }

    public float[] getRankProgressValues() {
        ensureLoaded();
        float[] stored = currentSave.getRankProgress();
        if (stored == null || stored.length != RankProgress.RANK_COUNT) {
            stored = new float[RankProgress.RANK_COUNT];
            currentSave.setRankProgress(stored);
        }
        return stored;
    }

    public void updateRankProgressValues(float[] values) {
        ensureLoaded();
        float[] target = getRankProgressValues();
        System.arraycopy(values, 0, target, 0, Math.min(values.length, target.length));
        save();
    }

    public boolean hasActiveRun() {
        ensureLoaded();
        return currentSave.isHasActiveRun();
    }

    public RunSaveData getActiveRunData() {
        ensureLoaded();
        return currentSave.getActiveRun();
    }

    public void saveRunMapState(RunState runState) {
        ensureLoaded();
        RunSaveData runData = RunSaveData.fromRunState(runState, null);
        currentSave.setActiveRun(runData);
        currentSave.setHasActiveRun(true);
        save();
    }

    public void saveCombatState(RunState runState, CombatSaveData combatSaveData) {
        ensureLoaded();
        RunSaveData runData = RunSaveData.fromRunState(runState, combatSaveData);
        currentSave.setActiveRun(runData);
        currentSave.setHasActiveRun(true);
        save();
    }

    /** A High Card mindig ismert, a többi kombinációt egyszer ki kell játszani, hogy felfedezett legyen. */
    public boolean isComboDiscovered(HandEvaluator.Combo combo) {
        if (combo == null || combo == HandEvaluator.Combo.HIGH_CARD) {
            return true;
        }
        ensureLoaded();
        for (String name : currentSave.getDiscoveredCombos()) {
            if (combo.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** Felfedezettként jelöli és elmenti a kombinációt; true, ha most fedeztük fel először. */
    public boolean discoverCombo(HandEvaluator.Combo combo) {
        if (isComboDiscovered(combo)) {
            return false;
        }
        String[] current = currentSave.getDiscoveredCombos();
        String[] updated = Arrays.copyOf(current, current.length + 1);
        updated[current.length] = combo.name();
        currentSave.setDiscoveredCombos(updated);
        save();
        return true;
    }

    public void clearActiveRun() {
        ensureLoaded();
        currentSave.setHasActiveRun(false);
        currentSave.setActiveRun(null);
        save();
    }
}
