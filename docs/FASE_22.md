# FASE 22 — Sistema de logros 🏆 ✅

## 🎯 Objetivo
Todos los logros del documento original en una pestaña propia, **Panthrixs Galaxy** (tecla `L`),
organizados en un árbol que sigue la progresión del mod. Y dos **secretos** con su estructura escondida.

## 🌳 El árbol de logros
```
Panthrixs Galaxy
├─ Ingeniero espacial (Banco de Ingeniería)
│  ├─ Respira tranquilo (bombona) ─ Preparado para el espacio (traje completo) ─ Equipaje espacial (mochila)
│  │     └─ ¡Tenemos despegue! (cohete)
│  │           └─ Un pequeño paso (pisar la Luna)
│  │                 ├─ Minería lunar · Astronauta (5 min en la Luna) · Base lunar
│  │                 ├─ ❓ EL LADO OSCURO (secreto)
│  │                 └─ Planeta Rojo (pisar Marte)
│  │                       ├─ Minería marciana · Primer contacto (ver una criatura marciana)
│  │                       ├─ Colono (base en Marte) ─ Estación orbital (base en el Espacio)
│  │                       ├─ Entre rocas (cinturón de asteroides)
│  │                       └─ Más allá de la Tierra (3 cuerpos celestes)
│  │                             ├─ Explorador del Sistema Solar (todos) ─ Más allá de las estrellas (planeta secreto)
│  │                             └─ ❓ NO ESTAMOS SOLOS (secreto)
│  └─ Primer disparo (pistola láser)
│        ├─ Tecnología láser (láser avanzado) ─ Espectro de colores (3 colores)
│        ├─ Espada de energía (espada láser) ─ Tecnología prohibida (espada negra)
│        └─ Encuentro hostil (matar un alien)
│              ├─ Cazador marciano (10 criaturas marcianas) ─ Depredador espacial (50 criaturas)
│              └─ Rey del planeta (derrotar a la Reina)
```

## 📋 Todos los logros
| Grupo | Logro | Cómo se consigue |
|---|---|---|
| Primeros pasos | **Ingeniero espacial** | Fabricar el Banco de Ingeniería Espacial |
| | **Respira tranquilo** | Fabricar una bombona de oxígeno |
| | **Preparado para el espacio** | Tener las 4 piezas del traje espacial |
| | **Equipaje espacial** | Fabricar una mochila espacial |
| | **¡Tenemos despegue!** | Construir un cohete |
| 🌙 Luna | **Un pequeño paso** | Pisar la Luna |
| | **Minería lunar** | Conseguir lunarita, selenita o helio-3 |
| | **Astronauta** | Estar **5 minutos** (en total) en la Luna |
| | **Base lunar** | Respirar dentro de una **base sellada** en la Luna (Fase 19) |
| 🔴 Marte | **Planeta Rojo** | Pisar Marte |
| | **Minería marciana** | Conseguir marteíta, cristal o mineral marciano |
| | **Primer contacto** | **Ver** de cerca una criatura marciana |
| | **Colono** | Respirar dentro de una base sellada en Marte |
| | **Estación orbital** ⭐ | Respirar en una estación sellada en el Espacio o en el cinturón |
| 🔫 Armas | **Primer disparo** | Fabricar una pistola láser |
| | **Tecnología láser** | Fabricar un láser avanzado (azul, púrpura, dorado, blanco o el rifle) |
| | **Espectro de colores** | Tener **3 armas láser de colores distintos** a la vez |
| | **Espada de energía** | Fabricar una espada láser |
| | **Tecnología prohibida** | Fabricar la **espada láser negra** |
| 👽 Combate | **Encuentro hostil** | Matar un alienígena |
| | **Cazador marciano** | Matar **10** criaturas marcianas |
| | **Depredador espacial** | Matar **50** criaturas de otros mundos |
| | **Rey del planeta** | Derrotar a la **Reina alienígena** |
| 🌌 Exploración | **Entre rocas** ⭐ | Llegar al cinturón de asteroides |
| | **Más allá de la Tierra** | Visitar **3** cuerpos celestes |
| | **Explorador del Sistema Solar** | Visitar la Luna, Marte, el cinturón, Mercurio, Venus, Plutón y Xenoria |
| | **Más allá de las estrellas** | Encontrar el **planeta secreto** |
| ❓ Secretos | **EL LADO OSCURO** | ??? |
| | **NO ESTAMOS SOLOS** | ??? |

⭐ = extra, no estaban en el documento.
✏️ Dos cambios de nombre: el documento tenía **dos** "Primer contacto"; el de combate se llama ahora **"Encuentro hostil"**.
El logro de la Reina (Fase 18) se llama ahora **"Rey del planeta"**, como en el documento.

## 🤫 Los secretos (¡spoilers!)
<details>
<summary>Pulsa para verlos</summary>

- **EL LADO OSCURO:** en la Luna hay **bases lunares abandonadas** (paredes derrumbadas, un cofre con botín).
  Una de cada 8 esconde un **MONOLITO** negro. Acércate a él: el logro aparece y te revela unas **coordenadas**...
- **Más allá de las estrellas:** ...las del planeta secreto **NYX**, en el Espacio en (-6500, 0, 6000) desde la llegada
  desde la Tierra. **No sale en el cielo ni en el panel de la nave**: hay que ir a ciegas con la nave avanzada.
  Nyx es un mundo negro y oscuro, lleno de cristal cósmico y xenita... y de depredadores.
- **NO ESTAMOS SOLOS:** en Xenoria hay **aldeas alienígenas**: 4 cabañas-cúpula alrededor de una plaza con un
  **ARCHIVO ALIENÍGENA** brillante y 3 exploradores pacíficos. Acércate al archivo.
</details>

Los logros secretos están **ocultos**: no aparecen en la pantalla de logros hasta que los consigues.
(Minecraft no permite mostrar "???" en su lugar sin trucos.)

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Logros de "consigue este objeto", "entra en esta dimensión", "mata a este mob" | ✅ Sí | JSON en `data/panthrixsgalaxy/advancements/` |
| Contar tiempo, contar muertes, "3 planetas cualesquiera", "3 colores", ver una criatura | ❌ No | `advancement/PGAdvancementEvents.java` (criterio `minecraft:impossible` + Java) |
| Base lunar / Colono / Estación orbital | ❌ No | El distribuidor de oxígeno (Fase 19) da el logro a quien respire dentro |
| Bloques que dan un logro al acercarte | ❌ No | `block/PGDiscoveryBlock.java` (monolito y archivo) |
| Estructuras (base abandonada, aldea) y el planeta Nyx | ⚠️ Parcial | `world/feature/PGLunarRuinFeature.java`, `PGAlienVillageFeature.java` + JSON |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `data/.../advancements/*.json` | Los 30 logros (título, icono, padre, cómo se consigue) |
| `advancement/PGAdvancements.java` | "Dar un logro desde Java" |
| `advancement/PGAdvancementEvents.java` | Los logros con contadores |
| `data/.../tags/entity_types/aliens.json` | Qué cuenta como "alien" para "Encuentro hostil" |
| `planet/PGPlanets.java` | `NYX` y `isSecret` (no se dibuja ni sale en el panel) |

**Cambios en archivos existentes:** `moon_first_step.json` (ahora cuelga de "¡Tenemos despegue!"),
`alien_queen_slain.json` (nuevo nombre y padre), biomas de la Luna y Xenoria (nuevas estructuras),
`PGOxygenDistributorBlockEntity`, `SpaceSkyRenderer` y `ShipHudOverlay` (planetas secretos).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` y pulsa **L**: pestaña **Panthrixs Galaxy**.
2. Fabrica el Banco, una bombona, una mochila... → van saliendo los avisos.
3. Prueba los logros especiales:
   - [ ] Quédate 5 min en la Luna → "Astronauta".
   - [ ] Usa un módulo habitable en la Luna, entra y cierra → "Base lunar".
   - [ ] Ten pistola roja, verde y azul en el inventario → "Espectro de colores".
4. Secretos: el comando `/locate` no encuentra las bases abandonadas ni las aldeas. Para probar rápido, pon un monolito a mano:
   `/setblock ~2 ~ ~ panthrixsgalaxy:pg_monolith` → a los 2 segundos: "EL LADO OSCURO" + coordenadas.
5. Planeta secreto: `/execute in panthrixsgalaxy:nyx run tp @s 0 150 0` → "Más allá de las estrellas".
6. Para empezar de cero: `/advancement revoke @s everything`.

## ✅ Checklist final
- [x] Todos los logros del documento (primeros pasos, Luna, Marte, armas, combate, exploración, secretos)
- [x] Árbol organizado en la pestaña Panthrixs Galaxy
- [x] Contadores con Java (tiempo, muertes, planetas, colores) que no se pierden al morir
- [x] Bases lunares abandonadas con el monolito; aldeas alienígenas con el archivo
- [x] Planeta secreto Nyx, invisible en el cielo y en el panel
- [x] Logros secretos ocultos hasta conseguirlos
- [ ] Probado en tu PC ← te toca a ti
