# FASE 15 — Armas láser 🔫 ✅

## 🎯 Objetivo
Las primeras armas espaciales: disparan **rayos de energía** que gastan la energía del arma (y, si se vacía, la de tu mochila).
Servirán contra las criaturas de los planetas (Fase 17) y los jefes (Fase 18).

## 🔫 Las dos armas
| | **Pistola láser** | **Rifle láser** |
|---|---|---|
| Materiales | Luna (lunarita, selenita) | Marte (marteíta, cristal marciano) |
| Daño | 2,5 ❤ | 5,5 ❤ + empujón |
| Disparos por segundo | 2,5 | 1 |
| Alcance | ~60 bloques | ~90 bloques |
| Atraviesa | — | **2 enemigos** además del primero |
| Energía | 20 000 FE (80 disparos) | 60 000 FE (100 disparos) |
| Color del rayo | 🔴 Rojo | 🔵 Cian |

## ⚡ Energía
1. Cada disparo gasta energía **del arma** (barra amarilla bajo el icono).
2. Si el arma está vacía, la saca del **depósito de energía de la mochila** (Fase 7).
3. Sin energía: suena un "clic" y aparece *"Sin energía"*.
4. **Recargar:** clic derecho con el arma sobre un **generador, panel solar, reactor o celda de energía** — igual que una batería.
   (Mayús + clic derecho la **descarga** en la máquina.)

Después de cada disparo, encima de la barra rápida verás: *"Láser: 42 disparos (+120 con la mochila)"*.

## 💥 El rayo
- No cae: va **recto**, muy rápido. Desaparece al llegar al final de su alcance.
- **No rompe bloques**: al chocar solo saltan chispas del color del rayo.
- Un **escudo** lo bloquea, la armadura lo reduce, y el encantamiento **Protección contra proyectiles** también.
- No daña la nave en la que vas montado.
- Si matas a alguien: *"X fue alcanzado por el láser de Y"*.

## 🛠️ Fabricación (Banco de Ingeniería → categoría **Láseres**)
| Pieza | Receta |
|---|---|
| **Lente de enfoque** (×2) | 4 paneles de cristal + cristal de selenita en el centro |
| **Emisor láser** | 4 placas reforzadas + 2 redstone + 2 lingotes de cobre + lente de enfoque |
| **Pistola láser** | 2 lingotes de lunarita + emisor + batería básica + 2 placas |
| **Rifle láser** | 3 lingotes de marteíta + emisor + cristal marciano + batería avanzada + placa |

La batería de la receta **no** pasa su carga al arma: el arma sale descargada.

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Objetos, recetas, texturas, traducciones, tipo de daño | ✅ Sí | JSON y PNG en `resources/` |
| Disparo con "proyectil" | ⚠️ Parcial (tiene "ranged item", pero sin energía FE ni atravesar) | `weapon/PGLaserItem.java` + `entity/laser/PGLaserBoltEntity.java` |
| Energía FE en el arma + mochila | ❌ No | `PGLaserItem` (usa `ItemEnergyStorage` como las baterías) |
| Rayo brillante | ❌ No | `client/renderer/PGLaserBoltRenderer.java` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `weapon/LaserTier.java` | Datos de cada arma (tabla de arriba): cambia ahí el daño, la cadencia, el alcance... |
| `weapon/PGLaserItem.java` | Disparar, gastar energía (arma → mochila), barra de energía, descripción |
| `entity/laser/PGLaserBoltEntity.java` | El rayo: vuelo recto, impacto, atravesar, chispas |
| `client/renderer/PGLaserBoltRenderer.java` | Dibuja el rayo (núcleo blanco + halo de color) |
| `init/ModDamageTypes.java` + `data/.../damage_type/laser.json` | Tipo de daño "láser" |
| `data/minecraft/tags/damage_type/is_projectile.json` | El láser cuenta como proyectil (escudos, encantamientos) |

**Cambios en archivos existentes:** `ModItems`, `ModEntities`, `ModCreativeTabs` (armas vacías y cargadas en Equipo),
`PGClientModEvents`, `EngineeringCategory` (icono de Láseres).

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`. Modo **supervivencia** (en creativo no se gasta energía). Coge de la pestaña **Equipo** la pistola **cargada**.
2. Clic derecho: rayo rojo, sonido y *"Láser: 79 disparos"*.
   - [ ] Mata a un zombi. El rayo no atraviesa paredes ni rompe bloques: solo chispas.
3. Rifle cargado: pon 3 zombis en fila → el rayo cian los atraviesa a todos y los empuja.
4. Gasta la pistola entera → *"Sin energía"*. Equípate una mochila con energía → vuelve a disparar.
5. Clic derecho con la pistola sobre un generador encendido → se recarga (barra amarilla).
6. En multijugador, con un escudo, deja que un amigo te dispare → lo bloqueas.
7. Si te mata su láser → mensaje de muerte.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| No dispara y no dice nada | Estás en enfriamiento (pistola 0,4 s, rifle 1 s). |
| *"Sin energía"* con la mochila llena | El depósito que cuenta es el de **energía**, no el de combustible. Cárgala en un generador con la mano vacía. |
| El rayo no se ve | Mira el registro por errores del renderizador; comprueba que `PGClientModEvents` registra `LASER_BOLT`. |
| Clic derecho en el generador no carga el arma | Si estás pulsando Mayús, la descargas. Suelta Mayús. |
| El arma se ve torcida en la mano | Usa el modelo "handheld" (como una espada). Se puede ajustar en Blockbench → "Display". |

## ✅ Checklist final
- [x] Pistola láser y rifle láser
- [x] Rayo recto y rápido con alcance limitado, sin romper bloques
- [x] Energía del arma y, si se acaba, de la mochila
- [x] Recarga en generadores/celdas (como una batería)
- [x] Rifle que atraviesa enemigos y empuja
- [x] Tipo de daño "láser" con mensajes de muerte; lo bloquea el escudo
- [x] Recetas en el Banco de Ingeniería (categoría Láseres)
- [ ] Probado en tu PC ← te toca a ti
