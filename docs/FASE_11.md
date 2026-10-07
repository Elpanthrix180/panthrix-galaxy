# FASE 11 — De la Tierra al Espacio ✅

## 🎯 Objetivo
Que el viaje sea continuo: **Tierra → lanzamiento → atmósfera → Espacio → navegación**, sin portales
ni pantallas de "pulsa un botón y aparece el planeta".

## 🧠 Qué permite Minecraft (y cómo lo resolvemos)
| ✅ Posible | ❌ Límite de Minecraft → solución |
|---|---|
| Una dimensión **Espacio** vacía, sin aire y con cielo propio | Dos dimensiones no se ven a la vez → los planetas se **dibujan en el cielo** del Espacio y **crecen al acercarte** |
| Cambiar de dimensión **en pleno vuelo** con el piloto dentro | Siempre hay un instante de carga → **fundido a negro** de 1,5 s, sin menús |
| Moverse libremente en 3D | El jugador no tiene "gravedad 0" → en el Espacio vas dentro del cohete (cabina **presurizada**: dentro se respira) |

## 🚀 El viaje completo
```
 TIERRA                      │                     ESPACIO
 plataforma → T-10 → despegue│
 nubes (Y 192): el horizonte │
 se oscurece poco a poco     │
 Y 450 ── fundido a negro ───┼──► apareces a Y 100, misma posición, mirando al cielo negro
                             │    estrellas, el Sol, la Tierra enorme debajo, la Luna y Marte a lo lejos
                             │
                             │    W acelerar · S frenar · mira hacia donde vas · ESPACIO = destino
                             │
 aterrizaje en tu plataforma ◄── bajar de Y 40 = reentrada en la atmósfera (piloto automático)
                             │
                             │    Luna (≈600 bloques al norte) → órbita lunar (aterrizaje: Fase 12)
                             │    Marte (≈1 600 bloques)       → órbita de Marte (aterrizaje: Fase 13)
```

### 🌌 El cielo del Espacio
- **2 000 estrellas** fijas de distintos tamaños y brillos.
- **El Sol** siempre visible.
- **La Tierra** ocupa la parte de abajo del cielo cuando acabas de llegar; se hace más pequeña al alejarte.
- **La Luna y Marte**: puntos al principio, que **crecen** a medida que te acercas.
- Sin nubes, sin niebla, fondo negro.

### 🕹️ Controles en el Espacio
| Tecla | Acción | Combustible |
|---|---|---|
| **W** | Acelerar hacia donde miras | 1 mB/tick |
| **S** | Frenar | 1 mB cada 2 ticks |
| (nada) | Seguir por **inercia** | 0 |
| **Mirar** | Elegir la dirección | — |
| **ESPACIO** | Cambiar de destino (Tierra → Luna → Marte) | — |
| Mayús | (no puedes salir del cohete en el Espacio) | — |

**El panel de navegación** muestra el destino, la **distancia**, hacia dónde girar (◄ izquierda / ▲ en rumbo /
derecha ►), si mirar más arriba o abajo, el combustible y la velocidad.

### ⚠️ Reglas
| Situación | Qué pasa |
|---|---|
| Cohete **básico** intenta llegar a **Marte** | Rebota: *"no tiene potencia… necesitas un cohete de nivel superior"* |
| Llegar a la Luna o Marte | *(Fases 12 y 13)* Descenso y **aterrizaje** en el planeta |
| **Sin combustible** en el Espacio | La gravedad de la Tierra te atrae despacio hacia abajo → reentrada automática. Nunca te quedas atrapado |
| Bajar de **Y 40** en el Espacio | Reentrada: vuelves a la Tierra a Y 400 sobre tu **plataforma de despegue** y aterrizas solo |
| Guardar y salir en el Espacio | Al volver sigues en el Espacio, navegando |

## 🪐 El registro de planetas (preparado para el futuro)
Cada cuerpo celeste es una línea en `planet/PGPlanets.java`:

| Dato | Tierra | Luna | Marte |
|---|---|---|---|
| Dimensión | Overworld | *(Fase 12)* | *(Fase 13)* |
| Cohete necesario | — | básico (1) | avanzado (2) |
| Posición en el Espacio | debajo | 600 bloques al norte | 1 400 E, 900 N |
| Gravedad | 1,0 | 0,17 | 0,38 |
| Aire | sí | no | no |

**Añadir Venus, Júpiter o un planeta alienígena** = una línea nueva + su textura + (cuando toque) su dimensión.
Todo lo demás (cielo, navegación, nivel de cohete, panel) lo lee de aquí automáticamente.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Dimensión Espacio (vacía) | ✅ Sí | `data/panthrixsgalaxy/dimension/space.json` + `dimension_type/space.json` + bioma |
| **Cielo con estrellas y planetas que crecen** | ❌ No | `client/sky/PGSpaceEffects.java` + `SpaceSkyRenderer.java` |
| **Cambiar de dimensión en vuelo con el piloto** | ❌ No (MCreator usa portales) | `PGRocketEntity.travelTo()` |
| Navegación con W/S y mirada | ❌ No | `PGRocketEntity.tickInSpace()` |
| Registro de planetas | ❌ No | `planet/PGPlanet.java`, `planet/PGPlanets.java` |
| Oscurecer el horizonte al subir, fundido | ❌ No | `client/PGClientEvents.java`, `client/RocketHudOverlay.java` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `planet/PGPlanet.java` / `PGPlanets.java` | Registro de cuerpos celestes + dimensión Espacio |
| `data/panthrixsgalaxy/dimension/space.json` | La dimensión Espacio (mundo vacío) |
| `data/panthrixsgalaxy/dimension_type/space.json` | Sus reglas (sin camas, siempre de día para la luz, aspecto "space") |
| `data/panthrixsgalaxy/worldgen/biome/space.json` | Bioma vacío y negro |
| `client/sky/PGSpaceEffects.java` | Sin nubes, sin niebla, fondo negro |
| `client/sky/SpaceSkyRenderer.java` | Estrellas, Sol y planetas |
| `textures/environment/earth.png`, `moon.png`, `mars.png` | Imágenes de los planetas en el cielo |
| `entity/rocket/PGRocketEntity.java` | Viaje entre dimensiones, navegación, reentrada, órbitas |
| `client/RocketHudOverlay.java` | Panel de navegación y fundido a negro |

**Cambios en archivos existentes:**
- `LaunchState`: nueva etapa `IN_SPACE`.
- `PGRocketEntity`: al llegar a Y 450 **desde la Tierra** ya no baja, sino que pasa al Espacio (desde otras dimensiones sigue el vuelo de prueba). Guarda la plataforma de despegue para volver.
- `PGAtmosphere`: dentro del cohete siempre hay aire (cabina presurizada).
- `PanthrixsGalaxy.java`: el Espacio no tiene aire. `PGClientModEvents`: registra el cielo del Espacio.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Cohete **básico** con el depósito **lleno** (2 000 mB) en la plataforma. Súbete, ESPACIO, despega.
   - [ ] Por encima de las nubes, el horizonte se va oscureciendo.
   - [ ] En Y 450: fundido a negro → estás en el **Espacio** (cielo negro, estrellas, la Tierra abajo).
3. Panel: *"Destino: la Luna"*. Gira según el panel (◄ / ►) hasta ver *"▲ en rumbo"* y mantén **W**.
   - [ ] La Luna crece en el cielo. La distancia baja.
   - [ ] Al llegar: *"Órbita de la Luna alcanzada…"* y el cohete se detiene.
4. Pulsa **ESPACIO** → *"Destino: Marte"*. Ve hacia allí con el cohete básico → rebota (*"no tiene potencia"*).
5. Pulsa ESPACIO hasta *"Destino: la Tierra"*, mira hacia abajo y acelera.
   - [ ] Al bajar de Y 40: fundido → *"Reentrada…"* → descenso controlado → aterrizas en tu plataforma.
6. **Sin combustible:** gasta todo el combustible en el Espacio → el cohete empieza a caer solo → reentrada automática.
7. Prueba **F5** en el Espacio: ves el cohete flotando con las estrellas detrás.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| Al llegar a Y 450 el cohete baja en vez de ir al Espacio | Solo pasa al Espacio despegando desde la **Tierra** (Overworld). Comprueba que existe `data/panthrixsgalaxy/dimension/space.json`. |
| El cielo del Espacio sale azul o gris | Falta registrar los efectos (`PGClientModEvents.onRegisterDimensionEffects`) o el campo `"effects": "panthrixsgalaxy:space"`. |
| Los planetas salen como cuadrados morados | Faltan `textures/environment/earth.png`, `moon.png` o `mars.png`. |
| Tras el fundido apareces fuera del cohete | Se vuelve a sentar al piloto automáticamente en ¼ de segundo. Si no ocurre, mira el log (busca `rocket`). |
| Error al crear el mundo tras añadir el mod | Los mundos creados antes necesitan aceptar la nueva dimensión: Minecraft avisa de "configuración experimental"; pulsa continuar. |

## ✅ Checklist final
- [x] Dimensión Espacio sin aire y sin gravedad para el cohete
- [x] Cielo: estrellas, Sol, Tierra, Luna y Marte (que crecen al acercarte)
- [x] Transición Tierra → Espacio en pleno vuelo, con fundido
- [x] Navegación: acelerar, frenar, dirección, inercia, destinos
- [x] Panel de navegación con distancia y rumbo
- [x] Nivel de cohete por destino
- [x] Reentrada y aterrizaje automático en la plataforma
- [x] Registro de planetas preparado para la Luna, Marte y planetas futuros
- [ ] Probado en tu PC ← te toca a ti
