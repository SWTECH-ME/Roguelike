package io.github.some_rougelike.save;

import io.github.some_rougelike.logic.BossAbilityType;
import io.github.some_rougelike.logic.Enemy;

public class EnemySaveData {
    private String name;
    private int targetScore;
    private int maxPlays;
    private int maxDiscards;
    private boolean isBoss;
    private String specialAbility;
    private String abilityType;
    private String backgroundPath;
    private boolean defeated;

    public EnemySaveData() {
    }

    public EnemySaveData(String name, int targetScore, int maxPlays, int maxDiscards, boolean isBoss, String specialAbility, String abilityType, String backgroundPath, boolean defeated) {
        this.name = name;
        this.targetScore = targetScore;
        this.maxPlays = maxPlays;
        this.maxDiscards = maxDiscards;
        this.isBoss = isBoss;
        this.specialAbility = specialAbility;
        this.abilityType = abilityType;
        this.backgroundPath = backgroundPath;
        this.defeated = defeated;
    }

    public static EnemySaveData fromEnemy(Enemy enemy) {
        if (enemy == null) {
            return null;
        }
        return new EnemySaveData(
            enemy.getName(),
            enemy.getTargetScore(),
            enemy.getMaxPlays(),
            enemy.getMaxDiscards(),
            enemy.isBoss(),
            enemy.getSpecialAbility(),
            enemy.getAbilityType().name(),
            enemy.getBackgroundPath(),
            enemy.isDefeated()
        );
    }

    public Enemy toEnemy() {
        Enemy enemy;
        if (isBoss) {
            BossAbilityType resolvedType = BossAbilityType.fromNameOrDescription(abilityType, specialAbility);
            String desc = (specialAbility != null && !specialAbility.isEmpty()) ? specialAbility : resolvedType.getDescription();
            enemy = new Enemy(name, targetScore, maxPlays, maxDiscards, desc, backgroundPath);
            enemy.setAbilityType(resolvedType);
        } else {
            enemy = new Enemy(name, targetScore, maxPlays, maxDiscards, backgroundPath);
        }
        enemy.setDefeated(defeated);
        return enemy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    public int getMaxPlays() {
        return maxPlays;
    }

    public void setMaxPlays(int maxPlays) {
        this.maxPlays = maxPlays;
    }

    public int getMaxDiscards() {
        return maxDiscards;
    }

    public void setMaxDiscards(int maxDiscards) {
        this.maxDiscards = maxDiscards;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setBoss(boolean boss) {
        isBoss = boss;
    }

    public String getSpecialAbility() {
        return specialAbility;
    }

    public void setSpecialAbility(String specialAbility) {
        this.specialAbility = specialAbility;
    }

    public String getAbilityType() {
        return abilityType;
    }

    public void setAbilityType(String abilityType) {
        this.abilityType = abilityType;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }
}
