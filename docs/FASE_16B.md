# FASE 16B — Todos los colores de láseres y espadas 🌈 ✅

## 🎯 Objetivo
Completar lo que pedía el documento original y se quedó corto en las Fases 15 y 16:
**6 pistolas láser** y **7 espadas láser**, cada una con estadísticas distintas.
La espada **negra** es extremadamente rara y usa **tecnología alienígena** (Fase 17) y **necronita** (Fase 18).

## 🔫 Pistolas láser
| Arma | Daño | Disparos/s | Alcance | Disparos (carga llena) | Especial | Materiales |
|---|---|---|---|---|---|---|
| 🔴 **Roja** | 2,5 ❤ | 2,5 | 60 | 80 | Básica | Luna: lunarita |
| 🟢 **Verde** | 2,5 ❤ | 2,5 | 60 | **250** | **Gasta muy poca energía** | Luna: lunarita + esmeralda |
| 🔵 **Azul** | 3,5 ❤ | 2,5 | 64 | 85 | **Más daño** | Marte: marteíta + cristal marciano |
| 🟣 **Púrpura** | 3 ❤ | 2 | 64 | 85 | **Perfora armaduras** | Asteroides: osmio + astralita |
| 🟡 **Dorada** | 4,5 ❤ | **4** | 77 | 125 | Ráfaga rápida, atraviesa 1 enemigo | Xenita + oro + tecnología alienígena |
| ⚪ **Blanca** | **7 ❤** | 1,7 | 106 | 125 | Atraviesa 3, perfora armaduras, empuja | Cristal cósmico + necronita + tecnología alienígena |
| Rifle (cian) | 5,5 ❤ | 1 | 91 | 100 | Atraviesa 2, empuja | Marte (se queda como estaba) |

## ⚔️ Espadas láser
| Espada | Daño | Velocidad | Energía | Especial | Materiales |
|---|---|---|---|---|---|
| 🔵 **Azul** | 3,5 ❤ | normal | 40 000 | Equilibrada | Luna: selenita |
| 🟢 **Verde** | 3 ❤ | normal | 40 000 | **Gasta muy poca energía** (40 por golpe, 8/s) | Luna: selenita + esmeralda |
| 🔴 **Roja** | 4 ❤ | normal | 80 000 | **Quema** 3 s | Marte: cristal marciano + marteíta |
| 🟣 **Púrpura** | 4 ❤ + 1,5 ❤ | normal | 100 000 | **Perfora armaduras** (el extra ignora la armadura) | Asteroides: osmio + astralita |
| 🟡 **Dorada** | 4,5 ❤ | **muy rápida** | 120 000 | Velocidad | Xenita + bloque de oro + tecnología alienígena |
| ⚪ **Blanca** | 5,5 ❤ | rápida | 200 000 | El enemigo **brilla** (se ve tras las paredes) y **sale despedido** | Cristal cósmico + xenita + 2 tecnologías alienígenas |
| ⚫ **Negra** | **7 ❤** | rápida | 300 000 | **Marchita** y **te devuelve vida** (25 % del daño) | 2 necronita + cristal cósmico + **4 tecnologías alienígenas** |

> ⚠️ **Cambio:** la espada **púrpura** de la Fase 16 era la más fuerte (cristal cósmico + xenita). Ahora ese
> papel lo tienen la dorada, la blanca y la negra; la púrpura pasa a ser la que **perfora armaduras** y se fabrica
> con osmio y astralita.

## 📍 ¿Cuándo se pueden fabricar?
| Materiales | Fase en la que se consiguen |
|---|---|
| Lunarita, selenita, esmeralda | Ya (Luna, Tierra) |
| Marteíta, cristal marciano | Ya (Marte) |
| Tecnología alienígena | Ya (aliens de Marte, Fase 17) |
| Osmio, astralita | Fase 20 (asteroides) |
| Xenita, cristal cósmico | Fase 21 (planetas alienígenas) |
| **Necronita** | Fase 18 (la **Reina alienígena**) |

Mientras tanto puedes probarlas todas en **creativo** (pestaña Equipo, vacías y cargadas).

## 🧠 MCreator vs Java
Igual que en las Fases 15 y 16: lo nuevo son **datos**. Cada arma es **una línea** en:
- `weapon/LaserTier.java` (pistolas): se añadió `armorPiercing` (perfora armaduras).
- `weapon/LaserSwordTier.java` (espadas): se añadieron `idleCost` (gasto encendida) y `special` (habilidad).
- `init/ModItems.java`: registrar el objeto.
Y lo de siempre en JSON/PNG: textura, modelo, receta, traducción.

**Nuevo tipo de daño:** `piercing_laser` (`data/.../damage_type/piercing_laser.json`), en el tag
`minecraft:bypasses_armor` → la armadura no lo reduce.

## 🗂️ Archivos cambiados
| Archivo | Cambio |
|---|---|
| `weapon/LaserTier.java` | 5 pistolas nuevas + perforar armaduras + tono del sonido según potencia |
| `weapon/LaserSwordTier.java` | 4 espadas nuevas + habilidades especiales |
| `weapon/PGLaserSwordItem.java` | Aplica la habilidad de cada hoja y su gasto encendida |
| `weapon/PGLaserItem.java` | Sonido según el arma; descripción "Perfora armaduras" |
| `entity/laser/PGLaserBoltEntity.java` | Usa el daño que perfora si el arma lo tiene |
| `init/ModDamageTypes.java` | Tipo de daño `piercing_laser` |
| `init/ModItems.java` | Las 9 armas nuevas |

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Creativo → pestaña **Equipo**: las 7 pistolas (con el rifle) y las 7 espadas, vacías y cargadas.
2. Pasa a **supervivencia** con las cargadas:
   - [ ] **Verde** vs **roja**: dispara 10 veces con cada una y mira la barra: la verde casi no baja.
   - [ ] **Púrpura** contra un zombi con armadura de diamante (`/summon zombie ~ ~ ~ {ArmorItems:[{id:"diamond_boots",Count:1},{id:"diamond_leggings",Count:1},{id:"diamond_chestplate",Count:1},{id:"diamond_helmet",Count:1}]}`): muere mucho antes que con la roja.
   - [ ] **Dorada**: dispara muy deprisa.
   - [ ] **Blanca**: atraviesa una fila de 4 zombis.
3. Espadas:
   - [ ] **Blanca**: el enemigo se ilumina con contorno y sale volando.
   - [ ] **Negra**: el enemigo se marchita (corazones negros) y tú recuperas vida.
   - [ ] **Dorada**: la barra de ataque se recarga muy rápido.

## ✅ Checklist
- [x] Pistolas roja, verde, azul, púrpura, dorada y blanca (+ el rifle)
- [x] Espadas roja, azul, verde, púrpura, dorada, blanca y negra
- [x] Estadísticas distintas y habilidades especiales
- [x] Daño que perfora armaduras
- [x] Espada negra con tecnología alienígena y necronita
- [x] Texturas, modelos, recetas y traducciones
- [ ] Probado en tu PC ← te toca a ti
