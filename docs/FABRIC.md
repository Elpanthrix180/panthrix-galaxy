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
| F3 | Oxígeno, traje, mochila (tecla B, caer al morir, dibujo), gravedad, indicadores | ⏳ |
| F4 | Cohetes, naves, cielos de los planetas, tormentas de Marte | ⏳ |
| F5 | Armas (guardia), criaturas (aparición), Reina, logros | ⏳ |
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

### ⏳ Lo que todavía NO funciona en Fabric
Lo que en Forge depende de **eventos** (se conecta en F3-F5): gastar oxígeno, indicadores
en pantalla, tecla B, gravedad baja, aparición natural de criaturas, logros automáticos,
cielos especiales y tormentas.

> ℹ️ En Fabric los niveles de herramienta no se pueden "intercalar" como en Forge
> (la lunarita va entre hierro y diamante). En Fabric la lunarita pica como el hierro.

## 🧩 MCreator
MCreator trabaja con un solo cargador por proyecto. Para Fabric habría que crear otro espacio de
trabajo en MCreator; este proyecto usa código Java y no se puede abrir allí.
