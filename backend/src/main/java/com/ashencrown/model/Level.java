package com.ashencrown.model;

import java.util.List;

public class Level {
    private String id;
    private String name;
    private String description;
    private String campaignId;
    private int difficulty; // 1=Easy, 2=Normal, 3=Hard
    private int[][] mapLayout;
    private List<UnitDefinition> playerUnits;
    private List<UnitDefinition> enemyUnits;
    private String objective;
    private int turnLimit;

    public Level(String id, String name, String description, String campaignId, int difficulty,
                 int[][] mapLayout, List<UnitDefinition> playerUnits, List<UnitDefinition> enemyUnits,
                 String objective, int turnLimit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.campaignId = campaignId;
        this.difficulty = difficulty;
        this.mapLayout = mapLayout;
        this.playerUnits = playerUnits;
        this.enemyUnits = enemyUnits;
        this.objective = objective;
        this.turnLimit = turnLimit;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCampaignId() { return campaignId; }
    public int getDifficulty() { return difficulty; }
    public int[][] getMapLayout() { return mapLayout; }
    public List<UnitDefinition> getPlayerUnits() { return playerUnits; }
    public List<UnitDefinition> getEnemyUnits() { return enemyUnits; }
    public String getObjective() { return objective; }
    public int getTurnLimit() { return turnLimit; }
}
