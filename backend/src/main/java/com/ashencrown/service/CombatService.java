package com.ashencrown.service;

import com.ashencrown.dto.CombatPreviewDto;
import com.ashencrown.dto.CombatResultDto;
import com.ashencrown.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class CombatService {

    private final ExperienceService expService;
    private static final Random RNG = new Random();

    public CombatService(ExperienceService expService) {
        this.expService = expService;
    }

    // ── Validación de rango ────────────────────────────────────────────────────
    public boolean isInRange(Unit attacker, Unit target) {
        Weapon w = attacker.getEquippedWeapon();
        if (w == null) return false;
        int dist = attacker.getPosition().distanceTo(target.getPosition());
        return dist >= w.getMinRange() && dist <= w.getMaxRange();
    }

    public boolean isInAttackRange(Unit attacker, Unit defender) {
        return isInRange(attacker, defender);
    }

    // ── Vista previa ──────────────────────────────────────────────────────────
    public CombatPreviewDto previewCombat(Unit atk, Unit def, GameMap map) {
        CombatPreviewDto p = new CombatPreviewDto();
        p.setAttackerId(atk.getId());
        p.setDefenderId(def.getId());
        p.setAttackerName(atk.getName());
        p.setDefenderName(def.getName());
        p.setAttackerHp(atk.getCurrentHp());
        p.setDefenderHp(def.getCurrentHp());

        Weapon aw = atk.getEquippedWeapon();
        Weapon dw = def.getEquippedWeapon();

        int defAvoid = map.getTile(def.getPosition()).getTerrain().getAvoidBonus();
        int atkAvoid = map.getTile(atk.getPosition()).getTerrain().getAvoidBonus();

        int[] aT = triangle(aw != null ? aw.getType() : null, dw != null ? dw.getType() : null);
        p.setAttackerHitRate(hitRate(atk, def, aw, defAvoid, aT[0]));
        p.setAttackerCritRate(critRate(atk, def, aw));
        p.setAttackerDamage(damage(atk, def, aw, aT[1]));
        p.setAttackerDoubleAttacks(atk.getSpd() >= def.getSpd() + 4);
        p.setAttackerWeaponName(aw != null ? aw.getName() : "Sin arma");

        boolean canCounter = dw != null && isInRange(def, atk);
        p.setDefenderCanCounter(canCounter);
        if (canCounter && dw != null) {
            int[] dT = triangle(dw.getType(), aw != null ? aw.getType() : null);
            p.setDefenderHitRate(hitRate(def, atk, dw, atkAvoid, dT[0]));
            p.setDefenderCritRate(critRate(def, atk, dw));
            p.setDefenderDamage(damage(def, atk, dw, dT[1]));
            p.setDefenderDoubleAttacks(def.getSpd() >= atk.getSpd() + 4);
            p.setDefenderWeaponName(dw.getName());
        }

        if (aT[0] > 0)       p.setWeaponAdvantage("ATTACKER");
        else if (aT[0] < 0)  p.setWeaponAdvantage("DEFENDER");
        else                  p.setWeaponAdvantage("NONE");

        return p;
    }

    // ── Combate real ──────────────────────────────────────────────────────────
    public CombatResultDto executeCombat(Unit atk, Unit def, GameMap map) {
        CombatResultDto r = new CombatResultDto();
        r.setAttackerId(atk.getId());
        r.setDefenderId(def.getId());

        Weapon aw = atk.getEquippedWeapon();
        Weapon dw = def.getEquippedWeapon();

        int defAvoid = map.getTile(def.getPosition()).getTerrain().getAvoidBonus();
        int atkAvoid = map.getTile(atk.getPosition()).getTerrain().getAvoidBonus();

        int[] aT = triangle(aw != null ? aw.getType() : null, dw != null ? dw.getType() : null);
        int[] dT = triangle(dw != null ? dw.getType() : null, aw != null ? aw.getType() : null);

        boolean atkDoubles = atk.getSpd() >= def.getSpd() + 4;
        boolean defDoubles = def.getSpd() >= atk.getSpd() + 4;

        // Ronda 1: atacante
        doHit(atk, def, aw, defAvoid, aT, r);
        if (!def.isAlive()) {
            finishCombat(atk, def, r, true, expService);
            return r;
        }

        // Contraataque
        boolean defCanCounter = dw != null && isInRange(def, atk);
        if (defCanCounter) {
            doHit(def, atk, dw, atkAvoid, dT, r);
            if (!atk.isAlive()) {
                finishCombat(def, atk, r, false, expService);
                return r;
            }
        }

        // Doble ataque — atacante
        if (atkDoubles) {
            doHit(atk, def, aw, defAvoid, aT, r);
            if (!def.isAlive()) {
                finishCombat(atk, def, r, true, expService);
                return r;
            }
        }

        // Doble ataque — defensor
        if (defDoubles && defCanCounter) {
            doHit(def, atk, dw, atkAvoid, dT, r);
            if (!atk.isAlive()) {
                finishCombat(def, atk, r, false, expService);
                return r;
            }
        }

        // Nadie murió → EXP por golpe
        int exp = expService.expForHit();
        atk.addExp(exp);
        r.setAttackerExpGained(exp);
        List<String> gains = expService.applyLevelUp(atk);
        r.setAttackerLevelUpGains(gains);
        if (!gains.isEmpty() && !gains.get(0).startsWith("¡Sin"))
            r.addLog("★ " + atk.getName() + " sube a Lv " + atk.getLevel() + "!");

        r.setAttackerFinalHp(atk.getCurrentHp());
        r.setDefenderFinalHp(def.getCurrentHp());
        atk.setHasActed(true);
        return r;
    }

    // ── Internos ──────────────────────────────────────────────────────────────
    private void doHit(Unit attacker, Unit defender, Weapon w, int terrainAvoid,
                       int[] tri, CombatResultDto r) {
        int hr  = hitRate(attacker, defender, w, terrainAvoid, tri[0]);
        int cr  = critRate(attacker, defender, w);
        int dmg = damage(attacker, defender, w, tri[1]);

        boolean hit  = RNG.nextInt(100) < hr;
        boolean crit = hit && RNG.nextInt(100) < cr;

        if (!hit) {
            r.addRound(attacker.getId(), 0, false, true);
            r.addLog(attacker.getName() + " falla!");
            return;
        }
        if (crit) dmg *= 3;
        defender.receiveDamage(dmg);
        r.addRound(attacker.getId(), dmg, crit, false);
        r.addLog(attacker.getName() + " → " + dmg + (crit ? " ¡CRÍTICO!" : "")
                 + "  HP: " + defender.getCurrentHp() + "/" + defender.getMaxHp());
    }

    private void finishCombat(Unit killer, Unit dead, CombatResultDto r,
                               boolean killerIsAttacker, ExperienceService es) {
        if (killerIsAttacker) {
            r.setDefenderDefeated(true);
            r.setDefenderFinalHp(0);
            r.setAttackerFinalHp(killer.getCurrentHp());
        } else {
            r.setAttackerDefeated(true);
            r.setAttackerFinalHp(0);
            r.setDefenderFinalHp(killer.getCurrentHp());
        }
        r.addLog(dead.getName() + " ha sido derrotado!");

        int exp = es.expForKill(killer, dead);
        killer.addExp(exp);
        if (killerIsAttacker) r.setAttackerExpGained(r.getAttackerExpGained() + exp);
        else                  r.setDefenderExpGained(r.getDefenderExpGained() + exp);

        List<String> gains = es.applyLevelUp(killer);
        if (killerIsAttacker) r.setAttackerLevelUpGains(gains);
        else                  r.setDefenderLevelUpGains(gains);
        if (!gains.isEmpty() && !gains.get(0).startsWith("¡Sin"))
            r.addLog("★ " + killer.getName() + " sube a Lv " + killer.getLevel() + "!");

        killer.setHasActed(true);
    }

    // ── Fórmulas Fire Emblem ───────────────────────────────────────────────────
    private int hitRate(Unit atk, Unit def, Weapon w, int terrainAvoid, int triHit) {
        if (w == null) return 50;
        return Math.max(0, Math.min(100,
            atk.getSkl() * 2 + atk.getLck() / 2 + w.getHit()
            - def.getSpd() * 2 - terrainAvoid + triHit));
    }

    private int critRate(Unit atk, Unit def, Weapon w) {
        if (w == null) return 0;
        return Math.max(0, atk.getSkl() / 2 + w.getCrit() - def.getLck() / 2);
    }

    private int damage(Unit atk, Unit def, Weapon w, int triDmg) {
        if (w == null) return Math.max(0, atk.getStr() - def.getDef());
        return w.isMagical()
            ? Math.max(0, atk.getMag() + w.getMight() - def.getRes() + triDmg)
            : Math.max(0, atk.getStr() + w.getMight() - def.getDef() + triDmg);
    }

    // Triángulo de armas: SWORD>AXE>LANCE>SWORD  |  ANIMA>LIGHT>DARK>ANIMA
    private int[] triangle(WeaponType a, WeaponType d) {
        if (a == null || d == null) return new int[]{0, 0};
        boolean adv = (a == WeaponType.SWORD  && d == WeaponType.AXE)
                   || (a == WeaponType.AXE    && d == WeaponType.LANCE)
                   || (a == WeaponType.LANCE   && d == WeaponType.SWORD)
                   || (a == WeaponType.ANIMA   && d == WeaponType.LIGHT)
                   || (a == WeaponType.LIGHT   && d == WeaponType.DARK)
                   || (a == WeaponType.DARK    && d == WeaponType.ANIMA);
        boolean dis = (a == WeaponType.AXE    && d == WeaponType.SWORD)
                   || (a == WeaponType.LANCE   && d == WeaponType.AXE)
                   || (a == WeaponType.SWORD   && d == WeaponType.LANCE)
                   || (a == WeaponType.LIGHT   && d == WeaponType.ANIMA)
                   || (a == WeaponType.DARK    && d == WeaponType.LIGHT)
                   || (a == WeaponType.ANIMA   && d == WeaponType.DARK);
        if (adv) return new int[]{+15, +1};
        if (dis) return new int[]{-15, -1};
        return new int[]{0, 0};
    }
}
