package com.ashencrown.dto;

public class MoveResultDto {
    private String unitId;
    private int newX;
    private int newY;
    private boolean success;
    private String message;
    public String getUnitId()         { return unitId; }
    public void setUnitId(String u)   { unitId = u; }
    public int getNewX()              { return newX; }
    public void setNewX(int x)        { newX = x; }
    public int getNewY()              { return newY; }
    public void setNewY(int y)        { newY = y; }
    public boolean isSuccess()        { return success; }
    public void setSuccess(boolean s) { success = s; }
    public String getMessage()        { return message; }
    public void setMessage(String m)  { message = m; }
}
