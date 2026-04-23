# Ashen Crown — Guía completa

## Estructura del proyecto

```
Ashen_Crown/
├── Ashen_Crown_Backend/
│   └── backend/                    ← Todo el código Java
│       ├── src/main/java/com/ashencrown/
│       │   ├── AshenCrownApplication.java
│       │   ├── config/SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   └── GameController.java
│       │   ├── dto/
│       │   │   ├── AttackRequestDto.java
│       │   │   ├── AuthResponseDto.java
│       │   │   ├── CombatResultDto.java
│       │   │   ├── LanguageRequestDto.java
│       │   │   ├── LoginRequestDto.java
│       │   │   ├── MoveRequestDto.java
│       │   │   ├── MoveResultDto.java
│       │   │   └── RegisterRequestDto.java
│       │   ├── model/
│       │   │   ├── GameMap.java
│       │   │   ├── GameState.java
│       │   │   ├── Position.java
│       │   │   ├── TerrainType.java
│       │   │   ├── Tile.java
│       │   │   ├── Unit.java
│       │   │   └── User.java
│       │   ├── repository/
│       │   │   └── UserRepository.java
│       │   └── service/
│       │       ├── AuthService.java
│       │       ├── CombatService.java
│       │       ├── GameService.java
│       │       └── MovementService.java
│       ├── src/main/resources/application.properties
│       └── pom.xml
│
└── Ashen_Crown_Frontend/
    └── frontend/                   ← Todo el código TypeScript
        ├── index.html
        ├── package.json
        ├── tsconfig.json
        └── src/
            ├── main.ts
            ├── i18n/i18n.ts
            ├── services/
            │   ├── AuthService.ts
            │   └── GameService.ts
            ├── pages/
            │   ├── LandingPage.ts
            │   ├── LoginPage.ts
            │   ├── RegisterPage.ts
            │   ├── MenuPage.ts
            │   └── GamePage.ts
            └── styles/
                ├── global.css
                ├── LandingPage.css
                ├── auth.css
                └── game.css
```

---

## Instalación paso a paso

### 1. Instalar PostgreSQL

1. Descarga desde https://www.postgresql.org/download/windows/
2. Durante la instalación: puerto **5432**, usuario **postgres**, anota la contraseña
3. pgAdmin se instala automáticamente con él

### 2. Crear la base de datos

1. Abre pgAdmin
2. Clic derecho en "Databases" → Create → Database
3. Nombre: **ashen_crown** → Save

### 3. Configurar el backend

Edita `backend/src/main/resources/application.properties`:
```
spring.datasource.password=TU_PASSWORD_AQUI
```

### 4. Correr el backend

```bash
cd Ashen_Crown_Backend/backend
mvn spring-boot:run
```

Espera hasta ver: `Started AshenCrownApplication in X seconds`

### 5. Correr el frontend

```bash
cd Ashen_Crown_Frontend/frontend
npm install
npm run dev
```

Abre el navegador en: **http://localhost:5173**

---

## Flujo del usuario

1. **Landing** → Crear cuenta o Iniciar sesión
2. **Registro** → Llena correo, usuario y contraseña → redirige a Login
3. **Login** → Ingresa credenciales → va al Menú
4. **Menú** → Empezar juego / Continuar (bloqueado si no hay partida guardada) / Idioma / Cerrar sesión
5. **Juego** → Mapa de batalla con Marth y Jagen

---

## Endpoints del backend

### Autenticación
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | /api/auth/register | Crear cuenta |
| POST | /api/auth/login | Iniciar sesión |
| PUT  | /api/auth/language/{userId} | Cambiar idioma |

### Juego
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | /api/game/state | Estado del juego |
| POST | /api/game/new | Nueva partida |
| GET  | /api/game/reachable/{unitId} | Casillas alcanzables (BFS) |
| POST | /api/game/move | Mover unidad |
| POST | /api/game/attack | Atacar |
| POST | /api/game/wait/{unitId} | Esperar |
| POST | /api/game/end-turn | Fin de turno |

---

## Estructuras de datos (para la materia)

| Estructura | Archivo | Uso |
|---|---|---|
| Matriz 2D | GameMap.java | Tile[8][10] — el mapa |
| Cola (Queue/FIFO) | MovementService.java | BFS de movimiento |
| HashMap | MovementService.java | Casillas visitadas en BFS |
| HashSet | MovementService.java | Posiciones bloqueadas |
| ArrayList | GameState.java | Lista de unidades |
| Enum | TerrainType.java | 6 tipos de terreno |
