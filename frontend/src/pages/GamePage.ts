import {
  GameService, GameState, Unit, Position, CombatPreview, EnemyTurnResult, CombatResult, Weapon
} from '../services/GameService';
import { AudioManager } from '../services/AudioManager';

// ── Constantes del mapa ────────────────────────────────────────────────────────
const COLS = 20, ROWS = 15, TILE = 44;

// 0=PLAINS 1=FOREST 2=MOUNTAIN 3=WATER 4=FORT 5=ROAD
const MAP_LAYOUT = [
  [5,5,5,0,0,1,1,0,0,2,2,2,0,0,3,3,3,0,0,0],
  [5,0,0,0,1,1,0,0,2,2,0,0,0,3,3,0,0,0,1,0],
  [5,0,0,1,1,0,0,2,2,0,0,0,0,3,0,0,0,1,1,0],
  [5,5,0,1,0,0,2,2,0,0,0,0,0,3,0,0,1,1,0,0],
  [0,5,5,0,0,2,2,0,0,0,4,0,0,3,3,0,1,0,0,0],
  [0,0,5,0,0,2,0,0,0,0,0,0,3,3,0,0,0,0,0,0],
  [0,0,5,5,0,0,0,0,0,0,0,3,3,0,0,0,1,1,1,0],
  [0,0,0,5,5,0,0,0,0,0,3,3,0,0,0,1,1,0,0,0],
  [0,0,0,0,5,0,1,1,0,3,3,0,0,0,0,1,0,0,0,0],
  [0,0,0,0,5,5,1,0,3,3,0,0,0,0,0,0,0,0,4,0],
  [2,2,0,0,0,5,0,3,3,0,0,0,0,0,0,0,0,0,0,0],
  [2,2,2,0,0,5,3,3,0,0,0,0,0,0,0,0,0,1,0,0],
  [2,0,0,0,0,5,3,0,0,0,0,0,0,0,1,1,1,1,0,0],
  [0,0,0,0,0,5,0,0,0,1,1,1,0,0,1,0,0,0,0,0],
  [0,0,4,0,0,5,5,5,5,5,0,0,0,0,0,0,0,0,4,0],
];

const TERRAIN_COLORS: { [k: number]: [string, string] } = {
  0: ['#4a7a30','#5a8f3c'],   // plains
  1: ['#256025','#2e7a2e'],   // forest
  2: ['#7a7a7a','#909090'],   // mountain
  3: ['#1a5099','#2268c0'],   // water
  4: ['#8a6020','#a07030'],   // fort
  5: ['#b09868','#c0aa80'],   // road
};

const TERRAIN_AVOID = [0, 20, 30, 0, 20, 0];
const TERRAIN_COST  = [1,  2,  3, 99, 1, 1];

// ── Triángulo de armas ─────────────────────────────────────────────────────────
const ADV: Record<string, string>  = { SWORD:'AXE', AXE:'LANCE', LANCE:'SWORD', ANIMA:'LIGHT', LIGHT:'DARK', DARK:'ANIMA' };
const DIS: Record<string, string>  = { AXE:'SWORD', LANCE:'AXE', SWORD:'LANCE', LIGHT:'ANIMA', DARK:'LIGHT', ANIMA:'DARK' };

function weaponTriangle(a: string, d: string): [number, number] {
  if (ADV[a] === d) return [+15, +1];
  if (DIS[a] === d) return [-15, -1];
  return [0, 0];
}

// ── Cálculo local de preview ───────────────────────────────────────────────────
function calcPreview(atk: Unit, def: Unit, layout: number[][]): CombatPreview {
  const aw = atk.equippedWeapon;
  const dw = def.equippedWeapon;
  const defAvoid = TERRAIN_AVOID[layout[def.position.y]?.[def.position.x] ?? 0];
  const atkAvoid = TERRAIN_AVOID[layout[atk.position.y]?.[atk.position.x] ?? 0];

  const [aHitT, aDmgT] = aw && dw ? weaponTriangle(aw.type, dw.type) : [0, 0];
  const [dHitT, dDmgT] = aw && dw ? weaponTriangle(dw.type, aw.type) : [0, 0];

  const atkHit  = aw ? Math.max(0, Math.min(100, atk.skl*2 + atk.lck/2 + aw.hit - def.spd*2 - defAvoid + aHitT)) : 50;
  const atkCrit = aw ? Math.max(0, Math.floor(atk.skl/2) + aw.crit - Math.floor(def.lck/2)) : 0;
  const atkDmg  = aw ? (aw.magical
    ? Math.max(0, atk.mag + aw.might - def.res + aDmgT)
    : Math.max(0, atk.str + aw.might - def.def + aDmgT)) : Math.max(0, atk.str - def.def);
  const atkDouble = atk.spd >= def.spd + 4;

  const canCounter = !!(dw && dist(atk.position, def.position) >= dw.minRange && dist(atk.position, def.position) <= dw.maxRange);
  const defHit  = canCounter && dw ? Math.max(0, Math.min(100, def.skl*2 + def.lck/2 + dw.hit - atk.spd*2 - atkAvoid + dHitT)) : 0;
  const defCrit = canCounter && dw ? Math.max(0, Math.floor(def.skl/2) + dw.crit - Math.floor(atk.lck/2)) : 0;
  const defDmg  = canCounter && dw ? (dw.magical
    ? Math.max(0, def.mag + dw.might - atk.res + dDmgT)
    : Math.max(0, def.str + dw.might - atk.def + dDmgT)) : 0;
  const defDouble = canCounter && def.spd >= atk.spd + 4;

  let advantage = 'NONE';
  if (aw && dw) {
    if (ADV[aw.type] === dw.type)      advantage = 'ATTACKER';
    else if (DIS[aw.type] === dw.type) advantage = 'DEFENDER';
  }

  return {
    attackerId: atk.id, defenderId: def.id,
    attackerName: atk.name, defenderName: def.name,
    attackerHp: atk.currentHp, defenderHp: def.currentHp,
    attackerDamage: atkDmg, defenderDamage: defDmg,
    attackerHitRate: atkHit, defenderHitRate: defHit,
    attackerCritRate: atkCrit, defenderCritRate: defCrit,
    attackerDoubleAttacks: atkDouble, defenderDoubleAttacks: defDouble,
    defenderCanCounter: canCounter,
    weaponAdvantage: advantage,
    attackerWeaponName: aw?.name ?? 'Sin arma',
    defenderWeaponName: dw?.name ?? 'Sin arma',
  };
}

function dist(a: Position, b: Position) {
  return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
}

// ── Mapa de imágenes por nivel (14 niveles) ───────────────────────────────────
const LEVEL_MAP_IMAGES: Record<string, string> = {
  'level-1':  '/src/assets/Maps/Story/S0101.png',
  'level-2':  '/src/assets/Maps/Story/S0201.png',
  'level-3':  '/src/assets/Maps/Story/S0401.png',
  'level-4':  '/src/assets/Maps/Story/S0501.png',
  'level-5':  '/src/assets/Maps/Story/S0601.png',
  'level-6':  '/src/assets/Maps/Story/S0701.png',
  'level-7':  '/src/assets/Maps/Story/S0901.png',
  'level-8':  '/src/assets/Maps/Story/S0102.png',
  'level-9':  '/src/assets/Maps/Story/S0202.png',
  'level-10': '/src/assets/Maps/Story/S0302.png',
  'level-11': '/src/assets/Maps/Story/S0502.png',
  'level-12': '/src/assets/Maps/Story/S0702.png',
  'level-13': '/src/assets/Maps/Story/S1001.png',
  'level-14': '/src/assets/Maps/Story/S1101.png',
};
const DEFAULT_MAP_IMG = '/src/assets/Maps/map-01-noche-valdris.jpg';

// ── Clase principal ────────────────────────────────────────────────────────────
export class GamePage {

  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private container!: HTMLElement;
  private gameState!: GameState;
  private levelId?: string;

  private selectedUnit:   Unit | null = null;
  private reachableTiles: Position[]  = [];
  private pendingAttack:  Unit | null = null;   // enemigo seleccionado para atacar
  private highlightUnit:  Unit | null = null;   // resaltado durante fase IA

  private mapImage: HTMLImageElement | null = null;
  private charImages: Record<string, HTMLImageElement> = {};

  private floats: Array<{ x:number; y:number; text:string; color:string; frame:number }> = [];
  private animFrame = 0;
  private flashTargets = new Map<string, number>(); // unitId → flash alpha
  private deathAlphas  = new Map<string, number>(); // unitId → opacity (1=visible, 0=gone)

  /** Layout activo: backend o fallback hardcodeado */
  private get layout(): number[][] {
    return this.gameState?.mapLayout ?? MAP_LAYOUT;
  }

  // ── Render principal ───────────────────────────────────────────────────────
  async render(container: HTMLElement, initialState?: GameState, levelId?: string): Promise<void> {
    this.container = container;
    this.levelId = levelId;
    container.innerHTML = this.buildHTML();
    this.canvas = container.querySelector('#game-canvas') as HTMLCanvasElement;
    this.ctx    = this.canvas.getContext('2d')!;

    if (initialState) {
      this.gameState = initialState;
    } else {
      try {
        this.gameState = await GameService.newGame();
      } catch {
        this.gameState = this.fallbackState();
      }
    }

    await Promise.all([this.loadMap(), this.loadChars()]);
    AudioManager.playLevelMusic(levelId);
    this.draw();
    this.bindEvents();
  }

  // ── HTML ───────────────────────────────────────────────────────────────────
  private buildHTML(): string {
    const W = COLS * TILE, H = ROWS * TILE;
    return `
      <div class="game-wrapper">
        <div class="game-topbar">
          <span id="game-phase">⚔ FASE DEL JUGADOR</span>
          <span id="game-turn">TURNO 1</span>
        </div>
        <div class="game-layout">
          <div class="game-map-wrapper" style="position:relative;">
            <canvas id="game-canvas" width="${W}" height="${H}"></canvas>
            <div id="combat-preview" style="display:none; position:absolute; bottom:0; left:0; right:0;
              background:rgba(13,17,23,0.97); border-top:1px solid rgba(200,146,42,0.5);
              padding:12px 16px; font-family:var(--font-ui,monospace); color:#fff; font-size:0.6rem;">
              <div style="display:flex; gap:16px; align-items:center;">
                <div id="prev-atk" style="flex:1; text-align:center;"></div>
                <div style="color:#f0c040; font-size:0.75rem; font-weight:bold;">VS</div>
                <div id="prev-def" style="flex:1; text-align:center;"></div>
              </div>
              <div style="display:flex; gap:8px; margin-top:10px; justify-content:center;">
                <button id="btn-confirm-atk" class="game-btn game-btn-gold" style="flex:1; max-width:120px;">⚔ ATACAR</button>
                <button id="btn-cancel-atk"  class="game-btn game-btn-red"  style="flex:1; max-width:120px;">✕ CANCELAR</button>
              </div>
            </div>
          </div>

          <div class="game-sidebar">
            <div class="unit-card" id="unit-info" style="min-height:160px;">
              <div class="unit-card-name" id="ui-name" style="color:#888;">— Sin selección —</div>
              <div id="ui-body"></div>
            </div>

            <div class="game-msgbox" id="game-msg">Selecciona una unidad aliada.</div>

            <div class="game-controls">
              <button class="game-btn game-btn-gold" id="btn-endturn">▶ FIN DE TURNO</button>
              <button class="game-btn" id="btn-wait">⏸ ESPERAR</button>
              <button class="game-btn" id="btn-menu">↩ MENÚ</button>
            </div>
          </div>
        </div>
      </div>`;
  }

  // ── Carga de assets ────────────────────────────────────────────────────────
  private loadMap(): Promise<void> {
    const src = (this.levelId && LEVEL_MAP_IMAGES[this.levelId]) ?? DEFAULT_MAP_IMG;
    return new Promise(resolve => {
      const img = new Image();
      img.onload  = () => { this.mapImage = img; resolve(); };
      img.onerror = () => {
        // Fallback si la imagen del nivel no existe
        if (src !== DEFAULT_MAP_IMG) {
          const fallback = new Image();
          fallback.onload  = () => { this.mapImage = fallback; resolve(); };
          fallback.onerror = () => resolve();
          fallback.src = DEFAULT_MAP_IMG;
        } else {
          resolve();
        }
      };
      img.src = src;
    });
  }

  private async loadChars(): Promise<void> {
    const classMap: Record<string, string[]> = {
      // Clases jugador
      Lord:         ['lord',        'character-2'],
      Paladin:      ['paladin',     'character-3'],
      Cavalier:     ['cavalier',    'character-3'],
      Healer:       ['healer',      'character-5'],
      // Clases enemigo
      Warrior:      ['warrior',     'character-4'],
      Knight:       ['knight',      'character-3'],
      Myrmidon:     ['myrmidon',    'character-2'],
      Archer:       ['archer',      'character-6'],
      Thief:        ['thief',       'character-6'],
      Bandit:       ['bandit',      'character-4'],
      General:      ['knight',      'character-3'],
      Sorcerer:     ['mage-enemy',  'character-5'],
      DarkGeneral:  ['knight',      'character-3'],
      // Mago (distinguido por equipo en drawUnit)
      Mage:         ['mage-player', 'character-5'],
      'Mage-enemy': ['mage-enemy',  'character-5'],
    };
    const load = (src: string) => new Promise<HTMLImageElement | null>(res => {
      const i = new Image();
      i.onload  = () => res(i);
      i.onerror = () => res(null);
      i.src = src;
    });
    await Promise.all(
      Object.entries(classMap).map(async ([cls, names]) => {
        for (const name of names) {
          const img = await load(`/src/assets/characters/${name}.png`);
          if (img) { this.charImages[cls] = img; break; }
        }
      })
    );
  }

  private getCharImage(unit: Unit): HTMLImageElement | undefined {
    const key = unit.unitClass === 'Mage' && unit.team === 'enemy' ? 'Mage-enemy' : unit.unitClass;
    return this.charImages[key];
  }

  // ── Dibujo ─────────────────────────────────────────────────────────────────
  private draw(): void {
    const ctx = this.ctx;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // Terreno
    if (this.mapImage) {
      ctx.drawImage(this.mapImage, 0, 0, this.canvas.width, this.canvas.height);
    } else {
      for (let y = 0; y < ROWS; y++)
        for (let x = 0; x < COLS; x++) {
          const t = this.layout[y]?.[x] ?? 0;
          const [c1, c2] = TERRAIN_COLORS[t] ?? TERRAIN_COLORS[0];
          ctx.fillStyle = (x+y) % 2 === 0 ? c1 : c2;
          ctx.fillRect(x*TILE, y*TILE, TILE, TILE);
        }
    }

    // Grid sutil
    ctx.strokeStyle = 'rgba(0,0,0,0.15)';
    ctx.lineWidth = 0.5;
    for (let x = 0; x <= COLS; x++) { ctx.beginPath(); ctx.moveTo(x*TILE,0); ctx.lineTo(x*TILE,ROWS*TILE); ctx.stroke(); }
    for (let y = 0; y <= ROWS; y++) { ctx.beginPath(); ctx.moveTo(0,y*TILE); ctx.lineTo(COLS*TILE,y*TILE); ctx.stroke(); }

    // Tiles de movimiento (azul)
    for (const p of this.reachableTiles) {
      ctx.fillStyle = 'rgba(74,158,255,0.32)';
      ctx.fillRect(p.x*TILE, p.y*TILE, TILE, TILE);
      ctx.strokeStyle = '#4a9eff';
      ctx.lineWidth = 1.5;
      ctx.strokeRect(p.x*TILE+1, p.y*TILE+1, TILE-2, TILE-2);
    }

    // Rango de ataque (rojo) si hay unidad seleccionada y ya se movió
    if (this.selectedUnit) {
      const enemies = this.getAttackableEnemies(this.selectedUnit);
      for (const e of enemies) {
        ctx.fillStyle = 'rgba(255,74,74,0.32)';
        ctx.fillRect(e.position.x*TILE, e.position.y*TILE, TILE, TILE);
        ctx.strokeStyle = '#ff4a4a';
        ctx.lineWidth = 1.5;
        ctx.strokeRect(e.position.x*TILE+1, e.position.y*TILE+1, TILE-2, TILE-2);
      }
    }

    // Unidades (incluye las que están en animación de muerte)
    for (const unit of this.gameState?.units ?? []) {
      if (!unit.alive && !this.deathAlphas.has(unit.id)) continue;
      this.drawUnit(unit);
    }

    // Selección amarilla (pulso animado)
    if (this.selectedUnit) {
      const p = this.selectedUnit.position;
      const pulse = 0.65 + 0.35 * Math.sin(Date.now() / 200);
      ctx.strokeStyle = `rgba(240,192,64,${pulse})`;
      ctx.lineWidth = 3;
      ctx.strokeRect(p.x*TILE+2, p.y*TILE+2, TILE-4, TILE-4);
    }

    // Resaltado IA (azul claro)
    if (this.highlightUnit) {
      const p = this.highlightUnit.position;
      ctx.strokeStyle = '#80cfff';
      ctx.lineWidth = 3;
      ctx.strokeRect(p.x*TILE+2, p.y*TILE+2, TILE-4, TILE-4);
    }

    // Números flotantes de daño
    this.drawFloats();
    this.updateSidebar();
  }

  private drawUnit(unit: Unit): void {
    const ctx = this.ctx;
    const px = Math.round(unit.position.x * TILE);
    const py = Math.round(unit.position.y * TILE);
    const cx = px + TILE / 2, cy = py + TILE / 2;
    const r  = TILE * 0.38;

    const deathAlpha = this.deathAlphas.get(unit.id) ?? 1;
    const isDone = unit.hasMoved || unit.hasActed;
    ctx.globalAlpha = deathAlpha * (isDone ? 0.5 : 1);

    // Fondo del círculo del equipo
    const teamColor = unit.team === 'player' ? '#1a4a8a' : '#6a1010';
    ctx.beginPath();
    ctx.arc(cx, cy - 2, r, 0, Math.PI * 2);
    ctx.fillStyle = teamColor;
    ctx.fill();

    const img = this.getCharImage(unit);
    if (img) {
      // Clip al círculo y dibujar portrait recortado desde la mitad superior
      ctx.save();
      ctx.beginPath();
      ctx.arc(cx, cy - 2, r - 1, 0, Math.PI * 2);
      ctx.clip();
      if (isDone) ctx.filter = 'grayscale(70%)';
      const iw = img.width, ih = img.height;
      const srcY = ih * 0.05;          // recorta 5% superior (cabeza)
      const srcH = ih * 0.6;           // usa 60% de la imagen (desde arriba)
      const diam = r * 2;
      ctx.drawImage(img, 0, srcY, iw, srcH, cx - r + 1, cy - 2 - r + 1, diam - 2, diam - 2);
      ctx.filter = 'none';
      ctx.restore();
    } else {
      // Círculo con emoji si no hay imagen
      ctx.font = `${TILE * 0.38}px serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillStyle = '#fff';
      ctx.fillText(unit.team === 'player' ? '⚔' : '💀', cx, cy - 2);
    }

    // Borde del círculo según equipo
    ctx.beginPath();
    ctx.arc(cx, cy - 2, r, 0, Math.PI * 2);
    ctx.strokeStyle = unit.team === 'player' ? '#4a9eff' : '#ff4a4a';
    ctx.lineWidth = isDone ? 1.5 : 2.5;
    ctx.stroke();

    // HP bar
    const barW = TILE - 6;
    const barX = px + 3, barY = py + TILE - 7;
    const hpPct = unit.currentHp / unit.maxHp;
    ctx.globalAlpha = 1;
    ctx.fillStyle = 'rgba(0,0,0,0.7)';
    ctx.fillRect(barX, barY, barW, 5);
    ctx.fillStyle = hpPct > 0.5 ? '#4caf50' : hpPct > 0.25 ? '#f0c040' : '#e53935';
    ctx.fillRect(barX, barY, Math.round(barW * hpPct), 5);

    // Nombre corto encima
    ctx.font = `bold ${TILE * 0.19}px monospace`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillStyle = 'rgba(0,0,0,0.7)';
    ctx.fillText(unit.name.substring(0, 4), cx + 1, py + 2);
    ctx.fillStyle = unit.team === 'player' ? '#a0d8ff' : '#ffaaaa';
    ctx.fillText(unit.name.substring(0, 4), cx, py + 1);

    // Flash de daño (overlay rojo parpadeante)
    const flashAlpha = this.flashTargets.get(unit.id) ?? 0;
    if (flashAlpha > 0) {
      ctx.globalAlpha = flashAlpha * deathAlpha;
      ctx.fillStyle = '#ff2020';
      ctx.beginPath();
      ctx.arc(cx, cy - 2, r, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.globalAlpha = 1;
  }

  private drawFloats(): void {
    const ctx = this.ctx;
    const alive: typeof this.floats = [];
    for (const f of this.floats) {
      const alpha = Math.max(0, 1 - f.frame / 55);
      const offsetY = -(f.frame * 0.6);
      ctx.globalAlpha = alpha;
      ctx.font = `bold ${TILE*0.5}px monospace`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'bottom';
      ctx.strokeStyle = '#000';
      ctx.lineWidth = 4;
      ctx.strokeText(f.text, f.x, f.y + offsetY);
      ctx.fillStyle = f.color;
      ctx.fillText(f.text, f.x, f.y + offsetY);
      ctx.globalAlpha = 1;
      f.frame++;
      if (f.frame < 55) alive.push(f);
    }
    this.floats = alive;
  }

  private addFloat(pos: Position, text: string, color = '#fff'): void {
    this.floats.push({ x: pos.x*TILE + TILE/2, y: pos.y*TILE + TILE*0.4, text, color, frame: 0 });
    if (!this.animFrame) this.startAnim();
  }

  private needsAnim(): boolean {
    return this.floats.length > 0
      || this.selectedUnit !== null
      || this.flashTargets.size > 0
      || this.deathAlphas.size > 0;
  }

  private startAnim(): void {
    const loop = () => {
      if (this.needsAnim()) {
        this.draw();
        this.animFrame = requestAnimationFrame(loop);
      } else {
        this.animFrame = 0;
      }
    };
    this.animFrame = requestAnimationFrame(loop);
  }

  // ── Sidebar ────────────────────────────────────────────────────────────────
  private updateSidebar(): void {
    const phaseEl = this.container.querySelector('#game-phase') as HTMLElement;
    const turnEl  = this.container.querySelector('#game-turn')  as HTMLElement;
    const nameEl  = this.container.querySelector('#ui-name')    as HTMLElement;
    const bodyEl  = this.container.querySelector('#ui-body')    as HTMLElement;

    if (phaseEl) phaseEl.textContent = this.gameState.currentPhase === 'PLAYER'
      ? '⚔ FASE DEL JUGADOR' : '💀 FASE ENEMIGA';
    if (turnEl)  turnEl.textContent  = `TURNO ${this.gameState.turnNumber}`;

    const u = this.selectedUnit;
    if (!u || !nameEl || !bodyEl) return;

    nameEl.textContent = `${u.team === 'player' ? '⚔' : '💀'} ${u.name.toUpperCase()}`;
    nameEl.style.color = u.team === 'player' ? '#f0c040' : '#ff8888';

    const hpPct = (u.currentHp / u.maxHp * 100).toFixed(0);
    const hpCol = u.currentHp/u.maxHp > 0.5 ? '#4caf50' : u.currentHp/u.maxHp > 0.25 ? '#f0c040' : '#e53935';
    const w = u.equippedWeapon;

    bodyEl.innerHTML = `
      <div class="unit-stat">Clase: <span>${u.unitClass}</span>  Lv<span>${u.level}</span>  EXP:<span>${u.exp}/100</span></div>
      <div class="unit-stat">HP: <span style="color:${hpCol}">${u.currentHp}/${u.maxHp}</span></div>
      <div class="unit-hpbar-bg"><div class="unit-hpbar" style="width:${hpPct}%;background:${hpCol};"></div></div>
      <div class="unit-stat">STR:<span>${u.str}</span>  MAG:<span>${u.mag}</span>  SKL:<span>${u.skl}</span></div>
      <div class="unit-stat">SPD:<span>${u.spd}</span>  LCK:<span>${u.lck}</span>  DEF:<span>${u.def}</span>  RES:<span>${u.res}</span></div>
      <div class="unit-stat">MOV:<span>${u.mov}</span></div>
      ${w ? `<div class="unit-stat" style="margin-top:4px;">🗡 <span style="color:#f0c040">${w.name}</span>
        <span style="font-size:0.55rem;color:#aaa;"> MT:${w.might} HIT:${w.hit} CRT:${w.crit}</span></div>` : ''}
      <div class="unit-stat" style="margin-top:4px;">${u.hasMoved ? '⏸ <span style="color:#aaa">Ya actuó</span>' : '✓ <span style="color:#6f6">Listo</span>'}</div>`;
  }

  private msg(text: string): void {
    const el = this.container.querySelector('#game-msg') as HTMLElement;
    if (el) el.textContent = text;
  }

  // ── Lógica de movimiento ───────────────────────────────────────────────────
  private calcMovement(unit: Unit): Position[] {
    const reachable: Position[] = [];
    const costs = new Map<string, number>();
    const key = (p: Position) => `${p.x},${p.y}`;
    costs.set(key(unit.position), 0);
    reachable.push({ ...unit.position });

    let frontier = [{ x: unit.position.x, y: unit.position.y, cost: 0 }];
    while (frontier.length > 0) {
      const cur = frontier.shift()!;
      for (const nb of [
        { x: cur.x+1, y: cur.y }, { x: cur.x-1, y: cur.y },
        { x: cur.x, y: cur.y+1 }, { x: cur.x, y: cur.y-1 },
      ]) {
        if (nb.x < 0 || nb.x >= COLS || nb.y < 0 || nb.y >= ROWS) continue;
        const t = this.layout[nb.y]?.[nb.x] ?? 0;
        const movCost = TERRAIN_COST[t] ?? 99;
        if (movCost === 99) continue;
        const blocker = this.gameState.units.find(u => u.alive && u.position.x === nb.x && u.position.y === nb.y && u.id !== unit.id);
        if (blocker) continue;
        const newCost = cur.cost + movCost;
        const k = key(nb);
        if (newCost <= unit.mov && (!costs.has(k) || costs.get(k)! > newCost)) {
          costs.set(k, newCost);
          reachable.push(nb);
          frontier.push({ ...nb, cost: newCost });
          frontier.sort((a, b) => a.cost - b.cost);
        }
      }
    }
    return reachable;
  }

  private getAttackableEnemies(unit: Unit): Unit[] {
    const w = unit.equippedWeapon;
    const minR = w ? w.minRange : 1;
    const maxR = w ? w.maxRange : 1;
    return this.gameState.units.filter(u =>
      u.alive && u.team !== unit.team &&
      dist(unit.position, u.position) >= minR &&
      dist(unit.position, u.position) <= maxR
    );
  }

  private getUnitAt(x: number, y: number): Unit | undefined {
    return this.gameState.units.find(u => u.alive && u.position.x === x && u.position.y === y);
  }

  // ── Acciones del jugador ───────────────────────────────────────────────────
  private selectUnit(unit: Unit): void {
    this.selectedUnit  = unit;
    this.pendingAttack = null;
    this.reachableTiles = unit.hasMoved ? [] : this.calcMovement(unit);
    const enemies = this.getAttackableEnemies(unit);
    if (enemies.length > 0)
      this.msg(`${unit.name}: puede atacar ${enemies.length} enemigo(s) en rojo.`);
    else
      this.msg(`${unit.name} seleccionado. Casillas azules = movimiento.`);
    this.draw();
    if (!this.animFrame) this.startAnim(); // mantener loop para pulso de selección
  }

  private moveSelectedTo(x: number, y: number): void {
    if (!this.selectedUnit) return;
    const unit = this.selectedUnit;
    unit.position = { x, y };
    unit.hasMoved = true;
    this.reachableTiles = [];
    this.msg(`${unit.name} se movió a (${x},${y}).`);
    this.draw();
    this.checkAllMoved();
  }

  private showCombatPreview(attacker: Unit, defender: Unit): void {
    this.pendingAttack = defender;
    const p = calcPreview(attacker, defender, this.layout);
    const advSymbol = p.weaponAdvantage === 'ATTACKER' ? '▲' : p.weaponAdvantage === 'DEFENDER' ? '▼' : '–';

    const atkBlock = this.container.querySelector('#prev-atk') as HTMLElement;
    const defBlock = this.container.querySelector('#prev-def') as HTMLElement;
    if (!atkBlock || !defBlock) return;

    const fmt = (name: string, hp: number, dmg: number, hit: number, crit: number, dbl: boolean, weapon: string) =>
      `<div style="font-size:0.75rem;font-weight:bold;color:#f0c040;margin-bottom:4px;">${name}</div>
       <div style="font-size:0.6rem;color:#aaa;">🗡 ${weapon}</div>
       <div>HP: <b>${hp}</b></div>
       <div>DMG: <b style="color:#ff9a9a">${dmg}</b></div>
       <div>HIT: <b>${hit}%</b>  CRIT: <b>${crit}%</b>${dbl ? '  <span style="color:#ff0">×2</span>' : ''}</div>`;

    atkBlock.innerHTML = fmt(p.attackerName, p.attackerHp, p.attackerDamage, p.attackerHitRate, p.attackerCritRate, p.attackerDoubleAttacks, p.attackerWeaponName)
      + `<div style="margin-top:6px;color:#f0c040;font-size:0.7rem;">WPN: ${advSymbol}</div>`;
    defBlock.innerHTML = p.defenderCanCounter
      ? fmt(p.defenderName, p.defenderHp, p.defenderDamage, p.defenderHitRate, p.defenderCritRate, p.defenderDoubleAttacks, p.defenderWeaponName)
      : `<div style="font-size:0.75rem;font-weight:bold;color:#888;margin-bottom:4px;">${p.defenderName}</div>
         <div style="color:#888;font-size:0.6rem;">Sin contraataque</div><div>HP: <b>${p.defenderHp}</b></div>`;

    const panel = this.container.querySelector('#combat-preview') as HTMLElement;
    if (panel) panel.style.display = 'block';
  }

  private hideCombatPreview(): void {
    const panel = this.container.querySelector('#combat-preview') as HTMLElement;
    if (panel) panel.style.display = 'none';
    this.pendingAttack = null;
  }

  private async executeAttack(attacker: Unit, defender: Unit): Promise<void> {
    this.hideCombatPreview();
    try {
      const result = await GameService.attackUnit(attacker.id, defender.id);
      this.applyCombatResult(attacker, defender, result);
      this.selectedUnit  = null;
      this.reachableTiles = [];
      this.draw();
      this.checkWin();
      this.checkAllMoved();
    } catch {
      // Fallback local si el backend no está
      const dmg = Math.max(1, (attacker.str + (attacker.equippedWeapon?.might ?? 0)) - defender.def);
      defender.currentHp = Math.max(0, defender.currentHp - dmg);
      if (defender.currentHp <= 0) defender.alive = false;
      this.addFloat(defender.position, String(dmg), defender.alive ? '#fff' : '#f44');
      attacker.hasMoved = true;
      attacker.hasActed = true;
      this.selectedUnit  = null;
      this.reachableTiles = [];
      this.msg(`${attacker.name} ataca a ${defender.name} — ${dmg} de daño!`);
      this.draw();
      this.checkWin();
      this.checkAllMoved();
    }
  }

  private applyCombatResult(attacker: Unit, defender: Unit, r: CombatResult): void {
    // Aplicar rondas de combate una a una (con floats)
    for (const round of r.rounds) {
      const target = round.attackerId === attacker.id ? defender : attacker;
      if (!round.missed && round.damage > 0) {
        target.currentHp = Math.max(0, target.currentHp - round.damage);
        const color = round.crit ? '#ff0' : '#fff';
        const text  = round.crit ? `${round.damage}!!` : String(round.damage);
        this.addFloat(target.position, text, color);
      } else if (round.missed) {
        this.addFloat(round.attackerId === attacker.id ? defender.position : attacker.position, 'FALLO', '#88f');
      }
    }

    // Sincronizar con estado final del servidor
    attacker.currentHp = r.attackerFinalHp;
    defender.currentHp = r.defenderFinalHp;
    if (r.attackerDefeated) attacker.alive = false;
    if (r.defenderDefeated) defender.alive = false;

    // Actualizar EXP y nivel
    if (r.attackerExpGained > 0) {
      attacker.exp += r.attackerExpGained;
      if (attacker.exp >= 100) {
        attacker.exp -= 100;
        attacker.level++;
        this.addFloat(attacker.position, '★LV UP', '#f0c040');
      }
    }

    // Mensaje final
    if (r.defenderDefeated)
      this.msg(`¡${defender.name} derrotado! +${r.attackerExpGained} EXP`);
    else if (r.attackerDefeated)
      this.msg(`¡${attacker.name} fue derrotado en el contraataque!`);
    else
      this.msg(`${attacker.name} atacó a ${defender.name}. +${r.attackerExpGained} EXP`);

    // Level up gains
    if (r.attackerLevelUpGains?.length > 0 && !r.attackerLevelUpGains[0].startsWith('¡Sin'))
      this.msg(`★ ${attacker.name} ¡SUBE DE NIVEL! ${r.attackerLevelUpGains.join(' ')}`);
  }

  // ── Fase enemiga (animación correcta: sin aplicar finalState antes de animar) ─
  private async animateEnemyPhase(result: EnemyTurnResult): Promise<void> {
    this.gameState.currentPhase = 'ENEMY';
    this.draw();
    this.msg('Fase enemiga — ¡Los enemigos atacan!');
    await this.delay(400);

    for (const action of result.actions) {
      // Buscar la unidad en el estado ACTUAL (no en finalState)
      const unit = this.gameState.units.find(u => u.id === action.unitId);
      if (!unit || !unit.alive) continue;

      // 1. Resaltar la unidad que va a actuar
      this.highlightUnit = unit;
      this.draw();
      await this.delay(250);

      // 2. Animar el movimiento desde posición actual hasta destino
      const from = { x: unit.position.x, y: unit.position.y };
      const to   = { x: action.toX, y: action.toY };
      if (from.x !== to.x || from.y !== to.y) {
        await this.animateMovement(unit, from, to);
      }
      await this.delay(150);

      // 3. Combate (si atacó)
      if (action.attacked && action.combatResult) {
        const cr  = action.combatResult;
        const tgt = this.gameState.units.find(u => u.id === action.targetId);

        for (const round of cr.rounds ?? []) {
          await this.delay(240);
          const attIsEnemy = round.attackerId === unit.id;
          const dmgUnit    = attIsEnemy ? tgt : unit;
          if (!dmgUnit) continue;

          if (!round.missed && round.damage > 0) {
            dmgUnit.currentHp = Math.max(0, dmgUnit.currentHp - round.damage);
            const col  = round.crit ? '#ff0' : (attIsEnemy ? '#ffaaaa' : '#aaffaa');
            const text = round.crit ? `${round.damage}!!` : String(round.damage);
            this.addFloat(dmgUnit.position, text, col);
            await this.flashUnit(dmgUnit);
          } else if (round.missed) {
            this.addFloat(dmgUnit.position, 'FALLO', '#88f');
          }
          this.draw();
        }

        // Sincronizar HP final y muerte
        if (tgt) {
          tgt.currentHp = cr.defenderFinalHp;
          if (cr.defenderDefeated) {
            await this.deathAnimation(tgt);
            tgt.alive = false;
            this.msg(`¡${tgt.name} derrotado!`);
          } else {
            this.msg(`${unit.name} atacó a ${tgt.name}!`);
          }
        }
        if (cr.attackerDefeated) {
          await this.deathAnimation(unit);
          unit.alive = false;
        }
        await this.delay(300);
      }

      this.highlightUnit = null;
      this.draw();
    }

    // Ahora sí sincronizamos el estado final del servidor
    // (turnNumber, phase, y cualquier cosa que hayamos podido perder)
    this.gameState.turnNumber   = result.finalState.turnNumber;
    this.gameState.currentPhase = 'PLAYER';
    this.gameState.status       = result.finalState.status;

    // Resetear flags de jugador para el nuevo turno
    for (const u of this.gameState.units) {
      if (u.team === 'player') { u.hasMoved = false; u.hasActed = false; }
    }

    this.selectedUnit   = null;
    this.reachableTiles = [];
    this.draw();
    this.msg(`Turno ${this.gameState.turnNumber} — ¡Tu turno! Selecciona una unidad.`);
  }

  // ── Movimiento suave con easing ease-in-out ─────────────────────────────────
  private animateMovement(unit: Unit, from: Position, to: Position): Promise<void> {
    return new Promise(resolve => {
      const dur   = 320;
      const start = performance.now();
      const frame = (now: number) => {
        const t = Math.min(1, (now - start) / dur);
        // Ease-in-out cuadrático
        const e = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        unit.position = { x: from.x + (to.x - from.x) * e, y: from.y + (to.y - from.y) * e };
        this.draw();
        if (t < 1) requestAnimationFrame(frame);
        else { unit.position = to; this.draw(); resolve(); }
      };
      requestAnimationFrame(frame);
    });
  }

  // ── Flash rojo al recibir daño ──────────────────────────────────────────────
  private flashUnit(unit: Unit): Promise<void> {
    return new Promise(resolve => {
      let frame = 0;
      const total = 8;
      const loop = () => {
        this.flashTargets.set(unit.id, frame % 2 === 0 ? 0.85 : 0);
        this.draw();
        frame++;
        if (frame < total) requestAnimationFrame(loop);
        else { this.flashTargets.delete(unit.id); this.draw(); resolve(); }
      };
      requestAnimationFrame(loop);
    });
  }

  // ── Animación de muerte (fade out) ──────────────────────────────────────────
  private deathAnimation(unit: Unit): Promise<void> {
    return new Promise(resolve => {
      const start = performance.now();
      const dur   = 500;
      const frame = (now: number) => {
        const t = Math.min(1, (now - start) / dur);
        this.deathAlphas.set(unit.id, 1 - t);
        this.draw();
        if (t < 1) requestAnimationFrame(frame);
        else { this.deathAlphas.delete(unit.id); resolve(); }
      };
      requestAnimationFrame(frame);
    });
  }

  // ── Eventos ────────────────────────────────────────────────────────────────
  private bindEvents(): void {
    this.canvas.addEventListener('click', (e) => this.onCanvasClick(e));

    this.container.querySelector('#btn-wait')?.addEventListener('click', () => {
      if (!this.selectedUnit) { this.msg('Selecciona una unidad primero.'); return; }
      this.selectedUnit.hasMoved = true;
      this.selectedUnit.hasActed = true;
      this.msg(`${this.selectedUnit.name} espera.`);
      this.selectedUnit  = null;
      this.reachableTiles = [];
      this.draw();
      this.checkAllMoved();
    });

    this.container.querySelector('#btn-endturn')?.addEventListener('click', async () => {
      if (this.gameState.currentPhase !== 'PLAYER') return;
      this.selectedUnit   = null;
      this.reachableTiles = [];
      this.hideCombatPreview();
      try {
        // UNA sola llamada a endTurn() - procesa todos los enemigos
        const result = await GameService.endTurn();
        await this.animateEnemyPhase(result);
      } catch (error) {
        console.error('Error en fase enemiga:', error);
        // Fallback sin backend
        this.gameState.units.forEach(u => { if (u.team === 'player') u.hasMoved = u.hasActed = false; });
        this.gameState.turnNumber++;
        this.gameState.currentPhase = 'PLAYER';
        this.draw();
        this.msg(`Turno ${this.gameState.turnNumber} — ¡Tu turno!`);
      }
      this.checkWin();
    });

    this.container.querySelector('#btn-confirm-atk')?.addEventListener('click', async () => {
      if (!this.selectedUnit || !this.pendingAttack) return;
      await this.executeAttack(this.selectedUnit, this.pendingAttack);
    });

    this.container.querySelector('#btn-cancel-atk')?.addEventListener('click', () => {
      this.hideCombatPreview();
      this.msg('Ataque cancelado.');
      this.draw();
    });

    this.container.querySelector('#btn-menu')?.addEventListener('click', () => {
      AudioManager.stop();
      import('./MenuPage').then(({ MenuPage }) => new MenuPage().render(this.container));
    });
  }

  private onCanvasClick(e: MouseEvent): void {
    if (this.gameState.currentPhase !== 'PLAYER') return;

    const rect  = this.canvas.getBoundingClientRect();
    const scaleX = this.canvas.width  / rect.width;
    const scaleY = this.canvas.height / rect.height;
    const mx = Math.floor((e.clientX - rect.left) * scaleX / TILE);
    const my = Math.floor((e.clientY - rect.top)  * scaleY / TILE);
    if (mx < 0 || mx >= COLS || my < 0 || my >= ROWS) return;

    const clicked = this.getUnitAt(mx, my);

    if (!this.selectedUnit) {
      // Seleccionar unidad aliada
      if (clicked?.team === 'player' && !clicked.hasActed)
        this.selectUnit(clicked);
      else if (clicked?.hasActed)
        this.msg(`${clicked.name} ya actuó este turno.`);
      return;
    }

    // ── Con unidad seleccionada ──────────────────────────────────────────────
    const isReach  = this.reachableTiles.some(p => p.x === mx && p.y === my);
    const isEnemy  = clicked?.team === 'enemy';
    const inAtkRange = isEnemy && this.getAttackableEnemies(this.selectedUnit).some(e => e.id === clicked!.id);

    // Clic en enemigo en rango → preview combate
    if (inAtkRange) {
      this.showCombatPreview(this.selectedUnit, clicked!);
      return;
    }

    // Clic en casilla alcanzable vacía → mover
    if (isReach && !clicked) {
      this.moveSelectedTo(mx, my);
      return;
    }

    // Clic en la propia unidad → cancelar selección
    if (clicked?.id === this.selectedUnit.id) {
      this.selectedUnit   = null;
      this.reachableTiles = [];
      this.hideCombatPreview();
      this.msg('Selección cancelada.');
      this.draw();
      return;
    }

    // Clic en otro aliado → cambiar selección
    if (clicked?.team === 'player' && !clicked.hasActed) {
      this.hideCombatPreview();
      this.selectUnit(clicked);
      return;
    }

    this.msg('No puedes moverte ahí. Elige casillas azules o enemigos en rojo.');
  }

  // ── Utilidades ─────────────────────────────────────────────────────────────
  private checkAllMoved(): void {
    const vivas = this.gameState.units.filter(u => u.alive && u.team === 'player');
    if (vivas.length > 0 && vivas.every(u => u.hasActed))
      this.msg('✅ Todas las unidades actuaron. Presiona "FIN DE TURNO".');
  }

  private checkWin(): void {
    const playerAlive = this.gameState.units.some(u => u.alive && u.team === 'player');
    const enemyAlive  = this.gameState.units.some(u => u.alive && u.team === 'enemy');
    if (!enemyAlive)  this.msg('🏆 ¡VICTORIA! Todos los enemigos derrotados.');
    if (!playerAlive) this.msg('💀 DERROTA. Todas tus unidades han caído.');
  }

  private delay(ms: number): Promise<void> {
    return new Promise(r => setTimeout(r, ms));
  }

  // ── Estado de emergencia ───────────────────────────────────────────────────
  private fallbackState(): GameState {
    const mkWeapon = (name: string, type: string, might: number, hit: number): Weapon =>
      ({ name, type, might, hit, crit: 0, minRange: 1, maxRange: 1, uses: 30, magical: false });

    const mkUnit = (
      id: string, name: string, team: string, cls: string,
      maxHp: number, str: number, mag: number, skl: number, spd: number,
      lck: number, def: number, res: number, mov: number, lv: number,
      weapon: Weapon, x: number, y: number
    ): Unit => ({
      id, name, team, unitClass: cls, aiBehavior: 'AGGRESSIVE',
      maxHp, currentHp: maxHp, str, mag, skl, spd, lck, def, res, mov,
      level: lv, exp: 0, equippedWeapon: weapon,
      position: { x, y }, hasMoved: false, hasActed: false, alive: true,
    });

    return {
      units: [
        mkUnit('marth',   'Marth',    'player', 'Lord',    20, 5, 1, 7, 8, 6, 4, 2, 5, 3, mkWeapon('Espada Hierro','SWORD',5,90), 1, 12),
        mkUnit('jagen',   'Jagen',    'player', 'Paladin', 24, 9, 2,11, 9, 5, 9, 7, 7, 7, mkWeapon('Lanza Plata','LANCE',12,75), 0, 13),
        mkUnit('linde',   'Linde',    'player', 'Mage',    16, 1, 9, 8, 7, 5, 2, 6, 5, 3, mkWeapon('Trueno','ANIMA',6,80),       2, 13),
        mkUnit('bandit1', 'J.Bandido','enemy',  'Warrior', 22, 8, 0, 6, 5, 3, 7, 1, 5, 5, mkWeapon('Hacha Acero','AXE',11,60),  17, 1),
        mkUnit('knight1', 'S.Oscuro', 'enemy',  'Knight',  20, 7, 0, 5, 4, 2, 8, 2, 4, 4, mkWeapon('Lanza Acero','LANCE',9,70), 16, 4),
        mkUnit('myrmid1', 'Mercenario','enemy', 'Myrmidon',18, 6, 0, 9,10, 4, 4, 2, 5, 3, mkWeapon('Espada Hierro','SWORD',5,90),14, 5),
        mkUnit('mage1',   'M.Oscuro', 'enemy',  'Mage',    16, 1, 8, 7, 6, 3, 2, 5, 5, 3,
          { name:'Flujo', type:'DARK', might:7, hit:80, crit:5, minRange:1, maxRange:2, uses:45, magical:true }, 18, 9),
      ],
      turnNumber: 1, currentPhase: 'PLAYER', status: 'ACTIVE',
    };
  }
}
