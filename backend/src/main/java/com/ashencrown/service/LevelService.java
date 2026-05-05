package com.ashencrown.service;

import com.ashencrown.dto.CampaignDto;
import com.ashencrown.dto.LevelDto;
import com.ashencrown.dto.UnitDefinitionDto;
import com.ashencrown.model.Campaign;
import com.ashencrown.model.Level;
import com.ashencrown.model.UnitDefinition;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LevelService {

    private final Map<String, Campaign> campaigns = new LinkedHashMap<>();

    public LevelService() {
        initializeCampaigns();
    }

    // ── 3 campañas, 7 niveles en total ────────────────────────────────────────────
    private void initializeCampaigns() {

        // ── PRÓLOGO (niveles 1-2, dificultad fácil) ───────────────────────────────
        campaigns.put("prologue", new Campaign("prologue", "Prólogo",
            "Los primeros pasos de una leyenda",
            List.of(
                mkLevel("level-1", "El Despertar",
                    "Bandidos atacan la aldea al amanecer. Demuestra tu valía.", "prologue", 1,
                    mapLlanura(),
                    "Derrota a todos los enemigos", 20,
                    List.of(p("Marth", "Lord",    2, 2, 12, "SWORD"),
                            p("Jagen", "Paladin", 6, 1, 13, "LANCE")),
                    List.of(e("Ryke el Brutal",  "Warrior",  3, 17, 2, "AGGRESSIVE", "AXE"),
                            e("Vagabundo",       "Myrmidon", 2, 15, 4, "BERSERK",    "SWORD"))),

                mkLevel("level-2", "Emboscada en el Bosque",
                    "El ejército oscuro tiende una trampa en las sombras del bosque.", "prologue", 1,
                    mapBosque(),
                    "Derrota a todos los enemigos", 25,
                    List.of(p("Marth", "Lord",    2, 1, 12, "SWORD"),
                            p("Jagen", "Paladin", 6, 0, 13, "LANCE"),
                            p("Linde", "Mage",    3, 2, 13, "ANIMA")),
                    List.of(e("Jefe Emboscada", "Warrior",  4, 17, 2, "AGGRESSIVE", "AXE"),
                            e("Arquero Oscuro", "Archer",   3, 16, 4, "DEFENSIVE",  "BOW"),
                            e("Sicario",        "Myrmidon", 3, 15, 3, "BERSERK",    "SWORD")))
            )));

        // ── ACTO I (niveles 3-5, dificultad normal) ───────────────────────────────
        campaigns.put("act1", new Campaign("act1", "Acto I — La Guerra Comienza",
            "El reino lucha por su supervivencia en el campo de batalla",
            List.of(
                mkLevel("level-3", "Cruce del Río",
                    "El único paso al norte es un puente estrecho bajo fuego enemigo.", "act1", 2,
                    mapPuente(),
                    "Derrota a todos los defensores", 30,
                    List.of(p("Marth", "Lord",    4,  9, 13, "SWORD"),
                            p("Jagen", "Paladin", 8,  8, 13, "LANCE"),
                            p("Linde", "Mage",    5, 10, 13, "ANIMA")),
                    List.of(e("Capitán del Puente","Knight",  5,  9, 1, "DEFENSIVE",  "LANCE"),
                            e("Arquero 1",          "Archer",  4, 10, 2, "DEFENSIVE",  "BOW"),
                            e("Arquero 2",          "Archer",  4,  8, 2, "DEFENSIVE",  "BOW"),
                            e("Mago del Río",       "Mage",    5,  9, 3, "DEFENSIVE",  "ANIMA"))),

                mkLevel("level-4", "Desfiladero de Montaña",
                    "Montañas imponentes encierran el único camino al corazón del reino oscuro.", "act1", 2,
                    mapMontana(),
                    "Derrota a todos los enemigos", 35,
                    List.of(p("Marth", "Lord",    5, 2, 12, "SWORD"),
                            p("Jagen", "Paladin", 9, 1, 13, "LANCE"),
                            p("Linde", "Mage",    6, 3, 13, "ANIMA")),
                    List.of(e("Guardián del Paso","Knight",   6, 16, 2, "AGGRESSIVE", "LANCE"),
                            e("Berserker",         "Warrior",  5, 18, 3, "BERSERK",    "AXE"),
                            e("Espadachín",        "Myrmidon", 5, 14, 4, "BERSERK",    "SWORD"),
                            e("Francotirador",     "Archer",   5, 17, 5, "DEFENSIVE",  "BOW"),
                            e("Hechicero",         "Mage",     6, 15, 2, "AGGRESSIVE", "DARK"))),

                mkLevel("level-5", "Asalto al Fuerte",
                    "El fuerte enemigo controla la región. Infiltra y destruye su mando.", "act1", 3,
                    mapFuerte(),
                    "Derrota al comandante del fuerte", 40,
                    List.of(p("Marth", "Lord",    6, 2, 12, "SWORD"),
                            p("Jagen", "Paladin",10, 1, 13, "LANCE"),
                            p("Linde", "Mage",    7, 3, 13, "ANIMA")),
                    List.of(e("Comandante",   "Knight",   7, 10, 6, "GUARD",      "LANCE"),
                            e("Centinela N",  "Knight",   5, 10, 8, "GUARD",      "LANCE"),
                            e("Centinela S",  "Knight",   5, 11, 8, "GUARD",      "LANCE"),
                            e("Mago Fuerte",  "Mage",     6, 11, 7, "DEFENSIVE",  "ANIMA"),
                            e("Patrulla",     "Myrmidon", 5, 15, 3, "AGGRESSIVE", "SWORD")))
            )));

        // ── ACTO II (niveles 6-7, dificultad difícil) ─────────────────────────────
        campaigns.put("act2", new Campaign("act2", "Acto II — La Oscuridad Avanza",
            "La batalla final se acerca. El destino del reino pende de un hilo",
            List.of(
                mkLevel("level-6", "Traición",
                    "Un aliado de confianza revela sus colores oscuros. No hay tiempo para dudar.", "act2", 3,
                    mapTraicion(),
                    "Sobrevive y derrota al traidor", 35,
                    List.of(p("Marth", "Lord",    7, 10, 12, "SWORD"),
                            p("Jagen", "Paladin",11, 10, 13, "LANCE"),
                            p("Linde", "Mage",    8, 11, 13, "ANIMA")),
                    List.of(e("El Traidor",    "Paladin",  9, 17, 2, "AGGRESSIVE", "LANCE"),
                            e("Renegado 1",    "Warrior",  8, 16, 3, "AGGRESSIVE", "AXE"),
                            e("Renegado 2",    "Myrmidon", 7, 18, 4, "BERSERK",    "SWORD"),
                            e("Mago Renegado", "Mage",     8, 15, 2, "AGGRESSIVE", "DARK"))),

                mkLevel("level-7", "Ciudadela del Señor Oscuro",
                    "La batalla final. El Señor Oscuro espera en su fortaleza eterna.", "act2", 4,
                    mapCiudadela(),
                    "Derrota al Señor Oscuro", 50,
                    List.of(p("Marth", "Lord",    9,  2, 12, "SWORD"),
                            p("Jagen", "Paladin",13,  1, 13, "LANCE"),
                            p("Linde", "Mage",   10,  3, 13, "ANIMA")),
                    List.of(e("Señor Oscuro",  "Knight",  12, 11, 6, "GUARD",      "LANCE"),
                            e("Guardián I",    "Knight",  10,  9, 5, "GUARD",      "LANCE"),
                            e("Guardián II",   "Knight",  10, 13, 5, "GUARD",      "LANCE"),
                            e("Archimago",     "Mage",    11, 10, 8, "DEFENSIVE",  "DARK"),
                            e("Asesino Élite", "Myrmidon",10,  9, 9, "BERSERK",    "SWORD"),
                            e("Hechicera",     "Mage",    10, 12, 8, "AGGRESSIVE", "ANIMA")))
            )));

        // ── ACTO III (niveles 8-14, dificultad muy difícil) ───────────────────────
        campaigns.put("act3", new Campaign("act3", "Acto III — El Último Bastión",
            "Las fuerzas de la oscuridad reagrupan. La guerra por el trono de cenizas comienza",
            List.of(
                mkLevel("level-8", "El Éxodo",
                    "Los supervivientes huyen por bosques densos perseguidos por el ejército oscuro.", "act3", 3,
                    mapExodo(),
                    "Derrota a todos los enemigos", 30,
                    List.of(p("Marth", "Lord",    8,  1, 13, "SWORD"),
                            p("Jagen", "Paladin", 12,  0, 14, "LANCE"),
                            p("Linde", "Mage",     8,  2, 13, "ANIMA"),
                            p("Caeda", "Cavalier", 6,  1, 12, "LANCE")),
                    List.of(e("Cazador Oscuro",  "Warrior",  8, 17, 1, "AGGRESSIVE", "AXE"),
                            e("Arquero Élite",   "Archer",   7, 16, 3, "DEFENSIVE",  "BOW"),
                            e("Espía Renegado",  "Myrmidon", 7, 18, 2, "BERSERK",    "SWORD"))),

                mkLevel("level-9", "Las Ruinas del Norte",
                    "Ruinas antiguas ocultan tesoros y peligros en las montañas del norte.", "act3", 3,
                    mapRuinas(),
                    "Derrota a todos los guardianes", 35,
                    List.of(p("Marth", "Lord",    9,  1, 13, "SWORD"),
                            p("Jagen", "Paladin", 13,  0, 14, "LANCE"),
                            p("Linde", "Mage",     9,  2, 13, "ANIMA"),
                            p("Caeda", "Cavalier", 7,  1, 12, "LANCE")),
                    List.of(e("Guardián Pétreo", "Knight",   9, 16, 2, "GUARD",      "LANCE"),
                            e("Arquero Roca",    "Archer",   8, 17, 4, "DEFENSIVE",  "BOW"),
                            e("Mago de Ruinas",  "Mage",     9, 15, 3, "AGGRESSIVE", "DARK"),
                            e("Berserker Gris",  "Warrior",  8, 18, 5, "BERSERK",    "AXE"))),

                mkLevel("level-10", "El Río de los Muertos",
                    "Un río de aguas negras separa a los aliados de sus objetivos.", "act3", 3,
                    mapRioMuertos(),
                    "Cruza el río y derrota al general", 40,
                    List.of(p("Marth", "Lord",    10,  1, 13, "SWORD"),
                            p("Jagen", "Paladin", 14,  0, 14, "LANCE"),
                            p("Linde", "Mage",    10,  2, 13, "ANIMA"),
                            p("Caeda", "Cavalier", 8,  1, 12, "LANCE")),
                    List.of(e("General del Río",  "Knight",  10, 16, 7, "GUARD",      "LANCE"),
                            e("Centinela Norte",  "Archer",   9, 17, 1, "DEFENSIVE",  "BOW"),
                            e("Centinela Sur",    "Archer",   9, 17,13, "DEFENSIVE",  "BOW"),
                            e("Hechicero Río",    "Mage",    10, 15, 7, "DEFENSIVE",  "DARK"),
                            e("Lancero del Paso", "Knight",   8, 13, 7, "AGGRESSIVE", "LANCE"))),

                mkLevel("level-11", "La Alianza Rota",
                    "Los renegados controlan el cruce de caminos. Cada bando pelea por el control.", "act3", 4,
                    mapAlianzaRota(),
                    "Toma el control del cruce", 40,
                    List.of(p("Marth", "Lord",    11,  1, 13, "SWORD"),
                            p("Jagen", "Paladin", 15,  0, 14, "LANCE"),
                            p("Linde", "Mage",    11,  2, 13, "ANIMA"),
                            p("Caeda", "Cavalier", 9,  1, 12, "LANCE"),
                            p("Ogma",  "Warrior", 10,  2, 14, "AXE")),
                    List.of(e("Comandante Rojo",  "General",  11, 17, 7, "GUARD",      "LANCE"),
                            e("Magister Oscuro",  "Mage",     11, 16, 5, "AGGRESSIVE", "DARK"),
                            e("Hacha Roja",       "Warrior",  10, 18, 3, "BERSERK",    "AXE"),
                            e("Hacha Roja 2",     "Warrior",  10, 18,11, "BERSERK",    "AXE"),
                            e("Sombra Veloz",     "Myrmidon", 10, 15, 7, "BERSERK",    "SWORD"))),

                mkLevel("level-12", "Asedio a la Ciudadela",
                    "El ejército oscuro asedia la última ciudadela aliada. ¡Defiéndela o cae!", "act3", 4,
                    mapAsedio(),
                    "Defiende la ciudadela — derrota al comandante del asedio", 45,
                    List.of(p("Marth", "Lord",    12,  9,  6, "SWORD"),
                            p("Jagen", "Paladin", 16,  8,  7, "LANCE"),
                            p("Linde", "Mage",    12, 10,  6, "ANIMA"),
                            p("Caeda", "Cavalier",10,  9,  8, "LANCE"),
                            p("Ogma",  "Warrior", 11,  8,  8, "AXE")),
                    List.of(e("Mariscal Oscuro",  "General",  13, 17, 7, "AGGRESSIVE", "LANCE"),
                            e("Ariete Mágico",    "Mage",     12, 17, 5, "AGGRESSIVE", "DARK"),
                            e("Ariete Mágico 2",  "Mage",     12, 17, 9, "AGGRESSIVE", "ANIMA"),
                            e("Batallón Hacha 1", "Warrior",  11, 18, 3, "BERSERK",    "AXE"),
                            e("Batallón Hacha 2", "Warrior",  11, 18,11, "BERSERK",    "AXE"),
                            e("Arquero Asedio 1", "Archer",   11, 19, 4, "DEFENSIVE",  "BOW"),
                            e("Arquero Asedio 2", "Archer",   11, 19,10, "DEFENSIVE",  "BOW"))),

                mkLevel("level-13", "El Valle de las Sombras",
                    "El paso final hacia el trono es un valle envuelto en sombras eternas.", "act3", 4,
                    mapValleOscuro(),
                    "Despeja el valle de la oscuridad", 45,
                    List.of(p("Marth", "Lord",    13,  2, 12, "SWORD"),
                            p("Jagen", "Paladin", 17,  2, 13, "LANCE"),
                            p("Linde", "Mage",    13,  2, 11, "ANIMA"),
                            p("Caeda", "Cavalier",11,  3, 12, "LANCE"),
                            p("Ogma",  "Warrior", 12,  3, 13, "AXE")),
                    List.of(e("Señor Sombra",     "General",  14, 16,  7, "GUARD",      "LANCE"),
                            e("Archisorcerer",    "Sorcerer", 14, 17,  5, "DEFENSIVE",  "DARK"),
                            e("Sombra Élite 1",   "Myrmidon", 13, 16,  3, "BERSERK",    "SWORD"),
                            e("Sombra Élite 2",   "Myrmidon", 13, 16, 11, "BERSERK",    "SWORD"),
                            e("Ballestero Oscuro","Archer",   12, 17,  2, "DEFENSIVE",  "BOW"),
                            e("Mago Oscuro Valle","Mage",     13, 17, 12, "AGGRESSIVE", "DARK"))),

                mkLevel("level-14", "El Trono de Cenizas",
                    "El Señor Eterno de las Cenizas espera en su trono. Este es el momento decisivo.", "act3", 4,
                    mapTronoCenizas(),
                    "Derrota al Señor Eterno de las Cenizas", 60,
                    List.of(p("Marth", "Lord",    15,  9, 13, "SWORD"),
                            p("Jagen", "Paladin", 20,  8, 14, "LANCE"),
                            p("Linde", "Mage",    15, 10, 13, "ANIMA"),
                            p("Caeda", "Cavalier",13,  9, 12, "LANCE"),
                            p("Ogma",  "Warrior", 12,  8, 12, "AXE")),
                    List.of(e("El Señor Eterno",   "DarkGeneral",18,  9,  2, "GUARD",      "LANCE"),
                            e("Arcángel Caído 1",  "General",    15,  7,  3, "GUARD",      "LANCE"),
                            e("Arcángel Caído 2",  "General",    15, 11,  3, "GUARD",      "LANCE"),
                            e("Gran Sorcerer",     "Sorcerer",   16,  9,  4, "DEFENSIVE",  "DARK"),
                            e("Ejecutor Oscuro 1", "Myrmidon",   14,  7,  6, "BERSERK",    "SWORD"),
                            e("Ejecutor Oscuro 2", "Myrmidon",   14, 11,  6, "BERSERK",    "SWORD"),
                            e("Centinela del Trono","Archer",    14,  6,  1, "DEFENSIVE",  "BOW")))
            )));
    }

    // ── Constructores de nivel ────────────────────────────────────────────────────
    private Level mkLevel(String id, String name, String desc, String campaignId, int diff,
                          int[][] layout, String objective, int turns,
                          List<UnitDefinition> players, List<UnitDefinition> enemies) {
        return new Level(id, name, desc, campaignId, diff, layout, players, enemies, objective, turns);
    }

    private UnitDefinition p(String name, String cls, int lv, int x, int y, String weapon) {
        return new UnitDefinition(name, cls, lv, x, y, "player", null, weapon);
    }

    private UnitDefinition e(String name, String cls, int lv, int x, int y, String behavior, String weapon) {
        return new UnitDefinition(name, cls, lv, x, y, "enemy", behavior, weapon);
    }

    // ── Generadores de mapa (15 filas × 20 columnas) ──────────────────────────────
    // 0=PLAINS 1=FOREST 2=MOUNTAIN 3=WATER 4=FORT 5=ROAD

    /** Nivel 1: Llanura abierta con bosques laterales y camino central */
    private int[][] mapLlanura() {
        int[][] m = new int[15][20];
        // Bosque lateral izquierdo
        for (int y = 0; y < 10; y++) { m[y][0] = 1; m[y][1] = 1; }
        // Franja de bosque central-superior
        for (int x = 4; x < 14; x++) m[5][x] = 1;
        for (int x = 6; x < 12; x++) m[6][x] = 1;
        // Fuertes estratégicos
        m[3][10] = 4; m[11][5] = 4;
        // Camino inferior
        for (int x = 0; x < 8; x++) m[14][x] = 5;
        return m;
    }

    /** Nivel 2: Bosque denso con corredor central de camino */
    private int[][] mapBosque() {
        int[][] m = new int[15][20];
        // Todo bosque
        for (int[] row : m) Arrays.fill(row, 1);
        // Corredor de camino columnas 8-10
        for (int y = 0; y < 15; y++) { m[y][8] = 5; m[y][9] = 5; m[y][10] = 5; }
        // Claro inferior izquierdo (jugador)
        for (int y = 10; y < 15; y++) for (int x = 0; x < 6; x++) m[y][x] = 0;
        // Claro superior derecho (enemigo)
        for (int y = 0; y < 5; y++) for (int x = 13; x < 20; x++) m[y][x] = 0;
        // Fuertes en los claros
        m[12][4] = 4; m[2][16] = 4;
        return m;
    }

    /** Nivel 3: Puente — agua a los lados, camino en el centro */
    private int[][] mapPuente() {
        int[][] m = new int[15][20];
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 8;  x++) m[y][x] = 3;  // Agua izquierda
            for (int x = 12; x < 20; x++) m[y][x] = 3; // Agua derecha
            for (int x = 8; x < 12; x++) m[y][x] = 5;  // Camino (puente)
        }
        // Fuertes en las orillas del puente
        m[0][8] = 4; m[0][11] = 4;
        m[14][8] = 4; m[14][11] = 4;
        return m;
    }

    /** Nivel 4: Desfiladero de montaña con paso central */
    private int[][] mapMontana() {
        int[][] m = new int[15][20];
        // Fondo de llanura
        for (int[] row : m) Arrays.fill(row, 0);
        // Montañas en franjas laterales (dejan pasillos)
        for (int y = 2; y < 13; y++) {
            if (y != 5 && y != 9) { m[y][5] = 2; m[y][6] = 2; }  // Muro izquierdo
            if (y != 4 && y != 8) { m[y][13] = 2; m[y][14] = 2; } // Muro derecho
        }
        // Montañas en el centro (crean dos pasillos)
        for (int x = 7; x < 13; x++) { m[5][x] = 2; m[9][x] = 2; }
        // Fuertes estratégicos en los pasillos
        m[4][10] = 4; m[10][10] = 4;
        // Camino a lo largo
        for (int y = 0; y < 15; y++) { m[y][0] = 5; m[y][19] = 5; }
        return m;
    }

    /** Nivel 5: Fuerte central con foso de agua */
    private int[][] mapFuerte() {
        int[][] m = new int[15][20];
        // Llanura base
        // Foso exterior (agua)
        for (int x = 7; x < 15; x++) { m[4][x] = 3; m[11][x] = 3; }
        for (int y = 4; y < 12; y++) { m[y][7] = 3; m[y][14] = 3; }
        // Fuerte central (dentro del foso)
        for (int y = 5; y < 11; y++) for (int x = 8; x < 14; x++) m[y][x] = 4;
        // Puentes de acceso (norte y sur)
        m[4][10] = 5; m[4][11] = 5; m[11][10] = 5; m[11][11] = 5;
        // Camino de aproximación
        for (int y = 11; y < 15; y++) { m[y][10] = 5; m[y][11] = 5; }
        return m;
    }

    /** Nivel 6: Traición — bosque en flancos, centro abierto */
    private int[][] mapTraicion() {
        int[][] m = new int[15][20];
        // Bosque flanco izquierdo
        for (int y = 0; y < 15; y++) for (int x = 0; x < 5; x++) m[y][x] = 1;
        // Bosque flanco derecho
        for (int y = 0; y < 15; y++) for (int x = 15; x < 20; x++) m[y][x] = 1;
        // Centro: llanura con camino central
        for (int y = 0; y < 15; y++) m[y][9] = 5;
        for (int y = 0; y < 15; y++) m[y][10] = 5;
        // Fuertes en el centro
        m[7][7] = 4; m[7][12] = 4;
        // Algunos árboles en el centro para cobertura
        m[4][7] = 1; m[4][12] = 1; m[10][7] = 1; m[10][12] = 1;
        return m;
    }

    /** Nivel 7: Ciudadela — gran fuerte rodeado de agua, acceso por puentes */
    private int[][] mapCiudadela() {
        int[][] m = new int[15][20];
        // Fuerte central masivo
        for (int y = 4; y < 12; y++) for (int x = 8; x < 15; x++) m[y][x] = 4;
        // Foso de agua alrededor del fuerte
        for (int x = 7; x < 16; x++) { m[3][x] = 3; m[12][x] = 3; }
        for (int y = 3; y < 13; y++) { m[y][7] = 3; m[y][15] = 3; }
        // Puentes de acceso (sur y norte)
        m[12][10] = 5; m[12][11] = 5;
        m[3][10]  = 5; m[3][11]  = 5;
        // Camino de aproximación desde abajo
        for (int y = 12; y < 15; y++) { m[y][10] = 5; m[y][11] = 5; }
        // Bosque en flancos para cobertura táctica
        for (int y = 5; y < 11; y++) { m[y][1] = 1; m[y][2] = 1; }
        return m;
    }

    /** Nivel 8: Éxodo — bosque denso con senderos diagonales */
    private int[][] mapExodo() {
        int[][] m = new int[15][20];
        for (int[] row : m) Arrays.fill(row, 1);  // Todo bosque
        // Senderos diagonales
        for (int i = 0; i < 14; i++) { m[i][i] = 5; m[i][Math.min(i+1,19)] = 5; }
        // Camino horizontal inferior
        for (int x = 0; x < 10; x++) m[14][x] = 5;
        // Claro jugador (inferior izquierda)
        for (int y = 11; y < 15; y++) for (int x = 0; x < 5; x++) m[y][x] = 0;
        // Claro enemigo (superior derecha)
        for (int y = 0; y < 4; y++) for (int x = 15; x < 20; x++) m[y][x] = 0;
        m[13][3] = 4; m[1][17] = 4;
        return m;
    }

    /** Nivel 9: Ruinas del Norte — montañas con ruinas y río central */
    private int[][] mapRuinas() {
        int[][] m = new int[15][20];
        // Montañas en flanco derecho
        for (int y = 0; y < 15; y++) for (int x = 16; x < 20; x++) m[y][x] = 2;
        // Montañas intermedias
        for (int y = 0; y < 15; y++) { if (y != 4 && y != 9) { m[y][7] = 2; m[y][8] = 2; } }
        // Ruinas (fuertes) en el mapa
        m[3][4] = 4; m[3][12] = 4; m[7][5] = 4; m[7][13] = 4; m[11][4] = 4; m[11][12] = 4;
        // Río central con puente
        for (int x = 9; x < 15; x++) m[7][x] = 3;
        m[7][11] = 5; m[7][12] = 5;
        // Camino occidental
        for (int y = 0; y < 15; y++) m[y][0] = 5;
        return m;
    }

    /** Nivel 10: Río de los Muertos — agua central con tres puentes */
    private int[][] mapRioMuertos() {
        int[][] m = new int[15][20];
        // Agua en columnas 7-11
        for (int y = 0; y < 15; y++) for (int x = 7; x < 12; x++) m[y][x] = 3;
        // Tres puentes verticales
        m[2][9] = 5;  m[3][9] = 5;  m[4][9] = 5;   // Puente norte
        m[7][9] = 5;  m[7][10] = 5;                  // Puente central
        m[11][9] = 5; m[12][9] = 5; m[13][9] = 5;   // Puente sur
        // Fuertes en orillas
        m[2][7] = 4; m[4][11] = 4; m[7][7] = 4; m[7][11] = 4; m[11][7] = 4; m[13][11] = 4;
        // Bosque en flancos
        for (int y = 0; y < 15; y++) { m[y][0] = 1; m[y][1] = 1; m[y][18] = 1; m[y][19] = 1; }
        return m;
    }

    /** Nivel 11: Alianza Rota — cruz de caminos con bosques en esquinas */
    private int[][] mapAlianzaRota() {
        int[][] m = new int[15][20];
        // Cruz de caminos
        for (int x = 0; x < 20; x++) m[7][x] = 5;
        for (int y = 0; y < 15; y++) m[y][10] = 5;
        // Fuertes en la intersección
        m[4][5] = 4; m[4][15] = 4; m[10][5] = 4; m[10][15] = 4;
        // Bosques en las cuatro esquinas
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) m[y][x] = 1;
            for (int x = 14; x < 20; x++) m[y][x] = 1;
        }
        for (int y = 9; y < 15; y++) {
            for (int x = 0; x < 5; x++) m[y][x] = 1;
            for (int x = 14; x < 20; x++) m[y][x] = 1;
        }
        return m;
    }

    /** Nivel 12: Asedio — ciudad amurallada con puertas de acceso */
    private int[][] mapAsedio() {
        int[][] m = new int[15][20];
        // Muros de ciudad (fuertes) en la zona norte
        for (int x = 6; x < 16; x++) { m[1][x] = 4; m[8][x] = 4; }
        for (int y = 1; y < 9; y++) { m[y][6] = 4; m[y][15] = 4; }
        // Interior de la ciudad (caminos)
        for (int y = 2; y < 8; y++) for (int x = 7; x < 15; x++) m[y][x] = 5;
        // Puertas (aperturas en los muros)
        m[1][10] = 5; m[1][11] = 5;
        m[8][10] = 5; m[8][11] = 5;
        m[4][6] = 5;  m[4][15] = 5;
        // Bosque exterior fuera de las murallas
        for (int y = 9; y < 15; y++) for (int x = 0; x < 3; x++) m[y][x] = 1;
        for (int y = 9; y < 15; y++) for (int x = 17; x < 20; x++) m[y][x] = 1;
        for (int y = 0; y < 15; y++) { if (m[y][0] == 0) m[y][0] = 1; }
        return m;
    }

    /** Nivel 13: Valle Oscuro — valle estrecho entre montañas con río */
    private int[][] mapValleOscuro() {
        int[][] m = new int[15][20];
        // Montañas en flancos exteriores
        for (int y = 0; y < 15; y++) { m[y][0] = 2; m[y][1] = 2; m[y][18] = 2; m[y][19] = 2; }
        // Montañas intermedias (crean el valle)
        for (int y = 2; y < 13; y++) { m[y][4] = 2; m[y][5] = 2; m[y][14] = 2; m[y][15] = 2; }
        // Río en el centro del valle
        for (int x = 6; x < 14; x++) m[7][x] = 3;
        m[7][9] = 5; m[7][10] = 5;  // Puente central
        // Fuertes en el valle
        m[3][8] = 4; m[3][11] = 4; m[11][8] = 4; m[11][11] = 4;
        // Caminos por el valle
        for (int y = 0; y < 15; y++) { m[y][2] = 5; m[y][3] = 5; m[y][16] = 5; m[y][17] = 5; }
        return m;
    }

    /** Nivel 14: Trono de Cenizas — sala del trono final */
    private int[][] mapTronoCenizas() {
        int[][] m = new int[15][20];
        // Zona del trono (norte, fuertes)
        for (int y = 0; y < 4; y++) for (int x = 7; x < 15; x++) m[y][x] = 4;
        // Foso de ceniza (agua/peligro)
        for (int x = 6; x < 16; x++) { m[4][x] = 3; m[5][x] = 3; }
        // Puentes al trono
        m[4][9] = 5; m[5][9] = 5; m[4][10] = 5; m[5][10] = 5;
        // Columnas de piedra (montañas)
        for (int y = 6; y < 15; y++) { m[y][3] = 2; m[y][4] = 2; m[y][15] = 2; m[y][16] = 2; }
        // Camino central de aproximación
        for (int y = 6; y < 15; y++) { m[y][9] = 5; m[y][10] = 5; }
        // Bosque lateral para cobertura
        for (int y = 6; y < 13; y++) { m[y][1] = 1; m[y][2] = 1; m[y][17] = 1; m[y][18] = 1; }
        return m;
    }

    // ── API pública ───────────────────────────────────────────────────────────────
    public List<CampaignDto> getAllCampaigns() {
        List<CampaignDto> result = new ArrayList<>();
        for (Campaign c : campaigns.values()) result.add(toDto(c));
        return result;
    }

    public CampaignDto getCampaign(String id) {
        Campaign c = campaigns.get(id);
        return c != null ? toDto(c) : null;
    }

    public LevelDto getLevel(String levelId) {
        for (Campaign c : campaigns.values())
            for (Level l : c.getLevels())
                if (l.getId().equals(levelId)) return toLevelDto(l);
        return null;
    }

    private CampaignDto toDto(Campaign c) {
        List<LevelDto> lvls = new ArrayList<>();
        for (Level l : c.getLevels()) lvls.add(toLevelDto(l));
        return new CampaignDto(c.getId(), c.getTitle(), c.getDescription(), lvls);
    }

    private LevelDto toLevelDto(Level l) {
        List<UnitDefinitionDto> players = new ArrayList<>(), enemies = new ArrayList<>();
        for (UnitDefinition u : l.getPlayerUnits()) players.add(toUnitDto(u));
        for (UnitDefinition u : l.getEnemyUnits())  enemies.add(toUnitDto(u));
        return new LevelDto(l.getId(), l.getName(), l.getDescription(), l.getCampaignId(),
                l.getDifficulty(), l.getMapLayout(), players, enemies, l.getObjective(), l.getTurnLimit());
    }

    private UnitDefinitionDto toUnitDto(UnitDefinition u) {
        return new UnitDefinitionDto(u.getName(), u.getUnitClass(), u.getLevel(),
                u.getStartX(), u.getStartY(), u.getTeam(), u.getAiBehavior(), u.getEquippedWeaponType());
    }
}
