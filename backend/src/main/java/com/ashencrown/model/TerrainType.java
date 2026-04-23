package com.ashencrown.model;

public enum TerrainType {

    GRASS   ("Hierba",   1, 0),
    FOREST  ("Bosque",   2, 1),
    MOUNTAIN("Montaña",  3, 2),
    WATER   ("Agua",    -1, 0),
    FORT    ("Fuerte",   1, 3),
    ROAD    ("Camino",   1, 0);

    private final String displayName;
    private final int moveCost;
    private final int defenseBonus;

    TerrainType(String displayName, int moveCost, int defenseBonus) {
        this.displayName  = displayName;
        this.moveCost     = moveCost;
        this.defenseBonus = defenseBonus;
    }

    public String getDisplayName()  { return displayName; }
    public int getMoveCost()        { return moveCost; }
    public int getDefenseBonus()    { return defenseBonus; }
    public boolean isImpassable()   { return moveCost == -1; }
}
