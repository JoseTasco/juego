package com.ashencrown.service;

import com.ashencrown.dto.CombatResultDto;
import com.ashencrown.dto.EnemyActionDto;
import com.ashencrown.dto.EnemyTurnResultDto;
import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IA enemiga completa con 5 comportamientos:
 *   AGGRESSIVE, DEFENSIVE, GUARD, SUPPORT, BERSERK
 *
 * Algoritmo por turno:
 *   1. Selección de objetivo  (threat score)
 *   2. Pathfinding A* → tile óptimo de ataque
 *   3. Mover y atacar
 */
@Service
public class AIService {

    private final MovementService    movSvc;
    private final CombatService      cbtSvc;

    public AIService(MovementService movSvc, CombatService cbtSvc) {
        this.movSvc = movSvc;
        this.cbtSvc = cbtSvc;
    }

    // ── Punto de entrada ──────────────────────────────────────────────────────
    public EnemyTurnResultDto executeEnemyPhase(GameState state) {
        List<EnemyActionDto> actions = new ArrayList<>();
        
        // Resetear flags de enemigos al inicio
        state.getAliveUnitsByTeam("enemy").forEach(Unit::resetTurnFlags);
        
        List<Unit> enemies = new ArrayList<>(state.getAliveUnitsByTeam("enemy"));
        
        // Procesar TODOS los enemigos UNA sola vez
        for (Unit enemy : enemies) {
            if (!enemy.isAlive()) continue;
            EnemyActionDto action = processEnemy(enemy, state);
            if (action != null) actions.add(action);
        }

        // Terminar fase enemiga
        state.getAliveUnitsByTeam("player").forEach(Unit::resetTurnFlags);
        state.setTurnNumber(state.getTurnNumber() + 1);
        state.setCurrentPhase("PLAYER");

        return new EnemyTurnResultDto(actions, state);
    }

    // ── Dispatch por comportamiento ───────────────────────────────────────────
    private EnemyActionDto processEnemy(Unit enemy, GameState state) {
        EnemyActionDto a = new EnemyActionDto();
        a.setUnitId(enemy.getId());
        a.setUnitName(enemy.getName());
        a.setFromX(enemy.getPosition().getX());
        a.setFromY(enemy.getPosition().getY());

        switch (enemy.getAiBehavior()) {
            case GUARD:      return doGuard(enemy, state, a);
            case DEFENSIVE:  return doDefensive(enemy, state, a);
            case SUPPORT:    return doSupport(enemy, state, a);
            case BERSERK:    return doBerserk(enemy, state, a);
            default:         return doAggressive(enemy, selectTarget(enemy, state.getAliveUnitsByTeam("player")), state, a);
        }
    }

    // ── GUARD: nunca se mueve, ataca si alguien entra en rango ────────────────
    private EnemyActionDto doGuard(Unit enemy, GameState state, EnemyActionDto a) {
        a.setToX(enemy.getPosition().getX());
        a.setToY(enemy.getPosition().getY());
        tryAttack(enemy, state.getAliveUnitsByTeam("player"), state, a);
        return finalize(enemy, a);
    }

    // ── DEFENSIVE: ataca si el jugador ya está en rango, si no, se queda ──────
    private EnemyActionDto doDefensive(Unit enemy, GameState state, EnemyActionDto a) {
        a.setToX(enemy.getPosition().getX());
        a.setToY(enemy.getPosition().getY());
        tryAttack(enemy, state.getAliveUnitsByTeam("player"), state, a);
        return finalize(enemy, a);
    }

    // ── AGGRESSIVE: mueve hacia el objetivo más amenazante y ataca ────────────
    private EnemyActionDto doAggressive(Unit enemy, Unit target, GameState state, EnemyActionDto a) {
        if (target == null) return stayPut(enemy, a);

        List<Position> reachable = movSvc.getReachableTiles(enemy, state.getMap(), state.getUnits());
        Position bestPos = findBestAttackPosition(enemy, target, reachable, state);

        if (bestPos != null && !bestPos.equals(enemy.getPosition()))
            state.getMap().placeUnit(enemy, bestPos);

        a.setToX(enemy.getPosition().getX());
        a.setToY(enemy.getPosition().getY());
        tryAttack(enemy, state.getAliveUnitsByTeam("player"), state, a);
        return finalize(enemy, a);
    }

    // ── BERSERK: persigue a la unidad jugador con MENOS HP ────────────────────
    private EnemyActionDto doBerserk(Unit enemy, GameState state, EnemyActionDto a) {
        List<Unit> players = state.getAliveUnitsByTeam("player");
        Unit lowestHp = players.stream()
            .min(Comparator.comparingInt(Unit::getCurrentHp))
            .orElse(null);
        return doAggressive(enemy, lowestHp, state, a);
    }

    // ── SUPPORT: se mueve hacia aliados con poca vida para curarlos ───────────
    private EnemyActionDto doSupport(Unit enemy, GameState state, EnemyActionDto a) {
        List<Unit> allies = state.getAliveUnitsByTeam("enemy").stream()
            .filter(u -> !u.getId().equals(enemy.getId()))
            .collect(Collectors.toList());

        if (allies.isEmpty()) return doAggressive(enemy,
            selectTarget(enemy, state.getAliveUnitsByTeam("player")), state, a);

        Unit wounded = allies.stream()
            .min(Comparator.comparingDouble(u -> (double) u.getCurrentHp() / u.getMaxHp()))
            .orElse(null);

        if (wounded == null || (double) wounded.getCurrentHp() / wounded.getMaxHp() >= 0.6)
            return doAggressive(enemy, selectTarget(enemy, state.getAliveUnitsByTeam("player")), state, a);

        // Moverse hacia el aliado herido
        List<Position> reachable = movSvc.getReachableTiles(enemy, state.getMap(), state.getUnits());
        Position bestPos = reachable.stream()
            .min(Comparator.comparingInt(p -> p.distanceTo(wounded.getPosition())))
            .orElse(enemy.getPosition());

        if (!bestPos.equals(enemy.getPosition()))
            state.getMap().placeUnit(enemy, bestPos);

        a.setToX(enemy.getPosition().getX());
        a.setToY(enemy.getPosition().getY());

        // Curar si es adyacente
        if (enemy.getPosition().distanceTo(wounded.getPosition()) == 1) {
            Weapon w = enemy.getEquippedWeapon();
            int healAmt = (w != null) ? enemy.getMag() + 10 : 10;
            wounded.heal(healAmt);
            a.setAttacked(true);
            a.setTargetId(wounded.getId());
        }
        return finalize(enemy, a);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Unit selectTarget(Unit enemy, List<Unit> players) {
        return players.stream()
            .max(Comparator.comparingDouble(p -> threatScore(enemy, p)))
            .orElse(null);
    }

    private double threatScore(Unit enemy, Unit player) {
        Weapon w    = enemy.getEquippedWeapon();
        int might   = w != null ? w.getMight() : 0;
        int dmg     = w != null && w.isMagical()
            ? Math.max(0, enemy.getMag() + might - player.getRes())
            : Math.max(0, enemy.getStr() + might - player.getDef());

        double killScore = player.getMaxHp() > 0 ? (double) dmg / player.getMaxHp() * 100 : 0;

        double classBonus = switch (player.getUnitClass()) {
            case "Healer" -> 30;
            case "Lord"   -> 20;
            case "Mage"   -> 15;
            default       -> 0;
        };

        double hpPct    = (double) player.getCurrentHp() / player.getMaxHp();
        double lowHpBonus = hpPct < 0.25 ? 30 : hpPct < 0.5 ? 15 : 0;

        return killScore + classBonus + lowHpBonus;
    }

    /** Elige la posición alcanzable que permite atacar al objetivo con mejor terreno */
    private Position findBestAttackPosition(Unit enemy, Unit target,
                                            List<Position> reachable, GameState state) {
        Weapon  w        = enemy.getEquippedWeapon();
        int     minR     = w != null ? w.getMinRange() : 1;
        int     maxR     = w != null ? w.getMaxRange() : 1;

        Position bestAttack = null;
        int bestTerrain     = -1;
        int bestDist        = Integer.MAX_VALUE;

        for (Position pos : reachable) {
            int dist = pos.distanceTo(target.getPosition());
            if (dist >= minR && dist <= maxR) {
                int terrain = state.getMap().getTile(pos).getTerrain().getDefenseBonus();
                if (terrain > bestTerrain || (terrain == bestTerrain && dist < bestDist)) {
                    bestTerrain = terrain;
                    bestDist    = dist;
                    bestAttack  = pos;
                }
            }
        }

        if (bestAttack != null) return bestAttack;

        // No puede atacar este turno → acercarse lo máximo
        return reachable.stream()
            .min(Comparator.comparingInt(p -> p.distanceTo(target.getPosition())))
            .orElse(enemy.getPosition());
    }

    private void tryAttack(Unit enemy, List<Unit> players, GameState state, EnemyActionDto a) {
        Weapon w    = enemy.getEquippedWeapon();
        int minR    = w != null ? w.getMinRange() : 1;
        int maxR    = w != null ? w.getMaxRange() : 1;

        Unit inRange = players.stream()
            .filter(Unit::isAlive)
            .filter(p -> {
                int d = enemy.getPosition().distanceTo(p.getPosition());
                return d >= minR && d <= maxR;
            })
            .max(Comparator.comparingDouble(p -> threatScore(enemy, p)))
            .orElse(null);

        if (inRange != null) {
            CombatResultDto result = cbtSvc.executeCombat(enemy, inRange, state.getMap());
            a.setAttacked(true);
            a.setTargetId(inRange.getId());
            a.setCombatResult(result);
        }
    }

    private EnemyActionDto stayPut(Unit enemy, EnemyActionDto a) {
        a.setToX(enemy.getPosition().getX());
        a.setToY(enemy.getPosition().getY());
        return finalize(enemy, a);
    }

    private EnemyActionDto finalize(Unit enemy, EnemyActionDto a) {
        enemy.setHasMoved(true);
        enemy.setHasActed(true);
        return a;
    }
}
