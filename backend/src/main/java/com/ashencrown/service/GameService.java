package com.ashencrown.service;

import com.ashencrown.dto.*;
import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final MovementService movSvc;
    private final CombatService   cbtSvc;
    private final AIService       aiSvc;
    private final LevelService    levelSvc;
    private GameState gameState;

    public GameService(MovementService movSvc, CombatService cbtSvc, AIService aiSvc, LevelService levelSvc) {
        this.movSvc   = movSvc;
        this.cbtSvc   = cbtSvc;
        this.aiSvc    = aiSvc;
        this.levelSvc = levelSvc;
        this.gameState = createNewGame();
    }

    // ── Nueva partida ──────────────────────────────────────────────────────────
    public GameState createNewGame() {
        GameState state = new GameState();

        // ── Unidades del jugador ───────────────────────────────────────────────
        // Marth — Lord Lv3
        Unit marth = new Unit("marth", "Marth", "player", "Lord", AIBehavior.AGGRESSIVE,
            20, 5, 1, 7, 8, 6, 4, 2, 5, 3,
            Weapon.ironSword(), new Position(1, 12));

        // Jagen — Paladin Lv7
        Unit jagen = new Unit("jagen", "Jagen", "player", "Paladin", AIBehavior.AGGRESSIVE,
            24, 9, 2, 11, 9, 5, 9, 7, 7, 7,
            Weapon.silverLance(), new Position(0, 13));

        // Linde — Mage Lv3
        Unit linde = new Unit("linde", "Linde", "player", "Mage", AIBehavior.AGGRESSIVE,
            16, 1, 9, 8, 7, 5, 2, 6, 5, 3,
            Weapon.thunder(), new Position(2, 13));

        // ── Unidades enemigas ──────────────────────────────────────────────────
        // Jefe bandido — Warrior Lv5, AGGRESSIVE
        Unit banditLeader = new Unit("bandit1", "Jefe Bandido", "enemy", "Warrior", AIBehavior.AGGRESSIVE,
            22, 8, 0, 6, 5, 3, 7, 1, 5, 5,
            Weapon.steelAxe(), new Position(17, 1));

        // Soldado oscuro — Knight Lv4, AGGRESSIVE (antes DEFENSIVE, no se movía nunca)
        Unit darkKnight = new Unit("knight1", "Soldado Oscuro", "enemy", "Knight", AIBehavior.AGGRESSIVE,
            20, 7, 0, 5, 4, 2, 8, 2, 4, 4,
            Weapon.steelLance(), new Position(13, 3));

        // Mercenario — Myrmidon Lv3, BERSERK
        Unit mercenary = new Unit("myrmid1", "Mercenario", "enemy", "Myrmidon", AIBehavior.BERSERK,
            18, 6, 0, 9, 10, 4, 4, 2, 5, 3,
            Weapon.ironSword(), new Position(11, 5));

        // Mago oscuro — Mage Lv3, DEFENSIVE (antes GUARD, nunca se movía)
        Unit darkMage = new Unit("mage1", "Mago Oscuro", "enemy", "Mage", AIBehavior.DEFENSIVE,
            16, 1, 8, 7, 6, 3, 2, 5, 5, 3,
            Weapon.flux(), new Position(15, 7));

        state.addUnit(marth);
        state.addUnit(jagen);
        state.addUnit(linde);
        state.addUnit(banditLeader);
        state.addUnit(darkKnight);
        state.addUnit(mercenary);
        state.addUnit(darkMage);

        this.gameState = state;
        return state;
    }

    public GameState createGameFromLevel(String levelId) {
        LevelDto levelDto = levelSvc.getLevel(levelId);
        if (levelDto == null) {
            throw new IllegalArgumentException("Level not found: " + levelId);
        }

        GameState state = new GameState();
        state.getMap().getClass(); // Force map initialization

        // Create map with dynamic layout
        GameMap customMap = new GameMap(levelDto.getMapLayout());
        state.setMap(customMap);

        // Create player units from level definition
        for (UnitDefinitionDto unitDef : levelDto.getPlayerUnits()) {
            Unit unit = createUnitFromDefinition(unitDef);
            state.addUnit(unit);
        }

        // Create enemy units from level definition
        for (UnitDefinitionDto unitDef : levelDto.getEnemyUnits()) {
            Unit unit = createUnitFromDefinition(unitDef);
            state.addUnit(unit);
        }

        this.gameState = state;
        return state;
    }

    private Unit createUnitFromDefinition(UnitDefinitionDto def) {
        Weapon weapon = getWeaponByType(def.getEquippedWeaponType());
        AIBehavior aiBehavior = def.getAiBehavior() != null ? AIBehavior.valueOf(def.getAiBehavior()) : AIBehavior.AGGRESSIVE;

        String unitId = def.getName().toLowerCase().replaceAll(" ", "-") + "-" + System.nanoTime();
        return new Unit(
            unitId, def.getName(), def.getTeam(), def.getUnitClass(), aiBehavior,
            20 + def.getLevel(), 5 + def.getLevel()/2, 1 + def.getLevel()/3,
            7 + def.getLevel(), 8 + def.getLevel()/2, 6, 4 + def.getLevel()/2,
            2 + def.getLevel()/4, 5, 3,
            weapon, new Position(def.getStartX(), def.getStartY())
        );
    }

    private Weapon getWeaponByType(String type) {
        return switch (type) {
            case "SWORD" -> Weapon.ironSword();
            case "LANCE" -> Weapon.ironLance();
            case "AXE"   -> Weapon.ironAxe();
            case "BOW"   -> Weapon.ironBow();
            case "ANIMA" -> Weapon.thunder();
            case "LIGHT" -> Weapon.fire();
            case "DARK"  -> Weapon.flux();
            default      -> Weapon.ironSword();
        };
    }

    // ── Consultas ──────────────────────────────────────────────────────────────
    public GameState getGameState() { return gameState; }

    public List<Position> getReachableTiles(String unitId) {
        Unit unit = requireUnit(unitId);
        if (!unit.isAlive())   throw new IllegalStateException("La unidad está derrotada.");
        if (unit.isHasMoved()) throw new IllegalStateException("La unidad ya se movió este turno.");
        return movSvc.getReachableTiles(unit, gameState.getMap(), gameState.getUnits());
    }

    public CombatPreviewDto getCombatPreview(String attackerId, String defenderId) {
        Unit atk = requireUnit(attackerId);
        Unit def = requireUnit(defenderId);
        return cbtSvc.previewCombat(atk, def, gameState.getMap());
    }

    // ── Acciones del jugador ───────────────────────────────────────────────────
    public MoveResultDto moveUnit(String unitId, int destX, int destY) {
        requirePlayerPhase();
        Unit unit = requireUnit(unitId);
        if (!unit.isAlive())   throw new IllegalStateException("La unidad está derrotada.");
        if (unit.isHasMoved()) throw new IllegalStateException("La unidad ya se movió este turno.");
        if (!"player".equals(unit.getTeam())) throw new IllegalStateException("No puedes mover unidades enemigas.");

        Position dest = new Position(destX, destY);
        if (!movSvc.canMoveTo(unit, dest, gameState.getMap(), gameState.getUnits()))
            throw new IllegalStateException("Posición fuera de rango: " + dest);

        gameState.getMap().placeUnit(unit, dest);
        unit.setHasMoved(true);

        MoveResultDto r = new MoveResultDto();
        r.setUnitId(unitId);
        r.setNewX(destX);
        r.setNewY(destY);
        r.setSuccess(true);
        r.setMessage(unit.getName() + " se movió a " + dest);
        return r;
    }

    public CombatResultDto attackUnit(String attackerId, String defenderId) {
        requirePlayerPhase();
        Unit atk = requireUnit(attackerId);
        Unit def = requireUnit(defenderId);
        if (!atk.isAlive() || !def.isAlive()) throw new IllegalStateException("Una unidad está derrotada.");
        if (atk.isHasActed())  throw new IllegalStateException("La unidad ya actuó este turno.");
        if (atk.getTeam().equals(def.getTeam())) throw new IllegalStateException("No puedes atacar a tu propio equipo.");
        if (!cbtSvc.isInAttackRange(atk, def))   throw new IllegalStateException("El enemigo está fuera de rango.");

        CombatResultDto result = cbtSvc.executeCombat(atk, def, gameState.getMap());
        checkWinCondition();
        return result;
    }

    public void waitUnit(String unitId) {
        Unit unit = requireUnit(unitId);
        unit.setHasMoved(true);
        unit.setHasActed(true);
    }

    // ── Fin de turno → fase enemiga (puede requerir múltiples llamadas) ────────
    public EnemyTurnResultDto endPlayerTurn() {
        gameState.setCurrentPhase("ENEMY");
        EnemyTurnResultDto result = aiSvc.executeEnemyPhase(gameState);
        checkWinCondition();
        return result;
    }

    // ── Internos ───────────────────────────────────────────────────────────────
    private Unit requireUnit(String id) {
        Unit u = gameState.getUnitById(id);
        if (u == null) throw new IllegalArgumentException("Unidad no encontrada: " + id);
        return u;
    }

    private void requirePlayerPhase() {
        if (!"PLAYER".equals(gameState.getCurrentPhase()))
            throw new IllegalStateException("No es la fase del jugador.");
    }

    private void checkWinCondition() {
        boolean playerAlive = !gameState.getAliveUnitsByTeam("player").isEmpty();
        boolean enemyAlive  = !gameState.getAliveUnitsByTeam("enemy").isEmpty();
        if (!playerAlive) gameState.setStatus("ENEMY_WIN");
        else if (!enemyAlive) gameState.setStatus("PLAYER_WIN");
    }
}
