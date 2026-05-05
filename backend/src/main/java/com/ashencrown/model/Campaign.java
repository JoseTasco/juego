package com.ashencrown.model;

import java.util.List;

public class Campaign {
    private String id;
    private String title;
    private String description;
    private List<Level> levels;

    public Campaign(String id, String title, String description, List<Level> levels) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.levels = levels;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Level> getLevels() { return levels; }
}
