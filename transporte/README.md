# 🚌 Transporte Ibérico 1.0.0

Mod para **Minecraft Java 1.20.1 + Forge 47** con un vehículo: el **Autobús Urbano Azul**,
inspirado en los autobuses de Madrid. Lo puedes colocar, subirte, conducirlo con W/A/S/D y bajarte.

- ID del mod: `transporteiberico` · Paquete Java: `com.transporteiberico`
- Archivo final: `transporteiberico-1.0.0.jar`
- Sin dependencias: solo Forge.

---

## Camino fácil: descargar el `.jar` ya hecho (sin programar nada)

GitHub compila y prueba el mod solo, cada vez que hay cambios.

1. Entra en la página del proyecto en GitHub y pulsa la pestaña **Actions**.
2. Pulsa la ejecución más reciente que tenga un ✅ verde.
3. Baja hasta **Artifacts** y pulsa **transporteiberico-jar**. Se descarga un `.zip`.
4. Abre el `.zip` (doble clic) y saca el archivo `transporteiberico-1.0.0.jar`.

Para instalarlo en tu Minecraft:

1. Instala **Forge 1.20.1** (versión 47.x) desde la web oficial de Forge, con el instalador "Installer".
2. Pulsa a la vez las teclas **Windows + R**, escribe `%appdata%\.minecraft` y pulsa Intro.
3. Abre la carpeta **mods** (si no existe, créala: clic derecho → Nuevo → Carpeta → `mods`).
4. Copia ahí `transporteiberico-1.0.0.jar`.
5. Abre el lanzador de Minecraft, elige el perfil **forge 1.20.1** y pulsa Jugar.

---

## Etapa A — Qué hay en esta carpeta

```
transporte/
├── build.gradle          ← receta para fabricar el mod (Forge MDK oficial)
├── settings.gradle       ← nombre del proyecto
├── gradle.properties     ← versiones (Minecraft 1.20.1, Forge 47.3.0) y datos del mod
├── gradlew / gradlew.bat ← el "botón" que descarga Gradle y fabrica el mod
├── gradle/wrapper/       ← lo que usa gradlew (no se toca)
└── src/main/
    ├── java/com/transporteiberico/
    │   ├── TransporteIberico.java      ← el mod: arranca y registra todo
    │   ├── registry/TIRegistry.java    ← registra la entidad y el objeto
    │   ├── entity/BusEntity.java       ← el autobús: conducir, subir, bajar, chocar, guardar
    │   ├── item/BusItem.java           ← el objeto que coloca el autobús
    │   ├── client/TIClient.java        ← (solo con pantalla) registra el modelo y el dibujo
    │   ├── client/BusModel.java        ← el modelo 3D (carrocería, ventanas, ruedas, puertas…)
    │   ├── client/BusRenderer.java     ← dibuja el autobús en el mundo
    │   └── test/TISmokeTest.java       ← pruebas automáticas (las usa GitHub)
    └── resources/
        ├── META-INF/mods.toml          ← ficha del mod (nombre, versión, descripción)
        ├── pack.mcmeta
        └── assets/transporteiberico/
            ├── lang/es_es.json, en_us.json…           ← textos en español e inglés
            ├── models/item/autobus_urbano_azul.json  ← icono del objeto
            ├── textures/item/autobus_urbano_azul.png ← dibujo del icono
            └── textures/entity/autobus_urbano_azul.png ← colores del autobús
```

No hace falta modificar nada para que funcione. Si algún día quieres cambiar el nombre o la versión,
se hace en `gradle.properties`.

## Etapas B, C y D — Código, modelo, texturas e idiomas

Todos los archivos ya están completos en esta carpeta (ver la lista de arriba).

- El **autobús** no es un mob, así que no necesita "atributos" (vida, velocidad…): eso solo lo usan los
  seres vivos. Tiene su propia resistencia: se rompe con unos cuantos golpes, o con uno en modo creativo,
  y entonces suelta su objeto.
- El **modelo 3D** está hecho con cajas, como los mobs de Minecraft: carrocería azul, techo y franja
  blancos, ventanas, parabrisas, luna trasera, dos puertas en el lado derecho, cuatro ruedas que giran,
  faros, pilotos rojos, parachoques y letrero de línea naranja.
- La **conducción** usa las mismas teclas de moverse de Minecraft (W/A/S/D) y el mismo sistema que las
  barcas: el ordenador del conductor mueve el autobús y el servidor comprueba la posición. Por eso no hay
  teclas nuevas que configurar y funciona igual en un mundo individual y en un servidor.

## Etapa E — Fabricar el `.jar` en tu ordenador con Windows 11 (opcional)

Solo si quieres fabricarlo tú en vez de descargarlo.

1. **Instala Java 17 (gratis):** busca "Adoptium Temurin 17", descarga el instalador `.msi` de
   **Windows x64, JDK 17** e instálalo con "Siguiente". En la pantalla de opciones marca
   **"Set JAVA_HOME variable"**.
2. **Descarga el proyecto:** en la página de GitHub pulsa el botón verde **Code → Download ZIP**.
   Descomprímelo (clic derecho → Extraer todo), por ejemplo en `C:\mods\`.
3. Abre la carpeta `transporte` que hay dentro.
4. Haz clic en la barra de dirección de arriba de la ventana (donde pone la ruta), escribe `cmd` y pulsa
   Intro. Se abre una ventana negra ya situada en esa carpeta.
5. Escribe esto y pulsa Intro:
   ```
   gradlew.bat build
   ```
   La primera vez tarda bastante (descarga Minecraft y Forge). Al final debe poner **BUILD SUCCESSFUL**.
6. Tu mod está en `transporte\build\libs\transporteiberico-1.0.0.jar`.

Si sale un error, copia **todo** el texto de la ventana negra y pásamelo.

## Etapa F — Probarlo con un Minecraft de pruebas

1. En la misma ventana negra escribe:
   ```
   gradlew.bat runClient
   ```
2. Se abre Minecraft con el mod ya puesto. Crea un mundo nuevo (**Un jugador → Crear mundo**,
   con **Trucos: Sí**).
3. Para conseguir el autobús:
   - En **modo creativo**: abre el inventario (tecla E), pestaña **Herramientas y utilidades**,
     busca **Autobús Urbano Azul**.
   - O escribe en el chat (tecla T): `/give @s transporteiberico:autobus_urbano_azul`
4. Mira al suelo y haz **clic derecho** con el objeto: aparece el autobús (necesita 3×3×3 bloques libres).
5. Haz **clic derecho sobre el autobús** para subirte al asiento del conductor.
6. Conduce: **W** avanzar · **S** frenar y luego marcha atrás · **A** izquierda · **D** derecha.
7. Pulsa **Mayús izquierda** para bajarte. Sales por el lado de las puertas.
8. Para recogerlo, golpéalo varias veces (en creativo basta un golpe).

## Etapa G — Qué se comprueba y qué no

GitHub arranca un servidor Forge real y comprueba automáticamente (`TISmokeTest`) que:
- la entidad y el objeto están registrados y el autobús aparece,
- cae y se apoya en el suelo,
- W lo hace avanzar recto, A gira a la izquierda, D a la derecha, S frena y luego va marcha atrás,
  y sin pulsar nada se detiene; no pasa de la velocidad máxima,
- choca contra una pared de 3 bloques sin atravesarla y sube un escalón de 1 bloque,
- se guarda y se carga con la misma posición y orientación,
- un jugador se sube y es el conductor; no cabe un segundo jugador,
- al bajar, el autobús sigue en su sitio, no se duplica y el jugador sale al lado (no encima),
- al romperlo desaparece y suelta su objeto.

Lo que **no** se puede comprobar en GitHub, porque necesita pantalla: cómo se ve el modelo y que las
teclas respondan bien en tu ordenador. Eso lo compruebas tú con la Etapa F.

**Limitación conocida:** Minecraft solo permite cajas de choque cuadradas que no giran, así que la caja
del autobús mide 3×3×3 bloques aunque el modelo sea más largo (6 bloques). La parte delantera y trasera
del dibujo pueden atravesar un poco las paredes al chocar de frente.
