package com.ashencrown.service;

import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * A* para encontrar el camino más corto respetando costos de terreno.
 * Usado por la IA para calcular rutas óptimas hacia objetivos.
 */
@Service
public class PathfindingService {

    public List<Position> findPath(Position start, Position goal,
                                   GameMap map, Set<String> blocked) {
        Map<String, Integer>  gScore   = new HashMap<>();
        Map<String, Integer>  fScore   = new HashMap<>();
        Map<String, Position> cameFrom = new HashMap<>();
        Set<String>           closed   = new HashSet<>();

        PriorityQueue<Position> open = new PriorityQueue<>(
            Comparator.comparingInt(p -> fScore.getOrDefault(key(p), Integer.MAX_VALUE)));

        gScore.put(key(start), 0);
        fScore.put(key(start), h(start, goal));
        open.add(start);

        while (!open.isEmpty()) {
            Position cur = open.poll();
            if (cur.equals(goal)) return reconstruct(cameFrom, cur);
            closed.add(key(cur));

            for (Position nb : cur.neighbors()) {
                if (!map.isInBounds(nb.getX(), nb.getY())) continue;
                Tile tile = map.getTile(nb);
                if (tile.getTerrain().isImpassable()) continue;
                if (blocked.contains(key(nb)))         continue;
                if (closed.contains(key(nb)))          continue;

                int tentG = gScore.getOrDefault(key(cur), Integer.MAX_VALUE / 2)
                            + tile.getTerrain().getMoveCost();

                if (tentG < gScore.getOrDefault(key(nb), Integer.MAX_VALUE)) {
                    cameFrom.put(key(nb), cur);
                    gScore.put(key(nb), tentG);
                    fScore.put(key(nb), tentG + h(nb, goal));
                    open.add(nb);
                }
            }
        }
        return Collections.emptyList(); // no hay camino
    }

    private List<Position> reconstruct(Map<String, Position> cameFrom, Position cur) {
        List<Position> path = new ArrayList<>();
        while (cameFrom.containsKey(key(cur))) {
            path.add(0, cur);
            cur = cameFrom.get(key(cur));
        }
        return path;
    }

    private int h(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private String key(Position p) { return p.getX() + "," + p.getY(); }
}
