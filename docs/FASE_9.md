# FASE 9 — El cohete ✅

## 🎯 Objetivo
Fabricar los componentes, construir el cohete, montar la plataforma de lanzamiento, colocar el
cohete, repostarlo y subirse. (El despegue llega en la **Fase 10**.)

## 📦 Elementos creados

### 🚀 Componentes (Banco de Ingeniería → categoría **Cohetes**)
| Componente | ID | Receta |
|---|---|---|
| Casco del cohete | `pg_rocket_hull` | 8 placas reforzadas + 1 bloque de hierro |
| Motor de cohete | `pg_rocket_engine` | Placas, bloque de redstone, alto horno, 2 lingotes de hierro (toberas) |
| Depósito de combustible | `pg_rocket_fuel_tank` | Placas, 2 bloques de cobre, cubo |
| Cono de proa | `pg_rocket_nose_cone` | Pararrayos (punta), placas, panel de cristal |
| Aletas ×4 | `pg_rocket_fins` | 6 placas en escalera |

### 🚀 Cohetes (Banco → **Cohetes**)
| Cohete | ID | Destino | Combustible máx. | Receta |
|---|---|---|---|---|
| Cohete básico | `pg_basic_rocket` | 🌙 Luna | 2 000 mB | Cono + 2 cascos + depósito + 2 aletas + motor |
| Cohete avanzado | `pg_advanced_rocket` | 🔴 Marte | 5 000 mB | Lo mismo + 2 **bloques de lunarita** |

```
  .  N  .        L  N  L       N = cono de proa   H = casco   T = depósito
  H  T  H        H  T  H       F = aletas   E = motor   L = bloque de lunarita
  F  E  F        F  E  F
  (básico)       (avanzado)
```

### ⛽ Combustible
| Elemento | ID | Dónde | Detalles |
|---|---|---|---|
| Refinería de combustible | `pg_fuel_refinery` | Banco → **Máquinas** | Usa 40 FE/t; produce 40 mB/s; guarda 10 000 mB |
| Bidón de combustible | `pg_fuel_canister` | Mesa de trabajo (5 placas + cubo) | 1 000 mB |

| Material en la refinería | Combustible |
|---|---|
| Carbón / carbón vegetal | 100 mB |
| Polvo de blaze | 250 mB |
| Vara de blaze / helio-3 | 500 mB |
| Bloque de carbón | 900 mB |

### 🟨 Plataforma de lanzamiento
| Bloque | ID | Receta |
|---|---|---|
| Plataforma de lanzamiento ×4 | `pg_launch_pad` | 2 placas + tinte amarillo + 3 lingotes de hierro (mesa de trabajo) |

Hacen falta **9** (un cuadrado de **3×3**). El cohete se coloca en el **bloque central** y necesita 3 bloques libres encima.

## 🎮 Cómo se usa
| Acción | Resultado |
|---|---|
| Clic derecho con el cohete en el **centro** de la plataforma 3×3 | Coloca el cohete (mirando hacia donde miras) |
| Clic derecho en la refinería con carbón/blaze/helio-3 (o tolva) | Añade material para refinar |
| Clic derecho en la refinería con un **bidón** | Llena el bidón |
| Clic derecho en la refinería con la **mano vacía** | Llena el **depósito de combustible de la mochila** + información |
| Clic derecho en el cohete con un **bidón** | Pasa el combustible al cohete |
| Clic derecho en el cohete (mano vacía) | **Te subes**. Si llevas mochila, pasa su combustible al cohete automáticamente |
| **Mayús** (dentro) | Te bajas |
| **Mayús + clic derecho** con la mano vacía | Recoges el cohete (conserva el combustible) |

Dentro del cohete:
- Arriba aparece un **panel**: tipo de cohete, destino y barra de combustible.
- En **primera persona** ves el exterior, como desde la cabina. En tercera persona (F5) se ve el cohete y no el astronauta (va dentro).

## 🧠 MCreator vs Java
| Parte | ¿MCreator puede? | Qué hicimos |
|---|---|---|
| Componentes, bidón, plataforma | ✅ Sí | Objetos y bloques normales |
| Recetas en el Banco | ✅ (con nuestro tipo de receta) | JSON `panthrixsgalaxy:engineering`, categoría `rockets` |
| Refinería | ⚠️ Parcial | `block/entity/PGFuelRefineryBlockEntity.java` (reutiliza la base de máquinas de la Fase 7) |
| **Cohete como entidad montable con combustible** | ❌ No bien (MCreator hace mobs, no vehículos) | `entity/rocket/PGRocketEntity.java` |
| Modelo 3D y textura del cohete | ⚠️ (Blockbench + MCreator) | `client/model/PGRocketModel.java` + `textures/entity/rocket/*.png` |
| Vista de cabina y panel | ❌ No | `client/renderer/PGRocketRenderer.java`, `client/RocketHudOverlay.java` |

**Nuevo concepto: entidad.** Algo que existe en el mundo y se mueve (mobs, barcas, flechas…). Nuestro
cohete es una entidad, como una barca: se puede montar y guarda datos (nivel, combustible).

## 🗂️ Dónde está cada cosa
| Archivo | Qué hace |
|---|---|
| `entity/rocket/PGRocketEntity.java` | El cohete en el mundo: subir, repostar, recoger, guardar |
| `entity/rocket/RocketTier.java` | Niveles de cohete (básico, avanzado): combustible y alcance |
| `item/PGRocketItem.java` | El cohete como objeto: se coloca en la plataforma 3×3 |
| `item/PGFuelCanisterItem.java` | Bidón de combustible |
| `block/entity/PGFuelRefineryBlockEntity.java` | Refinería |
| `init/ModEntities.java` | Registro de la entidad cohete |
| `client/model/PGRocketModel.java` | Forma 3D del cohete (cajas) |
| `client/renderer/PGRocketRenderer.java` | Cómo se dibuja (y vista de cabina) |
| `client/RocketHudOverlay.java` | Panel de combustible |
| `textures/entity/rocket/basic.png`, `advanced.png` | Texturas del cohete (128×128) |

**Cambios en archivos existentes:** `PanthrixsGalaxy.java` (registra las entidades), `ModItems`/`ModBlocks`/`ModBlockEntities`
(sección COHETES), `ModCreativeTabs` (bidones vacíos y llenos; cohetes en Equipo), `PGClientModEvents` (dibujo del cohete),
`PGClientEvents` (no dibujar al astronauta dentro).

**Para cambiar el aspecto del cohete:** edita `textures/entity/rocket/basic.png`. La plantilla sigue el esquema de cajas de
Minecraft; con **Blockbench** (formato *Modded Entity*) puedes recrear el modelo y pintarlo cómodamente.

## 🧪 Cómo probarlo
1. `gradlew.bat runClient` (Windows) o `./gradlew runClient`.
2. **Plataforma:** coloca 9 plataformas de lanzamiento en 3×3.
3. **Cohete:** clic derecho con el **cohete básico** en el bloque **central**.
   - [ ] Aparece el cohete 3D (blanco y rojo) de casi 3 bloques de alto.
   - [ ] Si lo pones fuera del centro o sin plataforma → aviso en rojo.
4. **Refinería:** colócala, dale energía (generador pegado o cables) y échale carbón.
   - [ ] Se enciende y el combustible sube (clic derecho con la mano vacía para verlo).
   - [ ] Clic derecho con un bidón → se llena.
5. **Repostar:** clic derecho al cohete con el bidón → *"Combustible del cohete: 1000 / 2000 mB"*.
6. **Subirse:** clic derecho con la mano vacía.
   - [ ] Panel arriba con la barra de combustible.
   - [ ] En primera persona ves el exterior; con F5 ves el cohete.
   - [ ] Mayús para bajarte.
7. **Mochila:** llena la mochila en la refinería (mano vacía), súbete al cohete → mensaje de combustible pasado.
8. **Recoger:** Mayús + clic derecho con la mano vacía → el cohete vuelve al inventario con su combustible (mira la descripción).
9. Cohete **avanzado**: mismo proceso, se ve azul claro y admite 5 000 mB.

## 🐛 Errores comunes
| Problema | Solución |
|---|---|
| El cohete es invisible | Falta el registro del dibujo (`PGClientModEvents.onRegisterRenderers`) o la textura `textures/entity/rocket/basic.png`. |
| El cohete sale morado y negro | Falta la textura de ese nivel. |
| No me deja colocarlo | Debe ser el bloque **central** de 9 plataformas, con 3 bloques de aire encima. |
| La refinería no hace nada | Necesita **energía** y material. Mira el estado con clic derecho y la mano vacía. |
| No puedo recogerlo | Bájate primero, y usa Mayús + clic derecho **con la mano vacía**. |

## ✅ Checklist final
- [x] 5 componentes del cohete en el Banco
- [x] Cohete básico (Luna) y avanzado (Marte) con modelo 3D y texturas
- [x] Plataforma de lanzamiento 3×3
- [x] Refinería de combustible y bidón
- [x] Repostar con bidón o con la mochila
- [x] Subir, bajar y recoger el cohete
- [x] Panel de combustible y vista de cabina
- [x] Preparado para el despegue (nivel y alcance en `RocketTier`)
- [ ] Probado en tu PC ← te toca a ti
