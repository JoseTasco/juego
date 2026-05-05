import { Howl } from 'howler';

const MUSIC_BASE = '/src/assets/Music/0. Heroes/';

export const TRACKS = {
  menu:        MUSIC_BASE + 'FEH_04 (Menu).mp3',
  map1:        MUSIC_BASE + 'FEH_08 (Map 1).mp3',
  map2:        MUSIC_BASE + 'FEH_09 (Map 2).mp3',
  map3:        MUSIC_BASE + 'FEH_10 (Map 3).mp3',
  map4:        MUSIC_BASE + 'FEH_28 (Map 4).mp3',
  map5:        MUSIC_BASE + 'FEH_32 (Map 5).mp3',
  map6:        MUSIC_BASE + 'FEH_45 (Map 6).mp3',
  boss:        MUSIC_BASE + 'FEH_15 (Battle - Boss).mp3',
  battlePlayer:MUSIC_BASE + 'FEH_13 (Battle - Player).mp3',
  battleEnemy: MUSIC_BASE + 'FEH_14 (Battle - Enemy).mp3',
};

const LEVEL_MUSIC: Record<string, string> = {
  'level-1':  TRACKS.map1,
  'level-2':  TRACKS.map1,
  'level-3':  TRACKS.map2,
  'level-4':  TRACKS.map2,
  'level-5':  TRACKS.map3,
  'level-6':  TRACKS.map3,
  'level-7':  TRACKS.boss,
  'level-8':  TRACKS.map4,
  'level-9':  TRACKS.map4,
  'level-10': TRACKS.map5,
  'level-11': TRACKS.map5,
  'level-12': TRACKS.map6,
  'level-13': TRACKS.map6,
  'level-14': TRACKS.boss,
};

export class AudioManager {
  private static music: Howl | null = null;
  private static vol = 0.6;
  private static currentSrc = '';

  static playMusic(src: string, loop = true): void {
    if (this.currentSrc === src && this.music?.playing()) return;
    this.stop();
    this.currentSrc = src;
    this.music = new Howl({
      src:    [src],
      loop,
      volume: this.vol,
      onloaderror: (_id: number, err: unknown) => console.warn('Audio load error:', err),
    });
    this.music.play();
  }

  static playLevelMusic(levelId?: string): void {
    const src = (levelId && LEVEL_MUSIC[levelId]) ?? TRACKS.map1;
    this.playMusic(src);
  }

  static stop(): void {
    if (this.music) { this.music.unload(); this.music = null; this.currentSrc = ''; }
  }

  static pause(): void { this.music?.pause(); }
  static resume(): void { if (this.music && !this.music.playing()) this.music.play(); }

  static setVolume(v: number): void {
    this.vol = Math.max(0, Math.min(1, v));
    this.music?.volume(this.vol);
  }
}
