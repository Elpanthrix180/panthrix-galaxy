# Fase 25 — Versión final y publicación

## 🎯 Objetivo
Convertir el proyecto en un mod **listo para compartir**: un `.jar` con nombre y versión,
logo en la lista de mods, notas de la versión y una forma automática de compilarlo.

## 📦 Qué se ha preparado
| Elemento | Dónde | Para qué |
|---|---|---|
| Versión **1.0.0** | `gradle.properties` → `mod_version` | Número que ve el jugador |
| Nombre del `.jar` | `build.gradle` → `archivesName` | `panthrixsgalaxy-1.20.1-1.0.0.jar` (versión de Minecraft + del mod) |
| Logo | `src/main/resources/panthrixsgalaxy_logo.png` | Sale en el menú **Mods** de Forge |
| Datos del mod | `META-INF/mods.toml` | Créditos, web y página de errores |
| Novedades | `CHANGELOG.md` | Texto para la Release y para CurseForge/Modrinth |
| Compilación automática | `.github/workflows/build.yml` | GitHub compila el `.jar` en cada subida |
| Objetos de prueba ocultos | `ModCreativeTabs.isTestItem` | `pg_test_item` y `pg_test_block` solo salen en creativo con *Objetos de operador* activado |

> Los objetos de prueba **no se borran**: si alguien los tenía en un mundo, desaparecerían.
> Siguen existiendo (`/give`), solo que no estorban en las pestañas.

## 🛠️ Cómo sacar el `.jar`
### Opción A — En tu ordenador
1. Abre una terminal en la carpeta del proyecto.
2. `gradlew.bat build` (Windows) o `./gradlew build` (Mac/Linux).
3. Espera a `BUILD SUCCESSFUL`.
4. El mod está en `build/libs/panthrixsgalaxy-1.20.1-1.0.0.jar`.

### Opción B — GitHub lo hace por ti
1. Sube los cambios (push). En GitHub, pestaña **Actions** → "Compilar mod".
2. Cuando salga ✅, entra en la ejecución y descarga **panthrixsgalaxy-jar** (abajo, *Artifacts*).
3. Si sale ❌, abre el paso **Compilar**: ahí está el error de Java. Pégamelo y lo arreglamos.

### Crear una Release (descarga pública)
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub compila y crea la Release **v1.0.0** con el `.jar` y el texto de `CHANGELOG.md`.

## 🧪 Prueba final con el `.jar` (no con `runClient`)
1. Launcher de Minecraft → instala Forge 1.20.1 (47.x).
2. Copia el `.jar` en `.minecraft/mods`.
3. Abre el juego: en **Mods** debe salir *Panthrixs Galaxy 1.0.0* con el logo.
4. Crea un mundo nuevo y repasa lo esencial de [PRUEBAS.md](PRUEBAS.md).
5. Servidor: carpeta de un servidor Forge 1.20.1 → `mods/` → entra con 2 jugadores.

## 🌍 Publicar en CurseForge o Modrinth
1. Crea una cuenta y un proyecto nuevo (tipo *Mod*, Forge, 1.20.1).
2. **Descripción**: usa el README y el CHANGELOG. Añade capturas (Luna, Marte, cohete, Reina).
3. **Icono**: el logo (`panthrixsgalaxy_logo.png`), mejor recortado a cuadrado.
4. **Categorías**: *Adventure*, *Technology*, *Dimensions*.
5. Sube el `.jar`, tipo *Release*, versión del juego 1.20.1, cargador Forge.
6. Espera la revisión (de horas a un par de días).

## 📜 Licencia
Ahora mismo es **All Rights Reserved** (nadie puede copiar ni modificar el mod sin permiso).
Si quieres que otros puedan aprender de tu código o hacer addons, puedes cambiarla a **MIT**
en `gradle.properties` (`mod_license=MIT`) y añadir un archivo `LICENSE`. Es decisión tuya.

## 🔢 Próximas versiones
- Arreglo pequeño → `1.0.1`. Cosas nuevas → `1.1.0`. Cambio grande que rompe mundos → `2.0.0`.
- Cambia `mod_version`, añade una sección arriba en `CHANGELOG.md`, crea la etiqueta `v1.0.1`.
- **No borres ni cambies de nombre** bloques u objetos: los mundos guardados los perderían.

## 🧩 MCreator
MCreator exporta el `.jar` con *Build → Export mod*. Versión, logo y créditos están en
*Workspace settings*. La compilación automática de GitHub no aplica a MCreator.

## ✅ Lista de comprobación
- [ ] `gradlew build` → `BUILD SUCCESSFUL` (o ✅ en GitHub Actions)
- [ ] El `.jar` funciona en el launcher normal y sale el logo en **Mods**
- [ ] Servidor dedicado con el `.jar` arranca sin errores
- [ ] Release `v1.0.0` creada
- [ ] (Opcional) Publicado en CurseForge / Modrinth
