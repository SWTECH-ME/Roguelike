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

    /** 
     * @param enemy
     * @return EnemySaveData
     */
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

    /** 
     * @return Enemy
     */
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

    /** 
     * @return String
     */
    public String getName() {
        return name;
    }

    /** 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** 
     * @return int
     */
    public int getTargetScore() {
        return targetScore;
    }

    /** 
     * @param targetScore
     */
    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    /** 
     * @return int
     */
    public int getMaxPlays() {
        return maxPlays;
    }

    /** 
     * @param maxPlays
     */
    public void setMaxPlays(int maxPlays) {
        this.maxPlays = maxPlays;
    }

    /** 
     * @return int
     */
    public int getMaxDiscards() {
        return maxDiscards;
    }

    /** 
     * @param maxDiscards
     */
    public void setMaxDiscards(int maxDiscards) {
        this.maxDiscards = maxDiscards;
    }

    /** 
     * @return boolean
     */
    public boolean isBoss() {
        return isBoss;
    }

    /** 
     * @param boss
     */
    public void setBoss(boolean boss) {
        isBoss = boss;
    }

    /** 
     * @return String
     */
    public String getSpecialAbility() {
        return specialAbility;
    }

    /** 
     * @param specialAbility
     */
    public void setSpecialAbility(String specialAbility) {
        this.specialAbility = specialAbility;
    }

    /** 
     * @return String
     */
    public String getAbilityType() {
        return abilityType;
    }

    /** 
     * @param abilityType
     */
    public void setAbilityType(String abilityType) {
        this.abilityType = abilityType;
    }

    /** 
     * @return String
     */
    public String getBackgroundPath() {
        return backgroundPath;
    }

    /** 
     * @param backgroundPath
     */
    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    /** 
     * @return boolean
     */
    public boolean isDefeated() {
        return defeated;
    }

    /** 
     * @param defeated
     */
    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }
}
