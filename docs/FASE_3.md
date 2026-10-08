# FASE 3 — Herramientas ✅

## 🎯 Objetivo
Crear herramientas espaciales con una **progresión clara**: cada planeta te da un material
que permite picar las menas del siguiente destino.

## 📦 Elementos creados (20 herramientas)
Para cada material: **espada, pico, hacha, pala y azada**.

| Material | Viene de | Nivel (de peor a mejor) | Durabilidad | Velocidad de picado | Daño espada | Encantabilidad |
|---|---|---|---|---|---|---|
| *Hierro (Minecraft)* | Tierra | entre piedra y lunarita | 250 | 6 | 6 | 14 |
| **Lunarita** | 🌙 Luna | entre hierro y diamante | 450 | 6,5 | 6,5 | 16 |
| *Diamante (Minecraft)* | Tierra | entre lunarita y marteíta | 1561 | 8 | 7 | 10 |
| **Marteíta** | 🔴 Marte | entre diamante y netherita | 1100 | 8 | 7 | 12 |
| *Netherita (Minecraft)* | Nether | entre marteíta y osmio | 2031 | 9 | 8 | 15 |
| **Osmio** | ☄️ Asteroides | por encima de netherita | 2200 | 9 | 8 | 14 |
| **Xenita** | 👽 Alienígena | el mejor | 3000 | 10 | 9 | 18 |

Extras: las herramientas de osmio tienen el nombre amarillo, las de xenita cian, y las de xenita
**no se queman en lava** (como la netherita). Todas se reparan en el yunque con su lingote.

## ⛏️ Qué pico necesita cada mena
| Pico mínimo | Menas |
|---|---|
| Piedra | Lunarita, regolito rico en helio-3, mineral marciano, hierro oxidado |
| Hierro | Selenita, meteorita |
| **Lunarita** | Marteíta, cristal marciano, metal de asteroide |
| **Marteíta** | Osmio |
| **Osmio** | Xenita, astralita |
| **Xenita** | Cristal cósmico |

Así la progresión es: 🌍 hierro → 🌙 pico de lunarita → 🔴 pico de marteíta → ☄️ pico de osmio → 👽 pico de xenita.
Un pico mejor siempre puede picar lo de los niveles inferiores (un pico de diamante también pica marteíta,
porque el diamante está por encima de la lunarita).

## 🛠️ Cómo funciona (explicación sencilla)

### ¿Qué puede hacer MCreator aquí y qué no?
MCreator crea herramientas con un "nivel de minería" que es un **número** (0 madera, 1 piedra, 2 hierro,
3 diamante, 4 netherita). Con números no se puede decir "la lunarita va **entre** hierro y diamante",
ni crear niveles por encima de la netherita que el diamante no pueda saltarse.
Forge tiene una herramienta para eso, `TierSortingRegistry`, que MCreator no usa. Por eso esta parte
lleva un poco de Java: un único archivo corto.

### Las piezas
| Pieza | Archivo | Para qué |
|---|---|---|
| Niveles | `tool/PGToolTiers.java` | Durabilidad, velocidad, daño, encantabilidad y **orden** de cada material |
| Tags de nivel | `init/ModTags.java` + `data/panthrixsgalaxy/tags/blocks/needs_*_tool.json` | Qué bloques necesita cada nivel |
| Herramientas | `init/ModItems.java` (sección HERRAMIENTAS) | Registro de las 20 herramientas |
| Modelos | `assets/.../models/item/pg_*_pickaxe.json`... | Usan `item/handheld` para que se sujeten como herramientas |
| Texturas | `assets/.../textures/item/pg_*_*.png` | Dibujos 16×16 |
| Recetas | `data/panthrixsgalaxy/recipes/pg_*_*.json` | Recetas clásicas de Minecraft con lingote + palo |
| Traducciones | `lang/es_es.json`, `lang/en_us.json` | "Pico de lunarita", etc. |

### Cambios en archivos existentes
- `PanthrixsGalaxy.java`: llama a `PGToolTiers.register()` al arrancar (los niveles deben existir antes de que Forge los ordene).
- `ModCreativeTabs.java`: nueva pestaña **Panthrixs Galaxy: Herramientas**. La pestaña principal ya no muestra herramientas.
- Tags de la Fase 2: las menas de Marte, asteroides y alienígenas ahora piden picos espaciales en lugar de picos de hierro/diamante/netherita.

## 🔨 Recetas (mesa de trabajo, `#` = lingote, `|` = palo)
```
Pico        Hacha      Pala    Azada     Espada
# # #       # #         #      # #         #
  |         # |         |        |         #
  |           |         |        |         |
```

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Creativo:
   - [ ] Nueva pestaña **Panthrixs Galaxy: Herramientas** con 20 herramientas.
   - [ ] Las herramientas se sujetan en diagonal (como un pico normal) y tienen textura.
   - [ ] Al pasar el ratón se ve el daño y la velocidad de ataque.
3. Supervivencia (`/gamemode survival`). Coloca menas en creativo antes, o usa `/give`:
   - [ ] Mena de marteíta con pico de **hierro** → no suelta nada. Con pico de **lunarita** → marteíta en bruto.
   - [ ] Mena de osmio: con pico de **diamante** no suelta nada; con pico de **marteíta**, sí.
   - [ ] Mena de xenita: con pico de **netherita** no suelta nada; con pico de **osmio**, sí.
   - [ ] Cristal cósmico solo con pico de **xenita**.
   - [ ] Un pico de lunarita **no** puede picar obsidiana (eso sigue necesitando diamante).
4. Fabricación: `/give @s panthrixsgalaxy:pg_lunarite_ingot 3` y `/give @s minecraft:stick 2` → fabrica el pico.
5. Mesa de encantamientos: encanta un pico de xenita. Tira una herramienta de xenita a la lava: no se quema.
6. Yunque: repara un pico de lunarita gastado con un lingote de lunarita.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El juego falla al arrancar con `TierSortingRegistry` en el error | Un nivel apunta a otro que no existe. Revisa las listas `List.of(...)` de `PGToolTiers.java`. |
| La herramienta se sujeta como un objeto plano | Su modelo debe usar `"parent": "minecraft:item/handheld"`. |
| Una mena se rompe pero no suelta nada con el pico correcto | La mena está en el tag de un nivel más alto. Revisa `needs_*_tool.json`. |
| En el log aparece un aviso de niveles "sin ordenar" | Algún nivel no se registró: comprueba que `PGToolTiers.register()` está en `PanthrixsGalaxy.java`. |

## ✅ Checklist final
- [x] 4 niveles de herramienta ordenados entre los de Minecraft
- [x] 20 herramientas con texturas, modelos, recetas y traducciones
- [x] Menas con pico mínimo según la progresión espacial
- [x] Reparación con el lingote correspondiente
- [x] Pestaña creativa de herramientas
- [ ] Probado en tu PC ← te toca a ti
