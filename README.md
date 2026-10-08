# 🚀 Panthrixs Galaxy

Mod de exploración espacial y supervivencia para **Minecraft Java 1.20.1 + Forge**.
Construye cohetes, despega desde la Tierra, atraviesa la atmósfera y viaja a la Luna, Marte y más allá.

- Mod ID: `panthrixsgalaxy`
- Versión: **1.1.0** · Estado: **Fase 25 — Versión final** ✅ (novedades en [CHANGELOG](CHANGELOG.md))

## Instalar (jugadores)
1. Instala **Minecraft Java 1.20.1** con **Forge 47** (o más nuevo de la 1.20.1).
2. Descarga el `.jar` más reciente (`panthrixsgalaxy-1.20.1-X.Y.Z.jar`) de la pestaña **Releases** de GitHub.
3. Cópialo en la carpeta `mods` de Minecraft y abre el juego con el perfil de Forge.

Al entrar al mundo recibirás un **libro guía** con las instrucciones (si lo pierdes: `/pgguide`).

No necesita otros mods. Funciona en un jugador y en servidores (el `.jar` va en el cliente **y** en el servidor).

## Compilar el `.jar`
```bash
./gradlew build          # Mac / Linux
gradlew.bat build        # Windows
```
El archivo sale en `build/libs/`. GitHub también lo compila solo en cada subida (pestaña **Actions**).

## Probar el mod
```bash
./gradlew runClient      # Mac / Linux
gradlew.bat runClient    # Windows
```
Necesitas Java 17. La primera ejecución descarga Minecraft y Forge (tarda un rato).

## Documentación
- [Plan técnico](docs/PLAN_TECNICO.md) — arquitectura y qué se hace en cada fase
- [Fase 1](docs/FASE_1.md) — proyecto base
- [Fase 2](docs/FASE_2.md) — materiales y minerales
- [Fase 3](docs/FASE_3.md) — herramientas
- [Fase 4](docs/FASE_4.md) — traje espacial
- [Fase 5](docs/FASE_5.md) — oxígeno
- [Fase 6](docs/FASE_6.md) — mochilas
- [Fase 7](docs/FASE_7.md) — energía (7A generar y almacenar · 7B transportar y usar)
- [Fase 8](docs/FASE_8.md) — Banco de Ingeniería Espacial
- [Fase 9](docs/FASE_9.md) — el cohete
- [Fase 10](docs/FASE_10.md) — sistema de lanzamiento
- [Fase 11](docs/FASE_11.md) — de la Tierra al Espacio
- [Fase 12](docs/FASE_12.md) — la Luna
- [Fase 13](docs/FASE_13.md) — Marte
- [Fase 14](docs/FASE_14.md) — naves espaciales
- [Fase 15](docs/FASE_15.md) — armas láser
- [Fase 16](docs/FASE_16.md) — espadas láser
- [Fase 16B](docs/FASE_16B.md) — todos los colores de láseres y espadas
- [Fase 17](docs/FASE_17.md) — criaturas
- [Fase 18](docs/FASE_18.md) — la Reina alienígena (jefe)
- [Fase 19](docs/FASE_19.md) — bases y estaciones espaciales
- [Fase 20](docs/FASE_20.md) — cinturón de asteroides
- [Fase 21](docs/FASE_21.md) — planetas adicionales
- [Fase 22](docs/FASE_22.md) — logros
- [Fase 23](docs/FASE_23.md) — optimización y configuración
- [Fase 24](docs/FASE_24.md) — pruebas finales ([lista de pruebas](docs/PRUEBAS.md))
- [Fase 25](docs/FASE_25.md) — versión final y publicación
- [Libro guía](docs/LIBRO_GUIA.md) — mejora de la versión 1.1.0
