# FASE 7 — Energía

La fase se divide en dos partes (las dos en este documento):
- **7A — Generar y almacenar** ✅
- **7B — Transportar y usar** ✅ (cables, recargador de oxígeno eléctrico, oxígeno de emergencia)

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

> *(Desde la Fase 8)* Batería avanzada, panel solar, celda, reactor y recargador eléctrico se fabrican en el **Banco de Ingeniería Espacial**.

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

---

# FASE 7B — Transportar y usar ✅

## 🎯 Objetivo
Llevar la energía de un sitio a otro con cables y darle usos: fabricar oxígeno donde no hay aire.

## 📦 Elementos creados
| Elemento | ID | Qué hace | Receta |
|---|---|---|---|
| Cable energético ×8 | `pg_energy_cable` | Une máquinas; hasta 2 000 FE/t | 6 lana + 2 cobre + 1 redstone |
| Recargador de oxígeno eléctrico | `pg_electric_oxygen_recharger` | Agua + energía → oxígeno. **Funciona en la Luna** | Placas, selenita, 2 cubos, recargador de oxígeno, batería básica |

### 🔌 Cables
- Se **conectan solos** (con un "brazo") a otros cables y a cualquier bloque con energía, también de otros mods.
- No guardan energía: la reparten en el momento entre **todas las máquinas de la red**.
- Nunca devuelven la energía a la máquina que la envía.
- **La celda energética ahora envía su energía por los cables** (solo a cables, no a máquinas pegadas).
  Así un panel solar puede cargar una celda de día y la celda alimenta las máquinas de noche.
- Si la energía sale de una celda, no se reparte a otras celdas (para que no "rebote" entre ellas).

```
[Panel solar]─cable─cable─[Celda]─cable─cable─[Recargador eléctrico]
                               (de noche, la celda alimenta al recargador)
```

### 💧 Recargador de oxígeno eléctrico (electrólisis)
| | Valor |
|---|---|
| 1 unidad de oxígeno (1 segundo respirando) | 1 mB de agua + 20 FE |
| Llenar una bombona (600) | 600 mB de agua + 12 000 FE |
| Depósito de agua | 8 000 mB (8 cubos) |
| Agua automática | 200 mB por segundo si tiene una **fuente de agua** pegada a un lado |
| Luz encendida | Tiene agua y energía: listo |

| Acción | Resultado |
|---|---|
| Clic derecho con **cubo de agua** | +1 000 mB de agua (te devuelve el cubo vacío) |
| Clic derecho con **bombona** o **mochila** en la mano | La llena de oxígeno |
| Clic derecho con la **mano vacía** | Llena el **oxígeno y el agua** de la mochila equipada |
| Clic derecho con una batería | Igual que en las demás máquinas |

El recargador normal (sin energía) sigue existiendo para la Tierra: es más barato pero no funciona sin aire.

### 🆘 Oxígeno de emergencia (la mochila usa su energía y su agua)
Si estás sin aire, con el casco puesto y **se te acaban las bombonas**, la mochila equipada fabrica
oxígeno ella sola con su agua y su energía:
- Cada segundo gasta **2 mB de agua + 50 FE** (más caro que el recargador).
- Aviso naranja: *"Oxígeno de emergencia: la mochila usa agua y energía"*.
- Con la mochila básica llena de agua y energía: unos 100 segundos extra para volver a la base.

Así **todos los depósitos de la mochila tienen uso**: oxígeno, energía y agua. El de combustible llega con los cohetes (Fase 9).

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Cable que se conecta visualmente | ⚠️ Parcial (bloques con conexiones) | `block/PGCableBlock.java` + blockstate *multipart* |
| Red de cables que reparte energía | ❌ No | `block/entity/PGCableBlockEntity.java` |
| Electrólisis (agua + energía) | ⚠️ Parcial (procedimientos) | `block/entity/PGElectricOxygenRechargerBlockEntity.java` |
| Oxígeno de emergencia de la mochila | ❌ No | `OxygenHelper.tryEmergencyElectrolysis()` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `block/PGCableBlock.java` | El bloque del cable y sus 6 brazos |
| `block/entity/PGCableBlockEntity.java` | Busca las máquinas de la red y reparte la energía |
| `block/entity/PGElectricOxygenRechargerBlockEntity.java` | Recargador eléctrico |
| `system/oxygen/Electrolysis.java` | Los "precios" de agua y energía de cada unidad de oxígeno |
| `models/block/pg_energy_cable_core.json` / `_arm.json` | Modelo del cable (centro + brazo) |
| `blockstates/pg_energy_cable.json` | Qué brazos se dibujan según las conexiones |

**Cambios en archivos existentes:**
- `PGEnergyCellBlockEntity.java`: ahora envía energía a los cables.
- `OxygenHelper.java`, `PGOxygenEvents.java`, `OxygenState.java`, `OxygenHudOverlay.java`: oxígeno de emergencia.
- `ModBlocks`, `ModBlockEntities`: los dos bloques nuevos. Mensaje del recargador normal sin aire: ahora recomienda el eléctrico.

## 🧪 Cómo probarlo
1. **Cables:** generador → 5 cables → celda. Echa carbón al generador.
   - [ ] Los cables se unen solos y forman una línea con brazos hacia las máquinas.
   - [ ] La energía de la celda sube aunque esté lejos del generador.
2. **Celda por cable:** celda (cargada) → cables → recargador eléctrico. Clic derecho al recargador con la mano vacía (sin mochila) → su energía sube.
3. **Recargador eléctrico:** dale 2 cubos de agua. Clic derecho con una bombona vacía → *"Oxígeno producido: 600"*.
   - [ ] Ponlo junto a una fuente de agua: el agua sube sola.
   - [ ] La luz se enciende cuando tiene agua y energía.
4. **En "la Luna":** de momento no existe, pero comprueba que el recargador normal da el aviso nuevo en una dimensión sin aire (se verá en la Fase 12).
5. **Emergencia:** equipa una mochila, llénala de energía (clic derecho a una celda con la mano vacía) y de agua (clic derecho al recargador eléctrico con la mano vacía). Quita las bombonas, vacía el oxígeno de la mochila (o usa una nueva), ponte el casco y `/pgvacuum true`.
   - [ ] Aviso naranja de **oxígeno de emergencia** y no recibes daño.
   - [ ] Abre la mochila con **B**: el agua y la energía bajan.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El cable no se une a una máquina | Solo se une a bloques con energía. El recargador **normal** no usa energía. |
| La energía no llega | Comprueba que los cables se toquen de verdad (se ven los brazos). Máximo 512 cables por red. |
| La celda no envía energía | Solo envía a **cables**, no a máquinas pegadas directamente. |
| El recargador eléctrico dice "Falta energía" | Necesita al menos 20 FE por unidad de oxígeno; conéctalo con cables. |
| No funciona el oxígeno de emergencia | Necesita **casco** + mochila **equipada** con al menos 2 mB de agua y 50 FE. |

## ✅ Checklist de la 7B
- [x] Cables que se conectan solos y reparten energía por la red
- [x] Celda que alimenta máquinas por cable
- [x] Recargador de oxígeno eléctrico (electrólisis) que funciona sin aire
- [x] Bomba de agua automática
- [x] Depósito de agua de la mochila con uso real
- [x] Oxígeno de emergencia con la energía y el agua de la mochila
- [ ] Probado en tu PC ← te toca a ti
