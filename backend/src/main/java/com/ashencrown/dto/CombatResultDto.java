package com.ashencrown.dto;

import java.util.ArrayList;
import java.util.List;

public class CombatResultDto {

    private String  attackerId;
    private String  defenderId;
    private int     attackerFinalHp;
    private int     defenderFinalHp;
    private boolean attackerDefeated;
    private boolean defenderDefeated;
    private int     attackerExpGained;
    private int     defenderExpGained;
    private List<String>       attackerLevelUpGains = new ArrayList<>();
    private List<String>       defenderLevelUpGains = new ArrayList<>();
    private List<CombatRound>  rounds = new ArrayList<>();
    private List<String>       log    = new ArrayList<>();

    // ── Ronda individual ───────────────────────────────────────────────────────
    public static class CombatRound {
        private final String  attackerId;
        private final int     damage;
        private final boolean crit;
        private final boolean missed;

        public CombatRound(String attackerId, int damage, boolean crit, boolean missed) {
            this.attackerId = attackerId;
            this.damage     = damage;
            this.crit       = crit;
            this.missed     = missed;
        }

        public String  getAttackerId() { return attackerId; }
        public int     getDamage()     { return damage; }
        public boolean isCrit()        { return crit; }
        public boolean isMissed()      { return missed; }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    public void addRound(String attackerId, int damage, boolean crit, boolean missed) {
        rounds.add(new CombatRound(attackerId, damage, crit, missed));
    }

    public void addLog(String entry) { log.add(entry); }

    // ── Getters / Setters ──────────────────────────────────────────────────────
    public String  getAttackerId()                      { return attackerId; }
    public void    setAttackerId(String a)              { attackerId = a; }
    public String  getDefenderId()                      { return defenderId; }
    public void    setDefenderId(String d)              { defenderId = d; }
    public int     getAttackerFinalHp()                 { return attackerFinalHp; }
    public void    setAttackerFinalHp(int hp)           { attackerFinalHp = hp; }
    public int     getDefenderFinalHp()                 { return defenderFinalHp; }
    public void    setDefenderFinalHp(int hp)           { defenderFinalHp = hp; }
    public boolean isAttackerDefeated()                 { return attackerDefeated; }
    public void    setAttackerDefeated(boolean b)       { attackerDefeated = b; }
    public boolean isDefenderDefeated()                 { return defenderDefeated; }
    public void    setDefenderDefeated(boolean b)       { defenderDefeated = b; }
    public int     getAttackerExpGained()               { return attackerExpGained; }
    public void    setAttackerExpGained(int e)          { attackerExpGained = e; }
    public int     getDefenderExpGained()               { return defenderExpGained; }
    public void    setDefenderExpGained(int e)          { defenderExpGained = e; }
    public List<String>      getAttackerLevelUpGains()  { return attackerLevelUpGains; }
    public void              setAttackerLevelUpGains(List<String> g) { attackerLevelUpGains = g; }
    public List<String>      getDefenderLevelUpGains()  { return defenderLevelUpGains; }
    public void              setDefenderLevelUpGains(List<String> g) { defenderLevelUpGains = g; }
    public List<CombatRound> getRounds()                { return rounds; }
    public List<String>      getLog()                   { return log; }
}
