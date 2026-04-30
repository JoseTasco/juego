import { GameService, GameState, Unit, Position } from '../services/GameService';
import { AuthService } from '../services/AuthService';

const COLS = 10, ROWS = 8, TILE = 56;

// Costo de movimiento por tipo de terreno
const TERRAIN_COST: { [key: number]: number } = {
  0: 1,  // Bosque ligero
  1: 2,  // Bosque denso
  2: 3,  // Montaña
  3: 4,  // Agua
  4: 99, // Obstáculo (no se puede pasar)
  5: 1,  // Camino
};

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

// Unidades enemigas para probar el ataque
const ENEMY_UNITS = [
  { id:'bandit1', name:'Bandido', team:'enemy', unitClass:'Warrior', maxHp:15, currentHp:15, atk:6, def:3, spd:5, mov:4, position:{x:7,y:3}, hasMoved:false, hasActed:false, alive:true },
  { id:'bandit2', name:'Bandido', team:'enemy', unitClass:'Warrior', maxHp:15, currentHp:15, atk:6, def:3, spd:5, mov:4, position:{x:8,y:5}, hasMoved:false, hasActed:false, alive:true },
];

export class GamePage {

  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private gameState!: GameState;
  private selectedUnit: Unit | null = null;
  private reachableTiles: Position[] = [];
  private attackRangeTiles: Position[] = [];
  private container!: HTMLElement;
  
  private mapImage: HTMLImageElement | null = null;
  private imageLoaded = false;
  
  private characterImages: { [key: string]: HTMLImageElement } = {};
  private charactersLoaded = false;

  async render(container: HTMLElement): Promise<void> {
    this.container = container;
    container.innerHTML = this.getHTML();
    this.canvas = container.querySelector('#game-canvas') as HTMLCanvasElement;
    this.ctx    = this.canvas.getContext('2d')!;

    this.canvas.style.width  = '100%';
    this.canvas.style.height = 'auto';

    try {
      this.gameState = await GameService.newGame();
      // Agregar enemigos al gameState
      this.gameState.units.push(...ENEMY_UNITS);
    } catch {
      this.gameState = {
        units: [
          { id:'marth', name:'Marth', team:'player', unitClass:'Lord', maxHp:20, currentHp:20, atk:8, def:4, spd:9, mov:5, position:{x:1,y:3}, hasMoved:false, hasActed:false, alive:true },
          { id:'jagen', name:'Jagen', team:'player', unitClass:'Paladin', maxHp:18, currentHp:18, atk:7, def:6, spd:6, mov:6, position:{x:0,y:5}, hasMoved:false, hasActed:false, alive:true },
          ...ENEMY_UNITS,
        ],
        turnNumber: 1,
        currentPhase: 'PLAYER',
        status: 'ACTIVE',
      };
    }

    await Promise.all([
      this.loadMapImage(),
      this.loadCharacterImages()
    ]);
    
    this.render_map();
    this.agregarEventos(container);
  }

  private async loadMapImage(): Promise<void> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        console.log('✅ Mapa cargado');
        this.mapImage = img;
        this.imageLoaded = true;
        resolve();
      };
      img.onerror = () => {
        console.warn('⚠️ Mapa no cargado');
        this.imageLoaded = false;
        resolve();
      };
      img.src = '/src/assets/maps/map-01-noche-valdris.jpg';
    });
  }

  private async loadCharacterImages(): Promise<void> {
    const characterMap: { [key: string]: number } = {
      'Lord': 2,
      'Paladin': 3,
      'Warrior': 4,
      'Mage': 5,
      'Recruit': 6,
      'Thief': 6,
    };

    const loadImage = (src: string): Promise<HTMLImageElement | null> => {
      return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => {
          console.log(`✅ Cargado: ${src}`);
          resolve(img);
        };
        img.onerror = () => {
          console.warn(`❌ No se pudo cargar: ${src}`);
          resolve(null);
        };
        img.src = src;
      });
    };

    for (const [className, num] of Object.entries(characterMap)) {
      const rutas = [
        `/src/assets/characters/character-${num}.png`,
        `/assets/characters/character-${num}.png`,
      ];
      
      for (const ruta of rutas) {
        const img = await loadImage(ruta);
        if (img) {
          this.characterImages[className] = img;
          break;
        }
      }
    }
    
    this.charactersLoaded = Object.keys(this.characterImages).length > 0;
    console.log(`📊 Personajes cargados: ${Object.keys(this.characterImages).length}`);
  }

  private getHTML(): string {
    return `
      <div class="game-wrapper" style="background:#1a1a2e; min-height:100vh;">
        <div class="game-topbar" style="background:#16213e; padding:12px 20px; display:flex; justify-content:space-between; color:white;">
          <span id="game-phase" style="font-weight:bold;">⚔ FASE DEL JUGADOR</span>
          <span id="game-turn" style="font-weight:bold;">TURNO 1</span>
        </div>

        <div class="game-layout" style="display:flex; padding:20px; gap:20px;">
          <div class="game-map-wrapper" style="flex:2;">
            <canvas id="game-canvas" width="${COLS * TILE}" height="${ROWS * TILE}" style="border-radius:8px; box-shadow:0 4px 20px rgba(0,0,0,0.3); cursor:pointer;"></canvas>
          </div>

          <div class="game-sidebar" style="flex:1; background:#16213e; border-radius:12px; padding:16px; color:white;">
            <div class="unit-card" id="card-marth" style="background:#0f3460; border-radius:8px; padding:12px; margin-bottom:12px;">
              <div class="unit-card-name" style="font-size:18px; font-weight:bold;">⚔ MARTH</div>
              <div class="unit-stat">HP: <span id="hp-marth">20</span>/20</div>
              <div class="unit-hpbar-bg" style="background:#333; height:8px; border-radius:4px; margin:8px 0;"><div class="unit-hpbar" id="hpbar-marth" style="width:100%; height:100%; background:#4caf50; border-radius:4px;"></div></div>
              <div class="unit-stat">ATK: 8 | MOV: 5</div>
              <div class="unit-stat">POS: <span id="pos-marth">—</span></div>
              <div class="unit-stat">Estado: <span id="status-marth">✓ Listo</span></div>
            </div>
            <div class="unit-card" id="card-jagen" style="background:#0f3460; border-radius:8px; padding:12px; margin-bottom:12px;">
              <div class="unit-card-name" style="font-size:18px; font-weight:bold;">🛡 JAGEN</div>
              <div class="unit-stat">HP: <span id="hp-jagen">18</span>/18</div>
              <div class="unit-hpbar-bg" style="background:#333; height:8px; border-radius:4px; margin:8px 0;"><div class="unit-hpbar" id="hpbar-jagen" style="width:100%; height:100%; background:#4caf50; border-radius:4px;"></div></div>
              <div class="unit-stat">ATK: 7 | MOV: 6</div>
              <div class="unit-stat">POS: <span id="pos-jagen">—</span></div>
              <div class="unit-stat">Estado: <span id="status-jagen">✓ Listo</span></div>
            </div>
            
            <div style="margin-top:16px; padding:12px; background:#0f3460; border-radius:8px;">
              <div style="font-weight:bold; margin-bottom:8px;">💀 ENEMIGOS:</div>
              <div id="enemy-list" style="font-size:12px;">Bandidos en el mapa</div>
            </div>
            
            <div class="game-msgbox" id="game-msg" style="background:#0f3460; border-radius:8px; padding:12px; margin:12px 0; text-align:center;">🎮 Selecciona una unidad para moverla o atacar enemigos adyacentes</div>
            <div class="game-controls" style="display:flex; gap:10px; margin-top:16px;">
              <button class="game-btn" id="btn-endturn" style="flex:1; background:#f0c040; border:none; padding:10px; border-radius:8px; cursor:pointer; font-weight:bold;">▶ Fin de turno</button>
              <button class="game-btn" id="btn-wait" style="flex:1; background:#e74c3c; border:none; padding:10px; border-radius:8px; cursor:pointer; color:white;">⏸ Esperar</button>
              <button class="game-btn" id="btn-menu" style="flex:1; background:#555; border:none; padding:10px; border-radius:8px; cursor:pointer; color:white;">↩ Menú</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // Calcular movimiento considerando terreno
  private calcularMovimiento(unidad: Unit): Position[] {
    const movMaximo = unidad.mov;
    const reachable: Position[] = [];
    const costs = new Map<string, number>();
    const start = `${unidad.position.x},${unidad.position.y}`;
    costs.set(start, 0);
    reachable.push({ x: unidad.position.x, y: unidad.position.y });
    
    let frontier = [{ x: unidad.position.x, y: unidad.position.y, cost: 0 }];
    
    while (frontier.length > 0) {
      const current = frontier.shift()!;
      
      const neighbors = [
        { x: current.x + 1, y: current.y },
        { x: current.x - 1, y: current.y },
        { x: current.x, y: current.y + 1 },
        { x: current.x, y: current.y - 1 },
      ];
      
      for (const neighbor of neighbors) {
        if (neighbor.x < 0 || neighbor.x >= COLS || neighbor.y < 0 || neighbor.y >= ROWS) continue;
        
        const terrainType = MAP_LAYOUT[neighbor.y][neighbor.x];
        const moveCost = TERRAIN_COST[terrainType];
        
        if (moveCost === 99) continue;
        
        const unitThere = this.gameState.units.find(u => u.alive && u.position.x === neighbor.x && u.position.y === neighbor.y);
        if (unitThere && unitThere.id !== unidad.id) continue;
        
        const newCost = current.cost + moveCost;
        const key = `${neighbor.x},${neighbor.y}`;
        const existingCost = costs.get(key);
        
        if (newCost <= movMaximo && (existingCost === undefined || newCost < existingCost)) {
          costs.set(key, newCost);
          reachable.push({ x: neighbor.x, y: neighbor.y });
          frontier.push({ x: neighbor.x, y: neighbor.y, cost: newCost });
          frontier.sort((a, b) => a.cost - b.cost);
        }
      }
    }
    
    return reachable;
  }

  // Calcular enemigos en rango de ataque (adyacentes)
  private calcularEnemigosEnRango(unidad: Unit): Unit[] {
    const adyacentes = [
      { x: unidad.position.x + 1, y: unidad.position.y },
      { x: unidad.position.x - 1, y: unidad.position.y },
      { x: unidad.position.x, y: unidad.position.y + 1 },
      { x: unidad.position.x, y: unidad.position.y - 1 },
    ];
    
    return this.gameState.units.filter(u => 
      u.alive && 
      u.team === 'enemy' && 
      adyacentes.some(pos => pos.x === u.position.x && pos.y === u.position.y)
    );
  }

  private render_map(): void {
    const ctx = this.ctx;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // Dibujar mapa
    if (this.imageLoaded && this.mapImage) {
      ctx.drawImage(this.mapImage, 0, 0, this.canvas.width, this.canvas.height);
    } else {
      for (let y = 0; y < ROWS; y++) {
        for (let x = 0; x < COLS; x++) {
          const t = MAP_LAYOUT[y][x];
          const px = x * TILE;
          const py = y * TILE;
          const [c1, c2] = TERRAIN_COLORS[t];
          ctx.fillStyle = (x + y) % 2 === 0 ? c1 : c2;
          ctx.fillRect(px, py, TILE, TILE);
        }
      }
    }

    // Resaltar casillas alcanzables (movimiento)
    for (const pos of this.reachableTiles) {
      const px = pos.x * TILE;
      const py = pos.y * TILE;
      ctx.fillStyle = 'rgba(74,158,255,0.4)';
      ctx.fillRect(px, py, TILE, TILE);
      ctx.strokeStyle = '#4a9eff';
      ctx.lineWidth = 2;
      ctx.strokeRect(px + 2, py + 2, TILE - 4, TILE - 4);
    }

    // Resaltar unidad seleccionada
    if (this.selectedUnit) {
      const px = this.selectedUnit.position.x * TILE;
      const py = this.selectedUnit.position.y * TILE;
      ctx.strokeStyle = '#f0c040';
      ctx.lineWidth = 3;
      ctx.strokeRect(px + 2, py + 2, TILE - 4, TILE - 4);
      
      // Resaltar enemigos en rango de ataque
      const enemigosEnRango = this.calcularEnemigosEnRango(this.selectedUnit);
      for (const enemy of enemigosEnRango) {
        const ex = enemy.position.x * TILE;
        const ey = enemy.position.y * TILE;
        ctx.fillStyle = 'rgba(255,74,74,0.4)';
        ctx.fillRect(ex, ey, TILE, TILE);
        ctx.strokeStyle = '#ff4a4a';
        ctx.lineWidth = 2;
        ctx.strokeRect(ex + 2, ey + 2, TILE - 4, TILE - 4);
      }
    }

    // Dibujar unidades
    this.gameState.units.forEach(unit => {
      if (!unit.alive) return;
      
      const px = unit.position.x * TILE;
      const py = unit.position.y * TILE;
      
      ctx.shadowBlur = 3;
      ctx.shadowColor = 'rgba(0,0,0,0.4)';
      
      const img = this.characterImages[unit.unitClass];
      
      if (img && this.charactersLoaded) {
        ctx.globalAlpha = unit.hasMoved ? 0.65 : 0.95;
        ctx.drawImage(img, px + 4, py + 4, TILE - 8, TILE - 8);
        ctx.globalAlpha = 1;
      } else {
        // Fallback
        const cx = px + TILE / 2;
        const cy = py + TILE / 2;
        const color = unit.team === 'player' ? '#4a9eff' : '#ff4a4a';
        ctx.beginPath();
        ctx.arc(cx, cy, TILE * 0.32, 0, Math.PI * 2);
        ctx.fillStyle = color;
        ctx.fill();
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        ctx.font = `${TILE * 0.35}px serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = '#fff';
        ctx.fillText(unit.team === 'player' ? '⚔️' : '💀', cx, cy - 2);
      }
      
      // Mostrar HP y nombre
      const cx2 = px + TILE / 2;
      const cy2 = py + TILE - 8;
      ctx.font = `bold ${TILE * 0.14}px monospace`;
      ctx.fillStyle = '#ffffff';
      ctx.shadowBlur = 2;
      ctx.fillText(`${unit.currentHp}/${unit.maxHp}`, cx2, cy2);
      
      ctx.font = `${TILE * 0.12}px monospace`;
      ctx.fillStyle = '#ddd';
      ctx.fillText(unit.name.substring(0, 4), cx2, py + 6);
      
      ctx.shadowBlur = 0;
    });

    this.updateUI();
  }

  private updateUI(): void {
    const phase = this.container.querySelector('#game-phase') as HTMLElement;
    const turn  = this.container.querySelector('#game-turn')  as HTMLElement;
    if (phase) phase.textContent = this.gameState.currentPhase === 'PLAYER' ? '⚔ FASE DEL JUGADOR' : '💀 FASE ENEMIGA';
    if (turn)  turn.textContent  = `TURNO ${this.gameState.turnNumber}`;

    this.gameState.units.forEach(u => {
      const id = u.id;
      const hp  = this.container.querySelector(`#hp-${id}`) as HTMLElement;
      const bar = this.container.querySelector(`#hpbar-${id}`) as HTMLElement;
      const pos = this.container.querySelector(`#pos-${id}`) as HTMLElement;
      const st  = this.container.querySelector(`#status-${id}`) as HTMLElement;
      const card= this.container.querySelector(`#card-${id}`) as HTMLElement;

      if (hp) hp.textContent = String(u.currentHp);
      if (pos) pos.textContent = `(${u.position.x},${u.position.y})`;
      if (st) st.textContent = !u.alive ? '✝ Caído' : u.hasMoved ? '⏸ Esperando' : '✓ Listo';

      const pct = (u.currentHp / u.maxHp * 100);
      if (bar) {
        bar.style.width = pct + '%';
        bar.style.background = pct > 50 ? '#4caf50' : pct > 25 ? '#f0c040' : '#e53935';
      }
      if (card) card.style.opacity = u.hasMoved ? '0.6' : '1';
    });
    
    // Mostrar cantidad de enemigos
    const enemigosVivos = this.gameState.units.filter(u => u.alive && u.team === 'enemy').length;
    const enemyList = this.container.querySelector('#enemy-list') as HTMLElement;
    if (enemyList) enemyList.textContent = `💀 Enemigos restantes: ${enemigosVivos}`;
  }

  private msg(text: string): void {
    const el = this.container.querySelector('#game-msg') as HTMLElement;
    if (el) el.textContent = text;
  }

  private getUnitAt(x: number, y: number): Unit | undefined {
    return this.gameState.units.find(u => u.alive && u.position.x === x && u.position.y === y);
  }

  private async seleccionarUnidad(unit: Unit): Promise<void> {
    this.selectedUnit = unit;
    this.reachableTiles = this.calcularMovimiento(unit);
    
    const enemigosCerca = this.calcularEnemigosEnRango(unit);
    if (enemigosCerca.length > 0) {
      this.msg(`${unit.name} seleccionado. Puede moverse a casillas azules o atacar enemigos en rojo.`);
    } else {
      this.msg(`${unit.name} seleccionado. Movimiento: ${unit.mov} pasos.`);
    }
    this.render_map();
  }

  private async atacar(atacante: Unit, defensor: Unit): Promise<void> {
    // Cálculo de daño simple
    const damage = Math.max(1, atacante.atk - defensor.def);
    const newHp = Math.max(0, defensor.currentHp - damage);
    
    this.msg(`⚔️ ${atacante.name} ataca a ${defensor.name} por ${damage} de daño!`);
    
    defensor.currentHp = newHp;
    
    if (newHp <= 0) {
      defensor.alive = false;
      this.msg(`💀 ¡${defensor.name} ha sido derrotado!`);
    } else {
      this.msg(`${defensor.name} tiene ${newHp}/${defensor.maxHp} HP restantes.`);
    }
    
    atacante.hasMoved = true;
    atacante.hasActed = true;
    
    this.selectedUnit = null;
    this.reachableTiles = [];
    this.render_map();
    this.checkAllMoved();
  }

  private agregarEventos(container: HTMLElement): void {
    this.canvas.addEventListener('click', async (e) => {
      if (this.gameState.currentPhase !== 'PLAYER') {
        this.msg('Esperando turno del enemigo...');
        return;
      }
      
      const rect   = this.canvas.getBoundingClientRect();
      const scaleX = this.canvas.width  / rect.width;
      const scaleY = this.canvas.height / rect.height;
      const mx = Math.floor((e.clientX - rect.left) * scaleX / TILE);
      const my = Math.floor((e.clientY - rect.top)  * scaleY / TILE);
      if (mx < 0 || mx >= COLS || my < 0 || my >= ROWS) return;

      const clickedUnit = this.getUnitAt(mx, my);

      // Si no hay unidad seleccionada
      if (!this.selectedUnit) {
        if (clickedUnit && clickedUnit.team === 'player' && !clickedUnit.hasMoved) {
          await this.seleccionarUnidad(clickedUnit);
        } else if (clickedUnit?.hasMoved) {
          this.msg(`${clickedUnit.name} ya actuó este turno.`);
        }
        return;
      }

      // Si hay unidad seleccionada
      const isReachMove = this.reachableTiles.some(p => p.x === mx && p.y === my);
      const isEnemyInRange = clickedUnit && clickedUnit.team === 'enemy';
      const isAdjacent = this.calcularEnemigosEnRango(this.selectedUnit).some(e => e.id === clickedUnit?.id);
      
      // CASO 1: Mover a casilla alcanzable vacía
      if (isReachMove && !clickedUnit) {
        try {
          // Mover en local
          const oldPos = { ...this.selectedUnit.position };
          this.selectedUnit.position = { x: mx, y: my };
          this.selectedUnit.hasMoved = true;
          this.msg(`${this.selectedUnit.name} se movió de (${oldPos.x},${oldPos.y}) a (${mx},${my})`);
          
          this.selectedUnit = null;
          this.reachableTiles = [];
          this.render_map();
          this.checkAllMoved();
        } catch (error) {
          this.msg('Error al mover la unidad.');
        }
      }
      // CASO 2: Atacar enemigo adyacente
      else if (isEnemyInRange && isAdjacent && !this.selectedUnit.hasActed) {
        await this.atacar(this.selectedUnit, clickedUnit!);
      }
      // CASO 3: Cancelar selección
      else if (clickedUnit?.id === this.selectedUnit.id) {
        this.selectedUnit = null;
        this.reachableTiles = [];
        this.msg('Selección cancelada.');
        this.render_map();
      }
      // CASO 4: Movimiento inválido
      else {
        this.msg('No puedes moverte ahí. Solo casillas azules o atacar enemigos en rojo.');
      }
    });

    // Botón Esperar
    container.querySelector('#btn-wait')?.addEventListener('click', () => {
      if (!this.selectedUnit) {
        this.msg('Selecciona primero una unidad.');
        return;
      }
      this.selectedUnit.hasMoved = true;
      this.selectedUnit.hasActed = true;
      this.msg(`${this.selectedUnit.name} espera.`);
      this.selectedUnit = null;
      this.reachableTiles = [];
      this.render_map();
      this.checkAllMoved();
    });

    // Botón Fin de turno
    container.querySelector('#btn-endturn')?.addEventListener('click', () => {
      this.selectedUnit = null;
      this.reachableTiles = [];
      
      // Resetear estado de unidades
      this.gameState.units.forEach(u => {
        if (u.team === 'player') {
          u.hasMoved = false;
          u.hasActed = false;
        }
      });
      
      this.gameState.turnNumber++;
      this.msg(`Turno ${this.gameState.turnNumber} — ¡Mueve tus unidades!`);
      this.render_map();
    });

    // Botón Menú
    container.querySelector('#btn-menu')?.addEventListener('click', () => {
      import('./MenuPage').then(({ MenuPage }) => {
        new MenuPage().render(container);
      });
    });
  }

  private checkAllMoved(): void {
    const vivas = this.gameState.units.filter(u => u.alive && u.team === 'player');
    if (vivas.length > 0 && vivas.every(u => u.hasMoved)) {
      this.msg('✅ Todas las unidades actuaron. Presiona "Fin de turno" para continuar.');
    }
  }
}