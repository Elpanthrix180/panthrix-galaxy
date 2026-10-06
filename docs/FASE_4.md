# FASE 4 — Traje espacial ✅

## 🎯 Objetivo
Crear el traje espacial completo, fabricado con materiales de la **Tierra**, que será
la base de la supervivencia en el espacio. El **casco** será la pieza clave del sistema de
oxígeno (Fase 5).

## 📦 Elementos creados

### Piezas del traje
| Pieza | ID | Protección | Durabilidad |
|---|---|---|---|
| 🪖 Casco espacial | `pg_space_helmet` | 2 | 220 |
| 🧥 Pechera espacial (con guantes) | `pg_space_chestplate` | 6 | 320 |
| 👖 Pantalones espaciales | `pg_space_leggings` | 5 | 300 |
| 🥾 Botas espaciales | `pg_space_boots` | 2 | 260 |

En total protege igual que el hierro (15 puntos), con un poco más de dureza y durabilidad.
Se repara en el yunque con **placas reforzadas**.

### Componentes
| Componente | ID | Receta |
|---|---|---|
| Tela espacial ×4 | `pg_space_fabric` | 4 lana (cualquier color) + 4 cuerdas + 1 cuero |
| Placa reforzada ×2 | `pg_reinforced_plate` | 2 lingotes de hierro + 2 lingotes de cobre (en diagonal) |
| Visor del casco | `pg_helmet_visor` | 4 paneles de cristal + 2 pepitas de oro |
| Guantes espaciales | `pg_space_gloves` | 2 telas + 2 placas |

### Recetas (`F` = tela, `P` = placa, `V` = visor, `G` = guantes)
```
Casco      Pechera     Pantalones   Botas      Guantes
P F P      F . F       P F P        P . P      F . F
P V P      P G P       F . F        F . F      P . P
           F P F       F . F
```
Coste total del traje: 14 telas y 13 placas (≈ 14 hierro + 14 cobre + 16 lana).

### Comportamiento
- Al pasar el ratón: el casco indica *"Imprescindible para respirar fuera de la Tierra"*.
- Al completar las 4 piezas: mensaje verde **"Traje espacial completo: sistemas en línea"** y un pitido.
- El código ya sabe responder a dos preguntas que usarán las siguientes fases:
  `PGSpaceSuitItem.hasSpaceHelmet(jugador)` y `PGSpaceSuitItem.hasFullSpaceSuit(jugador)`.

## 🧠 MCreator vs Java en esta fase
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Armadura de 4 piezas, protección, durabilidad, textura | ✅ Sí | `armor/PGArmorMaterials.java` (equivale a la pantalla "Armor" de MCreator) |
| Descripción (tooltip) | ✅ Sí | `armor/PGSpaceSuitItem.java` |
| Aviso al completar el traje | ✅ Sí (procedimiento) | `event/PGSuitEvents.java` |
| **Guantes** | ❌ No | Minecraft solo tiene 4 ranuras de armadura. Sin añadir mods externos (como Curios) no existe ranura de manos. **Solución:** los guantes son un componente de la pechera y se **ven** en las manos del traje (la textura de los brazos tiene guantes). |
| **Mochila** | ⚠️ Parcial | Se hará en la **Fase 6** con su propio inventario y depósitos. La espalda de la pechera ya tiene las conexiones dibujadas. |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `armor/PGArmorMaterials.java` | Protección, durabilidad, encantabilidad, sonido y reparación del traje |
| `armor/PGSpaceSuitItem.java` | Pieza del traje + comprobaciones `hasSpaceHelmet` / `hasFullSpaceSuit` |
| `event/PGSuitEvents.java` | Aviso al completar el traje |
| `init/ModItems.java` (sección TRAJE ESPACIAL) | Registro de piezas y componentes |
| `textures/models/armor/space_suit_layer_1.png` | Cómo se ve puesto: casco, pechera con guantes y botas |
| `textures/models/armor/space_suit_layer_2.png` | Cómo se ven puestos los pantalones |
| `textures/item/pg_space_*.png` | Iconos del inventario |
| `data/panthrixsgalaxy/recipes/pg_space_*.json` | Recetas |

**Cambio en un archivo existente:** la pestaña creativa "Herramientas" ahora se llama
**"Panthrixs Galaxy: Equipo"** e incluye herramientas y trajes (`ModCreativeTabs.java`).

## 🖼️ Editar el aspecto del traje
Las texturas de armadura (64×32) siguen el mismo esquema que una skin de Minecraft.
Puedes abrir `space_suit_layer_1.png` en Blockbench o en un editor de píxeles y pintarla encima.
Mantén el tamaño 64×32 y el nombre del archivo.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Creativo, pestaña **Panthrixs Galaxy: Equipo**:
   - [ ] Están las 4 piezas con su icono.
   - [ ] Pestaña principal: tela, placa, visor y guantes.
3. Ponte las piezas una por una:
   - [ ] Se ven sobre el jugador (pulsa **F5** para verte): casco blanco con visor azul, guantes oscuros, botas oscuras, rodilleras naranjas.
   - [ ] Al ponerte la cuarta pieza aparece **"Traje espacial completo: sistemas en línea"** y suena un pitido.
   - [ ] La barra de armadura muestra 7,5 corazas (15 puntos).
4. Supervivencia: fabrica el traje desde cero con `/give @s minecraft:iron_ingot 14`, `copper_ingot 14`,
   `white_wool 16`, `string 16`, `leather 4`, `glass_pane 4`, `gold_nugget 2`.
5. Yunque: repara una pieza dañada con una placa reforzada.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El traje puesto se ve morado y negro | El nombre del material en `PGArmorMaterials` (`space_suit`) no coincide con `space_suit_layer_1.png` / `_layer_2.png`, o la carpeta no es `textures/models/armor/`. |
| Los pantalones se ven con la textura de la pechera | Los pantalones usan `layer_2`; revisa que exista. |
| No aparece el mensaje al completar el traje | Mira que `PGSuitEvents.java` tenga `@Mod.EventBusSubscriber`. El mensaje solo sale al ponerte una pieza, no al cargar el mundo. |
| La receta de la tela no funciona | Usa lana de cualquier color, cuero en el centro y cuerdas en las esquinas. |

## ✅ Checklist final
- [x] Casco, pechera, pantalones y botas con protección, durabilidad y reparación
- [x] Guantes como componente visible en el traje (con la limitación explicada)
- [x] Componentes fabricables con materiales de la Tierra
- [x] Texturas de inventario y texturas puestas
- [x] Aviso al completar el traje
- [x] Comprobaciones listas para el oxígeno (Fase 5)
- [ ] Probado en tu PC ← te toca a ti
