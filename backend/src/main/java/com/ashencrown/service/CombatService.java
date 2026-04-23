package com.ashencrown.service;

import com.ashencrown.dto.CombatResultDto;
import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

@Service
public class CombatService {

    private static final int ATTACK_RANGE = 1;

    public boolean isInAttackRange(Unit attacker, Unit defender) {
        return attacker.getPosition().distanceTo(defender.getPosition()) <= ATTACK_RANGE;
    }

    public CombatResultDto executeCombat(Unit attacker, Unit defender, GameMap map) {
        CombatResultDto result = new CombatResultDto();
        result.setAttackerId(attacker.getId());
        result.setDefenderId(defender.getId());

        Tile defenderTile   = map.getTile(defender.getPosition());
        int terrainDefBonus = defenderTile.getTerrain().getDefenseBonus();

        // Ronda 1: ataque
        int dmg1 = attacker.calculateDamageTo(defender);
        defender.receiveDamage(dmg1, terrainDefBonus);
        result.addLog(attacker.getName() + " ataca a " + defender.getName()
                + " por " + dmg1 + " de daño. HP: " + defender.getCurrentHp());

        if (!defender.isAlive()) {
            result.addLog(defender.getName() + " ha sido derrotado.");
            result.setDefenderDefeated(true);
            attacker.setHasActed(true);
            return result;
        }

        // Contraataque
        Tile attackerTile      = map.getTile(attacker.getPosition());
        int terrainAtkDefBonus = attackerTile.getTerrain().getDefenseBonus();
        int dmg2               = defender.calculateDamageTo(attacker);
        attacker.receiveDamage(dmg2, terrainAtkDefBonus);
        result.addLog(defender.getName() + " contraataca por " + dmg2
                + " de daño. HP: " + attacker.getCurrentHp());

        if (!attacker.isAlive()) {
            result.addLog(attacker.getName() + " ha sido derrotado.");
            result.setAttackerDefeated(true);
            return result;
        }

        // Doble ataque si SPD >= 5 más
        if (attacker.getSpd() >= defender.getSpd() + 5) {
            int dmg3 = attacker.calculateDamageTo(defender);
            defender.receiveDamage(dmg3, terrainDefBonus);
            result.addLog(attacker.getName() + " ataca de nuevo por " + dmg3
                    + ". HP: " + defender.getCurrentHp());
            if (!defender.isAlive()) {
                result.addLog(defender.getName() + " ha sido derrotado.");
                result.setDefenderDefeated(true);
            }
        }

        attacker.setHasActed(true);
        return result;
    }
}
