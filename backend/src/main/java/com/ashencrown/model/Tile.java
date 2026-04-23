package com.ashencrown.model;

public class Tile {

    private final Position position;
    private final TerrainType terrain;
    private Unit occupant;

    public Tile(int x, int y, TerrainType terrain) {
        this.position = new Position(x, y);
        this.terrain  = terrain;
        this.occupant = null;
    }

    public Position getPosition()   { return position; }
    public TerrainType getTerrain() { return terrain; }
    public Unit getOccupant()       { return occupant; }
    public void setOccupant(Unit u) { this.occupant = u; }
    public boolean isOccupied()     { return occupant != null; }
    public boolean isWalkable()     { return !terrain.isImpassable(); }

    @Override
    public String toString() {
        return "Tile" + position + "[" + terrain.getDisplayName() + "]"
               + (isOccupied() ? " <- " + occupant.getName() : "");
    }
}
