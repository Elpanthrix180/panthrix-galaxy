# FASE 12 — La Luna 🌙 ✅

## 🎯 Objetivo
El primer cuerpo celeste explorable: alunizar, caminar con gravedad baja, sobrevivir sin aire,
minar sus minerales, construir y volver a casa.

## 🌙 Cómo es la Luna
| Característica | Detalle |
|---|---|
| **Superficie** | 3 bloques de **regolito lunar** sobre **piedra lunar**; colinas suaves; lecho de roca abajo |
| **Cráteres** | Por todas partes: pequeños (radio 3-6) y grandes (radio 8-12), con el borde un poco elevado |
| **Minerales** | Lunarita (Y 5-80) y selenita (Y 5-50) en la piedra; **helio-3** cerca de la superficie, en el regolito |
| **Gravedad** | **0,17**: saltas unos 5 bloques y caes despacio; las caídas hacen mucho menos daño |
| **Aire** | **Ninguno**: casco + oxígeno (bombonas, mochila o electrólisis) |
| **Temperatura extrema** | Sin las **4 piezas del traje**, ½ corazón de daño cada 2 s |
| **Cielo** | Negro con estrellas (aunque sea de día), el Sol y **la Tierra** grande en el cielo |
| **Hora** | Siempre de día (luz fija). Los **paneles solares producen el doble** (no hay atmósfera) |
| **Agua** | No hay: lleva cubos/agua en la mochila para el recargador eléctrico |
| **Criaturas y estructuras** | Llegarán en las Fases 17 y 19 |

## 🚀 Ir y volver
```
 TIERRA ── despegue ──► ESPACIO ── navega a la Luna (≈600 bloques al norte) ──► llegada a la órbita
                                                                                   │
                                                          descenso automático ◄────┘ (fundido)
                                                                   │
                                                          ALUNIZAJE en la superficie
                                                                   │
             ESPACIO (órbita lunar, destino: la Tierra) ◄── despegue (sin plataforma) ── Y 250
                     │
                     └─► baja de Y 40 → reentrada → aterrizas en tu plataforma de la Tierra
```

| Regla | Detalle |
|---|---|
| Alunizaje | Al llegar a la órbita lunar con el cohete básico (o mejor), desciendes solo y te posas donde estés |
| Despegar desde la Luna | **No hace falta plataforma** (gravedad baja). Necesita el combustible mínimo |
| Salida al Espacio | A **Y 250** (en la Tierra eran 450: hay menos que subir) |
| Al volver al Espacio | Apareces en la órbita lunar con el destino **la Tierra** ya elegido |
| Colocar un cohete en la Luna | Sobre cualquier bloque (sin plataforma) |

**Combustible para volver:** el cohete básico gasta ~860 mB en subir desde la Tierra. Para volver,
reposta en la Luna: una **refinería** + **paneles solares** + **helio-3** lunar (500 mB cada uno), o
lleva combustible extra en bidones y en el depósito de la mochila.

## 🏆 Logro: "Un pequeño paso"
Aparece una pestaña nueva de logros, **Panthrixs Galaxy**, con:
- **Panthrixs Galaxy** (raíz): se consigue al entrar en el mundo.
- **Un pequeño paso** 🎯: *"Pisa la Luna por primera vez"*.

El resto de logros (fabricación, Marte, armas, combate, secretos) llegan en la **Fase 22**, todos en esta misma pestaña.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Dimensión con terreno propio | ✅ Sí (generador de dimensiones) | JSON: `dimension/moon.json`, `worldgen/noise_settings/moon.json`, `worldgen/noise/moon_hills.json` |
| Bioma lunar | ✅ Sí | `worldgen/biome/moon.json` |
| Menas | ✅ Sí | `worldgen/configured_feature/moon_ore_*.json` + `placed_feature/` |
| **Cráteres** | ❌ No (solo estructuras fijas) | `world/feature/PGCraterFeature.java` |
| **Gravedad baja** | ⚠️ Parcial (procedimiento de velocidad, poco fiable) | `system/gravity/PGGravity.java` (atributo de gravedad de Forge) |
| Cielo con la Tierra | ❌ No | `client/sky/PGMoonEffects.java` |
| Temperatura extrema | ✅ Sí (procedimiento) | `PGOxygenEvents` + daño `extreme_temperature` |
| Logro | ✅ Sí | `advancements/root.json`, `moon_first_step.json` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `data/.../dimension/moon.json` + `dimension_type/moon.json` | La dimensión Luna (altura 0-256, siempre de día, aspecto "moon") |
| `data/.../worldgen/noise_settings/moon.json` | El terreno: piedra lunar, regolito arriba, colinas, lecho de roca |
| `data/.../worldgen/noise/moon_hills.json` | El "ruido" que da forma a las colinas |
| `data/.../worldgen/biome/moon.json` | Bioma: qué cráteres y menas tiene |
| `data/.../worldgen/configured_feature/` + `placed_feature/` | Cráteres y menas: cómo son y cuántos hay |
| `world/feature/PGCraterFeature.java` + `init/ModFeatures.java` | Cómo se excava un cráter |
| `system/gravity/PGGravity.java` | Gravedad por planeta y caídas suaves |
| `client/sky/PGMoonEffects.java` | Cielo lunar con la Tierra |
| `data/.../advancements/` | Logros |

**Cambios en archivos existentes:**
- `PGPlanet`/`PGPlanets`: la Luna tiene dimensión; nuevo dato `exitHeight` (altura de salida al Espacio) y regla de plataforma según la gravedad.
- `PGRocketEntity`: alunizaje, despegue desde la Luna sin plataforma, salida a la órbita lunar; gravedad en reposo según el planeta.
- `PGRocketItem`: en la Luna se coloca sin plataforma.
- `PGOxygenEvents`, `OxygenSyncPacket`, `ClientOxygenData`, `OxygenHudOverlay`: temperatura extrema.
- `PGSolarPanelBlockEntity`: funciona en dimensiones con hora fija (la Luna).
- `SpaceSkyRenderer`: reutilizable para el cielo desde la superficie.
- `PanthrixsGalaxy.java`: registra los features y marca la Luna sin aire.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`. **Equípate:** traje completo, casco, mochila con oxígeno/agua/energía, bombonas.
2. **Prueba rápida sin cohete:** `/execute in panthrixsgalaxy:moon run tp @s 0 120 0`
   - [ ] Cielo negro con estrellas, el Sol y la Tierra.
   - [ ] Suelo gris de regolito con **cráteres**.
   - [ ] Al caer desde 120 no mueres (gravedad baja). Salta: ¡casi 5 bloques!
   - [ ] Sin traje completo: *"⚠ Temperatura extrema"*; sin casco: aviso de oxígeno.
   - [ ] Logro **"Un pequeño paso"**.
   - [ ] Pica hacia abajo: piedra lunar con **lunarita** y **selenita**; **helio-3** cerca de la superficie.
3. **El viaje de verdad:** despega desde la Tierra con el cohete básico lleno → navega a la Luna → descenso → alunizaje.
4. **Volver:** reposta (bidones o refinería con helio-3 y paneles solares), súbete, ESPACIO (sin plataforma) → despegue → órbita lunar → baja hacia la Tierra → aterrizas en tu plataforma.
5. **Energía:** coloca un panel solar en la Luna → *"Produciendo 30 FE/t"* (el doble).

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El juego se cierra al entrar en la Luna | Algún JSON de `worldgen` tiene un error: busca `moon` en `run/logs/latest.log` (suele decir qué archivo y qué campo). |
| La Luna es plana y sin cráteres | El placed feature `moon_crater` no está en el bioma, o falta registrar `ModFeatures`. |
| El cielo de la Luna es azul | Falta registrar `PGMoonEffects` o el campo `"effects": "panthrixsgalaxy:moon"`. |
| No salto más alto | La gravedad se aplica cada segundo; espera un momento tras llegar. |
| El panel solar no produce en la Luna | Necesita ver el cielo directamente. |
| El cohete no despega en la Luna | Mira el panel: probablemente falta combustible (mínimo 1 000 mB). |

## ✅ Checklist final
- [x] Dimensión Luna con terreno, regolito, piedra lunar y lecho de roca
- [x] Cráteres
- [x] Lunarita, selenita y helio-3 generándose
- [x] Gravedad reducida y caídas suaves
- [x] Sin oxígeno + temperatura extrema (traje completo)
- [x] Cielo negro con estrellas y la Tierra visible
- [x] Alunizaje desde la órbita y despegue de vuelta (sin plataforma)
- [x] Paneles solares funcionando (el doble)
- [x] Logro "Un pequeño paso"
- [ ] Probado en tu PC ← te toca a ti
