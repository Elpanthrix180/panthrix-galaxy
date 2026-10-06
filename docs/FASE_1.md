# FASE 1 — Proyecto base ✅

## 🎯 Objetivo
Tener un mod que Minecraft 1.20.1 + Forge cargue sin errores, con un objeto y un bloque de prueba.

## 📦 Elementos creados

| Elemento | ID | Para qué sirve |
|---|---|---|
| PG_Test_Item | `panthrixsgalaxy:pg_test_item` | Clic derecho → mensaje en el chat (prueba que el código Java funciona) |
| PG_Test_Block | `panthrixsgalaxy:pg_test_block` | Bloque metálico que da luz (prueba modelos, texturas, minado y drop) |
| Pestaña creativa | `panthrixsgalaxy:pg_main_tab` | Agrupa todos los objetos del mod |

## 🗂️ Dónde está cada cosa

| Archivo | Qué hace |
|---|---|
| `gradle.properties` | Versión de Minecraft/Forge y datos del mod (nombre, versión, autor) |
| `build.gradle`, `settings.gradle` | Configuración de compilación (como el "motor" de MCreator) |
| `src/main/java/com/panthrixsgalaxy/PanthrixsGalaxy.java` | Clase principal |
| `.../init/ModItems.java` | Lista de objetos |
| `.../init/ModBlocks.java` | Lista de bloques |
| `.../init/ModCreativeTabs.java` | Pestaña creativa |
| `.../item/PGTestItem.java` | Comportamiento del objeto de prueba |
| `src/main/resources/META-INF/mods.toml` | Ficha del mod que lee Forge |
| `assets/panthrixsgalaxy/lang/es_es.json` / `en_us.json` | Nombres en español / inglés |
| `assets/panthrixsgalaxy/textures/item/pg_test_item.png` | Textura del objeto (16×16) |
| `assets/panthrixsgalaxy/textures/block/pg_test_block.png` | Textura del bloque (16×16) |
| `assets/panthrixsgalaxy/models/...`, `blockstates/...` | Cómo se dibujan objeto y bloque |
| `data/panthrixsgalaxy/loot_tables/blocks/pg_test_block.json` | El bloque se suelta a sí mismo |
| `data/minecraft/tags/blocks/mineable/pickaxe.json` | El bloque se mina con pico |

## 🛠️ Cómo probarlo en tu PC

1. Instala **Java 17** (por ejemplo Temurin 17). Si no lo tienes, Gradle intentará descargarlo.
2. Descarga el proyecto (`git clone` o "Download ZIP" en GitHub).
3. Abre una terminal en la carpeta del proyecto y ejecuta:
   - Windows: `gradlew.bat runClient`
   - Mac/Linux: `./gradlew runClient`
4. La primera vez tarda bastante (10–20 min): descarga Minecraft y Forge.
5. Se abre Minecraft. Crea un mundo en **Creativo**.

## 🧪 Pruebas
- [ ] En el menú principal → **Mods** aparece **Panthrixs Galaxy 0.1.0**.
- [ ] En el inventario creativo hay una pestaña **Panthrixs Galaxy** con la estrella.
- [ ] El objeto se llama **Objeto de prueba PG** y tiene descripción gris.
- [ ] Clic derecho con el objeto → mensaje cian *"¡Panthrixs Galaxy funciona!"*.
- [ ] Coloca el **Bloque de prueba PG**: se ve con textura (no morado y negro) y da luz.
- [ ] En Supervivencia, rompe el bloque con un pico → lo suelta. Con la mano → no suelta nada.

Para crear el `.jar` instalable: `gradlew build` → aparece en `build/libs/panthrixsgalaxy-0.1.0.jar`.
Se copia en la carpeta `mods` de una instalación de Forge 1.20.1.

## 🐛 Errores comunes

| Problema | Solución |
|---|---|
| `Unsupported class file major version` | Estás usando otra versión de Java. Usa Java 17. |
| Textura morada y negra | Nombre del `.png` o ruta del modelo mal escrita (todo en minúsculas). |
| Nombre raro tipo `item.panthrixsgalaxy.pg_test_item` | Falta la línea en `es_es.json` / `en_us.json`. |
| El juego no abre / "Mod file ... failed" | Mira `run/logs/latest.log` y busca `panthrixsgalaxy` o `ERROR`. |
| Gradle falla al descargar | Revisa tu conexión y vuelve a ejecutar; a veces el servidor de Forge va lento. |

## ✅ Checklist final
- [x] Proyecto Forge 1.20.1, Mod ID `panthrixsgalaxy`
- [x] Estructura organizada (`init/`, `item/`, recursos separados)
- [x] Objeto de prueba con textura, nombre y comportamiento
- [x] Bloque de prueba con textura, modelo, luz, drop y herramienta
- [x] Traducciones español e inglés
- [ ] Comprobado en tu PC con `runClient` ← te toca a ti
