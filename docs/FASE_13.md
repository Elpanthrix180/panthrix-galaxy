# FASE 13 — Marte 🔴 ✅

## 🎯 Objetivo
El segundo gran planeta: más lejos, más difícil y con tormentas de polvo. Necesitas el **cohete avanzado**.

## 🔴 Cómo es Marte
| Característica | Detalle |
|---|---|
| **Superficie** | 2 bloques de **suelo marciano** (rojo) sobre **piedra marciana**; lecho de roca abajo |
| **Relieve** | Colinas (±25 bloques) y **montañas** que suben hasta ~50 bloques por encima de la llanura |
| **Cráteres** | Menos que en la Luna, hechos con suelo rojo |
| **Minerales** | Mineral marciano (Y 5-140), marteíta (Y 5-90), cristal marciano (Y 5-50), hierro oxidado (Y 20-180) |
| **Agua** | 💧 **Bolsas de hielo** cerca de la superficie (Y 55-130). Al romper el hielo se convierte en agua: cógela con un cubo para el **recargador eléctrico** |
| **Gravedad** | **0,38**: saltas unos 2,5 bloques; caídas suaves |
| **Aire** | Ninguno respirable + temperatura extrema (traje completo) |
| **Cielo** | **Anaranjado** de día; de noche, estrellas y una luna (Fobos) |
| **Día y noche** | Sí (a diferencia de la Luna). Los paneles solares solo producen de día |
| **Criaturas y estructuras** | Fases 17 y 19 |

## 🌪️ Tormentas de polvo
| | |
|---|---|
| **Cuándo** | Cada **15 minutos**, una tormenta de **3 minutos** (empieza y acaba en 20 s) |
| **Aviso** | *"⚠ ¡Tormenta de polvo! El viento te empuja y apenas se ve"* / *"La tormenta de polvo ha pasado"* |
| **Visibilidad** | Niebla rojiza: solo se ve a ~30 bloques |
| **Polvo** | Partículas rojas volando en la dirección del viento |
| **Viento** | Te **empuja** si estás al aire libre (no bajo techo ni dentro del cohete). La dirección cambia en cada tormenta |
| **Energía** | Los paneles solares producen hasta un **70 % menos** |

## 🚀 Ir y volver
```
 TIERRA (cohete AVANZADO, 5 000 mB) ── despegue ──► ESPACIO ── navega a Marte (≈1 660 bloques) ──► órbita
                                                                                                  │
                                                               descenso automático ◄──────────────┘
                                                                        │
                                                               ATERRIZAJE en Marte
                                                                        │
           ESPACIO (órbita de Marte, destino: la Tierra) ◄── despegue (sin plataforma) ── Y 350
```
- Con el **cohete básico** no se puede: rebota al acercarse a Marte.
- Despegar desde Marte **no necesita plataforma** (gravedad 0,38).
- **Combustible para volver:** la refinería ahora acepta **mineral marciano** (150 mB cada uno: metano atrapado en la roca).
  Dale energía con paneles solares (de día y sin tormenta) o lleva baterías/celdas cargadas.

## 🏆 Logro: "Planeta Rojo"
*"Pisa Marte por primera vez"*. Aparece en la pestaña **Panthrixs Galaxy**, después de "Un pequeño paso".

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Dimensión, terreno con montañas, bioma, menas, hielo | ✅ Sí | JSON en `data/panthrixsgalaxy/` (`dimension`, `worldgen`) |
| Cráteres | ❌ No | El mismo `PGCraterFeature` de la Luna (ahora usa el material del suelo) |
| Cielo anaranjado sin nubes | ⚠️ Parcial | Colores del bioma + `client/sky/PGMarsEffects.java` |
| **Tormentas de polvo** (viento, niebla, polvo) | ❌ No | `system/weather/PGMarsWeather.java` + `client/PGClientEvents.java` |
| Gravedad, aire, temperatura | — | Automático: solo con los datos de Marte en `PGPlanets` |
| Logro | ✅ Sí | `advancements/mars_red_planet.json` |

Fíjate en lo poco de Java que ha hecho falta para el **planeta entero**: gracias al registro de planetas de la Fase 11,
la gravedad, el oxígeno, la temperatura, la navegación, el aterrizaje y el despegue funcionan solos con una línea en `PGPlanets`.

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `data/.../dimension/mars.json` + `dimension_type/mars.json` | La dimensión (altura 0-320, con día y noche) |
| `data/.../worldgen/noise_settings/mars.json` | Terreno: piedra marciana, suelo rojo, colinas y montañas |
| `data/.../worldgen/noise/mars_hills.json`, `mars_mountains.json` | Formas del relieve |
| `data/.../worldgen/biome/mars.json` | Colores del cielo y la niebla; cráteres y menas |
| `data/.../worldgen/configured_feature/mars_*.json` + `placed_feature/` | Menas, hielo y cráteres |
| `system/weather/PGMarsWeather.java` | Cuándo hay tormenta, viento |
| `client/sky/PGMarsEffects.java` | Cielo de Marte sin nubes |

**Cambios en archivos existentes:**
- `PGPlanets`: Marte ya tiene dimensión.
- `PGCraterFeature`: el cráter usa el bloque de la superficie (regolito en la Luna, suelo marciano en Marte).
- `PGSolarPanelBlockEntity`: menos producción con tormenta.
- `PGFuelRefineryBlockEntity`: acepta mineral marciano.
- `PGClientEvents`: polvo, niebla y avisos de tormenta. `PGClientModEvents`: registra el cielo de Marte.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`. Equipo completo (traje, oxígeno, mochila).
2. **Prueba rápida:** `/execute in panthrixsgalaxy:mars run tp @s 0 200 0`
   - [ ] Suelo rojo, montañas, algún cráter. Cielo anaranjado.
   - [ ] Logro **"Planeta Rojo"**.
   - [ ] Saltas unos 2,5 bloques. Caer desde 200 no te mata... de golpe (¡prueba con cuidado!).
   - [ ] `/time set night` → cielo oscuro con estrellas.
3. **Tormenta:** `/time add 0` no sirve; usa `/tick`… o simplemente espera: hay tormenta los **3 primeros minutos de cada 15**.
   Truco: `/time set 0` no cambia el reloj de tormentas; para forzarla pon `/time add 18000` varias veces hasta que salga el aviso.
   - [ ] Niebla rojiza, polvo volando, el viento te empuja; bajo techo no.
4. **Minería:** pica en la piedra marciana: marteíta, cristal marciano, hierro oxidado, mineral marciano. Busca **hielo** cerca de la superficie, rómpelo y recoge el agua con un cubo.
5. **El viaje de verdad:** cohete **avanzado** lleno → despega en la Tierra → Espacio → ESPACIO hasta *"Destino: Marte"* → navega → aterriza.
6. **Volver:** refinería + mineral marciano + energía → reposta → despega (sin plataforma) → órbita → baja hacia la Tierra.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El juego se cierra al entrar en Marte | Error en algún JSON de `worldgen`: busca `mars` en `run/logs/latest.log`. |
| El cielo no es naranja | Falta registrar `PGMarsEffects` o el campo `"effects": "panthrixsgalaxy:mars"`. |
| No llego a Marte con el cohete | Necesitas el **cohete avanzado** (nivel 2). |
| El hielo no da agua | Rómpelo sin "Toque de seda" y con un bloque debajo; se convierte en agua. |
| La tormenta no llega nunca | Son 3 minutos de cada 15 según el reloj del mundo; ten paciencia o avanza el tiempo. |

## ✅ Checklist final
- [x] Dimensión Marte con suelo rojo, montañas y cráteres
- [x] Mineral marciano, marteíta, cristal marciano y hierro oxidado
- [x] Hielo con agua para el recargador eléctrico
- [x] Gravedad 0,38, sin aire, temperatura extrema
- [x] Cielo anaranjado con día y noche
- [x] Tormentas de polvo: viento, niebla, polvo y menos energía solar
- [x] Llegada con el cohete avanzado, despegue de vuelta sin plataforma
- [x] Combustible en Marte (refinería con mineral marciano)
- [x] Logro "Planeta Rojo"
- [ ] Probado en tu PC ← te toca a ti
