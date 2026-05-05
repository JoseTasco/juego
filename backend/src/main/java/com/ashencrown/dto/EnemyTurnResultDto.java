package com.ashencrown.dto;

import com.ashencrown.model.GameState;
import java.util.List;

public class EnemyTurnResultDto {

    private List<EnemyActionDto> actions;
    private GameState            finalState;

    public EnemyTurnResultDto() {}

    public EnemyTurnResultDto(List<EnemyActionDto> actions, GameState finalState) {
        this.actions    = actions;
        this.finalState = finalState;
    }

    public List<EnemyActionDto> getActions()               { return actions; }
    public void                 setActions(List<EnemyActionDto> a) { actions = a; }
    public GameState            getFinalState()             { return finalState; }
    public void                 setFinalState(GameState s)  { finalState = s; }
}
