# FASE 14 — Naves espaciales 🛸 ✅

## 🎯 Objetivo
Un vehículo que **se pilota libremente**: despega en vertical desde cualquier sitio (sin plataforma), vuela por el planeta,
sale al Espacio, navega hasta otro planeta y aterriza donde tú quieras. El cohete te lleva; la nave **la conduces tú**.

## 🔁 Qué comparten la nave y el cohete
Para no duplicar código, los dos son **vehículos espaciales** (`entity/PGSpaceVehicle.java`). Todo lo que ya funcionaba
con el cohete funciona igual con la nave:

| Sistema | Cómo lo usa la nave |
|---|---|
| Cielo del Espacio con planetas (Fase 11) | Igual que en el cohete |
| Oxígeno (Fase 4) | Cabina **presurizada mientras tenga energía**. Sin energía, necesitas el casco |
| No poder bajarse en pleno vuelo | Igual que en el cohete |
| Tecla **ESPACIO** | Cohete: cuenta atrás. Nave: **encender / apagar motores** |
| Registro de planetas (`PGPlanets`) | Alturas de salida, distancias, alcance... los mismos datos |
| Combustible (refinería, bidón, mochila) | El mismo combustible |

## 🛸 Las dos naves
| | **Nave espacial** | **Nave avanzada** |
|---|---|---|
| Alcance | Luna, Marte y asteroides | Planetas exteriores y alienígenas (Fases 21-23) |
| Combustible | 8 000 mB | 20 000 mB |
| Energía (soporte vital) | 100 000 FE | 400 000 FE |
| Casco | 100 | 200 |
| Bodega | 27 huecos | 54 huecos |
| Velocidad máx. (planeta / Espacio) | 20 / 50 m/s | 28 / 80 m/s |

## 🎮 Controles (dentro de la nave)
| Tecla | Qué hace |
|---|---|
| **ESPACIO** | Encender / apagar motores |
| **W** | Empujar hacia **donde miras** (mira arriba para subir, abajo para bajar) |
| **S** | Frenar |
| Nada | Con motores encendidos, la nave **flota quieta** |
| Motores apagados | La nave cae con la gravedad del planeta (en la Luna, despacito) |
| **Mayús** | Bajarse (solo en tierra) |

## ⛽ Combustible, energía y casco
- **Combustible:** empujar gasta 1 mB por tick; mantenerse en el aire, 1 mB cada 2 ticks. En el Espacio, flotar es gratis.
  Se carga con el **bidón** (clic derecho en la nave) o al subir con la **mochila** (pasa su combustible).
- **Energía:** el soporte vital gasta 1 FE por tick con los motores encendidos. Con los motores apagados,
  el **panel solar** de la nave recarga 5 FE por tick (al sol o en el Espacio). También puedes darle una **batería** (clic derecho).
- **Casco:** los **choques fuertes** (a más de ~9 m/s) y los golpes lo dañan. A 0... **¡la nave explota!**
  Repáralo con clic derecho: **placa reforzada** (+10) o **lingote de metal de asteroide** (+30).

## 📦 Panel de control
**Mayús + clic derecho con la mano vacía** (estando fuera de la nave): se abre la **bodega** y, al lado,
las barras de combustible, energía y casco y el botón **"Recoger nave"**. Al recogerla, el objeto guarda
**todo**: combustible, energía, casco y la carga de la bodega.

## 🚀 El viaje
```
 PLANETA ── motores ON, W mirando arriba ──► altura de salida (Tierra Y 450, Luna Y 250, Marte Y 350) ──► ESPACIO
                                                                                                          │
   El panel muestra cada planeta: distancia y "◄ gira / ▲ en rumbo / gira ►". Vuela hacia él.  ◄─────────┘
                                                                                                          │
   Llegas flotando 30 bloques por debajo de la altura de salida, con motores encendidos ◄─────────────────┘
   → bajas tú (mirando abajo + W) y aterrizas DONDE QUIERAS.
 Para volver a la Tierra desde el Espacio: baja por debajo de Y 40.
```
- Si la nave no tiene alcance para un planeta, **rebota** y te avisa.
- Si se acaba el combustible en el Espacio, la nave cae poco a poco hacia la Tierra (igual que el cohete).

## 🛠️ Cómo fabricarla (Banco de Ingeniería → categoría **Naves**)
| Pieza | Receta |
|---|---|
| **Casco de nave** | 4 lingotes de marteíta + 4 placas reforzadas + casco de cohete en el centro |
| **Motor iónico** | 4 marteíta + cristal marciano + 2 placas + motor de cohete |
| **Cabina de nave** | 5 cristales + cristal marciano + 2 placas + batería básica |
| **Nave espacial** | cabina + 2 cascos de nave + depósito de cohete + 2 motores iónicos + 2 placas |
| **Nave avanzada** | nave espacial + 2 motores iónicos + metal de asteroide + osmio + batería avanzada |

Se necesitan materiales de **Marte**: la nave es la recompensa por haber llegado con el cohete avanzado.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Objetos, recetas, texturas, traducciones | ✅ Sí | JSON y PNG en `resources/` |
| Entidad pilotable con física libre (empuje, inercia, flotar) | ❌ No | `entity/ship/PGShipEntity.java` |
| Cambio de dimensión en vuelo con el piloto dentro | ❌ No | `PGShipEntity.travelTo` |
| Bodega + panel con barras y botón | ⚠️ Parcial | `menu/PGShipMenu.java`, `client/screen/PGShipScreen.java` |
| Panel de vuelo (HUD) | ⚠️ Parcial | `client/ShipHudOverlay.java` |
| Modelo 3D | ✅ Sí (Blockbench) | `client/model/PGShipModel.java` (editable en Blockbench, "Modded Entity") |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `entity/PGSpaceVehicle.java` | Lo que comparten cohete y nave |
| `entity/ship/ShipTier.java` | Los datos de cada nave (tabla de arriba) |
| `entity/ship/PGShipEntity.java` | Vuelo, combustible, energía, casco, viajes, bodega |
| `item/PGShipItem.java` | Colocar la nave en el suelo |
| `menu/PGShipMenu.java` + `client/screen/PGShipScreen.java` | Panel de control y bodega |
| `network/ShipActionPacket.java` | Botón "Recoger nave" |
| `client/model/PGShipModel.java` + `client/renderer/PGShipRenderer.java` | Modelo 3D (se inclina al avanzar) |
| `client/ShipHudOverlay.java` | Panel de vuelo |
| `textures/entity/ship/ship.png`, `advanced.png` | Texturas de las naves |

**Cambios en archivos existentes:**
- `PGRocketEntity`: ahora es un `PGSpaceVehicle`.
- `PGAtmosphere`, `PGRocketEvents`, `SpaceSkyRenderer`, `PGClientEvents`: usan `PGSpaceVehicle` (valen para los dos).
- `RocketLaunchPacket`: la tecla ESPACIO también enciende los motores de la nave.
- `ModEntities`, `ModItems`, `ModMenuTypes`, `PGNetwork`, `ModCreativeTabs`, `PGClientModEvents`: registros nuevos.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`. Pestaña **Equipo** del creativo → **Nave espacial**.
2. Colócala en el suelo (necesita 3×3 libres). Llénala: bidón lleno → clic derecho. Batería → clic derecho.
   - [ ] Al pasar el ratón por el objeto se ven combustible, energía, casco y bodega.
3. Súbete (clic derecho) → **ESPACIO** → *"Motores encendidos"*.
   - [ ] Mira arriba + **W**: sube. Suelta: flota. **S**: frena. Mira abajo + W: baja.
   - [ ] El panel de arriba muestra las tres barras, altitud, velocidad y la altura del Espacio.
   - [ ] **F5**: ves la nave inclinarse al avanzar y las llamas azules de los motores.
4. Choca a toda velocidad contra una pared: el casco baja. Repara con una placa reforzada.
5. Aterriza, bájate (Mayús) → **Mayús + clic derecho** con la mano vacía: bodega + barras. Mete cosas y pulsa **"Recoger nave"**.
   - [ ] Al volver a colocarla conserva combustible, energía, casco y carga.
6. **Viaje:** sube hasta Y 450 → Espacio. Busca la Luna en el panel, gira hasta *"▲ en rumbo"*, W. Entrarás flotando sobre la Luna: aterriza donde quieras.
7. Apaga la energía (`/data merge entity @e[type=panthrixsgalaxy:ship,limit=1,sort=nearest] {Energy:0}`) en el Espacio, con los motores encendidos y sin casco:
   - [ ] Aviso de oxígeno: la cabina ya no tiene aire.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| "No hay sitio" al colocarla | Necesita 3×3 bloques libres y 2 de alto encima del bloque donde haces clic. |
| ESPACIO no hace nada | Sin combustible no arrancan los motores. Mira el mensaje. |
| La nave no sube | Mira **hacia arriba**: W empuja hacia donde miras. |
| Rebota cerca de un planeta | Fuera de alcance: necesitas la nave avanzada (Fases 21-23). |
| No puedo bajarme | En el aire o en el Espacio no se puede: aterriza primero. |
| Se abre la bodega en vez de subirme | Estabas pulsando **Mayús**. |
| La nave explotó | Integridad 0 por choques. ¡Aterriza despacio! Se pierde la nave (la carga cae al suelo). |

## ✅ Checklist final
- [x] Vehículo compartido `PGSpaceVehicle` (cohete y nave)
- [x] Nave espacial y nave avanzada (alcance, combustible, energía, casco, bodega)
- [x] Vuelo libre: motores, empuje, freno, flotar, gravedad
- [x] Despegue sin plataforma y aterrizaje donde quieras
- [x] Salida al Espacio, navegación a planetas, vuelta a la Tierra
- [x] Soporte vital (energía) y panel solar
- [x] Daño del casco, reparación y explosión
- [x] Bodega, panel de control y "Recoger nave" (guarda todo en el objeto)
- [x] Panel de vuelo (HUD), modelo 3D, texturas e iconos
- [x] Recetas en el Banco de Ingeniería (categoría Naves)
- [ ] Probado en tu PC ← te toca a ti
