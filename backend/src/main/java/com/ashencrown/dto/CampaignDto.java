package com.ashencrown.dto;

import java.util.List;

public class CampaignDto {
    private String id;
    private String title;
    private String description;
    private List<LevelDto> levels;

    public CampaignDto() {}

    public CampaignDto(String id, String title, String description, List<LevelDto> levels) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.levels = levels;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<LevelDto> getLevels() { return levels; }
    public void setLevels(List<LevelDto> levels) { this.levels = levels; }
}
