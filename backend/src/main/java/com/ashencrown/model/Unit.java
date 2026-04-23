package com.ashencrown.model;

public class Unit {

    private final String id;
    private final String name;
    private final String team;
    private final String unitClass;

    private int maxHp;
    private int atk;
    private int def;
    private int spd;
    private int mov;

    private int currentHp;
    private Position position;
    private boolean hasMoved;
    private boolean hasActed;
    private boolean alive;

    public Unit(String id, String name, String team, String unitClass,
                int maxHp, int atk, int def, int spd, int mov,
                Position startPosition) {
        this.id        = id;
        this.name      = name;
        this.team      = team;
        this.unitClass = unitClass;
        this.maxHp     = maxHp;
        this.atk       = atk;
        this.def       = def;
        this.spd       = spd;
        this.mov       = mov;
        this.currentHp = maxHp;
        this.position  = startPosition;
        this.hasMoved  = false;
        this.hasActed  = false;
        this.alive     = true;
    }

    public void receiveDamage(int rawDamage, int terrainDefBonus) {
        int actualDamage = Math.max(0, rawDamage - this.def - terrainDefBonus);
        this.currentHp   = Math.max(0, this.currentHp - actualDamage);
        if (this.currentHp == 0) this.alive = false;
    }

    public int calculateDamageTo(Unit target) {
        return Math.max(0, this.atk - target.getDef());
    }

    public void resetTurnFlags() {
        this.hasMoved = false;
        this.hasActed = false;
    }

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getTeam()        { return team; }
    public String getUnitClass()   { return unitClass; }
    public int getMaxHp()          { return maxHp; }
    public int getCurrentHp()      { return currentHp; }
    public int getAtk()            { return atk; }
    public int getDef()            { return def; }
    public int getSpd()            { return spd; }
    public int getMov()            { return mov; }
    public Position getPosition()  { return position; }
    public boolean isHasMoved()    { return hasMoved; }
    public boolean isHasActed()    { return hasActed; }
    public boolean isAlive()       { return alive; }

    public void setPosition(Position p)    { this.position = p; }
    public void setHasMoved(boolean b)     { this.hasMoved = b; }
    public void setHasActed(boolean b)     { this.hasActed = b; }
    public void setCurrentHp(int hp)       { this.currentHp = Math.max(0, hp); }

    @Override
    public String toString() {
        return name + " [" + unitClass + "] HP:" + currentHp + "/" + maxHp + " en " + position;
    }
}
