package com.ashencrown.model;

public class GameMap {

    private final int cols;
    private final int rows;
    private final Tile[][] tiles;

    private static final int[][] DEFAULT_LAYOUT = {
        {0,0,5,5,0,0,0,2,2,2},
        {0,1,5,0,0,3,0,0,2,0},
        {0,1,0,0,3,3,0,0,0,0},
        {0,0,0,1,3,0,4,0,0,0},
        {0,0,1,1,0,0,0,0,1,0},
        {5,5,5,0,0,2,0,1,1,0},
        {0,0,5,0,0,2,0,0,0,0},
        {0,0,5,5,5,5,5,5,0,0},
    };

    private static final TerrainType[] TERRAIN_MAP = {
        TerrainType.GRASS, TerrainType.FOREST, TerrainType.MOUNTAIN,
        TerrainType.WATER, TerrainType.FORT,   TerrainType.ROAD
    };

    public GameMap() {
        this.rows  = DEFAULT_LAYOUT.length;
        this.cols  = DEFAULT_LAYOUT[0].length;
        this.tiles = new Tile[rows][cols];
        buildMap();
    }

    private void buildMap() {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                tiles[y][x] = new Tile(x, y, TERRAIN_MAP[DEFAULT_LAYOUT[y][x]]);
    }

    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return tiles[y][x];
    }

    public Tile getTile(Position pos) { return getTile(pos.getX(), pos.getY()); }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public int getCols() { return cols; }
    public int getRows() { return rows; }

    public void placeUnit(Unit unit, Position destination) {
        Position oldPos = unit.getPosition();
        if (oldPos != null && isInBounds(oldPos.getX(), oldPos.getY()))
            tiles[oldPos.getY()][oldPos.getX()].setOccupant(null);
        tiles[destination.getY()][destination.getX()].setOccupant(unit);
        unit.setPosition(destination);
    }

    public Unit getUnitAt(Position pos) {
        Tile tile = getTile(pos);
        return tile == null ? null : tile.getOccupant();
    }
}
