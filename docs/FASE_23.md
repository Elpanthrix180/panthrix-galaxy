# FASE 23 — Optimización y configuración ⚙️ ✅

## 🎯 Objetivo
Que el mod vaya fluido aunque tengas una base enorme llena de cables y máquinas, y poder **ajustar el juego
sin tocar el código** con un archivo de configuración.

## 🚀 Optimizaciones
| Qué | Antes | Ahora | Ganancia |
|---|---|---|---|
| **Redes de cables** (Fase 7) | Cada vez que entraba energía en un cable (varias veces por tick y por máquina), recorría **toda la red** buscando máquinas | Recuerda la lista de máquinas. Solo la vuelve a buscar si pones/quitas un cable o algo pegado a uno, o cada 5 s por seguridad | **Lo más importante**: una red de 100 cables con 10 generadores pasaba de ~2 000 recorridos por segundo a casi ninguno |
| **Distribuidor de oxígeno** (Fase 19) | Medía su sala (hasta 2 048 bloques) cada 2 s aunque no hubiera nadie | Si no hay ningún jugador a menos de 96 bloques, **descansa** (ni mide ni gasta energía) | Bases y estaciones abandonadas ya no cuestan nada |
| **Estrellas del cielo** (Fase 11) | Las 2 000 estrellas se calculaban de nuevo en **cada fotograma** | Se preparan **una vez** en la tarjeta gráfica y solo se dibujan (como hace Minecraft con sus estrellas) | Más FPS en el Espacio, la Luna, Mercurio, Plutón... |
| **Indicador de oxígeno** (Fase 4) | El servidor enviaba un mensaje a cada jugador cada segundo | Solo se envía cuando algo cambia (o cada 5 s por si acaso) | Menos tráfico de red en multijugador |
| **Combustible de la nave** | — | Admite multiplicadores con decimales sin perder precisión | — |

Lo que ya estaba bien hecho desde el principio y no hacía falta tocar: los rayos láser no se guardan en el mundo,
las criaturas desaparecen lejos de los jugadores, los cohetes/naves envían su posición cada tick solo a quien los ve,
y la generación de asteroides y estructuras es por chunk (no recorre el mundo entero).

## 🔧 Configuración
Al arrancar el juego por primera vez, Forge crea dos archivos en la carpeta **`config/`** (junto a `mods/`).
Ábrelos con el Bloc de notas, cambia un valor, guarda y **reinicia el mundo**.

### `panthrixsgalaxy-common.toml` — reglas del juego
| Sección | Opción | Por defecto | Para qué |
|---|---|---|---|
| `[oxygen]` | `oxygenPerSecond` | 1 | Oxígeno que gasta el traje por segundo (0 = nunca se gasta) |
| | `graceSeconds` | 5 | Segundos sin aire antes de recibir daño |
| | `temperatureDamage` | true | Daño por temperatura sin el traje completo |
| `[station]` | `maxRoomVolume` | 2048 | Tamaño máximo de una sala sellada |
| | `distributorBaseCost` | 5 | FE/t fijos del distribuidor |
| | `distributorBlocksPerFe` | 40 | +1 FE/t por cada tantos bloques de sala |
| `[planets]` | `marsDustStorms` | true | Tormentas de polvo en Marte |
| `[gameplay]` | `laserDamageMultiplier` | 1.0 | Daño de todos los láseres |
| | `bossHealthMultiplier` | 1.0 | Vida de los jefes (pon 2.0 o 3.0 si jugáis varios) |
| | `shipFuelMultiplier` | 1.0 | Combustible que gasta la nave (0.5 = la mitad) |
| `[performance]` | `cableNetworkRefreshTicks` | 100 | Cada cuánto se revisan las redes de cables aunque no cambien |
| | `distributorPlayerRange` | 96 | Distancia a la que un jugador "despierta" un distribuidor |

### `panthrixsgalaxy-client.toml` — tu pantalla
| Opción | Por defecto | Para qué |
|---|---|---|
| `showOxygenHud` | true | Indicador de oxígeno |
| `showVehicleHud` | true | Panel del cohete y de la nave |
| `cameraShake` | true | Temblor de la cámara al despegar |
| `stormParticles` | 14 | Partículas de polvo en Marte (0 = ninguna; bájalo si tu PC va lento) |
| `venusFog` | true | Niebla espesa de Venus |

⚠️ En multijugador manda el archivo `common` del **servidor**. Las tormentas de Marte se calculan también en tu
pantalla: si el servidor las desactiva, desactívalas también en tu `common` para no ver polvo "fantasma".

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Archivo de configuración | ⚠️ Parcial (plugin "Config") | `config/PGConfig.java` (`ForgeConfigSpec` de Forge) |
| Optimizaciones | ❌ No | Cambios pequeños en las clases afectadas |

## 🗂️ Archivos cambiados
| Archivo | Cambio |
|---|---|
| `config/PGConfig.java` | **Nuevo:** todas las opciones |
| `PanthrixsGalaxy.java` | Registra los dos archivos de configuración |
| `block/entity/PGCableBlockEntity.java` + `block/PGCableBlock.java` | Caché de la red de cables |
| `block/entity/PGOxygenDistributorBlockEntity.java` | Descansa sin jugadores cerca; coste configurable |
| `client/sky/SpaceSkyRenderer.java` | Estrellas en la tarjeta gráfica |
| `system/oxygen/PGOxygenEvents.java` | Oxígeno configurable; envía solo cambios |
| `system/oxygen/PGSealedRooms.java` | Tamaño máximo configurable |
| `system/weather/PGMarsWeather.java`, `entity/laser/PGLaserBoltEntity.java`, `entity/ship/PGShipEntity.java`, `entity/boss/PGAlienQueenEntity.java` | Opciones de juego |
| `client/OxygenHudOverlay.java`, `RocketHudOverlay.java`, `ShipHudOverlay.java`, `PGClientEvents.java` | Opciones de pantalla |

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`, crea un mundo y sal. Abre `run/config/panthrixsgalaxy-common.toml`.
2. Pon `oxygenPerSecond = 0`, guarda, entra al mundo, ve a la Luna → el oxígeno no baja.
3. Pon `bossHealthMultiplier = 2.0` → invoca a la Reina → su barra tarda el doble en bajar.
4. Red de cables grande (10 paneles solares + 50 cables + máquinas): pulsa **F3** y compara los FPS/TPS con antes.
5. `run/config/panthrixsgalaxy-client.toml`: `showVehicleHud = false` → sin panel en el cohete.

## ✅ Checklist final
- [x] Redes de cables sin recorridos repetidos
- [x] Distribuidores que descansan sin jugadores
- [x] Estrellas preparadas una sola vez
- [x] Menos mensajes de red (oxígeno)
- [x] Archivo de configuración común (reglas) y de cliente (pantalla)
- [ ] Probado en tu PC ← te toca a ti
