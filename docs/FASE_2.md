# FASE 2 — Materiales y minerales ✅

## 🎯 Objetivo
Crear todos los materiales del mod: piedras de cada cuerpo celeste, menas, materiales en bruto,
lingotes, cristales y bloques de almacenamiento. Cada uno con textura, nombre, herramienta correcta,
drops y recetas de horno.

> ℹ️ **Todavía no aparecen de forma natural en el mundo.** Las menas lunares se generarán en la Luna
> (Fase 12), las marcianas en Marte (Fase 13), las de asteroide en los asteroides (Fase 20) y las
> alienígenas en los planetas alienígenas (Fase 21). Ponerlas ahora en la Tierra rompería la progresión.
> Por eso, en esta fase se prueban en **modo creativo**.

## 📦 Elementos creados (25 bloques + 20 objetos)

### 🌙 Luna
| Bloque / objeto | ID | Se obtiene | Uso previsto |
|---|---|---|---|
| Regolito lunar | `pg_lunar_regolith` | Superficie lunar (pala) | Construcción de bases lunares, hormigón lunar |
| Piedra lunar | `pg_lunar_stone` | Subsuelo lunar (pico) | Construcción; roca donde están las menas |
| Mena de lunarita → Lunarita en bruto | `pg_lunarite_ore` → `pg_raw_lunarite` | Pico de piedra+ | Fundir en lingote |
| Lingote de lunarita | `pg_lunarite_ingot` | Horno | **Cohete avanzado** (Tierra → Marte), mejoras del traje, herramientas de lunarita |
| Mena de selenita → Cristal de selenita | `pg_selenite_ore` → `pg_selenite_crystal` | Pico de hierro+ | **Paneles solares** y circuitos |
| Regolito rico en helio-3 → Helio-3 | `pg_helium_3_ore` → `pg_helium_3` | Pico de piedra+ | **Combustible** del cohete avanzado y del reactor |
| Bloque de lunarita | `pg_lunarite_block` | 9 lingotes | Almacenamiento / decoración |

### 🔴 Marte
| Bloque / objeto | ID | Se obtiene | Uso previsto |
|---|---|---|---|
| Suelo marciano | `pg_martian_soil` | Superficie (pala) | Construcción de colonias |
| Piedra marciana | `pg_martian_stone` | Subsuelo (pico) | Construcción; roca de las menas |
| Mena mineral marciana → Mineral marciano | `pg_martian_ore` → `pg_martian_mineral` | Pico de piedra+ | **Cerámica térmica** para motores |
| Mena de marteíta → Marteíta en bruto | `pg_martianite_ore` → `pg_raw_martianite` | Pico de lunarita+ | Fundir en lingote |
| Lingote de marteíta | `pg_martianite_ingot` | Horno | **Nave espacial**, herramientas de marteíta |
| Mena de cristal marciano → Cristal marciano | `pg_martian_crystal_ore` → `pg_martian_crystal` | Pico de lunarita+ | **Lentes de los láseres**, baterías avanzadas |
| Mena de hierro oxidado → Hierro oxidado en bruto | `pg_rusted_iron_ore` → `pg_raw_rusted_iron` | Pico de piedra+ | Se funde en **hierro normal** (sobrevivir en Marte); más adelante, **extraer oxígeno** |
| Bloque de marteíta | `pg_martianite_block` | 9 lingotes | Almacenamiento |

### ☄️ Asteroides
| Bloque / objeto | ID | Se obtiene | Uso previsto |
|---|---|---|---|
| Roca de asteroide | `pg_asteroid_rock` | Asteroides (pico) | Construcción; roca de las menas |
| Mena de meteorita → Fragmento de meteorita | `pg_meteorite_ore` → `pg_meteorite_fragment` | Pico de hierro+ | **Escudos térmicos** para entrar en atmósferas |
| Mena de metal de asteroide → Metal en bruto → Lingote | `pg_asteroid_metal_ore` → `pg_raw_asteroid_metal` → `pg_asteroid_metal_ingot` | Pico de lunarita+ | Casco de la **nave avanzada** |
| Mena de osmio → Osmio en bruto → Lingote | `pg_osmium_ore` → `pg_raw_osmium` → `pg_osmium_ingot` | Pico de marteíta+ | Blindaje avanzado, reactor, **lingote de necronita** |
| Bloques de metal de asteroide y de osmio | `pg_asteroid_metal_block`, `pg_osmium_block` | 9 lingotes | Almacenamiento |

### 👽 Alienígena
| Bloque / objeto | ID | Se obtiene | Uso previsto |
|---|---|---|---|
| Piedra alienígena | `pg_alien_stone` | Planetas alienígenas (pico) | Construcción; roca de las menas |
| Mena de xenita → Xenita en bruto → Lingote | `pg_xenite_ore` → `pg_raw_xenite` → `pg_xenite_ingot` | Pico de osmio+ | **Circuitos alienígenas**, nave avanzada |
| Mena de astralita → Cristal de astralita | `pg_astralite_ore` → `pg_astralite_crystal` | Pico de osmio+ | Celdas energéticas, navegación interestelar |
| Mena de cristal cósmico → Cristal cósmico | `pg_cosmic_crystal_ore` → `pg_cosmic_crystal` | Pico de **xenita** | **Láser blanco**, mochila experimental |
| Fragmento de necronita | `pg_necronite_shard` | Drop de la **Reina Alienígena** (Fase 18) | Lingote de necronita |
| Lingote de necronita | `pg_necronite_ingot` | 4 fragmentos + 4 lingotes de osmio | **Espada láser negra**, tecnología prohibida |
| Bloque de xenita / de necronita | `pg_xenite_block`, `pg_necronite_block` | 9 lingotes | Almacenamiento |

Ningún material es solo decorativo: todos tienen un uso en fases posteriores.

> 🔧 **Corrección (Fase 3):** el **cohete básico** (Tierra → Luna) se fabricará con materiales de la
> **Tierra** (hierro, cobre, redstone...), porque la lunarita solo existe en la Luna. La lunarita pasa al
> cohete avanzado. Los niveles de pico de las menas también cambiaron: ver `docs/FASE_3.md`.

## 🛠️ Cómo funciona (explicación sencilla)
Para que un bloque exista en Minecraft hacen falta **6 piezas**. Ejemplo con la mena de lunarita:

| Pieza | Archivo | Para qué |
|---|---|---|
| 1. Registro Java | `init/ModBlocks.java` → `PG_LUNARITE_ORE` | Le dice al juego que el bloque existe, su dureza y su sonido |
| 2. Blockstate | `assets/.../blockstates/pg_lunarite_ore.json` | Qué modelo usar |
| 3. Modelo | `assets/.../models/block/` y `models/item/` | Forma (cubo) y qué textura lleva |
| 4. Textura | `assets/.../textures/block/pg_lunarite_ore.png` | El dibujo de 16×16 |
| 5. Loot table | `data/panthrixsgalaxy/loot_tables/blocks/pg_lunarite_ore.json` | Qué suelta (con toque de seda, la mena; si no, el mineral en bruto; Fortuna da más) |
| 6. Traducción | `assets/.../lang/es_es.json` | El nombre en el juego |

Y además los **tags** (`data/minecraft/tags/blocks/`):
- `mineable/pickaxe.json` / `mineable/shovel.json` → con qué herramienta se rompe rápido.
- `needs_stone_tool.json`, `needs_iron_tool.json`, `needs_diamond_tool.json` → qué pico mínimo hace falta.
- *(Desde la Fase 3)* `data/panthrixsgalaxy/tags/blocks/needs_lunarite_tool.json` y similares → menas que
  necesitan picos espaciales. Mira `docs/FASE_3.md`.

Los objetos solo necesitan: registro (`init/ModItems.java`), modelo, textura y traducción.

**Cambio respecto a la Fase 1:** antes había que registrar cada bloque **dos veces** (en `ModBlocks`
y su objeto en `ModItems`). Ahora `ModBlocks.registerBlock(...)` crea el objeto del bloque
automáticamente, así es imposible olvidarlo. El bloque de prueba sigue funcionando igual.
También hay una **segunda pestaña creativa** "Panthrixs Galaxy: Bloques"; la primera tiene los objetos.

## 🔥 Recetas
| Receta | Dónde |
|---|---|
| Material en bruto o mena → lingote | Horno (10 s) y alto horno (5 s) |
| Hierro oxidado en bruto → lingote de hierro | Horno / alto horno |
| 9 lingotes ↔ 1 bloque | Mesa de trabajo |
| 4 fragmentos de necronita + 4 lingotes de osmio → lingote de necronita | Mesa de trabajo (sin forma) |

## 🖼️ Texturas
Todas en `assets/panthrixsgalaxy/textures/` (16×16, estilo Minecraft). Son una **primera versión**:
puedes sustituir cualquier `.png` por uno tuyo con el mismo nombre y el juego lo usará sin tocar código.
Recomendado: [Blockbench](https://www.blockbench.net/) o cualquier editor de píxeles.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Mundo en **creativo**:
   - [ ] Pestaña **Panthrixs Galaxy** con 21 objetos (prueba + 20 materiales).
   - [ ] Pestaña **Panthrixs Galaxy: Bloques** con 26 bloques.
   - [ ] Ningún objeto/bloque se ve morado y negro y todos tienen nombre en español.
   - [ ] Los nombres raros tienen color: helio-3 y osmio amarillo, cristal cósmico cian, necronita morado.
3. Cambia a **supervivencia** (`/gamemode survival`), coloca menas y rómpelas:
   - [ ] Mena de lunarita con pico de **madera** → no suelta nada. Con pico de **piedra** → lunarita en bruto.
   - [ ] *(Actualizado en la Fase 3)* Mena de osmio necesita pico de **marteíta**; cristal cósmico, de **xenita**.
   - [ ] Las menas de cristal sueltan experiencia.
   - [ ] Regolito y suelo marciano se recogen con la mano o la pala.
4. Horno: funde lunarita en bruto → lingote. Funde hierro oxidado → lingote de hierro.
5. Mesa de trabajo: 9 lingotes → bloque, y al revés.
6. Prueba rápida con comandos:
   `/give @s panthrixsgalaxy:pg_necronite_shard 4` y `/give @s panthrixsgalaxy:pg_osmium_ingot 4` → fabrica el lingote de necronita.
   Tira el lingote de necronita a la lava: no debe quemarse.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| Bloque morado y negro | El nombre del `.png` no coincide con el del modelo, o falta el blockstate. |
| Al romper la mena no suelta nada | Revisa que el pico sea del nivel correcto; si sigue igual, la loot table tiene un error (mira `latest.log`, busca `loot`). |
| Se rompe lentísimo con pico | Falta el bloque en `mineable/pickaxe.json`. |
| La receta no aparece | Error en el JSON de la receta: busca `recipe` y `panthrixsgalaxy` en `run/logs/latest.log`. |
| Nombre `block.panthrixsgalaxy...` | Falta la línea en `es_es.json`. |

## ✅ Checklist final
- [x] 6 piedras base / superficies (Luna, Marte, asteroides, alienígena)
- [x] 13 menas con drops, Fortuna, toque de seda y experiencia
- [x] 20 materiales (en bruto, lingotes, cristales, helio-3, necronita)
- [x] 6 bloques de almacenamiento
- [x] Niveles de herramienta (piedra, hierro, diamante, netherita)
- [x] Recetas de horno, alto horno y almacenamiento
- [x] Traducciones ES / EN y texturas 16×16
- [ ] Probado en tu PC ← te toca a ti
