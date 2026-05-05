package com.ashencrown.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameState {

    private GameMap map;
    private final List<Unit> units;
    private int turnNumber;
    private String currentPhase;
    private String status;
    private int enemySubTurn;

    public GameState() {
        this.map          = new GameMap();
        this.units        = new ArrayList<>();
        this.turnNumber   = 1;
        this.currentPhase = "PLAYER";
        this.status       = "ACTIVE";
        this.enemySubTurn = 0;
    }

    public GameMap getMap()             { return map; }
    public void setMap(GameMap newMap)  { this.map = newMap; }
    public List<Unit> getUnits()        { return units; }
    public int getTurnNumber()          { return turnNumber; }
    public String getCurrentPhase()     { return currentPhase; }
    public String getStatus()           { return status; }
    public int getEnemySubTurn()        { return enemySubTurn; }

    public void setTurnNumber(int n)    { this.turnNumber = n; }
    public void setCurrentPhase(String p){ this.currentPhase = p; }
    public void setStatus(String s)     { this.status = s; }
    public void setEnemySubTurn(int s)  { this.enemySubTurn = s; }

    /** Expone el layout del mapa como int[][] para el frontend */
    public int[][] getMapLayout() {
        return map != null ? map.getMapLayout() : null;
    }

    public Unit getUnitById(String id) {
        return units.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Unit> getAliveUnitsByTeam(String team) {
        return units.stream()
                    .filter(u -> u.isAlive() && u.getTeam().equals(team))
                    .collect(Collectors.toList());
    }

    public void addUnit(Unit unit) {
        units.add(unit);
        map.placeUnit(unit, unit.getPosition());
    }
}
