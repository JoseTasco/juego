package com.ashencrown.dto;

public class MoveRequestDto {
    private String unitId;
    private int destX;
    private int destY;
    public String getUnitId()      { return unitId; }
    public void setUnitId(String u){ unitId = u; }
    public int getDestX()          { return destX; }
    public void setDestX(int x)    { destX = x; }
    public int getDestY()          { return destY; }
    public void setDestY(int y)    { destY = y; }
}
