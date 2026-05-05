package com.ashencrown.dto;

public class EnemyActionDto {

    private String         unitId;
    private String         unitName;
    private int            fromX;
    private int            fromY;
    private int            toX;
    private int            toY;
    private boolean        attacked;
    private String         targetId;
    private CombatResultDto combatResult;

    public String          getUnitId()                    { return unitId; }
    public void            setUnitId(String v)            { unitId = v; }
    public String          getUnitName()                  { return unitName; }
    public void            setUnitName(String v)          { unitName = v; }
    public int             getFromX()                     { return fromX; }
    public void            setFromX(int v)                { fromX = v; }
    public int             getFromY()                     { return fromY; }
    public void            setFromY(int v)                { fromY = v; }
    public int             getToX()                       { return toX; }
    public void            setToX(int v)                  { toX = v; }
    public int             getToY()                       { return toY; }
    public void            setToY(int v)                  { toY = v; }
    public boolean         isAttacked()                   { return attacked; }
    public void            setAttacked(boolean v)         { attacked = v; }
    public String          getTargetId()                  { return targetId; }
    public void            setTargetId(String v)          { targetId = v; }
    public CombatResultDto getCombatResult()              { return combatResult; }
    public void            setCombatResult(CombatResultDto v) { combatResult = v; }
}
