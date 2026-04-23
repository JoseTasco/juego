package com.ashencrown.dto;

public class AttackRequestDto {
    private String attackerId;
    private String defenderId;
    public String getAttackerId()       { return attackerId; }
    public void setAttackerId(String a) { attackerId = a; }
    public String getDefenderId()       { return defenderId; }
    public void setDefenderId(String d) { defenderId = d; }
}
