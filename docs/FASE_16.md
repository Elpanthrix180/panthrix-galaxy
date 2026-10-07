# FASE 16 — Espadas láser ⚔️ ✅

## 🎯 Objetivo
Armas cuerpo a cuerpo de energía: se **encienden y se apagan**, gastan energía en vez de desgastarse,
tienen una hoja brillante de colores y pueden **devolver los rayos láser** en guardia.

## ⚔️ Las tres espadas
| | 🔵 **Azul** | 🔴 **Roja** | 🟣 **Morada** |
|---|---|---|---|
| Cristal | Selenita (Luna) | Cristal marciano (Marte) | Cristal cósmico + xenita (planetas lejanos) |
| Daño (encendida) | 3,5 ❤ (como diamante) | 4 ❤ + **quema 3 s** | 5 ❤ y más rápida |
| Energía | 40 000 FE | 80 000 FE | 160 000 FE |
| Gasto por golpe | 100 FE | 150 FE | 200 FE |
| Golpes con la carga llena* | ~400 | ~530 | ~800 |

\* Sin contar que encendida gasta además 20 FE por segundo aunque no golpees.

**Apagada** es solo una empuñadura: pega como el puño y no corta nada.
**Nunca se rompe**: no tiene durabilidad, lo que se gasta es la energía. Admite encantamientos (Filo, Botín...).

## 🎮 Controles
| Acción | Qué hace |
|---|---|
| **Mayús + clic derecho** | Encender / apagar (con sonido) |
| **Clic derecho** (apagada) | Encender |
| **Clic derecho mantenido** (encendida) | **Guardia** |
| Cambiar de hueco o guardarla | Se apaga sola |

## 🛡️ Guardia
Mientras mantienes el clic derecho con la espada encendida:
- Los golpes que vienen **de delante** hacen **la mitad** de daño (cuesta 50 FE cada uno).
- Los **rayos láser** que te llegan de frente **rebotan** hacia quien los disparó (también 50 FE). ¡Ahora son tuyos!
- Los golpes por la espalda, la caída, el fuego, etc. no se bloquean.

## ⚡ Energía
Igual que las armas láser (Fase 15) — ahora las dos comparten el mismo código (`PortableEnergy`):
1. Primero gasta la energía de la espada (barra amarilla).
2. Si se vacía, la de la **mochila**.
3. Sin ninguna, la espada **se apaga sola** con un aviso.
4. Recarga: **clic derecho** (sin Mayús) con la espada sobre un generador, panel, reactor o celda de energía.
   Con Mayús la **descargas** en la máquina.

## 🛠️ Fabricación (Banco de Ingeniería → categoría **Espadas láser**)
| Pieza | Receta |
|---|---|
| **Empuñadura** | redstone + 3 placas reforzadas + batería básica |
| **Espada azul** | cristal de selenita + emisor láser (Fase 15) + empuñadura, en columna |
| **Espada roja** | cristal marciano + 2 marteíta + emisor + empuñadura |
| **Espada morada** | cristal cósmico + 2 xenita + emisor + empuñadura |

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Objetos, texturas, recetas, traducciones | ✅ Sí | JSON y PNG |
| Modelo que cambia al encenderse | ⚠️ Parcial | "Item property" `panthrixsgalaxy:active` + `overrides` en el JSON del modelo |
| Daño distinto encendida/apagada, sin durabilidad, energía FE | ❌ No | `weapon/PGLaserSwordItem.java` |
| Guardia y desviar láseres | ❌ No | `weapon/PGLaserSwordEvents.java` + `PGLaserBoltEntity.deflect` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `weapon/LaserSwordTier.java` | Datos de cada espada (cambia ahí daño, energía, colores) |
| `weapon/PGLaserSwordItem.java` | Encender/apagar, daño, energía, guardia, chispas |
| `weapon/PGLaserSwordEvents.java` | La guardia reduce el daño de frente |
| `system/energy/PortableEnergy.java` | **Nuevo:** "gastar del objeto o de la mochila", compartido con las armas láser |
| `models/item/pg_*_laser_sword.json` + `_active.json` | Modelo apagada / encendida |

**Cambios en archivos existentes:** `PGLaserItem` (usa `PortableEnergy`), `PGLaserBoltEntity` (rebota en la guardia),
`ModItems`, `ModCreativeTabs`, `PGClientModEvents` (registra la propiedad "active"), `EngineeringCategory`.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient`, modo **supervivencia**. Pestaña **Equipo** → espada azul **cargada**.
2. **Mayús + clic derecho** → sonido y aparece la hoja azul. Otra vez → se apaga.
   - [ ] Al pasar el ratón: *"● Encendida"* y el daño cambia (+6 encendida, nada apagada).
3. Encendida, golpea a un zombi: chispas azules, la barra de energía baja. La durabilidad no existe.
4. Espada **roja**: el zombi arde.
5. **Guardia vs mobs:** mantén clic derecho frente a un esqueleto: sus flechas hacen la mitad de daño.
6. **Guardia vs láser** (multijugador): que un amigo te dispare con la pistola láser mientras mantienes clic derecho mirándole → el rayo vuelve hacia él.
   (En la Fase 17 habrá criaturas que disparan láseres para probarlo solo.)
7. Recárgala: clic derecho sobre un generador encendido.
8. Gasta toda la energía (`/data` o pegando mucho) → *"La espada láser se ha quedado sin energía"* y se apaga.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| No se enciende | Sin energía en la espada ni en la mochila. Recárgala. |
| La hoja no aparece aunque está encendida | Falta el modelo `_active` o el registro de la propiedad en `PGClientModEvents`. |
| Se apaga sola | La has cambiado de hueco (se apaga al guardarla) o se quedó sin energía. |
| Al hacer clic derecho en el generador se descarga | Estabas pulsando Mayús: sin Mayús la carga. |
| La guardia no para un golpe | Solo de frente, y no los que atraviesan escudos (caída, magia...). |

## ✅ Checklist final
- [x] Espadas azul, roja y morada + empuñadura
- [x] Encender/apagar con sonido, modelo con y sin hoja
- [x] Daño solo encendida; sin durabilidad; energía del arma o de la mochila
- [x] Se apaga al guardarla o sin energía; zumbido
- [x] Roja quema; morada más rápida
- [x] Guardia: mitad de daño de frente y devuelve los rayos láser
- [x] Energía común con las armas láser (`PortableEnergy`)
- [x] Recetas en el Banco (categoría Espadas láser)
- [ ] Probado en tu PC ← te toca a ti
