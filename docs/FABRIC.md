# 🧵 Panthrixs Galaxy para Fabric (1.20.1)

La versión Fabric usa **el mismo código** que la de Forge. Solo unas pocas piezas son distintas
en cada cargador. Así, lo que añadas al mod (un bloque, un objeto, una máquina...) lo escribes
**una vez** y sale en las dos versiones.

## 🗂️ Cómo está organizado
```
src/main/java/...          ← CÓDIGO COMÚN (casi todo el mod) + piezas de Forge
src/main/resources/...     ← texturas, modelos, recetas, idiomas, mundos (COMÚN)
fabric/                    ← proyecto Fabric
  build.gradle             ← usa src/main (menos lo que es solo de Forge)
  src/main/java/...        ← piezas de Fabric (mismos nombres que las de Forge)
  src/main/resources/      ← fabric.mod.json, mixins, access widener
```

### Las piezas que cambian entre Forge y Fabric
| Pieza | Para qué | Forge | Fabric |
|---|---|---|---|
| `platform/PGRegistry` | Registrar bloques, objetos... | DeferredRegister | registro directo, en orden |
| `platform/PGPlatform` | Lo poco que es distinto (menús, huevos, combustible...) | APIs de Forge | Fabric API |
| `network/PGNetwork` | Mensajes servidor ↔ pantalla | SimpleChannel | Fabric Networking |
| `config/PGConfig` | Configuración | ForgeConfigSpec | **se genera** del de Forge (PGConfigSpec) |
| `system/backpack/PGBackpackSlot` | Hueco de mochila | capability | mixin en el jugador |
| `system/gravity/PGGravity` | Gravedad de los planetas | atributo de Forge | (Fase F3) |
| Energía | Hablar con cables de otros mods | Forge Energy | Team Reborn Energy |

El resto del mod solo llama a esas piezas y **no sabe** en qué cargador está.

### Listas comunes
Para no tener dos listas que mantener, estas son comunes y cada cargador las recorre:
- `entity/mob/PGMobSetup` → atributos y aparición de las criaturas.
- `client/PGClientRegistrations` → quién dibuja cada entidad, modelos, ventanas y cielos.

### ¿Cómo añado algo nuevo?
- **Un bloque, objeto, entidad, receta...** → como siempre, en `src/main` (sale en los dos).
- **Una opción de configuración** → en `config/PGConfig.java` (sale en los dos).
- **Algo que necesita un evento de Forge** → la lógica en una clase común con un método normal,
  y una línea en el "pegamento" de cada cargador (`forge/PGForgeEvents` y
  `fabric/PanthrixsGalaxyFabric`).

## 🛠️ Compilar y probar
```bash
./gradlew -p fabric build       # .jar en fabric/build/libs/
./gradlew -p fabric runClient   # abre Minecraft con Fabric y el mod
```
GitHub compila los dos `.jar` en cada subida (pestaña **Actions**).
Para jugar en Fabric hace falta **Fabric Loader** y **Fabric API**.

## 📋 Fases de la versión Fabric
| Fase | Qué | Estado |
|---|---|---|
| **F1** | Proyecto, código común, registro de todo, ventanas, dibujos, red, energía | ✅ compila |
| **F2** | Máquinas y energía, tolvas, energía de objetos, comandos, libro guía | ✅ compila |
| **F3** | Oxígeno, traje, mochila (tecla B, caer al morir, dibujo), gravedad, indicadores | ✅ compila y arranca |
| **F4** | Cohetes, naves, cielos de los planetas, nieblas, cámara | ✅ compila y arranca |
| **F5** | Armas (guardia), criaturas (aparición), Reina, logros | ✅ compila y arranca |
| F6 | Pruebas, `.jar` de Fabric en la Release | ⏳ |

### ✅ Qué tiene ya la versión Fabric (F1)
- Todos los bloques, objetos, entidades, máquinas, menús, recetas y estructuras registrados.
- Las 3 pestañas del creativo.
- Ventanas (banco, mochila, nave), dibujos de las entidades y efectos de cada dimensión.
- Configuración (`config/panthrixsgalaxy-common.toml` y `-client.toml`).
- Red servidor ↔ pantalla, energía compatible con otros mods (Team Reborn Energy).
- Hueco de mochila guardado con el jugador.
- Niveles de minado (`data/fabric/tags/blocks/needs_tool_level_N`).

### ✅ Fase F2
- **Máquinas**: generador, panel solar, reactor, celda, cables, recargadores y refinería funcionan
  igual (su lógica es común; en Fabric el combustible se mira en `FuelRegistry`).
- **Tolvas y tuberías** llenan los huecos de las máquinas (`fabric/PGFabricItems`, Transfer API).
- **Energía de objetos con otros mods**, en los dos sentidos (`fabric/PGFabricEnergy`):
  los cargadores de otros mods cargan las baterías y armas del mod, y las máquinas del mod
  cargan baterías de otros mods.
- **Comandos** `/pgtp`, `/pgkit`, `/pgrefill`, `/pgvacuum` y `/pgguide`.
- **Libro guía** al entrar por primera vez.
- Estos comandos y el libro ahora son **comunes**: Forge los conecta en `forge/PGForgeEvents`
  y Fabric en `fabric/PanthrixsGalaxyFabric` (una línea cada uno).

### ✅ Fase F3
- **Oxígeno**: se gasta, avisa, daña sin aire y por temperatura (lógica común `PGOxygenEvents`).
- **Traje**: aviso al completarlo (`PGSuitEvents`).
- **Mochila**: tecla **B**, se ve en la espalda, cae al suelo al morir (sin keepInventory),
  se conserva al volver del End y los demás jugadores la ven.
- **Gravedad** de cada planeta y caídas más suaves (mixin `fabric/mixin/LivingEntityMixin`).
- **Indicadores** de oxígeno, cohete y nave (los mismos dibujos que en Forge).
- Lo que pasa cada tick en la pantalla (tecla B, ESPACIO en el cohete, polvo de Marte): `client/PGClientTick`, común.
- Los "pegamentos" de eventos son `forge/PGForgeEvents` y `fabric/PGFabricEvents`.

### ✅ Fase F4
- **Cohetes y naves**: no se puede bajar en pleno vuelo (`PGRocketEvents.canDismount`, común;
  en Fabric con el mixin `EntityMixin`) y el astronauta no se dibuja dentro del vehículo.
- **Cielos** del Espacio, la Luna, los asteroides, Mercurio, Plutón y Nyx: cada cielo implementa
  `client/sky/PGSkyEffects`. Forge los llama directamente; Fabric los registra por dimensión.
- **Nieblas**: tormentas de Marte, Venus y el horizonte que se oscurece al subir (`PGClientVisuals`).
- **Temblor de cámara** al despegar.
- Mixins de pantalla en `fabric/mixin/client` (niebla, cámara, dibujo del jugador).

> ⚠️ La prueba de arranque usa un **servidor**: los mixins de pantalla solo se comprueban al
> compilar (sin avisos importantes). Hay que probarlos abriendo el juego (`runClient`).

### 🧪 Prueba de arranque automática
En cada subida, GitHub arranca **un servidor de Forge y otro de Fabric** con el mod
(`./gradlew runServer -Psmoke`). El mod escribe `SMOKE TEST OK` con las dimensiones cargadas
y apaga el servidor (`system/PGSmokeTest`). Si un mixin, un registro o un archivo de datos
falla al arrancar, la ejecución sale en rojo. Así se detectan errores sin abrir el juego.

### ✅ Fase F5
- **Guardia de la espada láser**: al bloquear, el daño se reduce (mixin en `actuallyHurt`).
- **Aparición natural** de las criaturas en sus planetas (`PGFabricMobs`, BiomeModifications).
- **Logros automáticos** (llegar a planetas, morir en el espacio, etc.) desde `PGFabricEvents`.
- Arreglo del **libro de recetas** (`ClientRecipeBookMixin`) para las recetas del mod.
- La prueba de arranque ahora **audita todos los mixins** (`MixinEnvironment.audit()`): si alguno
  no encaja con Minecraft, sale en rojo.

### ⏳ Lo que falta
F6: pruebas finales, PR y release con los dos jars (Forge y Fabric).

> ℹ️ En Fabric los niveles de herramienta no se pueden "intercalar" como en Forge
> (la lunarita va entre hierro y diamante). En Fabric la lunarita pica como el hierro.

## 🧩 MCreator
MCreator trabaja con un solo cargador por proyecto. Para Fabric habría que crear otro espacio de
trabajo en MCreator; este proyecto usa código Java y no se puede abrir allí.
