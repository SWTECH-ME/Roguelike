package io.github.some_rougelike.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.github.some_rougelike.GameConfig;

public class ChapterGenerator {
    private static final BossAbilityType[] BOSS_ABILITIES = {
        BossAbilityType.HIDDEN_SUITS,
        BossAbilityType.NO_DISCARDS,
        BossAbilityType.MAX_SELECT_FOUR,
        BossAbilityType.SMALLER_HAND,
        BossAbilityType.ONE_LESS_PLAY,
        BossAbilityType.RED_CARDS_HALVED,
        BossAbilityType.BLACK_CARDS_HALVED,
        BossAbilityType.FACE_CARDS_NERFED,
        BossAbilityType.NUMBER_CARDS_PENALTY,
        BossAbilityType.CAP_MULTIPLIER,
        BossAbilityType.NO_DOUBLE_ROLLS,
        BossAbilityType.FORCED_RANDOM_DISCARD
    };

    private ChapterGenerator() {
    }

    public static List<Enemy> generateNormalEnemies() {
        List<Enemy> normalPool = new ArrayList<>();
        int count = GameConfig.NORMAL_ENEMY_NAMES.length;
        for (int i = 0; i < count; i++) {
            String name = GameConfig.NORMAL_ENEMY_NAMES[i];
            int target = i < GameConfig.NORMAL_ENEMY_BASE_TARGETS.length ? GameConfig.NORMAL_ENEMY_BASE_TARGETS[i] : 400;
            int plays = i < GameConfig.NORMAL_ENEMY_PLAYS.length ? GameConfig.NORMAL_ENEMY_PLAYS[i] : GameConfig.DEFAULT_MAX_PLAYS;
            int discards = i < GameConfig.NORMAL_ENEMY_DISCARDS.length ? GameConfig.NORMAL_ENEMY_DISCARDS[i] : GameConfig.DEFAULT_MAX_DISCARDS;
            String bg = (i % 2 == 0) ? GameConfig.BG_ENEMY_1 : GameConfig.BG_ENEMY_2;
            normalPool.add(new Enemy(name, target, plays, discards, bg));
        }
        Collections.shuffle(normalPool);

        List<Enemy> selected = new ArrayList<>();
        int pickCount = Math.min(GameConfig.NORMALS_PER_CHAPTER, normalPool.size());
        for (int i = 0; i < pickCount; i++) {
            selected.add(normalPool.get(i));
        }
        return selected;
    }

    public static Enemy generateUniqueBoss(List<String> usedBossNames) {
        List<Enemy> allBosses = createAllBosses();
        List<Enemy> availableBosses = new ArrayList<>();

        for (Enemy boss : allBosses) {
            if (usedBossNames == null || !usedBossNames.contains(boss.getName())) {
                availableBosses.add(boss);
            }
        }

        if (availableBosses.isEmpty()) {
            availableBosses.addAll(allBosses);
        }

        Collections.shuffle(availableBosses);
        Enemy chosen = availableBosses.get(0);
        if (usedBossNames != null && !usedBossNames.contains(chosen.getName())) {
            usedBossNames.add(chosen.getName());
        }
        return chosen;
    }

    private static List<Enemy> createAllBosses() {
        List<Enemy> bosses = new ArrayList<>();
        int count = Math.min(GameConfig.BOSS_NAMES.length, BOSS_ABILITIES.length);
        for (int i = 0; i < count; i++) {
            String name = GameConfig.BOSS_NAMES[i];
            int target = i < GameConfig.BOSS_BASE_TARGETS.length ? GameConfig.BOSS_BASE_TARGETS[i] : 500;
            BossAbilityType ability = BOSS_ABILITIES[i];
            bosses.add(new Enemy(
                name,
                target,
                GameConfig.BOSS_DEFAULT_PLAYS,
                GameConfig.BOSS_DEFAULT_DISCARDS,
                ability,
                GameConfig.BG_BOSS
            ));
        }
        return bosses;
    }
}
