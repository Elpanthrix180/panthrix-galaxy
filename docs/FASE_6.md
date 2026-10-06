# FASE 6 — Mochilas ✅

## 🎯 Objetivo
Mochilas espaciales equipables, con **inventario** y **depósitos** (oxígeno, energía, combustible
y agua) independientes, que se llevan en un **hueco propio** y se ven en la espalda del jugador.

## 🧭 Decisión de diseño: hueco propio de mochila (opción C)
Minecraft no tiene hueco para mochilas. Se valoraron tres opciones:
| Opción | Resultado |
|---|---|
| A. Llevarla en el inventario | Sencilla, pero no se "equipa" |
| B. En el hueco de la pechera | Te obliga a quitarte el traje ❌ |
| **C. Hueco propio** ✅ (elegida) | Se equipa, se ve en la espalda y es compatible con el traje completo |

El hueco se añade a cada jugador con una **capability** de Forge (un dato extra guardado con el
jugador), **sin mods externos**.

## 📦 Elementos creados

| Mochila | ID | Huecos | O₂ | Energía | Combustible | Agua | Materiales clave |
|---|---|---|---|---|---|---|---|
| Mochila espacial | `pg_space_backpack` | 9 | 1200 | 5 000 FE | 2 000 mB | 2 000 mB | Tela, placa reforzada, cofre (🌍 Tierra) |
| Avanzada | `pg_advanced_space_backpack` | 18 | 3000 | 20 000 FE | 4 000 mB | 4 000 mB | Lunarita, selenita (🌙 Luna) |
| Tecnológica | `pg_tech_space_backpack` | 27 | 6000 | 50 000 FE | 8 000 mB | 8 000 mB | Marteíta, cristal marciano (🔴 Marte) |
| Experimental | `pg_experimental_space_backpack` | 36 | 12000 | 200 000 FE | 16 000 mB | 16 000 mB | Xenita, osmio, cristal cósmico (👽) |

```
Espacial     Avanzada     Tecnológica   Experimental
F P F        L S L        M K M         X K X        F = tela espacial  P = placa reforzada  C = cofre
F C F        F C F        F C F         O C O        L = lunarita  S = selenita  M = marteíta  K = cristal
F F F        L F L        M F M         X O X        X = xenita  O = osmio
```

## 🎮 Cómo se usa
| Acción | Cómo |
|---|---|
| **Equipar** | Clic derecho con la mochila en la mano (si ya llevas otra, se intercambian) |
| **Abrir** | Tecla **B** (se puede cambiar en Opciones → Controles → Panthrixs Galaxy) |
| **Quitar** | **Mayús + B** → vuelve a tu inventario (o al suelo si está lleno) |
| **Llenar de oxígeno** | En el recargador: clic derecho con la mochila en la mano, o con la **mano vacía** para llenar la equipada |

- La pantalla de la mochila muestra el inventario y, a la derecha, un **panel de depósitos** con barras que se actualizan en directo.
- El **oxígeno de la mochila equipada se usa primero**, y después el de las bombonas. El indicador O₂ suma ambos.
- **Energía, combustible y agua** ya existen, pero se llenarán en fases futuras: energía (Fase 7), combustible (Fase 9) y agua (generadores de oxígeno y bases, Fases 7 y 19).
- La mochila **se ve en la espalda** (modelo 3D con dos bombonas laterales), también para los demás jugadores.
- **Al morir** la mochila cae al suelo con todo dentro, como la armadura (con `keepInventory` se conserva).
- No se pueden meter mochilas dentro de mochilas.
- Para mejorar de mochila, fabrica la nueva y pasa las cosas (las recetas no consumen la anterior, así nunca pierdes su contenido).

## 🧠 MCreator vs Java en esta fase
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Objeto con inventario propio | ⚠️ Parcial (inventario simple) | `item/PGBackpackItem.java` + `menu/BackpackContainer.java` |
| **Hueco de mochila nuevo** | ❌ No | Capability: `system/backpack/` |
| Depósitos separados con panel | ❌ No (GUI fija) | `menu/PGBackpackMenu.java` + `client/screen/PGBackpackScreen.java` |
| Tecla para abrir/quitar | ⚠️ Parcial | `client/PGKeyBindings.java` + paquete `BackpackActionPacket` |
| **Mochila visible en la espalda** | ❌ No | `client/BackpackRenderLayer.java` + modelo `models/item/worn/*.json` |

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `item/PGBackpackItem.java` | La mochila: depósitos, equipar, abrir, quitar, descripción |
| `system/backpack/PGBackpackSlot.java` | El hueco de mochila del jugador |
| `system/backpack/PGBackpackSlotProvider.java` | Guarda/carga el hueco con el jugador |
| `system/backpack/PGBackpackEvents.java` | Morir, reaparecer, cambiar de dimensión, sincronizar |
| `menu/BackpackContainer.java` | Lee y guarda los objetos de la mochila |
| `menu/PGBackpackMenu.java` | La ventana (huecos + datos de depósitos) |
| `init/ModMenuTypes.java` | Registro de la ventana |
| `client/screen/PGBackpackScreen.java` | Dibuja la ventana y el panel de depósitos |
| `client/BackpackRenderLayer.java` | Dibuja la mochila en la espalda |
| `client/PGClientModEvents.java` | Registra pantalla, tecla, modelos y capa |
| `client/PGClientEvents.java` | Detecta la tecla B |
| `network/BackpackSyncPacket.java`, `BackpackActionPacket.java` | Mensajes de la mochila |
| `models/item/worn/*.json` + `textures/item/worn/*.png` | Modelo 3D de la espalda |

**Cambios en archivos existentes:**
- `system/oxygen/OxygenHelper.java`: ahora también cuenta y gasta el oxígeno de la mochila equipada (primero la mochila).
- `block/PGOxygenRechargerBlock.java`: también llena mochilas.
- `PanthrixsGalaxy.java`: registra la ventana y el hueco de mochila. `ModCreativeTabs`: las mochilas van a **Equipo**.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. Pestaña **Equipo** → coge una **mochila espacial**.
   - [ ] Al pasar el ratón: huecos y 4 depósitos.
3. Clic derecho con ella → *"Mochila equipada (B para abrirla)"*, desaparece de la mano.
   - [ ] Pulsa **F5**: se ve en la espalda (también con el traje puesto).
4. Pulsa **B** → ventana de 1 fila + panel **Depósitos** a la derecha.
   - [ ] Mete objetos, cierra, vuelve a abrir: siguen ahí. Sal del mundo y vuelve: siguen ahí.
   - [ ] Intenta meter otra mochila dentro: vuelve a tu inventario.
5. Prueba la avanzada, tecnológica y experimental (2, 3 y 4 filas). Clic derecho con otra mochila → se intercambian.
6. **Oxígeno:** coloca un recargador y haz clic derecho con la **mano vacía** → se llena el O₂ de la mochila equipada.
   Después `/pgvacuum true` con el casco puesto y **sin bombonas** → respiras con la mochila; abre la mochila con B y verás bajar la barra O₂.
7. **Mayús + B** → la mochila vuelve al inventario.
8. **Muerte:** con la mochila equipada, `/kill` → la mochila aparece en el suelo con su contenido.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| La tecla B no hace nada | Mira en Opciones → Controles si otra tecla usa B. Necesitas una mochila **equipada**. |
| La mochila no se ve en la espalda | Revisa `models/item/worn/<mochila>.json` y su textura; mira el log por `worn`. |
| Se ve morada y negra en la espalda | Falta `textures/item/worn/<mochila>.png`. |
| El panel de depósitos no aparece | La ventana debe ser la de la mochila (con B), no un cofre normal. |
| Al morir no cae la mochila | Comprueba que `keepInventory` esté desactivado. |

## ✅ Checklist final
- [x] 4 mochilas: 9 / 18 / 27 / 36 huecos
- [x] Depósitos independientes: oxígeno, energía, combustible y agua
- [x] Hueco propio de mochila (capability), sin mods externos
- [x] Equipar, abrir (B), quitar (Mayús+B)
- [x] Pantalla con panel de depósitos en directo
- [x] Mochila visible en la espalda (3D, también para otros jugadores)
- [x] Oxígeno de la mochila conectado al traje y al recargador
- [x] Se guarda al salir, se conserva al cambiar de dimensión y cae al morir
- [ ] Probado en tu PC ← te toca a ti
