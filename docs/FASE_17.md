# FASE 17 — Criaturas (mobs) 👽 ✅

## 🎯 Objetivo
Dar vida a los planetas: criaturas propias de la Luna y de Marte, y los primeros alienígenas,
cada uno con su **comportamiento**: patrullar, perseguir, atacar, proteger su zona o tender emboscadas.

## 🌙 Luna
| Criatura | Vida | Daño | Comportamiento | Suelta |
|---|---|---|---|---|
| **Crawler lunar** | 6 ❤ | 1,5 ❤ | Rápido, en grupos de 2-4. **Trepa paredes** y **salta** sobre ti | Hilo, quitina |
| **Escorpión lunar** | 10 ❤ | 2 ❤ | Acorazado y lento. Su aguijón te **ralentiza** | Quitina |

## 🔴 Marte
| Criatura | Vida | Daño | Comportamiento | Suelta |
|---|---|---|---|---|
| **Crawler marciano** | 9 ❤ | 2 ❤ | Como el lunar, más grande y resistente | Hilo, quitina |
| **Escorpión marciano** | 13 ❤ | 2,5 ❤ | Muy acorazado. Su aguijón te **envenena** | Quitina, ojo de araña |
| **Gusano marciano** | 20 ❤ | 3,5 ❤ | **EMBOSCADA:** vive enterrado, invisible e invulnerable. Si te acercas a menos de 10 bloques, **sale del suelo** rugiendo. Si te alejas, se vuelve a enterrar | Quitina, mineral y cristal marciano |
| **Alien explorador** | 12 ❤ | 2 ❤ | **PATRULLA** tranquilo. **No ataca** si no le atacas; si le pegas, avisa a los demás aliens. Con poca vida **huye** | Tecnología alienígena (25 %) |
| **Alien soldado** | 15 ❤ | láser 2,5 ❤ | Grupos de 2-3 con **pistola láser**: **dispara** desde 16 bloques. **PROTEGE SU ZONA** (no se aleja más de 24 bloques de donde apareció) | Tecnología alienígena (35 %), a veces su **pistola láser** (5 %) |

## 👽 Alienígenas de otros mundos (por ahora solo con huevo)
Aparecerán en los planetas alienígenas (Fase 21). Ya puedes probarlos con su huevo en creativo:
| Criatura | Vida | Daño | Comportamiento |
|---|---|---|---|
| **Criatura alienígena** | 11 ❤ | 2,5 ❤ | Bestia muy rápida que caza en **manada**: si atacas a una, vienen todas |
| **Depredador alienígena** | 22 ❤ | 4,5 ❤ | **CAMUFLAJE:** te acecha **invisible**. A menos de 6 bloques (o si le golpeas) aparece de golpe con un rugido |

👑 La **Reina alienígena** es un jefe: Fase 18.

## 💡 Dónde y cuándo aparecen
- Solo en **su planeta**. En la Tierra no salen.
- Aparecen **de día y de noche** (en la Luna siempre es de día)... **pero no donde hay luz de antorchas o lámparas**.
  👉 **Ilumina tu base** y estará a salvo, igual que en la Tierra.
- No respiran ni necesitan traje: son de allí.
- En **Pacífico** no aparecen.

## 🧪 Botín nuevo
| Objeto | De dónde | Para qué |
|---|---|---|
| **Quitina alienígena** | Crawlers, escorpiones, gusanos, criaturas | 2 quitina + 2 hierro = 2 **placas reforzadas** (en vez del cobre) |
| **Tecnología alienígena** | Aliens | La tecnología más avanzada (espada negra, jefes, Fase 18+) |

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Mobs sencillos (vida, daño, perseguir, atacar), huevos, botín | ✅ Sí | Java mínimo + JSON del botín (`loot_tables/entities/`) |
| Aparecer solo en un planeta | ✅ Sí | `"spawners"` en `worldgen/biome/moon.json` y `mars.json` |
| Aparecer con sol pero no con antorchas | ❌ No | `entity/mob/PGSpawnRules.java` |
| Enterrarse / emerger (gusano), camuflaje (depredador), disparar láseres (soldado), huir con poca vida (explorador) | ❌ No | Una clase Java por criatura en `entity/mob/` |
| Modelos 3D | ✅ Sí (Blockbench) | Escorpión, gusano y criatura: modelos propios. Crawlers: el de la araña. Aliens: el humanoide (como un zombi) |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `entity/mob/PGCrawlerEntity.java` | Crawlers (lunar y marciano) |
| `entity/mob/PGScorpionEntity.java` | Escorpiones (lunar y marciano) |
| `entity/mob/PGMartianWormEntity.java` | Gusano: enterrarse y emerger |
| `entity/mob/PGAlienExplorerEntity.java`, `PGAlienSoldierEntity.java`, `PGAlienCreatureEntity.java`, `PGAlienPredatorEntity.java` | Los aliens |
| `entity/mob/PGMobEvents.java` | Vida/daño/velocidad de cada uno y dónde pueden aparecer |
| `entity/mob/PGSpawnRules.java` | La regla de "sí con sol, no con antorchas" |
| `client/model/` + `client/renderer/` | Modelos y dibujo |
| `textures/entity/mob/*.png` | Texturas (ábrelas con Blockbench o un editor de píxeles) |
| `data/.../loot_tables/entities/*.json` | Qué suelta cada uno |

**Cambios en archivos existentes:** `ModEntities`, `ModItems` (huevos, quitina, tecnología), `PGClientModEvents`, biomas de la Luna y Marte.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. **Creativo**: en la pestaña principal están los **9 huevos**.
2. **Supervivencia** (dificultad Normal) y ve a la Luna: `/execute in panthrixsgalaxy:moon run tp @s 0 120 0`
   - [ ] Al rato aparecen crawlers y escorpiones lunares. Un escorpión te ralentiza.
   - [ ] Pon antorchas alrededor: dentro del círculo iluminado no aparecen.
3. Marte: `/execute in panthrixsgalaxy:mars run tp @s 0 200 0`
   - [ ] **Gusano:** pon un huevo, aléjate 15 bloques y vuelve → sale del suelo con una nube de tierra. Enterrado no se le puede pegar.
   - [ ] **Explorador:** pasea sin atacarte. Pégale → se defiende; con poca vida huye.
   - [ ] **Soldado:** te dispara láseres rojos. Con la **espada láser en guardia** (clic derecho mantenido, mirándole) ¡se los devuelves!
4. **Depredador** (huevo): aléjate → desaparece. Acércate → aparece rugiendo.
5. **Criaturas** (3 huevos): pega a una → vienen todas.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| No aparece ninguna criatura | ¿Dificultad Pacífico? ¿Zona iluminada? Espera un poco: necesitan sitio libre en el suelo. |
| Criatura invisible o morada/negra | Falta su textura en `textures/entity/mob/` o el registro del dibujo en `PGClientModEvents`. |
| El gusano no sale | Tiene que haber un jugador en **supervivencia** a menos de 10 bloques (en creativo no te ve). |
| El juego se cierra al aparecer un mob | Falta registrar sus atributos en `PGMobEvents.onAttributes`. |
| Demasiadas criaturas | Baja los `"weight"` en `worldgen/biome/moon.json` o `mars.json`. |

## ✅ Checklist final
- [x] Luna: crawler lunar y escorpión lunar
- [x] Marte: gusano, escorpión y crawler marcianos; alien explorador y alien soldado
- [x] Alien: criatura alienígena y depredador (huevos; aparición natural en la Fase 21)
- [x] Comportamientos: patrullar, perseguir, atacar, disparar, proteger su zona, emboscada, camuflaje, manada, huir
- [x] Aparecen solo en su planeta, con sol pero no con antorchas
- [x] Botín: quitina y tecnología alienígena; huevos de aparición
- [x] Modelos, texturas, sonidos y traducciones
- [ ] Probado en tu PC ← te toca a ti
