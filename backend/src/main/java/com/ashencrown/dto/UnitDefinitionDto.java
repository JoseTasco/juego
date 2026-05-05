package com.ashencrown.dto;

public class UnitDefinitionDto {
    private String name;
    private String unitClass;
    private int level;
    private int startX;
    private int startY;
    private String team;
    private String aiBehavior;
    private String equippedWeaponType;

    public UnitDefinitionDto() {}

    public UnitDefinitionDto(String name, String unitClass, int level, int startX, int startY,
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

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnitClass() { return unitClass; }
    public void setUnitClass(String unitClass) { this.unitClass = unitClass; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getStartX() { return startX; }
    public void setStartX(int startX) { this.startX = startX; }
    public int getStartY() { return startY; }
    public void setStartY(int startY) { this.startY = startY; }
    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }
    public String getAiBehavior() { return aiBehavior; }
    public void setAiBehavior(String aiBehavior) { this.aiBehavior = aiBehavior; }
    public String getEquippedWeaponType() { return equippedWeaponType; }
    public void setEquippedWeaponType(String equippedWeaponType) { this.equippedWeaponType = equippedWeaponType; }
}
