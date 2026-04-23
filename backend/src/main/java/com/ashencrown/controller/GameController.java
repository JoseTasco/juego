package com.ashencrown.controller;

import com.ashencrown.dto.*;
import com.ashencrown.model.*;
import com.ashencrown.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints del juego.
 * Base URL: http://localhost:8080/api/game
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // GET /api/game/state — estado completo del juego
    @GetMapping("/state")
    public ResponseEntity<GameState> getState() {
        return ResponseEntity.ok(gameService.getGameState());
    }

    // POST /api/game/new — nueva partida
    @PostMapping("/new")
    public ResponseEntity<GameState> newGame() {
        return ResponseEntity.ok(gameService.createNewGame());
    }

    // GET /api/game/reachable/{unitId} — casillas alcanzables (BFS)
    @GetMapping("/reachable/{unitId}")
    public ResponseEntity<?> getReachableTiles(@PathVariable String unitId) {
        try {
            List<Position> tiles = gameService.getReachableTiles(unitId);
            return ResponseEntity.ok(tiles);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/game/move — mover unidad
    // Body: { "unitId": "marth", "destX": 3, "destY": 2 }
    @PostMapping("/move")
    public ResponseEntity<?> moveUnit(@RequestBody MoveRequestDto request) {
        try {
            return ResponseEntity.ok(gameService.moveUnit(
                    request.getUnitId(), request.getDestX(), request.getDestY()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/game/attack — atacar unidad enemiga
    // Body: { "attackerId": "marth", "defenderId": "goblin1" }
    @PostMapping("/attack")
    public ResponseEntity<?> attackUnit(@RequestBody AttackRequestDto request) {
        try {
            return ResponseEntity.ok(gameService.attackUnit(
                    request.getAttackerId(), request.getDefenderId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/game/wait/{unitId} — unidad espera
    @PostMapping("/wait/{unitId}")
    public ResponseEntity<?> waitUnit(@PathVariable String unitId) {
        try {
            gameService.waitUnit(unitId);
            return ResponseEntity.ok("Unidad " + unitId + " espera.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/game/end-turn — fin de turno del jugador
    @PostMapping("/end-turn")
    public ResponseEntity<GameState> endTurn() {
        return ResponseEntity.ok(gameService.endPlayerTurn());
    }
}
