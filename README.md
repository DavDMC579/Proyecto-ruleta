# 🎰 Roulette Roguelike Game - Progressive Quotas

Proyecto de juego de Ruleta Americana estilo Roguelike con cuotas progresivas, ventajas estratégicas, persistencia en PostgreSQL, arquitectura multicapa desacoplada y pruebas unitarias automatizadas.

---

## 📋 Cumplimiento de Requerimientos

### 🔹 Requerimientos Funcionales (@jugador / @sistema)
- **RQ01 (@jugador)**: Diferentes tipos de apuestas:
  - Número específico (Pleno 35:1)
  - Color (Rojo / Negro 1:1)
  - Paridad (Par / Impar 1:1)
  - Docenas (1st 12, 2nd 12, 3rd 12 2:1)
- **RQ02 (@jugador)**: Juego dividido en rondas con cuota objetivo inicial ($1,000 partiendo de $100) en 6 tiradas para avanzar al siguiente nivel.
- **RQ03 (@sistema)**: Apuesta mínima obligatoria por ronda; si los fondos son insuficientes para cubrirla, la partida se pierde (Game Over).
- **RQ04 (@sistema)**: Cuota dinámica que escala proporcionalmente la dificultad de la siguiente ronda si se supera el objetivo por un margen amplio (ej. $100 -> cuota $1,000 -> saldo $2,000 -> próxima cuota $6,000).
- **RQ05 (@jugador)**: Selección de 1 de 3 ventajas estratégicas al superar la cuota:
  - 🌟 **Money Boost**: +50% del balance inicial base.
  - 🔮 **Clairvoyance**: Revela el número exacto que saldrá en la siguiente tirada.
  - 🛡️ **Loss Mitigation**: Reembolsa el 50% de las pérdidas si no se acierta la apuesta.
- **RQ06 (@sistema)**: Guardado persistente del progreso de la partida (balance, nivel, cuota, tiradas, ventajas) para reanudar posteriormente.
- **RQ07 (@jugador)**: Panel informativo en el menú que enseña cómo funcionan los multiplicadores y valores de ganancia de la ruleta.

### 🔹 Requerimientos Técnicos y de Arquitectura (@vinni)
- **RQ07 (@vinni)**: Desarrollado en lenguaje **Java** con gestión de dependencias y construcción mediante **Maven** (`pom.xml`).
- **RQ08 (@vinni)**: Conexión a base de datos **PostgreSQL** para almacenamiento persistente del estado de partidas y jugadores (`schema.sql`, `db.properties`). Incluye fallback transparente In-Memory para pruebas locales inmediatas.
- **RQ09 (@vinni)**: Todo el código fuente (clases, variables, métodos y paquetes) estructurado y encapsulado de forma estricta en **idioma inglés**.
- **RQ10 (@vinni)**: Arquitectura multicapa desacoplada lógicamente:
  - `model` (entidades, apuestas, ruleta, ventajas)
  - `dto` (Data Transfer Objects)
  - `dao` & `dao.impl` (persistencia con interfaces)
  - `service` & `service.impl` (lógica de negocio y reglas del juego)
  - `view` (interfaz gráfica Swing y terminal de consola)
- **RQ11 (@vinni)**: Patrones de diseño implementados:
  - **DTOs**: `PlayerDTO`, `GameSessionDTO`, `BetDTO`, `SpinResultDTO`
  - **Patrón DAO con interfaces**: `PlayerDao`, `GameSessionDao` con implementaciones JDBC PostgreSQL
  - **Patrón Factory**: `BetFactory` para instanciación desacoplada de apuestas
  - **Inversión de Dependencias (SOLID)**: Inyección de dependencias a través de interfaces en todos los servicios
- **RQ12 (@vinni)**: Pruebas unitarias automatizadas con **JUnit 5** (`BetMathTest`, `BalanceServiceTest`, `RouletteWheelTest`, `QuotaServiceTest`, `PerkServiceTest`, `BetFactoryTest`).

---

## 📂 Estructura del Proyecto

```
Juego Programacion/
├── pom.xml                                   # Configuración de Maven y dependencias (Java 17, PostgreSQL, JUnit 5)
├── schema.sql                                # Script DDL para PostgreSQL
├── README.md                                 # Documentación técnica y funcional
├── src/
│   ├── main/
│   │   ├── java/com/roulette/
│   │   │   ├── RouletteApplication.java      # Clase principal con inicio GUI / Consola
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java       # Configuración y conexión JDBC a PostgreSQL
│   │   │   ├── model/
│   │   │   │   ├── entity/                   # Entidades Player y GameSession
│   │   │   │   ├── bet/                      # Interfaz Bet, NumberBet, ColorBet, ParityBet, DozenBet
│   │   │   │   ├── perk/                     # PerkType y Perk
│   │   │   │   └── roulette/                 # RoulettePocket, RouletteWheel, PocketColor
│   │   │   ├── dto/                          # DTOs para transporte de datos desacoplado
│   │   │   ├── factory/
│   │   │   │   └── BetFactory.java           # Patrón Factory para creación de apuestas
│   │   │   ├── dao/
│   │   │   │   ├── PlayerDao.java            # Interface DAO
│   │   │   │   ├── GameSessionDao.java       # Interface DAO
│   │   │   │   └── impl/                     # Implementaciones PostgreSQL e In-Memory
│   │   │   ├── service/
│   │   │   │   ├── GameSessionService.java   # Interface de servicio
│   │   │   │   ├── BettingService.java       # Interface de apuestas y mitigación
│   │   │   │   ├── RouletteService.java      # Interface de ruleta y clairvoyance
│   │   │   │   ├── QuotaService.java         # Interface de cuota dinámica progresiva
│   │   │   │   └── impl/                     # Implementaciones con Inversión de Dependencias
│   │   │   └── view/
│   │   │       ├── GameGuiView.java          # Interfaz Gráfica interactiva Swing
│   │   │       └── ConsoleView.java          # Interfaz de Consola interactiva
│   │   └── resources/
│   │       ├── db.properties                 # Credenciales de conexión a PostgreSQL
│   │       ├── schema.sql                    # Script SQL
│   │       └── roulette_table.png            # Asset visual pixel art
│   └── test/
│       └── java/com/roulette/
│           ├── BetMathTest.java              # Pruebas de matemáticas y pagos (35:1, 2:1, 1:1, etc.)
│           ├── BalanceServiceTest.java       # Pruebas de balance y apuesta mínima obligatoria
│           ├── RouletteWheelTest.java        # Pruebas de ruleta y predeterminación Clairvoyance
│           ├── QuotaServiceTest.java         # Pruebas de cuota dinámica y escalado a 6,000
│           ├── PerkServiceTest.java          # Pruebas de ventajas estratégicas
│           └── BetFactoryTest.java           # Pruebas del patrón Factory
```

---

## 🚀 Cómo Ejecutar el Proyecto con Maven

### 1. Ejecutar las Pruebas Unitarias Automatizadas (RQ12):
```bash
mvn test
```

### 2. Ejecutar la Aplicación en Modo Interfaz Gráfica (Swing):
```bash
mvn exec:java
```

### 3. Ejecutar la Aplicación en Modo Consola / Terminal:
```bash
mvn exec:java -Dexec.args="--console"
```

### 4. Empaquetar el JAR Ejecutable:
```bash
mvn clean package
java -jar target/roulette-roguelike-game-1.0.0.jar
```

---

## 🗄️ Configuración de Base de Datos PostgreSQL (RQ08)

1. Crear la base de datos en PostgreSQL:
```sql
CREATE DATABASE roulette_db;
```
2. Ejecutar el script `schema.sql` en la base de datos.
3. Modificar las credenciales en `src/main/resources/db.properties` si es necesario (usuario, contraseña y puerto).
4. Si PostgreSQL no está activo, el sistema activa automáticamente un repositorio **In-Memory persistente** para que la aplicación siempre funcione sin interrupciones.
