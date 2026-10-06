# FASE 5 — Oxígeno ✅

## 🎯 Objetivo
Que respirar fuera de la Tierra sea un reto: el traje gasta oxígeno de las bombonas, el casco
es imprescindible y, sin él, el jugador recibe una advertencia, unos segundos de margen y
después empieza a perder vida.

## 📦 Elementos creados

| Elemento | ID | Capacidad | Receta |
|---|---|---|---|
| Bombona de oxígeno | `pg_oxygen_tank` | 600 (10 min) | 5 placas reforzadas + 1 palanca (válvula) |
| Bombona de oxígeno grande | `pg_large_oxygen_tank` | 1800 (30 min) | 2 bombonas + 4 placas |
| Tanque de oxígeno espacial | `pg_space_oxygen_tank` | 6000 (100 min) | 2 bombonas grandes + 4 lingotes de **lunarita** |
| Recargador de oxígeno | `pg_oxygen_recharger` | — | placas, cristal, barrotes de hierro, 1 bombona, redstone |

```
Bombona      Grande     Espacial    Recargador
. L .        P T P      L T L       P C P      P = placa reforzada   L = palanca / lingote de lunarita
P . P        P T P      L T L       B T B      T = bombona           C = cristal
P P P                               P R P      B = barrotes de hierro  R = redstone
```

## 🫁 Cómo funciona

### Variables del sistema
| Variable | Valor | Dónde se cambia |
|---|---|---|
| Oxígeno máximo | Capacidad de cada bombona (600 / 1800 / 6000) | `init/ModItems.java` |
| Oxígeno actual | Guardado dentro de cada bombona (barra azul bajo el icono) | — |
| Consumo | 1 unidad por segundo | `PGOxygenEvents.CONSUMPTION_PER_SECOND` |
| Aviso de oxígeno bajo | Menos de 60 (1 minuto) | `PGOxygenEvents.LOW_OXYGEN` |
| Tiempo de gracia | 5 segundos | `PGOxygenEvents.GRACE_SECONDS` |
| Daño | ½ corazón por segundo; 1 corazón a partir de 15 s | `PGOxygenEvents.STRONG_DAMAGE_SECONDS` |
| Recarga | Llena la bombona al instante en el recargador | `block/PGOxygenRechargerBlock.java` |

### Lo que pasa cada segundo
```
¿Hay aire respirable?
├── SÍ → no se gasta nada, no se muestra el indicador.
└── NO → ¿llevas casco espacial y te queda oxígeno en alguna bombona?
         ├── SÍ → se gasta 1 de oxígeno. Si queda menos de 1 minuto: aviso "Oxígeno bajo".
         └── NO → pitido de alarma + aviso rojo "¡SIN CASCO!" o "¡SIN OXÍGENO!"
                  → 5 segundos de margen ("Te quedan 4 s de aire"...)
                  → después: pierdes vida poco a poco ("¡Te estás asfixiando!").
```
- Las bombonas sirven en **cualquier sitio del inventario** (o en la mano secundaria). Se gastan en orden.
- *(Desde la Fase 6)* El depósito de oxígeno de la **mochila equipada** se gasta antes que las bombonas.
- El daño por falta de oxígeno **atraviesa la armadura**. Mensaje de muerte: *"Jugador se quedó sin oxígeno"*.
- En **creativo** no se gasta oxígeno ni se recibe daño.
- Las bombonas fabricadas salen **vacías**; las de la pestaña creativa salen **llenas**.
- El recargador **no funciona donde no hay aire** (Luna, Marte). En la Fase 7 haremos uno con energía.

### Indicador en pantalla (HUD)
Solo aparece cuando no hay aire. Encima de la barra de comida:
`O₂ [██████████░░░░] 72%` (azul = bien, naranja = bajo, rojo = peligro), y avisos en el centro.

### ¿Dónde "no hay aire"?
La Luna, Marte y el espacio aún no existen, así que **en la Tierra siempre hay aire**.
Para probar el sistema he añadido un **modo de prueba**:
```
/pgvacuum true    → a partir de ahora, para ti no hay aire (como en el espacio)
/pgvacuum false   → vuelve a la normalidad
```
(Al morir, el modo de prueba se desactiva solo.)
Cuando creemos la Luna (Fase 12) bastará con una línea: `PGAtmosphere.addAirlessDimension(LUNA)`.

## 🧠 MCreator vs Java en esta fase
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Bombonas con oxígeno guardado (NBT) y barra | ⚠️ Parcial (procedimientos con NBT) | `item/PGOxygenTankItem.java` |
| Comprobación cada segundo, casco, gracia y daño | ⚠️ Parcial (procedimiento "cada tick") | `system/oxygen/PGOxygenEvents.java` |
| Recargador (clic derecho) | ✅ Sí | `block/PGOxygenRechargerBlock.java` |
| Daño propio con mensaje de muerte | ⚠️ Limitado | `damage_type/no_oxygen.json` + `init/ModDamageTypes.java` |
| **Indicador en pantalla** con datos del servidor | ❌ No bien: MCreator tiene "overlays", pero no envía datos del servidor de forma fiable | `network/` (paquete) + `client/OxygenHudOverlay.java` |
| Comando de prueba | ✅ Sí | `command/PGCommands.java` |

**Nuevo concepto: la red (`network/`).** En Minecraft el *servidor* (que existe incluso en un mundo de
un jugador) decide qué pasa, y el *cliente* dibuja la pantalla. Para que el indicador sepa cuánto
oxígeno queda, el servidor le manda un mensaje pequeño (un "paquete") cada segundo. Este mismo canal
lo usaremos para cohetes, naves y máquinas.

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `system/oxygen/PGAtmosphere.java` | Dónde hay aire y dónde no (+ modo de prueba) |
| `system/oxygen/PGOxygenEvents.java` | La lógica de cada segundo (consumo, avisos, daño) |
| `system/oxygen/OxygenHelper.java` | Contar y gastar el oxígeno de las bombonas |
| `system/oxygen/OxygenState.java` | Los 5 estados posibles (con aire, bien, bajo, sin casco, sin oxígeno) |
| `item/PGOxygenTankItem.java` | Las bombonas |
| `block/PGOxygenRechargerBlock.java` | El recargador |
| `init/ModDamageTypes.java` + `data/panthrixsgalaxy/damage_type/no_oxygen.json` | El daño por falta de oxígeno |
| `network/PGNetwork.java`, `network/OxygenSyncPacket.java` | Mensajes servidor → pantalla |
| `client/ClientOxygenData.java`, `client/OxygenHudOverlay.java` | El indicador en pantalla |
| `command/PGCommands.java` | El comando `/pgvacuum` |

**Cambios en archivos existentes:** `PanthrixsGalaxy.java` registra la red; `ModItems`/`ModBlocks`
tienen las secciones OXÍGENO; `ModCreativeTabs` muestra las bombonas llenas.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`. Crea un mundo con **trucos activados**.
2. Ponte en **supervivencia**: `/gamemode survival`.
3. **Sin casco:** `/pgvacuum true`
   - [ ] Suena un pitido y aparece en rojo parpadeante *"⚠ ¡SIN CASCO! No hay aire respirable"*.
   - [ ] Debajo: *"Te quedan 4 s de aire"*, que baja cada segundo.
   - [ ] Al llegar a 0: *"¡Te estás asfixiando!"* y pierdes ½ corazón por segundo (1 corazón tras 15 s).
4. **Con casco y bombona:** coge una bombona llena de la pestaña creativa y ponte el casco espacial.
   - [ ] El aviso desaparece y la barra **O₂** se ve azul.
   - [ ] La barra azul bajo el icono de la bombona baja poco a poco; el texto al pasar el ratón cuenta los segundos.
5. **Oxígeno bajo / vacío:** `/give @s panthrixsgalaxy:pg_oxygen_tank` (sale vacía).
   Quédate solo con esa bombona → *"⚠ ¡SIN OXÍGENO!"*. Con poco oxígeno → *"Oxígeno bajo"* en naranja.
6. **Recargador:** coloca un recargador, clic derecho con una bombona vacía → sonido, nubecita y *"Bombona llena de oxígeno"*.
7. **Muerte:** sin casco, espera hasta morir → mensaje *"<tu nombre> se quedó sin oxígeno"*.
8. `/pgvacuum false` → todo vuelve a la normalidad y el indicador desaparece.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| `/pgvacuum` dice "comando desconocido" | El mundo necesita **trucos activados** (o abrir a LAN con trucos). |
| No aparece el indicador | Solo se ve sin aire (`/pgvacuum true`). Si sigue sin verse, revisa `client/OxygenHudOverlay.java` y el log. |
| El juego se cierra al recibir daño por oxígeno | Falta `data/panthrixsgalaxy/damage_type/no_oxygen.json` o tiene un error. |
| El mensaje de muerte sale como `death.attack.panthrixsgalaxy...` | Falta la línea en `lang/es_es.json`. |
| Error al conectarse a un servidor: versión de red distinta | Cliente y servidor deben tener la **misma versión** del mod. |

## ✅ Checklist final
- [x] 3 bombonas (10, 30 y 100 minutos) con oxígeno guardado y barra
- [x] Recargador de oxígeno
- [x] Consumo por segundo solo donde no hay aire
- [x] Casco imprescindible: advertencia, 5 s de gracia y daño progresivo
- [x] Indicador en pantalla con avisos
- [x] Tipo de daño propio con mensaje de muerte
- [x] Modo de prueba `/pgvacuum`
- [x] Preparado para Luna/Marte (`addAirlessDimension`) y mochila (Fase 6)
- [ ] Probado en tu PC ← te toca a ti
