package com.ashencrown.model;

public class GameMap {

    private static final int COLS = 20;
    private static final int ROWS = 15;

    // 0=PLAINS 1=FOREST 2=MOUNTAIN 3=WATER 4=FORT 5=ROAD
    private static final int[][] DEFAULT_LAYOUT = {
        {5,5,5,0,0,1,1,0,0,2,2,2,0,0,3,3,3,0,0,0},
        {5,0,0,0,1,1,0,0,2,2,0,0,0,3,3,0,0,0,1,0},
        {5,0,0,1,1,0,0,2,2,0,0,0,0,3,0,0,0,1,1,0},
        {5,5,0,1,0,0,2,2,0,0,0,0,0,3,0,0,1,1,0,0},
        {0,5,5,0,0,2,2,0,0,0,4,0,0,3,3,0,1,0,0,0},
        {0,0,5,0,0,2,0,0,0,0,0,0,3,3,0,0,0,0,0,0},
        {0,0,5,5,0,0,0,0,0,0,0,3,3,0,0,0,1,1,1,0},
        {0,0,0,5,5,0,0,0,0,0,3,3,0,0,0,1,1,0,0,0},
        {0,0,0,0,5,0,1,1,0,3,3,0,0,0,0,1,0,0,0,0},
        {0,0,0,0,5,5,1,0,3,3,0,0,0,0,0,0,0,0,4,0},
        {2,2,0,0,0,5,0,3,3,0,0,0,0,0,0,0,0,0,0,0},
        {2,2,2,0,0,5,3,3,0,0,0,0,0,0,0,0,0,1,0,0},
        {2,0,0,0,0,5,3,0,0,0,0,0,0,0,1,1,1,1,0,0},
        {0,0,0,0,0,5,0,0,0,1,1,1,0,0,1,0,0,0,0,0},
        {0,0,4,0,0,5,5,5,5,5,0,0,0,0,0,0,0,0,4,0},
    };

    private static final TerrainType[] TERRAIN_MAP = {
        TerrainType.PLAINS, TerrainType.FOREST, TerrainType.MOUNTAIN,
        TerrainType.WATER,  TerrainType.FORT,   TerrainType.ROAD
    };

    private final Tile[][] tiles;
    private final int cols;
    private final int rows;

    public GameMap() {
        this.cols = COLS;
        this.rows = ROWS;
        this.tiles = new Tile[ROWS][COLS];
        buildMap(DEFAULT_LAYOUT);
    }

    public GameMap(int[][] layout) {
        this.rows = layout.length;
        this.cols = layout[0].length;
        this.tiles = new Tile[this.rows][this.cols];
        buildMap(layout);
    }

    private void buildMap(int[][] layout) {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                tiles[y][x] = new Tile(x, y, TERRAIN_MAP[layout[y][x]]);
    }

    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return tiles[y][x];
    }

    public Tile getTile(Position pos) { return getTile(pos.getX(), pos.getY()); }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS;
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

    /** Devuelve el layout como int[][] (0=PLAINS…5=ROAD) para serialización JSON */
    public int[][] getMapLayout() {
        int[][] layout = new int[rows][cols];
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                layout[y][x] = terrainToInt(tiles[y][x].getTerrain());
        return layout;
    }

    private int terrainToInt(TerrainType t) {
        return switch (t) {
            case PLAINS   -> 0;
            case FOREST   -> 1;
            case MOUNTAIN -> 2;
            case WATER    -> 3;
            case FORT     -> 4;
            case ROAD     -> 5;
        };
    }
}
