package com.ashencrown.dto;

import java.util.List;

public class LevelDto {
    private String id;
    private String name;
    private String description;
    private String campaignId;
    private int difficulty;
    private int[][] mapLayout;
    private List<UnitDefinitionDto> playerUnits;
    private List<UnitDefinitionDto> enemyUnits;
    private String objective;
    private int turnLimit;

    public LevelDto() {}

    public LevelDto(String id, String name, String description, String campaignId, int difficulty,
                    int[][] mapLayout, List<UnitDefinitionDto> playerUnits, List<UnitDefinitionDto> enemyUnits,
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

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public int[][] getMapLayout() { return mapLayout; }
    public void setMapLayout(int[][] mapLayout) { this.mapLayout = mapLayout; }
    public List<UnitDefinitionDto> getPlayerUnits() { return playerUnits; }
    public void setPlayerUnits(List<UnitDefinitionDto> playerUnits) { this.playerUnits = playerUnits; }
    public List<UnitDefinitionDto> getEnemyUnits() { return enemyUnits; }
    public void setEnemyUnits(List<UnitDefinitionDto> enemyUnits) { this.enemyUnits = enemyUnits; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public int getTurnLimit() { return turnLimit; }
    public void setTurnLimit(int turnLimit) { this.turnLimit = turnLimit; }
}
