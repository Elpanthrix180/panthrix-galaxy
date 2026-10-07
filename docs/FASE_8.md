# FASE 8 — Banco de Ingeniería Espacial ✅

## 🎯 Objetivo
Una estación de fabricación propia para la tecnología avanzada, con categorías y una guía de
recetas integrada.

## 📦 Elementos creados
| Elemento | ID | Receta (mesa de trabajo normal) |
|---|---|---|
| Banco de Ingeniería Espacial | `pg_engineering_bench` | 3 paneles de cristal + 4 placas reforzadas + mesa de trabajo + bloque de redstone |

```
G G G     G = panel de cristal
P C P     P = placa reforzada   C = mesa de trabajo
P R P     R = bloque de redstone
```
Todo se consigue en la **Tierra**, así que es lo primero que fabricas antes del traje.

## 🖥️ La ventana
- **Cuadrícula 3×3 + resultado**, igual que una mesa de trabajo.
- **Guía de recetas** a la izquierda:
  1. 10 botones de categoría (pasa el ratón para ver el nombre).
  2. Las recetas de esa categoría.
  3. **Clic en una receta** → sus ingredientes aparecen **en gris** en la cuadrícula (si una casilla admite varios objetos, van cambiando).
  4. Clic otra vez para quitarla.
- Lo que dejes en la cuadrícula vuelve a tu inventario al cerrar.

### Categorías
| Categoría | Recetas ahora | Fases futuras |
|---|---|---|
| 👨‍🚀 Trajes | Casco, pechera, pantalones, botas, guantes | Trajes avanzados |
| 🎒 Mochilas | Las 4 mochilas | — |
| 🫁 Oxígeno | Bombona grande, tanque espacial | — |
| 🔋 Energía | Batería avanzada, panel solar, celda energética | — |
| ⚙️ Máquinas | Recargador de oxígeno eléctrico, reactor de helio-3 | Más máquinas |
| 🚀 Cohetes | *Próximamente* | Fase 9 |
| 🛸 Naves | *Próximamente* | Fase 14 |
| 🔫 Láseres | *Próximamente* | Fase 15 |
| ⚔️ Espadas láser | *Próximamente* | Fase 16 |
| 👽 Tecnología alienígena | *Próximamente* | Fases 17-21 |

## 🔁 Recetas que se han movido al Banco
**Ya no se pueden fabricar en la mesa de trabajo normal:**
- **Traje:** casco, pechera, pantalones, botas y guantes espaciales.
- **Mochilas:** las 4.
- **Oxígeno:** bombona grande y tanque espacial.
- **Energía:** batería avanzada, panel solar y celda energética.
- **Máquinas:** recargador de oxígeno eléctrico y reactor de helio-3.

Los dibujos de las recetas (posiciones de los ingredientes) **no cambian**, solo el sitio donde se fabrican.

**Siguen en la mesa normal:** materiales y bloques de almacenamiento, herramientas, componentes del traje
(tela, placa, visor), cables, generador, batería básica, bombona de oxígeno, recargador normal y el propio Banco.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Bloque que abre una ventana | ✅ Sí | `block/PGEngineeringBenchBlock.java` |
| Ventana con cuadrícula y resultado | ✅ Sí (GUI) | `menu/PGEngineeringBenchMenu.java` |
| **Tipo de receta propio** en JSON | ⚠️ Limitado ("recipe" de MCreator solo para tipos de Minecraft) | `recipe/PGEngineeringRecipe.java` + `init/ModRecipes.java` |
| Categorías y guía con ingredientes en gris | ❌ No | `client/screen/PGEngineeringBenchScreen.java` |

## ✍️ Cómo añadir una receta al Banco (sin tocar Java)
Crea un archivo en `src/main/resources/data/panthrixsgalaxy/recipes/` (por ejemplo `mi_receta.json`):
```json
{
  "type": "panthrixsgalaxy:engineering",
  "engineering_category": "machines",
  "pattern": [
    "PRP",
    "PGP"
  ],
  "key": {
    "P": { "item": "panthrixsgalaxy:pg_reinforced_plate" },
    "R": { "item": "minecraft:redstone" },
    "G": { "item": "panthrixsgalaxy:pg_generator" }
  },
  "result": { "item": "panthrixsgalaxy:pg_energy_cell" }
}
```
- `engineering_category`: `suits`, `backpacks`, `oxygen`, `energy`, `rockets`, `ships`, `lasers`, `laser_swords`, `machines` o `alien`.
- Lo demás es igual que una receta normal con forma de Minecraft.
- La receta aparece sola en la guía del Banco.

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `block/PGEngineeringBenchBlock.java` | El bloque (abre la ventana) |
| `menu/PGEngineeringBenchMenu.java` | Huecos, resultado, gasto de ingredientes |
| `recipe/PGEngineeringRecipe.java` | El tipo de receta del Banco |
| `recipe/EngineeringCategory.java` | Las 10 categorías y sus iconos |
| `init/ModRecipes.java` | Registro del tipo de receta |
| `client/screen/PGEngineeringBenchScreen.java` | La pantalla y la guía de recetas |
| `textures/gui/engineering_bench.png` | El fondo de la ventana |

**Cambios en archivos existentes:** las 16 recetas de la lista ahora tienen `"type": "panthrixsgalaxy:engineering"`;
`PanthrixsGalaxy.java` registra los tipos de receta; `ModMenuTypes`, `ModBlocks` y `PGClientModEvents` registran el Banco.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Fabrica el Banco en la mesa de trabajo (o cógelo de la pestaña de bloques) y colócalo.
3. Clic derecho → se abre la ventana con la **guía** a la izquierda.
   - [ ] Pasa el ratón por los iconos de categoría: salen sus nombres.
   - [ ] Categoría **Trajes** → clic en el casco → aparecen en gris placas, tela y visor en la cuadrícula.
   - [ ] Categorías Cohetes, Naves, Láseres... → *"Próximamente..."*.
4. Pon los ingredientes reales → aparece el casco en el resultado. Cógelo: los ingredientes se gastan.
   - [ ] Mayús + clic en el resultado fabrica varios de golpe.
5. Intenta fabricar el casco en una **mesa de trabajo normal** → no sale nada.
6. Deja objetos en la cuadrícula y cierra → vuelven a tu inventario.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| La guía no muestra recetas | El JSON de la receta tiene algún error: busca `engineering` en `run/logs/latest.log`. |
| La ventana se ve morada y negra | Falta `textures/gui/engineering_bench.png`. |
| Aparece el resultado pero no se puede coger | El servidor y el cliente deben tener la misma versión del mod. |
| La receta sale en la categoría "Máquinas" sin querer | Revisa que `engineering_category` esté bien escrito. |

## ✅ Checklist final
- [x] Banco de Ingeniería Espacial fabricable con materiales de la Tierra
- [x] Ventana de fabricación 3×3
- [x] Tipo de receta propio en JSON con 10 categorías
- [x] Guía de recetas con ingredientes en gris
- [x] 16 recetas avanzadas movidas al Banco
- [ ] Probado en tu PC ← te toca a ti
