# FASE 20 — El cinturón de asteroides ☄️ ✅

## 🎯 Objetivo
Una zona nueva del Espacio llena de **rocas flotando** entre las que se vuela con la nave, se **mina**, y donde se
encuentran **restos de naves**, **puestos alienígenas** y **tecnología alienígena**.

## 🗺️ Cómo llegar
```
 Planeta ── nave ──► ESPACIO ── vuela hacia "el cinturón de asteroides" (≈1 640 bloques) ──► entras flotando a Y 270
```
- Solo con la **NAVE** (alcance 3, Fase 14). El cohete no tiene alcance: rebota.
- En el panel de la nave aparece una línea nueva: *"el cinturón de asteroides: 812 bloques · ◄ gira..."*.
- Para salir: sube con la nave por encima de **Y 300** → vuelves al Espacio, junto al cinturón.

## ☄️ Cómo es
| | |
|---|---|
| **Suelo** | ¡Ninguno! Solo vacío y asteroides flotando (de Y 20 a Y 250) |
| **Gravedad** | **0,08**: das saltos enormes y caes despacísimo |
| **Aire** | Ninguno (traje completo y oxígeno) |
| **Cielo** | Negro con estrellas, el Sol y Marte pequeñito a lo lejos |
| **Asteroides** | Uno cada 2 chunks, de radio 3 a 9, con forma irregular |

⚠️ **Cuidado:** si saltas de un asteroide y no llegas a otro, caes (muy despacio) hacia el vacío del fondo.
Ve siempre con la nave cerca. Y vuela despacio: **chocar contra un asteroide daña el casco**.

## ⛏️ Minerales de los asteroides
| Mineral | Cuánto | Para qué |
|---|---|---|
| Metal de asteroide | 8 % | Reparar la nave (+30 de casco), nave avanzada |
| Meteorito | 4 % | Fragmentos de meteorito |
| **Osmio** | 3 % | Herramientas de osmio, pistola/espada púrpura y **¡la necronita!** (4 fragmentos + 4 osmio) |
| **Astralita** | 1,5 % | Pistola y espada púrpura |
| Asteroides especiales | | 15 % con **núcleo de hielo** (agua) · 6 % con **núcleo de osmio** (¡un tesoro!) |

## 🛸 Lo que puedes encontrar
| Estructura | Frecuencia | Qué tiene |
|---|---|---|
| **Restos de nave** | 1 de cada 45 chunks | Casco destrozado con agujeros, motor apagado, chatarra flotando y un **cofre**: placas, hierro, redstone, metal de asteroide, bidones llenos, bombonas, baterías... y con suerte tecnología alienígena, una pistola láser o piezas de nave |
| **Puesto alienígena** | 1 de cada 70 chunks | Asteroide **hueco** de piedra alienígena con cristales cósmicos brillando, un túnel de entrada y un **cofre de tecnología alienígena** (osmio, astralita, cristal cósmico, fragmentos de necronita, pistola verde, ¡incluso una baliza!). Lo **protegen 2 aliens soldado** |

🏆 **Logro "Entre rocas":** llega con tu nave al cinturón de asteroides.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Dimensión, bioma, cielo vacío, logro, botín de cofres | ✅ Sí | JSON (`dimension/asteroids.json`, `worldgen/biome/asteroid_belt.json`, `loot_tables/chests/`) |
| Asteroides flotantes con minerales y núcleos | ❌ No | `world/feature/PGAsteroidFeature.java` |
| Restos de nave y puesto alienígena con guardianes | ⚠️ Parcial (estructuras NBT) | `world/feature/PGShipwreckFeature.java`, `PGAlienOutpostFeature.java` |
| Destino nuevo en el Espacio | — | **Una línea** en `PGPlanets` (gravedad, aire, navegación, salida: todo automático) |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `planet/PGPlanets.java` | `ASTEROIDS`: posición en el Espacio, alcance 3, gravedad 0,08, salida a Y 300 |
| `world/feature/PGAsteroidFeature.java` | Cada asteroide |
| `world/feature/PGShipwreckFeature.java` | Restos de nave |
| `world/feature/PGAlienOutpostFeature.java` | Puesto alienígena |
| `client/sky/PGAsteroidEffects.java` | El cielo del cinturón |
| `data/.../worldgen/placed_feature/asteroid_belt_*.json` | **Cuántos** hay de cada cosa (cambia `"chance"`) |
| `textures/environment/asteroids.png` | Cómo se ve el cinturón desde el Espacio |

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Prueba rápida: `/execute in panthrixsgalaxy:asteroids run tp @s 0 150 0` (en creativo para volar).
   - [ ] Asteroides flotando por todas partes, con minerales. Alguno con hielo dentro.
   - [ ] Cielo negro con estrellas y Marte pequeñito.
   - [ ] Busca restos de nave (paneles grises con agujeros) y abre el cofre.
   - [ ] Busca un asteroide verde hueco: dentro, 2 aliens soldado y un cofre.
2. **El viaje de verdad:** nave con combustible → sube al Espacio → en el panel busca *"el cinturón de asteroides"* → gira hasta *"▲ en rumbo"* → W.
   - [ ] Entras flotando. Logro **"Entre rocas"**.
3. Mina osmio → 4 fragmentos de necronita + 4 osmio = **lingote de necronita**.
4. Sube por encima de Y 300 → vuelves al Espacio.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| Con el cohete no llego | Necesitas la **nave** (alcance 3). |
| El juego se cierra al entrar | Error en un JSON de `worldgen` o `dimension`: busca `asteroid` en `run/logs/latest.log`. |
| No veo asteroides | Están de Y 20 a Y 250; mira alrededor. Uno cada 2 chunks. |
| Me caí al vacío | La gravedad es muy baja: vuela con la nave cerca y no saltes lejos de las rocas. |
| Demasiados o muy pocos asteroides | Cambia `"chance"` en `placed_feature/asteroid_belt_asteroid.json` (1 = en cada chunk). |

## ✅ Checklist final
- [x] Dimensión cinturón de asteroides (vacío, gravedad 0,08, sin aire, cielo propio)
- [x] Destino nuevo en el Espacio, solo con la nave
- [x] Asteroides con metal de asteroide, meteorito, osmio, astralita; núcleos de hielo y osmio
- [x] Restos de naves con cofre de botín y chatarra flotando
- [x] Puestos alienígenas con tecnología alienígena y guardianes
- [x] Logro "Entre rocas"; la necronita ya se puede fabricar
- [ ] Probado en tu PC ← te toca a ti
