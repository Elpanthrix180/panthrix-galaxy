# FASE 18 — Jefes: la Reina alienígena 👑 ✅

## 🎯 Objetivo
El primer jefe del mod: mucho más fuerte que un mob normal, con **mucha vida**, **ataques especiales**,
**fases de combate**, **barra de jefe** y un **botín exclusivo**: la **necronita**.

## 👑 La Reina alienígena
| | |
|---|---|
| **Vida** | 300 (150 ❤) |
| **Armadura** | 10 + dureza 4 (como armadura de diamante) |
| **Daño cuerpo a cuerpo** | 6 ❤ (9 ❤ en la fase 3) |
| **Tamaño** | 3 bloques de alto |
| **Inmune a** | fuego, lava, empujones, daño de caída |
| **Barra de jefe** | morada → roja (fase 2) → blanca y pantalla oscura (fase 3) |
| **Nunca desaparece sola** | Aunque te alejes |

## ⚔️ Las 3 fases
| Fase | Vida | Qué hace |
|---|---|---|
| **1** | 100 % → 66 % | Te persigue y golpea. Si estás lejos, **escupe ácido** (rayos verdes, 4 ❤) cada 2 s. Cada 20 s llama a **2 crías** (criaturas alienígenas) |
| **2 — Furia** | 66 % → 33 % | **+25 % de velocidad**, escupe más a menudo. **Salto de impacto**: salta hacia ti y al caer lanza una **onda de choque** (5 ❤ + empujón + lentitud) a todos en 6 bloques. Llama a crías **y un soldado** |
| **3 — Desesperación** | 33 % → 0 | **+50 % de daño**. **Lluvia de ácido**: 12 escupitajos en círculo cada 6 s. Llama a un **depredador** (una vez) |

Al **cambiar de fase** ruge, aparece un mensaje y tiene un **escudo de 2 segundos** (partículas moradas, palpita y no recibe daño).
Como mucho tiene 8 aliens a su alrededor.

💡 **Truco:** el ácido es un rayo como los láseres: ¡**la guardia de la espada láser lo devuelve**! (Fase 16)

## 📡 Cómo llamarla: la Baliza alienígena
1. Fabrica la **Baliza alienígena** en el Banco de Ingeniería → categoría **Alienígena**:
   5 tecnologías alienígenas + cristal marciano + 2 bloques de redstone + batería básica.
2. Ve a **otro planeta** (en la Tierra no funciona) y haz clic derecho en el suelo.
3. Cae un rayo y aparece la Reina. Solo puede haber **una** en 128 bloques.

(En la Fase 21 tendrá su **colmena** en los planetas alienígenas.)

## 💎 Botín exclusivo
| Objeto | Cantidad |
|---|---|
| **Fragmento de necronita** | 4-6 (+ Botín) |
| Tecnología alienígena | 4-8 |
| Cristal cósmico | 1-2 |
| Experiencia | 200 + 500 del logro |

**Necronita** = 4 fragmentos + 4 lingotes de osmio (asteroides, Fase 20). Sirve para la **espada láser negra**,
la **pistola láser blanca** y la tecnología más avanzada.

🏆 **Logro "La caída de la Reina"** (desafío): derrota a la Reina alienígena.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Mob con mucha vida, botín, logro, objeto de invocación | ✅ Sí | JSON (`loot_tables/entities/alien_queen.json`, `advancements/alien_queen_slain.json`) |
| Barra de jefe | ⚠️ Parcial | `ServerBossEvent` en Java |
| Fases, escudo, salto de impacto, lluvia de ácido, invocar crías | ❌ No | `entity/boss/PGAlienQueenEntity.java` |
| Modelo con cresta y cola | ✅ Sí (Blockbench) | `client/model/PGAlienQueenModel.java` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `entity/boss/PGAlienQueenEntity.java` | La Reina: fases, ataques, barra de jefe |
| `item/PGAlienBeaconItem.java` | La baliza que la llama |
| `entity/mob/PGAliens.java` | **Nuevo:** "¿es un alien?" (los aliens ya no se disparan entre ellos) |
| `client/model/PGAlienQueenModel.java` + `client/renderer/PGAlienQueenRenderer.java` | Modelo y dibujo |
| `textures/entity/boss/alien_queen.png` | Textura (128x128) |

**Cambios en archivos existentes:** `PGLaserBoltEntity` (rayos con valores propios para el ácido; los aliens no se dañan entre ellos),
`ModEntities`, `ModItems`, `PGMobEvents`, `PGClientModEvents`, `EngineeringCategory` (icono de Alienígena).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Ponte equipo bueno (`/give @s panthrixsgalaxy:pg_black_laser_sword`... o la que quieras, cargada desde creativo).
2. Ve a Marte: `/execute in panthrixsgalaxy:mars run tp @s 0 200 0`, ponte en **supervivencia**.
3. Usa la **Baliza alienígena** (creativo, pestaña principal) en el suelo.
   - [ ] Rayo, mensaje y aparece la Reina con su **barra de jefe** morada.
   - [ ] Aléjate: escupe ácido verde. Haz **guardia** con la espada láser → se lo devuelves.
   - [ ] Cada 20 s aparecen crías.
4. Bájale a 2/3 de vida → ruge, escudo morado, barra roja, mensaje de **Furia**. Salta y te lanza una onda de choque.
5. Bájale a 1/3 → barra blanca, pantalla más oscura, **lluvia de ácido** en círculo, llega un depredador.
6. Derrótala → necronita, tecnología alienígena, logro **"La caída de la Reina"**.
7. Prueba la baliza en la Tierra → *"La señal no llega..."*.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| No aparece la barra de jefe | Tienes que estar cerca (la ves mientras la Reina se dibuja en tu pantalla). |
| La baliza no hace nada | ¿Estás en la Tierra? ¿Ya hay una Reina cerca? |
| No le hago daño | Tiene el escudo de cambio de fase (2 s). Espera a que acabe. |
| Demasiado difícil | Juega en Fácil, usa la guardia contra el ácido y mata a las crías primero. Cambia la vida en `createAttributes()`. |

## ✅ Checklist final
- [x] Reina alienígena con 300 de vida, armadura y modelo propio
- [x] 3 fases con ataques especiales (ácido, crías, salto de impacto, lluvia de ácido, depredador)
- [x] Escudo al cambiar de fase, barra de jefe que cambia de color
- [x] Baliza alienígena para invocarla (solo en otros planetas)
- [x] Botín exclusivo: necronita (+ tecnología alienígena y cristal cósmico)
- [x] Logro "La caída de la Reina"
- [ ] Probado en tu PC ← te toca a ti
