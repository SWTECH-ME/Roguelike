package io.github.some_rougelike.logic;

public class Enemy {
    private String name;
    private int targetScore;
    private int maxPlays;
    private int maxDiscards;
    private boolean isBoss;
    private String specialAbility;
    private BossAbilityType abilityType;
    private String backgroundPath;
    private boolean defeated;

    public Enemy(String name, int targetScore, int maxPlays, int maxDiscards, String backgroundPath) {
        this.name = name;
        this.targetScore = targetScore;
        this.maxPlays = maxPlays;
        this.maxDiscards = maxDiscards;
        this.backgroundPath = backgroundPath;
        this.isBoss = false;
        this.abilityType = BossAbilityType.NONE;
        this.specialAbility = "";
        this.defeated = false;
    }

    public Enemy(String name, int targetScore, int maxPlays, int maxDiscards, String specialAbility, String backgroundPath) {
        this(name, targetScore, maxPlays, maxDiscards, backgroundPath);
        this.isBoss = true;
        this.specialAbility = specialAbility;
        this.abilityType = BossAbilityType.fromNameOrDescription(null, specialAbility);
    }

    public Enemy(String name, int targetScore, int maxPlays, int maxDiscards, BossAbilityType abilityType, String backgroundPath) {
        this(name, targetScore,
            abilityType != null ? abilityType.modifyMaxPlays(maxPlays) : maxPlays,
            abilityType != null ? abilityType.modifyMaxDiscards(maxDiscards) : maxDiscards,
            backgroundPath);
        this.isBoss = true;
        this.abilityType = abilityType != null ? abilityType : BossAbilityType.NONE;
        this.specialAbility = this.abilityType.getDescription();
    }

    public String getName() { return name; }
    public int getTargetScore() { return targetScore; }
    public int getMaxPlays() { return maxPlays; }
    public int getMaxDiscards() { return maxDiscards; }
    public boolean isBoss() { return isBoss; }
    public String getSpecialAbility() { return specialAbility; }
    public BossAbilityType getAbilityType() { return abilityType != null ? abilityType : BossAbilityType.NONE; }
    /** 
     * @param abilityType
     */
    public void setAbilityType(BossAbilityType abilityType) {
        this.abilityType = abilityType != null ? abilityType : BossAbilityType.NONE;
        if (this.specialAbility == null || this.specialAbility.isEmpty()) {
            this.specialAbility = this.abilityType.getDescription();
        }
    }
    public String getBackgroundPath() { return backgroundPath; }
    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean defeated) { this.defeated = defeated; }
}
