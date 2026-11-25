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

    /**
     * Kiszámítja a célpontszámot fejezet és a fejezeten belüli vak pozíció alapján.
     * blindIndexInChapter: 0 = Normal 1, 1 = Normal 2, ..., NORMALS_PER_CHAPTER = Boss.
     * Minden vak célpontszáma szigorúan növekvő a futás során.
     */
    public static int calculateTargetScore(int chapterIndex, int blindIndexInChapter) {
        int chapter = Math.max(1, chapterIndex);
        int blindNum = (chapter - 1) * (GameConfig.NORMALS_PER_CHAPTER + 1) + blindIndexInChapter + 1;
        int raw = GameConfig.BASE_BLIND_TARGET
                + GameConfig.BLIND_TARGET_LINEAR_STEP * (blindNum - 1)
                + GameConfig.BLIND_TARGET_QUADRATIC_STEP * (blindNum - 1) * (blindNum - 1);
        return Math.max(GameConfig.BASE_BLIND_TARGET, Math.round(raw / 50f) * 50);
    }

    /** 
     * @return List<Enemy>
     */
    public static List<Enemy> generateNormalEnemies() {
        return generateNormalEnemies(1);
    }

    /** 
     * @param chapterIndex
     * @return List<Enemy>
     */
    public static List<Enemy> generateNormalEnemies(int chapterIndex) {
        List<Enemy> normalPool = new ArrayList<>();
        int count = GameConfig.NORMAL_ENEMY_NAMES.length;
        for (int i = 0; i < count; i++) {
            String name = GameConfig.NORMAL_ENEMY_NAMES[i];
            int plays = i < GameConfig.NORMAL_ENEMY_PLAYS.length ? GameConfig.NORMAL_ENEMY_PLAYS[i] : GameConfig.DEFAULT_MAX_PLAYS;
            int discards = i < GameConfig.NORMAL_ENEMY_DISCARDS.length ? GameConfig.NORMAL_ENEMY_DISCARDS[i] : GameConfig.DEFAULT_MAX_DISCARDS;
            String bg = (i % 2 == 0) ? GameConfig.BG_ENEMY_1 : GameConfig.BG_ENEMY_2;
            normalPool.add(new Enemy(name, 0, plays, discards, bg));
        }
        Collections.shuffle(normalPool);

        List<Enemy> selected = new ArrayList<>();
        int pickCount = Math.min(GameConfig.NORMALS_PER_CHAPTER, normalPool.size());
        for (int i = 0; i < pickCount; i++) {
            Enemy base = normalPool.get(i);
            int target = calculateTargetScore(chapterIndex, i);
            selected.add(new Enemy(base.getName(), target, base.getMaxPlays(), base.getMaxDiscards(), base.getBackgroundPath()));
        }
        return selected;
    }

    /** 
     * @param usedBossNames
     * @return Enemy
     */
    public static Enemy generateUniqueBoss(List<String> usedBossNames) {
        return generateUniqueBoss(usedBossNames, 1);
    }

    /** 
     * @param usedBossNames
     * @param chapterIndex
     * @return Enemy
     */
    public static Enemy generateUniqueBoss(List<String> usedBossNames, int chapterIndex) {
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

        int target = calculateTargetScore(chapterIndex, GameConfig.NORMALS_PER_CHAPTER);
        return new Enemy(
            chosen.getName(),
            target,
            chosen.getMaxPlays(),
            chosen.getMaxDiscards(),
            chosen.getAbilityType(),
            chosen.getBackgroundPath()
        );
    }

    /** 
     * @return List<Enemy>
     */
    public static List<Enemy> createAllBosses() {
        List<Enemy> bosses = new ArrayList<>();
        int count = Math.min(GameConfig.BOSS_NAMES.length, BOSS_ABILITIES.length);
        for (int i = 0; i < count; i++) {
            String name = GameConfig.BOSS_NAMES[i];
            int target = calculateTargetScore(i + 1, GameConfig.NORMALS_PER_CHAPTER);
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
