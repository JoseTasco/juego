package com.ashencrown.model;

public class Unit {

    private final String     id;
    private final String     name;
    private final String     team;
    private final String     unitClass;
    private final AIBehavior aiBehavior;

    // Stats Fire Emblem
    private int maxHp;
    private int str;
    private int mag;
    private int skl;
    private int spd;
    private int lck;
    private int def;
    private int res;
    private int mov;

    // Progresión
    private int level;
    private int exp;

    // Equipamiento
    private Weapon equippedWeapon;

    // Estado
    private int      currentHp;
    private Position position;
    private boolean  hasMoved;
    private boolean  hasActed;
    private boolean  alive;

    // ── Constructor completo Fire Emblem ───────────────────────────────────────
    public Unit(String id, String name, String team, String unitClass, AIBehavior aiBehavior,
                int maxHp, int str, int mag, int skl, int spd, int lck,
                int def, int res, int mov, int level,
                Weapon weapon, Position startPosition) {
        this.id            = id;
        this.name          = name;
        this.team          = team;
        this.unitClass     = unitClass;
        this.aiBehavior    = aiBehavior;
        this.maxHp         = maxHp;
        this.str           = str;
        this.mag           = mag;
        this.skl           = skl;
        this.spd           = spd;
        this.lck           = lck;
        this.def           = def;
        this.res           = res;
        this.mov           = mov;
        this.level         = level;
        this.exp           = 0;
        this.equippedWeapon = weapon;
        this.currentHp     = maxHp;
        this.position      = startPosition;
        this.hasMoved      = false;
        this.hasActed      = false;
        this.alive         = true;
    }

    // ── Métodos de combate ─────────────────────────────────────────────────────
    public void receiveDamage(int damage) {
        this.currentHp = Math.max(0, this.currentHp - damage);
        if (this.currentHp == 0) this.alive = false;
    }

    public void heal(int amount) {
        this.currentHp = Math.min(maxHp, this.currentHp + amount);
    }

    public void addExp(int amount) {
        this.exp += amount;
    }

    public boolean canLevelUp() {
        return this.exp >= 100;
    }

    public void consumeLevelUp() {
        while (this.exp >= 100) {
            this.exp -= 100;
            this.level++;
        }
    }

    public void resetTurnFlags() {
        this.hasMoved = false;
        this.hasActed = false;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String     getId()              { return id; }
    public String     getName()            { return name; }
    public String     getTeam()            { return team; }
    public String     getUnitClass()       { return unitClass; }
    public AIBehavior getAiBehavior()      { return aiBehavior; }
    public int        getMaxHp()           { return maxHp; }
    public int        getCurrentHp()       { return currentHp; }
    public int        getStr()             { return str; }
    public int        getMag()             { return mag; }
    public int        getSkl()             { return skl; }
    public int        getSpd()             { return spd; }
    public int        getLck()             { return lck; }
    public int        getDef()             { return def; }
    public int        getRes()             { return res; }
    public int        getMov()             { return mov; }
    public int        getLevel()           { return level; }
    public int        getExp()             { return exp; }
    public Weapon     getEquippedWeapon()  { return equippedWeapon; }
    public Position   getPosition()        { return position; }
    public boolean    isHasMoved()         { return hasMoved; }
    public boolean    isHasActed()         { return hasActed; }
    public boolean    isAlive()            { return alive; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setPosition(Position p)   { this.position  = p; }
    public void setHasMoved(boolean b)    { this.hasMoved  = b; }
    public void setHasActed(boolean b)    { this.hasActed  = b; }
    public void setCurrentHp(int hp)      { this.currentHp = Math.max(0, hp); }
    public void setMaxHp(int hp)          { this.maxHp     = hp; }
    public void setStr(int v)             { this.str = v; }
    public void setMag(int v)             { this.mag = v; }
    public void setSkl(int v)             { this.skl = v; }
    public void setSpd(int v)             { this.spd = v; }
    public void setLck(int v)             { this.lck = v; }
    public void setDef(int v)             { this.def = v; }
    public void setRes(int v)             { this.res = v; }
    public void setLevel(int v)           { this.level = v; }
    public void setExp(int v)             { this.exp   = v; }
    public void setEquippedWeapon(Weapon w){ this.equippedWeapon = w; }

    @Override
    public String toString() {
        return name + " [" + unitClass + "] Lv" + level + " HP:" + currentHp + "/" + maxHp;
    }
}
