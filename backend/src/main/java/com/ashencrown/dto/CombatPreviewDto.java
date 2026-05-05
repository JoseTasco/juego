package com.ashencrown.dto;

public class CombatPreviewDto {

    private String  attackerId;
    private String  defenderId;
    private String  attackerName;
    private String  defenderName;
    private int     attackerHp;
    private int     defenderHp;
    private int     attackerDamage;
    private int     defenderDamage;
    private int     attackerHitRate;
    private int     defenderHitRate;
    private int     attackerCritRate;
    private int     defenderCritRate;
    private boolean attackerDoubleAttacks;
    private boolean defenderDoubleAttacks;
    private boolean defenderCanCounter;
    private String  weaponAdvantage;   // "ATTACKER" | "DEFENDER" | "NONE"
    private String  attackerWeaponName;
    private String  defenderWeaponName;

    public String  getAttackerId()                    { return attackerId; }
    public void    setAttackerId(String v)            { attackerId = v; }
    public String  getDefenderId()                    { return defenderId; }
    public void    setDefenderId(String v)            { defenderId = v; }
    public String  getAttackerName()                  { return attackerName; }
    public void    setAttackerName(String v)          { attackerName = v; }
    public String  getDefenderName()                  { return defenderName; }
    public void    setDefenderName(String v)          { defenderName = v; }
    public int     getAttackerHp()                    { return attackerHp; }
    public void    setAttackerHp(int v)               { attackerHp = v; }
    public int     getDefenderHp()                    { return defenderHp; }
    public void    setDefenderHp(int v)               { defenderHp = v; }
    public int     getAttackerDamage()                { return attackerDamage; }
    public void    setAttackerDamage(int v)           { attackerDamage = v; }
    public int     getDefenderDamage()                { return defenderDamage; }
    public void    setDefenderDamage(int v)           { defenderDamage = v; }
    public int     getAttackerHitRate()               { return attackerHitRate; }
    public void    setAttackerHitRate(int v)          { attackerHitRate = v; }
    public int     getDefenderHitRate()               { return defenderHitRate; }
    public void    setDefenderHitRate(int v)          { defenderHitRate = v; }
    public int     getAttackerCritRate()              { return attackerCritRate; }
    public void    setAttackerCritRate(int v)         { attackerCritRate = v; }
    public int     getDefenderCritRate()              { return defenderCritRate; }
    public void    setDefenderCritRate(int v)         { defenderCritRate = v; }
    public boolean isAttackerDoubleAttacks()          { return attackerDoubleAttacks; }
    public void    setAttackerDoubleAttacks(boolean v){ attackerDoubleAttacks = v; }
    public boolean isDefenderDoubleAttacks()          { return defenderDoubleAttacks; }
    public void    setDefenderDoubleAttacks(boolean v){ defenderDoubleAttacks = v; }
    public boolean isDefenderCanCounter()             { return defenderCanCounter; }
    public void    setDefenderCanCounter(boolean v)   { defenderCanCounter = v; }
    public String  getWeaponAdvantage()               { return weaponAdvantage; }
    public void    setWeaponAdvantage(String v)       { weaponAdvantage = v; }
    public String  getAttackerWeaponName()            { return attackerWeaponName; }
    public void    setAttackerWeaponName(String v)    { attackerWeaponName = v; }
    public String  getDefenderWeaponName()            { return defenderWeaponName; }
    public void    setDefenderWeaponName(String v)    { defenderWeaponName = v; }
}
