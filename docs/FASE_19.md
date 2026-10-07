# FASE 19 — Bases y estaciones espaciales 🏠🛰️ ✅

## 🎯 Objetivo
Poder **vivir** en la Luna, en Marte y en el Espacio: bloques para construir bases y, lo más importante,
**salas selladas con aire** donde se respira sin casco ni traje.

## 🧱 Bloques de construcción
| Bloque | Para qué | Receta (mesa de crafteo) |
|---|---|---|
| **Panel de estación** | Paredes | 4 placas reforzadas → 4 |
| **Panel de aviso** | Esquinas y zonas de peligro (franjas amarillas) | 2 paneles + tinte amarillo + tinte negro → 2 |
| **Suelo de estación** | Suelos (rejilla) | 2 placas + 2 piedra lisa → 4 |
| **Techo de estación** | Techos | 2 placas + 2 hierro → 4 |
| **Ventana de estación** | Ventanas (cristal reforzado, hermético) | cristal + placa + cristal → 2 |
| **Luz de estación** | Luces (nivel 15: ¡no aparecen criaturas!) | polvo de piedra luminosa + redstone + placa → 2 |
| **Puerta hermética** | Puertas: **cerrada no deja escapar el aire**. Se abre con la mano | 5 placas + ventana → 2 |
| **Depósito** | Almacén de **54 huecos** (como un cofre doble) | 8 placas + cofre |

Banco de Ingeniería → nueva categoría **Estación espacial**:
| Bloque | Para qué |
|---|---|
| **Distribuidor de oxígeno** | Llena de **aire** la sala sellada en la que está |
| **Tanque de oxígeno** | Guarda 20 000 de oxígeno: reserva del distribuidor y depósito para tus bombonas |
| **Módulo habitable** | ¡Una base completa de un clic! (ver abajo) |

Los generadores, paneles solares, celdas y cables de la Fase 7 sirven para dar energía a la base.

## 💨 Salas selladas: cómo funciona el aire
1. Construye una sala **cerrada** (paredes, suelo, techo, ventanas, puertas herméticas).
2. Pon dentro (o en una pared/el techo) un **distribuidor de oxígeno** y dale **energía**.
3. Cada 2 segundos el distribuidor **mide la sala**: se "expande" por todos los huecos hasta chocar con paredes.
   - 🟢 **Cerrada** (y como mucho 2 048 bloques de aire) → **hay aire**: se respira sin casco, no hace falta el traje
     y el recargador de oxígeno normal vuelve a funcionar.
   - 🔴 **Fuga** → no hay aire. Mira (clic derecho con la mano vacía en el distribuidor) qué dice.
4. Gasto: **5 FE/t + 1 FE/t por cada 40 bloques** de sala (una sala de 75 bloques: 6 FE/t; un panel solar en la Luna da hasta 30).
5. Sin energía, gasta oxígeno de un **tanque de oxígeno pegado** al distribuidor (reserva).

**¿Qué cierra una sala?** Bloques enteros (piedra, paneles, cristal, máquinas...), puertas y trampillas **cerradas**
y paneles de cristal finos. **¿Qué NO cierra?** Agujeros, puertas abiertas, losas, escaleras, vallas, antorchas... (el aire pasa).
💡 El distribuidor puede estar **en una pared o en el techo**: da aire al lado cerrado aunque el otro lado sea el exterior.

## 🏠 Módulo habitable
Clic derecho en el suelo → construye **delante de ti** una base de 7 × 5 × 7 (dentro: 5 × 3 × 5):
- suelo, paredes con ventanas, techo con 4 luces y paneles de aviso en las esquinas;
- **puerta hermética** mirando hacia ti;
- **distribuidor de oxígeno** en el techo con un **panel solar** encima y un **tanque de oxígeno** de reserva al lado;
- un **depósito** y un **recargador de oxígeno** dentro.

En el **Espacio** (sin suelo), haz clic derecho al aire: se construye a la altura de tus pies → **¡tu primera estación espacial!**
Únelas entre sí o amplíalas con más bloques (la sala nueva debe seguir cerrada y tener un distribuidor).

⚠️ Por la noche (Marte) el panel solar no produce: llena el **tanque de reserva** (clic derecho con bombonas llenas)
o añade una celda de energía.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Paneles, suelos, techos, ventanas, luces, puertas, depósito | ✅ Sí | Bloques sencillos + JSON (modelos, recetas, botín) |
| **Salas selladas con aire** (medir la sala, fugas) | ❌ No | `system/oxygen/PGSealedRooms.java` |
| Distribuidor y tanque de oxígeno | ❌ No | `block/entity/PGOxygenDistributorBlockEntity.java`, `PGOxygenStorageBlockEntity.java` |
| Construir el módulo de un clic | ⚠️ Parcial (estructuras NBT) | `item/PGHabitatModuleItem.java` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `system/oxygen/PGSealedRooms.java` | Mide salas, decide qué cierra y dónde hay aire |
| `system/oxygen/PGAtmosphere.java` | **Cambio:** dentro de una sala sellada hay aire |
| `block/entity/PGOxygenDistributorBlockEntity.java` | El distribuidor (energía o reserva) |
| `block/PGOxygenStorageBlock.java` + `block/entity/PGOxygenStorageBlockEntity.java` | El tanque de oxígeno |
| `block/PGStorageCrateBlock.java` + `block/entity/PGStorageCrateBlockEntity.java` | El depósito |
| `item/PGHabitatModuleItem.java` | El módulo habitable |
| `data/panthrixsgalaxy/tags/blocks/airtight.json` | Bloques que cierran aunque no sean enteros (añade los tuyos) |

**Cambios en archivos existentes:** `ModBlocks`, `ModBlockEntities`, `ModItems`, `ModTags`, `PGOxygenRechargerBlock`
(funciona dentro de una base sellada), `EngineeringCategory` (+ Estación espacial), `PGEngineeringBenchScreen`
(las categorías ya ocupan 3 filas).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Ve a la Luna (`/execute in panthrixsgalaxy:moon run tp @s 0 120 0`), modo **supervivencia**, **sin casco**
   (con bombonas para no morir mientras pruebas).
2. Usa un **Módulo habitable** (creativo, pestaña principal) en el suelo.
3. Entra y **cierra la puerta**. Espera 2-4 segundos.
   - [ ] El indicador de oxígeno dice que **hay aire**. Sin casco no te ahogas. Sin traje no te quema la temperatura.
   - [ ] Clic derecho con la mano vacía en el distribuidor (techo): *"🟢 Sala sellada: 73 bloques con aire"*.
4. **Abre la puerta** → a los 2 s: sin aire. *"🔴 FUGA"*.
5. Rompe una ventana → fuga. Ponla otra vez → vuelve el aire.
6. El recargador de oxígeno de dentro llena tus bombonas.
7. Espacio: ve al Espacio con el cohete o `/execute in panthrixsgalaxy:space run tp @s 0 100 0`, usa el módulo al aire.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| Siempre dice FUGA | Busca agujeros, puertas abiertas, losas, escaleras o antorchas en las paredes. La sala debe tener como mucho 2 048 bloques de aire. |
| Dice "Sin energía" | Dale energía (panel solar, generador, cable) o llena el tanque de reserva pegado al distribuidor. |
| El módulo no se construye | Necesita un hueco libre de 7 × 6 × 7 delante de ti (sin bloques sólidos). |
| Tarda en haber aire | El distribuidor mide la sala cada 2 segundos. |
| En la Tierra el distribuidor no hace nada | Normal: aquí ya hay aire. |

## ✅ Checklist final
- [x] Paneles, panel de aviso, suelos, techos, ventanas, luces, puertas herméticas, depósito
- [x] Salas selladas con aire (fugas, tamaño máximo, puertas abiertas)
- [x] Distribuidor de oxígeno (energía o reserva) y tanque de oxígeno
- [x] Módulo habitable de un clic, también en el Espacio (estaciones espaciales)
- [x] Recargador normal dentro de las bases; luces que impiden que aparezcan criaturas
- [ ] Probado en tu PC ← te toca a ti
