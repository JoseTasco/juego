package com.ashencrown.service;

import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Calcula las casillas alcanzables por una unidad usando BFS.
 *
 * ESTRUCTURA DE DATOS:
 *   - Queue<BfsNode>     → Cola FIFO para la exploración por capas
 *   - Map<String,Integer>→ HashMap de posiciones visitadas
 *   - Set<String>        → HashSet de posiciones bloqueadas por enemigos
 */
@Service
public class MovementService {

    public List<Position> getReachableTiles(Unit unit, GameMap map, List<Unit> units) {

        Queue<BfsNode> queue         = new LinkedList<>();
        Map<String, Integer> visited = new HashMap<>();
        List<Position> reachable     = new ArrayList<>();

        Set<String> blocked = new HashSet<>();
        for (Unit other : units) {
            if (other.isAlive() && !other.getId().equals(unit.getId()))
                blocked.add(key(other.getPosition()));
        }

        Position start = unit.getPosition();
        queue.add(new BfsNode(start, unit.getMov()));
        visited.put(key(start), unit.getMov());

        while (!queue.isEmpty()) {
            BfsNode current = queue.poll();
            reachable.add(current.position);
            if (current.remainingMov == 0) continue;

            for (Position neighbor : current.position.neighbors()) {
                int x = neighbor.getX(), y = neighbor.getY();
                if (!map.isInBounds(x, y)) continue;

                Tile tile = map.getTile(x, y);
                if (tile.getTerrain().isImpassable()) continue;
                if (blocked.contains(key(neighbor))) continue;

                int cost         = tile.getTerrain().getMoveCost();
                int newRemaining = current.remainingMov - cost;
                if (newRemaining < 0) continue;

                String nKey = key(neighbor);
                if (!visited.containsKey(nKey) || visited.get(nKey) < newRemaining) {
                    visited.put(nKey, newRemaining);
                    queue.add(new BfsNode(neighbor, newRemaining));
                }
            }
        }
        return reachable;
    }

    public boolean canMoveTo(Unit unit, Position destination, GameMap map, List<Unit> units) {
        return getReachableTiles(unit, map, units).stream().anyMatch(p -> p.equals(destination));
    }

    private String key(Position p) { return p.getX() + "," + p.getY(); }

    private static class BfsNode {
        final Position position;
        final int remainingMov;
        BfsNode(Position position, int remainingMov) {
            this.position     = position;
            this.remainingMov = remainingMov;
        }
    }
}
