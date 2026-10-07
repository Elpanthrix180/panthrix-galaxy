# 🚀 Panthrixs Galaxy — Plan técnico

Documento vivo: lo actualizaremos al terminar cada fase.

## 1. Datos del proyecto

| Campo | Valor |
|---|---|
| Nombre | Panthrixs Galaxy |
| Mod ID / Namespace | `panthrixsgalaxy` |
| Minecraft | Java Edition 1.20.1 |
| Loader | Forge 47.x (proyecto creado con 47.3.0) |
| Java | 17 |
| Paquete Java | `com.panthrixsgalaxy` |
| Dependencias externas | Ninguna (solo Forge) |

## 2. MCreator y este repositorio (importante)

MCreator es un programa de escritorio con ventanas: **no se puede ejecutar en este entorno en la nube**.
Lo que hace MCreator por dentro es generar un proyecto Forge con Gradle, código Java y archivos JSON.
Este repositorio es exactamente eso, pero escrito a mano y ordenado, así que:

- Todo lo que MCreator haría (objetos, bloques, recetas, armaduras, logros, mobs sencillos, dimensiones)
  lo escribimos en archivos JSON o en Java muy simple y comentado.
- Las partes que **MCreator no sabe hacer bien** (cohete que despega físicamente, oxígeno, energía,
  mochilas con depósitos, gravedad reducida, cielo espacial) habrían necesitado Java de todas formas.

> ⚠️ Un workspace de MCreator (`.mcreator`) y un proyecto Forge escrito a mano **no se pueden mezclar
> fácilmente**: MCreator regenera su código y sobrescribe los cambios. Hay que elegir un camino.
> Recomendación: seguir con este proyecto (lo vemos y lo explicamos fase a fase).
> Si prefieres MCreator, se puede crear el workspace en tu PC y yo te guío paso a paso; los nombres,
> texturas y diseño de este plan sirven igual.

## 3. Convención de nombres

En el diseño usamos `PG_Nombre`. Minecraft obliga a que los IDs de registro estén **en minúsculas**,
así que se convierten así:

| Diseño | ID en el juego | Constante Java |
|---|---|---|
| PG_Space_Helmet | `panthrixsgalaxy:pg_space_helmet` | `PG_SPACE_HELMET` |
| PG_Lunarite | `panthrixsgalaxy:pg_lunarite` | `PG_LUNARITE` |
| PG_Space_Rocket | `panthrixsgalaxy:pg_space_rocket` | `PG_SPACE_ROCKET` |

## 4. Organización del código

```
src/main/java/com/panthrixsgalaxy/
├── PanthrixsGalaxy.java     Clase principal (solo conecta los registros)
├── init/                    Listas de registro: ModItems, ModBlocks, ModCreativeTabs...
├── item/                    Objetos con comportamiento propio
├── armor/                   Traje espacial (material + piezas)
├── event/                   Reacciones a eventos del juego (traje completo...)
├── tool/                    Niveles de herramienta (PGToolTiers)
├── weapon/                  (Fase 15-16) Láseres y espadas láser
├── entity/rocket/           Cohete: entidad y niveles (Fase 9)
├── entity/                  (Fase 14, 17-18) Naves, mobs, jefes
├── client/model/, renderer/ Modelos 3D y dibujo de entidades
├── world/feature/           Generación propia (cráteres)
├── system/gravity/          Gravedad por planeta
├── system/weather/          Tormentas de polvo de Marte
├── system/oxygen/           Oxígeno: atmósfera, consumo, avisos, daño
├── block/                   Bloques con comportamiento propio (recargador, máquinas)
├── block/entity/            "Cerebros" de las máquinas (block entities)
├── system/energy/           Energía: almacén, baterías, carga de objetos
├── network/                 Mensajes servidor <-> pantalla
├── client/                  Solo pantalla: indicadores (HUD)
├── command/                 Comandos de prueba (/pgvacuum)
├── system/backpack/         Hueco de mochila del jugador (capability)
├── menu/ + client/screen/   Ventanas: mochila (Fase 6), Banco de Ingeniería (Fase 8)
├── recipe/                  Recetas del Banco de Ingeniería (tipo propio + categorías)
├── client/sky/              Cielo de la dimensión Espacio
└── planet/                  Registro de cuerpos celestes (PGPlanets)
src/main/resources/
├── assets/panthrixsgalaxy/  blockstates, models, textures, lang, sounds
└── data/panthrixsgalaxy/    recipes, loot_tables, advancements, dimension, worldgen, tags
```
Las carpetas marcadas con (Fase N) se crean cuando lleguemos a esa fase.

## 5. Arquitectura clave para el futuro

### 5.1 Registro de planetas (preparado para añadir planetas sin rehacer nada)
Cada cuerpo celeste será un objeto de datos `PGPlanet` con: id, dimensión, gravedad, ¿oxígeno?,
temperatura, nivel de cohete necesario y posición en el mapa espacial. Añadir Venus = añadir
una entrada + su dimensión JSON + texturas. Todo lo demás (oxígeno, gravedad, navegación) lee
esos datos automáticamente.

### 5.2 Viaje físico (sin portales)
```
Tierra (overworld) → cohete sube (entidad con física propia, cámara del jugador montado)
→ al pasar Y≈350 cambia a dimensión "Espacio" (cielo negro, estrellas, planetas renderizados)
→ el jugador pilota la nave en el Espacio hacia el planeta
→ al acercarse al planeta → cambia a la dimensión del planeta a gran altura
→ descenso controlado → aterrizaje.
```
El cambio de dimensión es inevitable (Minecraft no permite dos mundos en uno), pero se hace en
pleno vuelo, sin portal ni pantalla de "pulsa botón", para que se sienta continuo.

### 5.3 Sistemas con datos guardados en el jugador / objeto
- **Oxígeno y energía**: guardados en el propio objeto (bombona, batería, mochila) mediante NBT.
- **Advertencia sin casco**: comprobación cada segundo con tiempo de gracia antes del daño.
- **Energía**: usaremos el sistema de energía que ya trae Forge (`IEnergyStorage`), sin APIs externas,
  mostrado en el juego como "PG Energía".

## 6. Fases: qué hace MCreator y qué necesita Java

| Fase | Sistema | ¿MCreator puede? | Solución aquí |
|---|---|---|---|
| 1 | Proyecto base | Sí | ✅ Hecho |
| 2 | Materiales y minerales | Sí | ✅ Hecho (generación en el mundo: Fases 12, 13, 20, 21) |
| 3 | Herramientas | Sí (tiers simples) | ✅ Hecho: 4 niveles con orden propio (Java mínimo: `TierSortingRegistry`) |
| 4 | Traje espacial | Sí (armadura) | ✅ Hecho. Guantes integrados en la pechera (no hay ranura de manos); mochila en Fase 6 |
| 5 | Oxígeno | Parcial (procedimientos) | ✅ Hecho: bombonas con NBT, evento por segundo, HUD con red propia, `/pgvacuum` |
| 6 | Mochilas | Parcial (sin depósitos separados) | ✅ Hecho: hueco propio (capability), 4 mochilas, depósitos, panel, modelo en la espalda |
| 7 | Energía | Parcial | ✅ 7A generar y almacenar · 7B cables, recargador eléctrico, oxígeno de emergencia |
| 8 | Banco de Ingeniería | Parcial (GUI básica) | ✅ Hecho: tipo de receta propio con categorías, guía integrada, 16 recetas movidas |
| 9 | Cohete | No (entidad montable con física) | ✅ Hecho: componentes, 2 cohetes (entidad + modelo), plataforma 3×3, refinería y combustible |
| 10 | Lanzamiento | No | ✅ Hecho: máquina de estados, cuenta atrás, ascenso físico, explosión, descenso controlado |
| 11 | Tierra → Espacio | No | ✅ Hecho: dimensión Espacio, cielo con planetas, transición en vuelo, navegación, registro de planetas |
| 12 | Luna | Parcial (dimensión) | ✅ Hecho: dimensión JSON, cráteres (Java), menas, gravedad, temperatura, cielo con la Tierra, logro |
| 13 | Marte | Parcial | ✅ Hecho: dimensión JSON con montañas, menas, hielo, tormentas de polvo (Java), logro |
| 14 | Naves | No | ✅ Hecho: nave y nave avanzada pilotables (Java), bodega, HUD, viajes entre planetas |
| 15 | Armas láser | Parcial | ✅ Hecho: 6 pistolas de colores (16B) y rifle láser (Java: proyectil + energía FE del arma o la mochila), tipo de daño |
| 16 | Espadas láser | Parcial | ✅ Hecho: 7 espadas (16B) con habilidades; encender/apagar, energía, guardia que desvía láseres |
| 17 | Mobs | Sí (básicos) | ✅ Hecho: 9 criaturas (Luna, Marte, aliens) con IA propia, aparición por planeta, botín |
| 18 | Jefes | Parcial | ✅ Hecho: Reina alienígena (3 fases, barra de jefe, baliza de invocación, necronita, logro) |
| 19 | Estaciones espaciales | Parcial | ✅ Hecho: bloques de base, salas selladas con aire, distribuidor y tanque de oxígeno, módulo habitable |
| 20 | Asteroides | Parcial | ✅ Hecho: cinturón de asteroides (dimensión), asteroides con minerales, restos de naves, puestos alienígenas |
| 21 | Planetas adicionales | Sí (dimensiones JSON) | ✅ Hecho: Mercurio, Venus, Plutón, Xenoria (alienígena) + 4 gigantes gaseosos; colmena de la Reina |
| 22 | Logros | Sí | ✅ Hecho: 30 logros en árbol, contadores Java, secretos (monolito, aldea alienígena, planeta Nyx) |
| 23 | Optimización | — | ✅ Hecho: caché de cables, distribuidores en reposo, estrellas en GPU, menos paquetes; archivo de configuración |
| 24 | Pruebas finales ✅ | Comandos de prueba | PRUEBAS.md, revisión y arreglos |
| 25 | Lanzamiento | — | `.jar` final |

## 7. Progresión

Tierra → herramientas → traje → mochila → oxígeno → cohete → Luna → minerales lunares →
energía → láseres → Marte → mobs → espadas láser → Sistema Solar → asteroides →
tecnología alienígena → planetas alienígenas → jefes finales.
