package com.ashencrown.service;

import com.ashencrown.model.Unit;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExperienceService {

    private static final Random RNG = new Random();

    // HP, STR, MAG, SKL, SPD, LCK, DEF, RES — probabilidades por clase
    private static final Map<String, int[]> GROWTHS = new HashMap<>();
    static {
        GROWTHS.put("Lord",     new int[]{75, 50, 25, 60, 65, 60, 35, 30});
        GROWTHS.put("Paladin",  new int[]{70, 55, 20, 55, 45, 45, 50, 35});
        GROWTHS.put("Mage",     new int[]{55, 15, 80, 60, 55, 45, 20, 60});
        GROWTHS.put("Knight",   new int[]{80, 55, 10, 45, 25, 35, 65, 25});
        GROWTHS.put("Archer",   new int[]{65, 50, 15, 70, 55, 45, 35, 25});
        GROWTHS.put("Healer",   new int[]{55, 15, 75, 60, 50, 60, 15, 65});
        GROWTHS.put("Thief",    new int[]{60, 45, 10, 75, 75, 80, 25, 20});
        GROWTHS.put("Cavalier", new int[]{75, 55, 15, 55, 50, 40, 45, 30});
        GROWTHS.put("Myrmidon", new int[]{65, 55, 15, 80, 80, 55, 30, 20});
        GROWTHS.put("Warrior",  new int[]{85, 70, 10, 50, 40, 30, 50, 20});
        GROWTHS.put("Bandit",   new int[]{80, 65,  5, 45, 35, 25, 45, 15});
    }

    private static final int[] DEFAULT_GROWTHS = {70, 45, 20, 50, 50, 40, 35, 25};

    // ── Cálculo de EXP ─────────────────────────────────────────────────────────
    public int expForKill(Unit attacker, Unit defender) {
        return Math.max(10, 100 - (defender.getLevel() - attacker.getLevel()) * 10);
    }

    public int expForHit()  { return 31; }
    public int expForHeal() { return 15; }

    // ── Level up ───────────────────────────────────────────────────────────────
    public List<String> applyLevelUp(Unit unit) {
        List<String> gains = new ArrayList<>();
        if (!unit.canLevelUp()) return gains;

        unit.consumeLevelUp();

        int[] gr = GROWTHS.getOrDefault(unit.getUnitClass(), DEFAULT_GROWTHS);

        if (RNG.nextInt(100) < gr[0]) { unit.setMaxHp(unit.getMaxHp() + 1); gains.add("HP+1"); }
        if (RNG.nextInt(100) < gr[1]) { unit.setStr(unit.getStr() + 1);     gains.add("STR+1"); }
        if (RNG.nextInt(100) < gr[2]) { unit.setMag(unit.getMag() + 1);     gains.add("MAG+1"); }
        if (RNG.nextInt(100) < gr[3]) { unit.setSkl(unit.getSkl() + 1);     gains.add("SKL+1"); }
        if (RNG.nextInt(100) < gr[4]) { unit.setSpd(unit.getSpd() + 1);     gains.add("SPD+1"); }
        if (RNG.nextInt(100) < gr[5]) { unit.setLck(unit.getLck() + 1);     gains.add("LCK+1"); }
        if (RNG.nextInt(100) < gr[6]) { unit.setDef(unit.getDef() + 1);     gains.add("DEF+1"); }
        if (RNG.nextInt(100) < gr[7]) { unit.setRes(unit.getRes() + 1);     gains.add("RES+1"); }

        if (gains.isEmpty()) gains.add("¡Sin cambios!");
        return gains;
    }
}
