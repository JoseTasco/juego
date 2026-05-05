package com.ashencrown.model;

public class UnitDefinition {
    private String name;
    private String unitClass;
    private int level;
    private int startX;
    private int startY;
    private String team; // "PLAYER" or "ENEMY"
    private String aiBehavior; // For enemy units
    private String equippedWeaponType;

    public UnitDefinition(String name, String unitClass, int level, int startX, int startY,
                         String team, String aiBehavior, String equippedWeaponType) {
        this.name = name;
        this.unitClass = unitClass;
        this.level = level;
        this.startX = startX;
        this.startY = startY;
        this.team = team;
        this.aiBehavior = aiBehavior;
        this.equippedWeaponType = equippedWeaponType;
    }

    // Getters
    public String getName() { return name; }
    public String getUnitClass() { return unitClass; }
    public int getLevel() { return level; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public String getTeam() { return team; }
    public String getAiBehavior() { return aiBehavior; }
    public String getEquippedWeaponType() { return equippedWeaponType; }
}
