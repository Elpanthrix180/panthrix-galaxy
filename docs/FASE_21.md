# FASE 21 — Planetas adicionales 🪐 ✅

## 🎯 Objetivo
Completar el Sistema Solar y llegar al primer **planeta alienígena**. Gracias al registro de planetas (Fase 11),
cada planeta es **una línea en `PGPlanets`** + sus archivos JSON: gravedad, aire, temperatura, navegación,
llegada y salida funcionan solos.

## 🗺️ El mapa del Espacio (vista desde arriba)
```
   Saturno 🪐                                        Xenoria 👽
                                 Júpiter 🟠
   Asteroides ∴∵∴                    Marte 🔴
                   Luna ⚪
                  llegada (la Tierra está debajo)
   Mercurio ⚫                                        Urano 🔵
        Venus 🟡
   Neptuno 🔵                  Plutón 🤍
```
En el Espacio, el panel de la nave muestra la Tierra y los **5 cuerpos más cercanos** (distancia y dirección).

## 🪐 Los planetas
| Planeta | Alcance | Se puede aterrizar | Gravedad | Cómo es | Recursos | Criaturas |
|---|---|---|---|---|---|---|
| **Mercurio** | 4 | ✅ | 0,38 | Roca gris quemada, cráteres, **siempre de día**, muy caliente (el agua se evapora). En el cielo, Venus brillante | **Osmio**, meteorito | Escorpiones lunares |
| **Venus** | 4 | ✅ | 0,9 | Roca volcánica amarilla, **lagos de lava**, cielo amarillo y **niebla espesa** (se ve a 48 bloques), muy caliente | **Astralita**, helio-3 | Gusanos y escorpiones marcianos |
| **Júpiter** | — | ❌ gigante gaseoso | — | Bandas y la Gran Mancha Roja | — | — |
| **Saturno** | — | ❌ gigante gaseoso | — | Con sus anillos | — | — |
| **Urano** | — | ❌ gigante gaseoso | — | Azul celeste | — | — |
| **Neptuno** | — | ❌ gigante gaseoso | — | Azul intenso | — | — |
| **Plutón** | 5 | ✅ | **0,06** | Hielo de nitrógeno (resbala), hielo azul, cráteres, **siempre de noche**. Neptuno a lo lejos | **Helio-3** (mucho) | Ninguna: silencio total |
| **Xenoria** 👽 | 5 | ✅ | 0,7 | **Planeta alienígena**: cielo morado, tierra alienígena, piedra alienígena con cristales | **Xenita**, astralita, **cristal cósmico** | Manadas de criaturas, depredadores, exploradores y soldados. 👑 **La colmena de la Reina** |

- Ninguno tiene aire respirable: traje completo y oxígeno (o una base sellada de la Fase 19).
- Alcance 4 y 5 = **nave avanzada** (Fase 14). Con la nave normal rebotas: *"no tiene alcance"*.
- Los gigantes gaseosos se ven (¡y son enormes!) pero al acercarte rebotas: *"no hay suelo donde aterrizar"*.
- El cohete ahora solo deja elegir los destinos a los que puede llegar (Tierra, Luna, Marte).

## 👑 La colmena de la Reina (Xenoria)
Una gran cúpula orgánica (1 de cada 90 chunks) con cristales cósmicos que brillan dentro. En el centro te espera
la **Reina alienígena** (la de la Fase 18, con sus 3 fases) junto a su **tesoro**: tecnología alienígena, xenita,
cristal cósmico, fragmentos de necronita... y con suerte una pistola dorada o una espada blanca.

## 🧱 Bloques nuevos
Roca de Mercurio, roca volcánica de Venus, hielo de nitrógeno (Plutón, muy resbaladizo) y tierra alienígena (Xenoria).

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| 4 dimensiones con terreno, menas, criaturas, lava, cielo de color | ✅ Sí | JSON en `data/panthrixsgalaxy/` (`dimension`, `dimension_type`, `worldgen`) |
| Planetas en el cielo del Espacio, navegación, gravedad, aire | — | Automático: una línea por planeta en `PGPlanets` |
| Cielo negro con un planeta (Mercurio, Plutón) | ❌ No | `client/sky/PGAirlessSkyEffects.java` (uno para todos) |
| Niebla de Venus | ❌ No | `PGClientEvents.onVenusFog` |
| Colmena con la Reina | ⚠️ Parcial | `world/feature/PGAlienHiveFeature.java` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `planet/PGPlanets.java` | Los 8 cuerpos nuevos: posición, alcance, gravedad, salida |
| `data/.../dimension/<planeta>.json` + `dimension_type/` | Cada dimensión (Mercurio y Venus son "muy calientes": el agua se evapora) |
| `data/.../worldgen/noise_settings/<planeta>.json` + `noise/<planeta>_hills.json` | Terreno |
| `data/.../worldgen/biome/<planeta>.json` | Colores del cielo, criaturas, menas, lava, cráteres, colmena |
| `textures/environment/<planeta>.png` | Cómo se ve cada planeta desde el Espacio |

**Cambios en archivos existentes:** `PGRocketEntity` (elige solo destinos alcanzables), `PGShipEntity` y `ShipHudOverlay`
(gigantes gaseosos; solo los 5 planetas más cercanos), `PGClientEvents`, `PGClientModEvents`, `PGAlienBeaconItem`.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Prueba rápida de cada planeta:
   `/execute in panthrixsgalaxy:mercury run tp @s 0 150 0` (cambia `mercury` por `venus`, `pluto` o `xenoria`).
   - [ ] Mercurio: gris, cráteres, siempre de día, Venus en el cielo.
   - [ ] Venus: cielo amarillo, niebla, lagos de lava.
   - [ ] Plutón: de noche, hielo resbaladizo, saltos enormes.
   - [ ] Xenoria: cielo morado, tierra morada, aliens. Busca la colmena (`/locate` no la encuentra: es una "feature"; vuela con la nave).
2. **El viaje de verdad:** nave **avanzada** llena → Espacio → elige un planeta en el panel → vuela → aterriza.
   - [ ] Acércate a Júpiter: rebotas con *"es un gigante gaseoso"*.
3. Con la nave normal intenta ir a Venus → rebotas *"no tiene alcance"*.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El juego se cierra al entrar en un planeta | Error en su JSON: busca su nombre en `run/logs/latest.log`. |
| No puedo llegar | Mercurio y Venus necesitan alcance 4, Plutón y Xenoria alcance 5: **nave avanzada**. |
| El agua desaparece en Mercurio/Venus | Es normal: hace tanto calor que se evapora. Usa el tanque de oxígeno de la Fase 19. |
| No encuentro la colmena | Es rara (1 de cada 90 chunks). Vuela bajo con la nave por Xenoria. |

## ✅ Checklist final
- [x] Mercurio, Venus, Plutón y Xenoria (planeta alienígena): terreno, recursos, criaturas, cielo
- [x] Júpiter, Saturno, Urano y Neptuno visibles desde el Espacio (gigantes gaseosos sin suelo)
- [x] Colmena de la Reina alienígena con su tesoro
- [x] Alcance 4-5: nave avanzada; el cohete solo ofrece sus destinos
- [x] Panel de la nave con los planetas más cercanos
- [ ] Probado en tu PC ← te toca a ti
