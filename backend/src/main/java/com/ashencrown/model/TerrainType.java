package com.ashencrown.model;

public enum TerrainType {

    PLAINS  ("Llanura",  1, 0, 0),
    FOREST  ("Bosque",   2, 1, 20),
    MOUNTAIN("Montaña",  3, 2, 30),
    WATER   ("Agua",    -1, 0, 0),
    FORT    ("Fuerte",   1, 3, 20),
    ROAD    ("Camino",   1, 0, 0);

    private final String displayName;
    private final int    moveCost;
    private final int    defenseBonus;
    private final int    avoidBonus;

    TerrainType(String displayName, int moveCost, int defenseBonus, int avoidBonus) {
        this.displayName  = displayName;
        this.moveCost     = moveCost;
        this.defenseBonus = defenseBonus;
        this.avoidBonus   = avoidBonus;
    }

    public String getDisplayName()  { return displayName; }
    public int    getMoveCost()     { return moveCost; }
    public int    getDefenseBonus() { return defenseBonus; }
    public int    getAvoidBonus()   { return avoidBonus; }
    public boolean isImpassable()   { return moveCost == -1; }
}
