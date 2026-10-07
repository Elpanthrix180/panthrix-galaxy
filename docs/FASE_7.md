# FASE 7 — Energía

La fase se divide en dos partes:
- **7A — Generar y almacenar** ✅ (este documento)
- **7B — Transportar y usar** ⏳ (cables, máquinas que consumen energía, recargador de oxígeno eléctrico)

## 🎯 Objetivo de la 7A
Crear la energía del mod: producirla, guardarla y cargar con ella baterías y mochilas.

## ⚡ La unidad: PG Energía (FE)
Usamos el sistema de energía **que ya trae Forge** (FE = Forge Energy). Ventajas:
- No hace falta ninguna API externa.
- Es **compatible con otros mods** (Mekanism, Thermal...): sus cables y máquinas pueden conectarse a las nuestras.

`FE/t` = energía por tick (20 ticks = 1 segundo). 40 FE/t = 800 FE por segundo.

## 📦 Elementos creados

### Producir energía
| Bloque | ID | Produce | Combustible | Almacena | Materiales |
|---|---|---|---|---|---|
| Generador | `pg_generator` | 40 FE/t | Combustible de horno (1 carbón = 64 000 FE) | 20 000 FE | Placas reforzadas + horno (🌍) |
| Panel solar | `pg_solar_panel` | hasta 15 FE/t | Luz del sol | 10 000 FE | Cristal + selenita (🌙) |
| Reactor de helio-3 | `pg_helium_3_reactor` | 400 FE/t | Helio-3 (1 = 160 000 FE) | 200 000 FE | Marteíta, cristal marciano, lunarita + generador (🔴) |

### Guardar energía
| Elemento | ID | Capacidad | Materiales |
|---|---|---|---|
| Batería básica (objeto) | `pg_basic_battery` | 50 000 FE | Cobre, hierro, redstone (🌍) |
| Batería avanzada (objeto) | `pg_advanced_battery` | 250 000 FE | Lunarita, selenita, bloque de redstone (🌙) |
| Celda energética (bloque) | `pg_energy_cell` | 1 000 000 FE | Lunarita, selenita, bloques de redstone (🌙) |

Además, la **mochila** ya puede llenar su depósito de energía.

### ☀️ Panel solar: Sol → panel → energía → batería
- Necesita **ver el cielo** (nada encima) y que sea **de día**.
- Produce el máximo a mediodía; menos al amanecer, al atardecer y con lluvia o tormenta.
- **Sin atmósfera (Luna, espacio): produce el doble.** No hay aire que filtre la luz.

## 🎮 Cómo se usa
| Acción | Resultado |
|---|---|
| Clic derecho en el generador con **carbón/madera** (o en el reactor con **helio-3**) | Añade combustible (hasta 64) |
| Tolva apuntando al generador o reactor | Le echa combustible automáticamente |
| Generador / panel / reactor **al lado** de una celda (o de una máquina de otro mod) | Le envía la energía solo |
| Clic derecho con una **batería** en la mano | Carga la batería con la energía del bloque |
| **Mayús +** clic derecho con una batería en una **celda** | Descarga la batería en la celda |
| Clic derecho con la **mano vacía** | Muestra la energía y el estado, y **carga la mochila equipada** |
| Romper una celda | Conserva la energía (se ve en la descripción del objeto) |

El generador y el reactor **se iluminan** y cambian de textura mientras funcionan.

## 🧠 MCreator vs Java en esta fase
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Bloques con energía (Forge Energy) | ⚠️ Parcial (tiene energía básica en "tile entities") | `block/entity/PG*BlockEntity.java` |
| Panel solar según la hora y el cielo | ⚠️ Parcial (procedimientos) | `PGSolarPanelBlockEntity.calculateGeneration()` |
| Envío automático a los vecinos | ⚠️ Parcial | `PGEnergyBlockEntity.pushEnergyToNeighbors()` |
| Baterías compatibles con otros mods | ❌ No | `item/PGBatteryItem.java` + `system/energy/ItemEnergyStorage.java` |
| La celda conserva la energía al romperla | ✅ Sí (loot table) | `loot_tables/blocks/pg_energy_cell.json` (`copy_nbt`) |

**Nuevo concepto: "block entity".** Es un bloque con memoria y cerebro: guarda datos (energía,
combustible) y hace cosas 20 veces por segundo. MCreator lo llama "tile entity".

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `system/energy/PGEnergyStorage.java` | El almacén de energía de una máquina |
| `system/energy/ItemEnergyStorage.java` | La energía guardada en un objeto (batería) |
| `system/energy/EnergyHelper.java` | Cargar y descargar objetos (baterías, mochila) |
| `block/PGEnergyBlock.java` | El bloque común de todas las máquinas (encendido, forma, clic derecho) |
| `block/entity/PGEnergyBlockEntity.java` | El "cerebro" común: guardar, enviar a vecinos, clic derecho |
| `block/entity/PGGeneratorBlockEntity.java` | Generador |
| `block/entity/PGSolarPanelBlockEntity.java` | Panel solar |
| `block/entity/PGReactorBlockEntity.java` | Reactor de helio-3 |
| `block/entity/PGEnergyCellBlockEntity.java` | Celda energética |
| `item/PGBatteryItem.java` | Baterías |
| `init/ModBlockEntities.java` | Registro de los "cerebros" |

Los números (producción, capacidad...) están al principio de cada `PG*BlockEntity.java` para que puedas ajustarlos.

**Cambios en archivos existentes:** `PanthrixsGalaxy.java` (registra las block entities),
`ModBlocks`/`ModItems` (sección ENERGÍA), `ModCreativeTabs` (baterías vacías y llenas).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. **Generador:** colócalo y haz clic derecho con carbón.
   - [ ] Se enciende (textura naranja y luz).
   - [ ] Clic derecho con la mano vacía → *"Generador: X / 20000 FE"* y *"Produciendo 40 FE/t · Combustible: …"*.
   - [ ] Pon una tolva encima con carbón → el combustible entra solo.
3. **Celda:** colócala **junto** al generador → clic derecho a la celda con la mano vacía: la energía sube.
   - [ ] Rómpela y vuelve a colocarla: conserva la energía (mírala al pasar el ratón).
4. **Panel solar:** colócalo al aire libre de día → *"Produciendo 15 FE/t"* a mediodía. Ponle un bloque encima → *"Sin luz solar"*. Pruébalo de noche (`/time set night`).
5. **Baterías:** clic derecho en la celda con una batería vacía → se carga (barra amarilla). Mayús + clic derecho con una batería llena → se descarga en la celda.
6. **Mochila:** con una mochila equipada, clic derecho en la celda con la mano vacía → *"Mochila cargada"*. Ábrela con **B**: la barra de energía ha subido.
7. **Reactor:** `/give @s panthrixsgalaxy:pg_helium_3 8`, clic derecho al reactor → se enciende (verde brillante) a 400 FE/t.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El bloque es invisible | Le falta `getRenderShape` → `MODEL` (ya está en `PGEnergyBlock`). Revisa el blockstate. |
| Bloque morado y negro | El blockstate necesita las dos variantes `lit=false` y `lit=true`. |
| El generador no pasa energía a la celda | Tienen que **tocarse** (una cara con otra). Los cables llegan en la 7B. |
| El panel solar no produce | Debe ver el cielo **directamente** (sin cristal ni hojas encima) y ser de día. |
| La celda pierde la energía al romperla | Revisa `copy_nbt` en su loot table. |

## ✅ Checklist de la 7A
- [x] Energía propia compatible con Forge (FE)
- [x] Generador, panel solar y reactor de helio-3
- [x] Celda energética que conserva la energía
- [x] Batería básica y avanzada (compatibles con otros mods)
- [x] Envío automático a máquinas vecinas
- [x] Carga de baterías y del depósito de energía de la mochila
- [x] Alimentación con tolva
- [ ] Probado en tu PC ← te toca a ti

## ⏳ Qué falta (Fase 7B)
- **Cables** para llevar energía de un sitio a otro.
- **Recargador de oxígeno eléctrico**: funcionará en la Luna usando energía + agua (electrólisis), con el depósito de agua de la mochila.
- Máquinas que **consumen** energía y el uso de la energía de la mochila por parte del traje.
