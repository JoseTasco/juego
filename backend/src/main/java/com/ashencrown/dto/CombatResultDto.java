package com.ashencrown.dto;

import java.util.ArrayList;
import java.util.List;

public class CombatResultDto {
    private String attackerId;
    private String defenderId;
    private boolean attackerDefeated;
    private boolean defenderDefeated;
    private List<String> log = new ArrayList<>();

    public String getAttackerId()              { return attackerId; }
    public void setAttackerId(String a)        { attackerId = a; }
    public String getDefenderId()              { return defenderId; }
    public void setDefenderId(String d)        { defenderId = d; }
    public boolean isAttackerDefeated()        { return attackerDefeated; }
    public void setAttackerDefeated(boolean b) { attackerDefeated = b; }
    public boolean isDefenderDefeated()        { return defenderDefeated; }
    public void setDefenderDefeated(boolean b) { defenderDefeated = b; }
    public List<String> getLog()               { return log; }
    public void addLog(String entry)           { log.add(entry); }
}
