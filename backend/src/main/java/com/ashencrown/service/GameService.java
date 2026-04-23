package com.ashencrown.service;

import com.ashencrown.dto.*;
import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final MovementService movementService;
    private final CombatService   combatService;
    private GameState gameState;

    public GameService(MovementService movementService, CombatService combatService) {
        this.movementService = movementService;
        this.combatService   = combatService;
        this.gameState       = createNewGame();
    }

    public GameState createNewGame() {
        GameState state = new GameState();

        Unit marth = new Unit("marth", "Marth",  "player", "Lord",    20, 8, 4, 9, 5, new Position(1, 3));
        Unit jagen = new Unit("jagen", "Jagen",  "player", "Paladin", 18, 7, 6, 6, 6, new Position(0, 5));

        Unit enemigo = new Unit("enemigo1", "Jagen Oscuro", "enemy", "Paladin", 18, 7, 6, 6, 6, new Position(8, 2));

        state.addUnit(marth);
        state.addUnit(jagen);
        state.addUnit(enemigo);

        this.gameState = state;
        return state;
    }

    public GameState getGameState() { return gameState; }

    public List<Position> getReachableTiles(String unitId) {
        Unit unit = gameState.getUnitById(unitId);
        if (unit == null)       throw new IllegalArgumentException("Unidad no encontrada: " + unitId);
        if (!unit.isAlive())    throw new IllegalStateException("La unidad está derrotada.");
        if (unit.isHasMoved())  throw new IllegalStateException("La unidad ya se movió este turno.");
        return movementService.getReachableTiles(unit, gameState.getMap(), gameState.getUnits());
    }

    public MoveResultDto moveUnit(String unitId, int destX, int destY) {
        if (!"PLAYER".equals(gameState.getCurrentPhase()))
            throw new IllegalStateException("No es la fase del jugador.");

        Unit unit = gameState.getUnitById(unitId);
        if (unit == null)         throw new IllegalArgumentException("Unidad no encontrada: " + unitId);
        if (!unit.isAlive())      throw new IllegalStateException("La unidad está derrotada.");
        if (unit.isHasMoved())    throw new IllegalStateException("La unidad ya se movió este turno.");
        if (!"player".equals(unit.getTeam())) throw new IllegalStateException("No puedes mover unidades enemigas.");

        Position destination = new Position(destX, destY);
        if (!movementService.canMoveTo(unit, destination, gameState.getMap(), gameState.getUnits()))
            throw new IllegalStateException("Posición fuera de rango: " + destination);

        gameState.getMap().placeUnit(unit, destination);
        unit.setHasMoved(true);

        MoveResultDto result = new MoveResultDto();
        result.setUnitId(unitId);
        result.setNewX(destX);
        result.setNewY(destY);
        result.setSuccess(true);
        result.setMessage(unit.getName() + " se movió a " + destination);
        return result;
    }

    public CombatResultDto attackUnit(String attackerId, String defenderId) {
        if (!"PLAYER".equals(gameState.getCurrentPhase()))
            throw new IllegalStateException("No es la fase del jugador.");

        Unit attacker = gameState.getUnitById(attackerId);
        Unit defender = gameState.getUnitById(defenderId);
        if (attacker == null || defender == null) throw new IllegalArgumentException("Unidad no encontrada.");
        if (!attacker.isAlive() || !defender.isAlive()) throw new IllegalStateException("Una unidad está derrotada.");
        if (attacker.isHasActed()) throw new IllegalStateException("La unidad ya actuó este turno.");
        if (attacker.getTeam().equals(defender.getTeam())) throw new IllegalStateException("No puedes atacar a tu propio equipo.");
        if (!combatService.isInAttackRange(attacker, defender)) throw new IllegalStateException("El enemigo está fuera de rango.");

        CombatResultDto result = combatService.executeCombat(attacker, defender, gameState.getMap());
        checkWinCondition();
        return result;
    }

    public void waitUnit(String unitId) {
        Unit unit = gameState.getUnitById(unitId);
        if (unit == null) throw new IllegalArgumentException("Unidad no encontrada: " + unitId);
        unit.setHasMoved(true);
        unit.setHasActed(true);
    }

    public GameState endPlayerTurn() {
        gameState.getAliveUnitsByTeam("player").forEach(Unit::resetTurnFlags);
        gameState.setCurrentPhase("ENEMY");
        processEnemyTurn();
        return gameState;
    }

    private void processEnemyTurn() {
        gameState.getAliveUnitsByTeam("enemy").forEach(Unit::resetTurnFlags);
        gameState.setTurnNumber(gameState.getTurnNumber() + 1);
        gameState.setCurrentPhase("PLAYER");
    }

    private void checkWinCondition() {
        boolean playerAlive = !gameState.getAliveUnitsByTeam("player").isEmpty();
        boolean enemyAlive  = !gameState.getAliveUnitsByTeam("enemy").isEmpty();
        if (!playerAlive) gameState.setStatus("ENEMY_WIN");
        else if (!enemyAlive) gameState.setStatus("PLAYER_WIN");
    }
}
