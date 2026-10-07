# FASE 10 — Sistema de lanzamiento ✅

## 🎯 Objetivo
Que el cohete despegue de verdad: cuenta atrás, motores encendidos, ascenso físico atravesando
las nubes hasta el límite de la atmósfera, y aterrizaje.

## 🚀 Las etapas del vuelo
```
 EN LA PLATAFORMA ──ESPACIO──► CUENTA ATRÁS (10 s) ──► ASCENSO ──Y 450──► DESCENSO CONTROLADO ──► ATERRIZAJE
                                   │ ESPACIO o bajarse        │ sin combustible
                                   ▼                          ▼
                              CANCELADO                    CAÍDA LIBRE ──golpe fuerte──► 💥 EXPLOSIÓN
```

| Etapa | Qué pasa | Qué ves |
|---|---|---|
| **Preparado** | Necesita estar **sobre la plataforma** y tener el **combustible mínimo** | Panel: *"Listo · Pulsa ESPACIO…"* (o qué falta) |
| **Cuenta atrás** | 10 s. Un pitido por segundo, cada vez más agudo. A **T-3** se encienden los motores | **T-10 … T-1** en grande (rojo los 3 últimos), humo, llamas, la cámara empieza a temblar |
| **Ascenso** | Acelera hasta su velocidad máxima gastando combustible | **¡DESPEGUE!**, nube de humo en la plataforma, columna de fuego y humo, temblor fuerte; altitud, velocidad y capa de la atmósfera |
| **Capas** | Atmósfera baja → **Atravesando las nubes** (Y≈192) → Alta atmósfera → **Límite del espacio** (Y≥320) | En el panel |
| **Descenso controlado** | Al llegar a **Y=450** (vuelo de prueba) baja solo; frena con los retropropulsores en los últimos 40 bloques | Llamas bajo el cohete cerca del suelo |
| **Aterrizaje** | Al tocar el suelo vuelve a estar en reposo | *"Aterrizaje completado"* |
| **Sin combustible** | Cae sin control. Si choca a mucha velocidad **explota** | **¡SIN COMBUSTIBLE!** parpadeando, humo |
| **Choque** | Si al subir golpea un bloque (un techo), **explota** | 💥 |

**Durante el vuelo no puedes bajarte del cohete.** En la cuenta atrás sí (y eso cancela el lanzamiento).

### Datos de cada cohete
| Cohete | Combustible máx. | Mínimo para despegar | Gasto | Velocidad máx. |
|---|---|---|---|---|
| Básico | 2 000 mB | 1 000 mB | 2 mB/tick | 20 bloques/s |
| Avanzado | 5 000 mB | 2 500 mB | 3 mB/tick | 28 bloques/s |

Con el mínimo, el cohete básico llega con margen al límite de la atmósfera (~860 mB desde el nivel del mar).
Todos estos números se cambian en `entity/rocket/RocketTier.java`.

> ℹ️ **Vuelo de prueba:** en esta fase el cohete aún no sale al espacio. Al llegar a Y=450 aparece
> *"Límite de la atmósfera alcanzado… iniciando descenso controlado"* y aterriza. En la **Fase 11**,
> en ese punto pasará al espacio.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Etapas del vuelo (máquina de estados) | ❌ No para vehículos | `entity/rocket/PGRocketEntity.java` + `LaunchState.java` |
| Tecla ESPACIO dentro del cohete | ⚠️ Parcial | `client/PGClientEvents.java` + `network/RocketLaunchPacket.java` |
| No poder bajarse en vuelo | ❌ No | `entity/rocket/PGRocketEvents.java` |
| Temblor de cámara | ❌ No | `PGClientEvents.onCameraAngles` |
| Partículas y sonidos | ✅ Sí | `PGRocketEntity.spawnEngineParticles` + sonidos de Minecraft |
| Cuenta atrás grande y panel de vuelo | ⚠️ Parcial (overlays) | `client/RocketHudOverlay.java` |

**Nuevo concepto: máquina de estados.** El cohete siempre está en una etapa (`LaunchState`) y cada tick
hace lo que toca en esa etapa. Cambiar de etapa es tan simple como `setLaunchState(...)`. Así es fácil
añadir etapas nuevas en la Fase 11 (en el espacio, navegando, descendiendo a un planeta...).

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `entity/rocket/LaunchState.java` | Las 5 etapas del vuelo |
| `entity/rocket/PGRocketEntity.java` | Lógica de cada etapa, combustible, explosión, partículas, sonidos |
| `entity/rocket/RocketTier.java` | Datos de vuelo de cada cohete (ampliado) |
| `entity/rocket/PGRocketEvents.java` | Impide bajarse en vuelo |
| `network/RocketLaunchPacket.java` | Mensaje "he pulsado ESPACIO" |
| `client/PGClientEvents.java` | Detecta ESPACIO y hace temblar la cámara |
| `client/RocketHudOverlay.java` | Panel de vuelo y textos grandes |

**Cambios en archivos existentes:** `PGRocketEntity` (reescrito, conserva todo lo de la Fase 9),
`RocketTier` (datos de vuelo), `ModEntities` (posición enviada cada tick para un vuelo suave),
`PGNetwork` (nuevo paquete), `RocketHudOverlay` (panel ampliado).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`. Mundo en **supervivencia** (para ver el temblor y el peligro reales).
2. Coloca el cohete básico en la plataforma 3×3 y dale **al menos 1 000 mB** (bidón lleno de la pestaña creativa).
3. Súbete:
   - [ ] Panel: *"Listo · Pulsa ESPACIO para iniciar la cuenta atrás"*. La barra tiene una marca blanca (el mínimo).
   - [ ] Con menos de 1 000 mB → *"Combustible insuficiente"*.
4. Pulsa **ESPACIO**:
   - [ ] T-10…T-1 en grande con pitidos. A T-3: humo, llamas y temblor.
   - [ ] Pulsa ESPACIO otra vez → *"Lanzamiento cancelado"*. Vuelve a empezar.
5. **¡Despegue!**:
   - [ ] El cohete sube acelerando, con fuego y humo; la cámara tiembla.
   - [ ] Pulsa Mayús → *"¡No puedes salir del cohete en pleno vuelo!"*.
   - [ ] Mira las capas en el panel: nubes (≈192), alta atmósfera, límite del espacio (≥320). Prueba F5 para verlo desde fuera.
6. En Y=450: mensaje de vuelo de prueba y **descenso controlado** hasta aterrizar en la plataforma.
7. **Prueba de fallo:** reposta justo 1 000 mB y lanza desde muy abajo (una cueva profunda con salida al cielo,
   o cambia `ATMOSPHERE_TOP` a un número alto) → se acaba el combustible → *¡SIN COMBUSTIBLE!* → cae y **explota**.
8. **Prueba de choque:** pon un bloque 10 bloques por encima del cohete y lanza → explota al chocar.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| ESPACIO no hace nada | Debes estar **dentro** del cohete y sin ninguna pantalla abierta. Mira el mensaje del panel. |
| "El cohete debe estar sobre la plataforma" | El bloque justo debajo del cohete debe ser una plataforma de lanzamiento. |
| El cohete vuela a saltos | Normal con conexión lenta. En un mundo local debería ir suave. |
| El cohete atraviesa un techo | No debería: si pasa, mira el log (busca `rocket`). |
| Me quedo dentro tras una explosión | No debería: la explosión expulsa al piloto primero. |

## ✅ Checklist final
- [x] Cuenta atrás de 10 s con pitidos y cancelación
- [x] Encendido de motores con humo, llamas y temblor de cámara
- [x] Despegue físico con aceleración y gasto de combustible
- [x] Atravesar las nubes y la atmósfera (capas en el panel)
- [x] Sin combustible: caída y explosión; choque al subir: explosión
- [x] No se puede bajar en vuelo
- [x] Descenso controlado y aterrizaje (base para aterrizar en planetas)
- [ ] Probado en tu PC ← te toca a ti
