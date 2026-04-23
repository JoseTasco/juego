import { GameService, GameState, Unit, Position } from '../services/GameService';
import { AuthService } from '../services/AuthService';

const COLS = 10, ROWS = 8, TILE = 56;

const TERRAIN_COLORS: { [key: number]: [string, string] } = {
  0: ['#5a8a3c','#6da048'],
  1: ['#2d6e2d','#3a8a3a'],
  2: ['#888','#999'],
  3: ['#1a5a9e','#2278c8'],
  4: ['#b08030','#c89040'],
  5: ['#c8b078','#d8c090'],
};

const MAP_LAYOUT = [
  [0,0,5,5,0,0,0,2,2,2],
  [0,1,5,0,0,3,0,0,2,0],
  [0,1,0,0,3,3,0,0,0,0],
  [0,0,0,1,3,0,4,0,0,0],
  [0,0,1,1,0,0,0,0,1,0],
  [5,5,5,0,0,2,0,1,1,0],
  [0,0,5,0,0,2,0,0,0,0],
  [0,0,5,5,5,5,5,5,0,0],
];

export class GamePage {

  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private gameState!: GameState;
  private selectedUnit: Unit | null = null;
  private reachableTiles: Position[] = [];
  private container!: HTMLElement;

  async render(container: HTMLElement): Promise<void> {
    this.container = container;
    container.innerHTML = this.getHTML();
    this.canvas = container.querySelector('#game-canvas') as HTMLCanvasElement;
    this.ctx    = this.canvas.getContext('2d')!;

    this.canvas.style.width  = '100%';
    this.canvas.style.height = 'auto';

    try {
      this.gameState = await GameService.newGame();
    } catch {
      this.gameState = {
        units: [
          { id:'marth', name:'Marth', team:'player', unitClass:'Lord',    maxHp:20, currentHp:20, atk:8, def:4, spd:9, mov:5, position:{x:1,y:3}, hasMoved:false, hasActed:false, alive:true },
          { id:'jagen', name:'Jagen', team:'player', unitClass:'Paladin', maxHp:18, currentHp:18, atk:7, def:6, spd:6, mov:6, position:{x:0,y:5}, hasMoved:false, hasActed:false, alive:true },
        ],
        turnNumber: 1,
        currentPhase: 'PLAYER',
        status: 'ACTIVE',
      };
    }

    this.render_map();
    this.agregarEventos(container);
  }

  private getHTML(): string {
    return `
      <div class="game-wrapper">
        <div class="game-topbar">
          <span id="game-phase">⚔ FASE DEL JUGADOR</span>
          <span id="game-turn">TURNO 1</span>
        </div>

        <div class="game-layout">
          <div class="game-map-wrapper">
            <canvas id="game-canvas" width="${COLS * TILE}" height="${ROWS * TILE}"></canvas>
          </div>

          <div class="game-sidebar">
            <div class="unit-card" id="card-marth">
              <div class="unit-card-name">⚔ MARTH</div>
              <div class="unit-stat">HP: <span id="hp-marth">20</span>/20</div>
              <div class="unit-hpbar-bg"><div class="unit-hpbar" id="hpbar-marth" style="width:100%"></div></div>
              <div class="unit-stat">ATK: <span>8</span> | MOV: <span>5</span></div>
              <div class="unit-stat">POS: <span id="pos-marth">—</span></div>
              <div class="unit-stat">Estado: <span id="status-marth">Listo</span></div>
            </div>
            <div class="unit-card" id="card-jagen">
              <div class="unit-card-name">🛡 JAGEN</div>
              <div class="unit-stat">HP: <span id="hp-jagen">18</span>/18</div>
              <div class="unit-hpbar-bg"><div class="unit-hpbar" id="hpbar-jagen" style="width:100%"></div></div>
              <div class="unit-stat">ATK: <span>7</span> | MOV: <span>6</span></div>
              <div class="unit-stat">POS: <span id="pos-jagen">—</span></div>
              <div class="unit-stat">Estado: <span id="status-jagen">Listo</span></div>
            </div>
            <div class="game-msgbox" id="game-msg">Selecciona una unidad para moverla.</div>
            <div class="game-controls">
              <button class="game-btn game-btn-gold"  id="btn-endturn">▶ Fin de turno</button>
              <button class="game-btn game-btn-red"   id="btn-wait">⏸ Esperar</button>
              <button class="game-btn"                id="btn-menu">↩ Menú</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  private render_map(): void {
    const ctx = this.ctx;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    const reachSet = new Set(this.reachableTiles.map(p => `${p.x},${p.y}`));

    for (let y = 0; y < ROWS; y++) {
      for (let x = 0; x < COLS; x++) {
        const t    = MAP_LAYOUT[y][x];
        const [c1,c2] = TERRAIN_COLORS[t];
        const px   = x * TILE, py = y * TILE;
        const isSelected = this.selectedUnit?.position.x === x && this.selectedUnit?.position.y === y;
        const isReach    = reachSet.has(`${x},${y}`) && !isSelected;

        ctx.fillStyle = (x + y) % 2 === 0 ? c1 : c2;
        ctx.fillRect(px, py, TILE, TILE);

        if (t === 1) {
          ctx.fillStyle = 'rgba(0,60,0,0.35)';
          ctx.beginPath(); ctx.arc(px+TILE/2, py+TILE/2, 14, 0, Math.PI*2); ctx.fill();
        }
        if (t === 2) {
          ctx.fillStyle = 'rgba(255,255,255,0.15)';
          ctx.beginPath(); ctx.moveTo(px+TILE/2,py+8); ctx.lineTo(px+TILE-8,py+TILE-10); ctx.lineTo(px+8,py+TILE-10); ctx.closePath(); ctx.fill();
        }
        if (t === 3) {
          ctx.strokeStyle = 'rgba(100,180,255,0.3)'; ctx.lineWidth = 1;
          for (let i=0;i<3;i++) { ctx.beginPath(); ctx.moveTo(px+6,py+14+i*14); ctx.bezierCurveTo(px+18,py+10+i*14,px+36,py+18+i*14,px+50,py+14+i*14); ctx.stroke(); }
        }
        if (t === 4) {
          ctx.strokeStyle='#f0c040'; ctx.lineWidth=2;
          ctx.strokeRect(px+8,py+8,TILE-16,TILE-16);
          ctx.strokeRect(px+14,py+14,TILE-28,TILE-28);
        }

        if (isReach) {
          ctx.fillStyle = 'rgba(74,158,255,0.30)'; ctx.fillRect(px,py,TILE,TILE);
          ctx.strokeStyle='rgba(74,158,255,0.6)'; ctx.lineWidth=1; ctx.strokeRect(px+1,py+1,TILE-2,TILE-2);
        }
        if (isSelected) {
          ctx.fillStyle='rgba(240,192,64,0.45)'; ctx.fillRect(px,py,TILE,TILE);
          ctx.strokeStyle='#f0c040'; ctx.lineWidth=2; ctx.strokeRect(px+1,py+1,TILE-2,TILE-2);
        }

        ctx.strokeStyle='rgba(0,0,0,0.15)'; ctx.lineWidth=0.5; ctx.strokeRect(px,py,TILE,TILE);
      }
    }

    this.gameState.units.forEach(unit => {
      if (!unit.alive) return;
      const px = unit.position.x * TILE, py = unit.position.y * TILE;
      const cx = px + TILE/2, cy = py + TILE/2;
      const color = unit.team === 'player' ? '#4a9eff' : '#ff4a4a';

      ctx.fillStyle    = unit.hasMoved ? 'rgba(80,80,120,0.85)' : color + 'dd';
      ctx.strokeStyle  = (this.selectedUnit?.id === unit.id) ? '#f0c040' : '#000';
      ctx.lineWidth    = (this.selectedUnit?.id === unit.id) ? 2.5 : 1.5;
      this.roundRect(px+6, py+6, TILE-12, TILE-12, 6);
      ctx.fill(); ctx.stroke();

      ctx.font = `${TILE*0.38}px serif`;
      ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
      ctx.fillStyle = '#fff';
      ctx.fillText(unit.unitClass === 'Lord' ? '⚔' : unit.team === 'player' ? '🛡' : '💀', cx, cy-4);

      ctx.font = `bold ${TILE*0.18}px monospace`;
      ctx.fillStyle = '#fff';
      ctx.fillText(`${unit.currentHp}/${unit.maxHp}`, cx, cy+14);

      if (unit.hasMoved) {
        ctx.fillStyle = 'rgba(0,0,0,0.45)';
        this.roundRect(px+6, py+6, TILE-12, TILE-12, 6);
        ctx.fill();
      }
    });

    this.updateUI();
  }

  private roundRect(x: number, y: number, w: number, h: number, r: number): void {
    const ctx = this.ctx;
    ctx.beginPath();
    ctx.moveTo(x+r,y);
    ctx.lineTo(x+w-r,y); ctx.arcTo(x+w,y,x+w,y+r,r);
    ctx.lineTo(x+w,y+h-r); ctx.arcTo(x+w,y+h,x+w-r,y+h,r);
    ctx.lineTo(x+r,y+h); ctx.arcTo(x,y+h,x,y+h-r,r);
    ctx.lineTo(x,y+r); ctx.arcTo(x,y,x+r,y,r);
    ctx.closePath();
  }

  private updateUI(): void {
    const phase = this.container.querySelector('#game-phase') as HTMLElement;
    const turn  = this.container.querySelector('#game-turn')  as HTMLElement;
    if (phase) phase.textContent = this.gameState.currentPhase === 'PLAYER' ? '⚔ FASE DEL JUGADOR' : '💀 FASE ENEMIGA';
    if (turn)  turn.textContent  = `TURNO ${this.gameState.turnNumber}`;

    this.gameState.units.forEach(u => {
      const id = u.id;
      const hp  = this.container.querySelector(`#hp-${id}`)     as HTMLElement;
      const bar = this.container.querySelector(`#hpbar-${id}`)  as HTMLElement;
      const pos = this.container.querySelector(`#pos-${id}`)    as HTMLElement;
      const st  = this.container.querySelector(`#status-${id}`) as HTMLElement;
      const card= this.container.querySelector(`#card-${id}`)   as HTMLElement;

      if (!hp) return;
      hp.textContent  = String(u.currentHp);
      pos.textContent = `(${u.position.x},${u.position.y})`;
      st.textContent  = !u.alive ? '✝ Caído' : u.hasMoved ? '⏸ Esperando' : '✓ Listo';

      const pct = (u.currentHp / u.maxHp * 100);
      bar.style.width      = pct + '%';
      bar.style.background = pct > 50 ? '#4caf50' : pct > 25 ? '#f0c040' : '#e53935';

      if (card) card.className = 'unit-card' + (this.selectedUnit?.id === u.id ? ' unit-card-selected' : '');
    });
  }

  private msg(text: string): void {
    const el = this.container.querySelector('#game-msg') as HTMLElement;
    if (el) el.textContent = text;
  }

  private getUnitAt(x: number, y: number): Unit | undefined {
    return this.gameState.units.find(u => u.alive && u.position.x === x && u.position.y === y);
  }

  private agregarEventos(container: HTMLElement): void {
    this.canvas.addEventListener('click', async (e) => {
      if (this.gameState.currentPhase !== 'PLAYER') return;
      const rect   = this.canvas.getBoundingClientRect();
      const scaleX = this.canvas.width  / rect.width;
      const scaleY = this.canvas.height / rect.height;
      const mx = Math.floor((e.clientX - rect.left) * scaleX / TILE);
      const my = Math.floor((e.clientY - rect.top)  * scaleY / TILE);
      if (mx < 0 || mx >= COLS || my < 0 || my >= ROWS) return;

      const clickedUnit = this.getUnitAt(mx, my);

      if (!this.selectedUnit) {
        if (clickedUnit && clickedUnit.team === 'player' && !clickedUnit.hasMoved) {
          this.selectedUnit = clickedUnit;
          try {
            this.reachableTiles = await GameService.getReachableTiles(clickedUnit.id);
          } catch {
            this.reachableTiles = [];
          }
          this.msg(`${clickedUnit.name} seleccionado. MOV: ${clickedUnit.mov}. Elige destino.`);
        } else if (clickedUnit?.hasMoved) {
          this.msg(`${clickedUnit.name} ya actuó este turno.`);
        }
      } else {
        const isReach = this.reachableTiles.some(p => p.x === mx && p.y === my);

        if (isReach && !clickedUnit) {
          try {
            const result = await GameService.moveUnit(this.selectedUnit.id, mx, my);
            if (result.success) {
              const unit = this.gameState.units.find(u => u.id === this.selectedUnit!.id);
              if (unit) {
                unit.position = { x: mx, y: my };
                unit.hasMoved = true;
                this.selectedUnit = { ...unit };
              }
              this.msg(result.message);
            }
          } catch {
            this.msg('Error al mover la unidad.');
          }
          this.selectedUnit   = null;
          this.reachableTiles = [];
          this.checkAllMoved();
        } else if (clickedUnit?.id === this.selectedUnit.id) {
          this.selectedUnit = null; this.reachableTiles = [];
          this.msg('Selección cancelada.');
        } else if (clickedUnit && clickedUnit.team === 'enemy' && !this.selectedUnit.hasActed) {
          // ── ATAQUE ──────────────────────────────────────
          const dx = Math.abs(this.selectedUnit.position.x - clickedUnit.position.x);
          const dy = Math.abs(this.selectedUnit.position.y - clickedUnit.position.y);
          const adyacente = (dx + dy) === 1;

          if (!adyacente) {
            this.msg(`${clickedUnit.name} está fuera de rango. Acércate primero.`);
            } else {
              try {
                const resultado = await GameService.attackUnit(this.selectedUnit.id, clickedUnit.id);

                // Refrescar HP desde el backend
                try { this.gameState = await GameService.getState(); } catch { /* ok */ }

                if (resultado.defenderDefeated) {
                  const defensor =this.gameState.units.find(u => u.id === resultado.defenderId);
                  if (defensor) { defensor.alive = false; defensor.currentHp = 0; }
                  this.msg(`¡${resultado.defenderId} ha sido derrotado!`);
                } else if (resultado.attackerDefeated) {
                  const atacante = this.gameState.units.find(u => u.id === resultado.attackerId);
                  if (atacante) { atacante.alive = false; atacante.currentHp = 0; }
                  this.msg(`Combate: ${resultado.log.join(' | ')}`);
                }

                // Marcar unidad como actuada 
                const unitLocal = this.gameState.units.find(u => u.id === this.selectedUnit!.id); 
                if (unitLocal) { unitLocal.hasMoved = true; unitLocal.hasActed = true; }

              } catch {
                this.msg('Error al ejecutar el ataque.');
              }
              this.selectedUnit  = null;
              this.reachableTiles = [];
              this.checkAllMoved();
          }

        } else {
          this.msg('Casilla fuera de rango de movimiento.');
        }
      }
      this.render_map();
    });

    // Esperar
    container.querySelector('#btn-wait')?.addEventListener('click', async () => {
      if (!this.selectedUnit) { this.msg('Selecciona primero una unidad.'); return; }
      try { await GameService.waitUnit(this.selectedUnit.id); } catch { /* ok */ }
      const unit = this.gameState.units.find(u => u.id === this.selectedUnit!.id);
      if (unit) { unit.hasMoved = true; unit.hasActed = true; }
      this.msg(`${this.selectedUnit.name} espera.`);
      this.selectedUnit = null; this.reachableTiles = [];
      this.render_map();
      this.checkAllMoved();
    });

    // Fin de turno
    container.querySelector('#btn-endturn')?.addEventListener('click', async () => {
      this.selectedUnit = null; this.reachableTiles = [];
      try {
        this.gameState = await GameService.endTurn();
      } catch {
        this.gameState.units.forEach(u => { u.hasMoved = false; u.hasActed = false; });
        this.gameState.turnNumber++;
        this.gameState.currentPhase = 'PLAYER';
      }
      this.msg(`Turno ${this.gameState.turnNumber} — ¡Mueve tus unidades!`);
      this.render_map();
    });

    // Volver al menú
    container.querySelector('#btn-menu')?.addEventListener('click', () => {
      import('./MenuPage').then(({ MenuPage }) => {
        new MenuPage().render(container);
      });
    });
  }

  private checkAllMoved(): void {
    const vivas = this.gameState.units.filter(u => u.alive && u.team === 'player');
    if (vivas.every(u => u.hasMoved)) {
      this.msg('Todas las unidades actuaron. Presiona ▶ Fin de turno.');
    }
  }
}
