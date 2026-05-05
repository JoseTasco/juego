package com.ashencrown.controller;

import com.ashencrown.dto.CampaignDto;
import com.ashencrown.dto.LevelDto;
import com.ashencrown.service.LevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints para niveles y campañas.
 * Base URL: http://localhost:8080/api/levels
 */
@RestController
@RequestMapping("/api/levels")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class LevelController {

    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

    // GET /api/levels/campaigns
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignDto>> getAllCampaigns() {
        return ResponseEntity.ok(levelService.getAllCampaigns());
    }

    // GET /api/levels/campaigns/{campaignId}
    @GetMapping("/campaigns/{campaignId}")
    public ResponseEntity<?> getCampaign(@PathVariable String campaignId) {
        CampaignDto campaign = levelService.getCampaign(campaignId);
        if (campaign == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(campaign);
    }

    // GET /api/levels/{levelId}
    @GetMapping("/{levelId}")
    public ResponseEntity<?> getLevel(@PathVariable String levelId) {
        LevelDto level = levelService.getLevel(levelId);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(level);
    }
}
