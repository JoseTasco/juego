const BASE_URL = 'http://localhost:8080/api/game';

export interface Position { x: number; y: number; }

export interface Unit {
  id: string;
  name: string;
  team: string;
  unitClass: string;
  maxHp: number;
  currentHp: number;
  atk: number;
  def: number;
  spd: number;
  mov: number;
  position: Position;
  hasMoved: boolean;
  hasActed: boolean;
  alive: boolean;
}

export interface GameState {
  units: Unit[];
  turnNumber: number;
  currentPhase: string;
  status: string;
}

export interface MoveResult {
  unitId: string;
  newX: number;
  newY: number;
  success: boolean;
  message: string;
}

export interface CombatResult {
  attackerId: string;
  defenderId: string;
  attackerDefeated: boolean;
  defenderDefeated: boolean;
  log: string[];
}

export const GameService = {

  async getState(): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/state`);
    return res.json();
  },

  async newGame(): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/new`, { method: 'POST' });
    return res.json();
  },

  async getReachableTiles(unitId: string): Promise<Position[]> {
    const res = await fetch(`${BASE_URL}/reachable/${unitId}`);
    return res.json();
  },

  async moveUnit(unitId: string, destX: number, destY: number): Promise<MoveResult> {
    const res = await fetch(`${BASE_URL}/move`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ unitId, destX, destY }),
    });
    return res.json();
  },

  async attackUnit(attackerId: string, defenderId: string): Promise<CombatResult> {
    const res = await fetch(`${BASE_URL}/attack`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ attackerId, defenderId }),
    });
    return res.json();
  },

  async waitUnit(unitId: string): Promise<void> {
    await fetch(`${BASE_URL}/wait/${unitId}`, { method: 'POST' });
  },

  async endTurn(): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/end-turn`, { method: 'POST' });
    return res.json();
  },
};
