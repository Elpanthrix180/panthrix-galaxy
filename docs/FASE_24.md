# Fase 24 — Pruebas finales

## 🎯 Objetivo
Revisar el mod entero antes de la versión final: buscar errores, comprobar que se puede
completar en supervivencia y dejar herramientas para probarlo rápido en el juego.

## 🧪 Herramientas de prueba (solo administradores / modo trucos)
| Comando | Qué hace |
|---|---|
| `/pgtp <planeta>` | Te lleva a la superficie de un planeta (`moon`, `mars`, `venus`, `xenoria`, `nyx`...; pulsa Tab para ver la lista) |
| `/pgkit starter` | Lo necesario para ir de la Tierra a la Luna |
| `/pgkit explorer` | Equipo para Marte y la nave |
| `/pgkit endgame` | Lo mejor del mod |
| `/pgrefill` | Llena de oxígeno, energía y combustible todo lo que llevas |

La lista completa de pruebas, paso a paso, está en **[PRUEBAS.md](PRUEBAS.md)**.

## 🔍 Revisión del código
Se revisaron todas las clases (registro, bloques, máquinas, oxígeno, cohetes, naves,
armas, mobs, jefa, estaciones, generación de mundo, menús y pantallas).

### Errores encontrados y arreglados
| Zona | Problema | Arreglo |
|---|---|---|
| Mobs | Los mobs de Luna/Marte casi no aparecían de día (la regla vanilla miraba la luz del sol) | `checkSpawnRules` propio: solo cuenta la luz de bloques (`PGSpawnRules`) |
| Alienígenas | Al pegar a uno, solo avisaba a los de su misma clase | `PGAlienHurtByTargetGoal`: avisa a todos los alienígenas cercanos |
| Soldado alien | Olvidaba la zona que vigila al recargar el mundo | Se guarda `GuardCenter` en el NBT |
| Láser | El disparo podía atravesar una pared justo después de golpear a un mob | Comprobación de bloques tras cada impacto |
| Cohete / nave | Colocados fuera de la Tierra, al volver del Espacio llegaban a (0,0,0) | Punto de llegada estándar si no hay origen |
| Nave | Recoger la nave perdía su origen; podía soltar objetos antes de explotar | Se guarda el origen en el objeto; explosión antes de soltar |
| Nave | El viaje entre dimensiones podía repetirse en el mismo tick | Bandera `transferring` |
| Distribuidor O₂ | En un solo jugador, el cliente borraba la sala del servidor (y podía colgar el juego) | Solo el servidor guarda salas; mapa concurrente |
| Salas selladas | Una fuga podía cargar trozos de mundo sin generar (tirones) | Si el siguiente bloque no está cargado, cuenta como fuga |
| Módulo hábitat | El panel solar no era hermético | `pg_solar_panel` añadido a la etiqueta `airtight` |
| Máquinas / tanque O₂ | No se podía poner un cable pegado a una máquina sin agacharse | Con un bloque en la mano, el clic coloca el bloque |
| Banco de ingeniería | Con 11 categorías, las recetas podían tapar el texto de ayuda | Máximo calculado según el espacio libre (15) |
| Cráteres | En cuestas quedaba suelo flotando | El fondo nunca queda por encima del terreno |
| Mochila | El panel de tanques tapaba los tooltips | Se dibuja con el fondo |

## 🗺️ Prueba de progresión (simulada)
Se simuló una partida en supervivencia siguiendo recetas, botines y mundos:
- **Los 9 mundos se pueden alcanzar** (Tierra → Luna → Marte → … → Nyx secreto).
- **140 de 154 objetos se pueden conseguir.** Los que no son a propósito: monolito y archivo
  alienígena (estructuras de descubrimiento), bloque y objeto de prueba, y huevos de mob.

## ⚠️ Limitaciones
- En este entorno no se puede descargar Forge, así que la comprobación es de sintaxis y de datos.
  **La prueba real es `gradlew runClient`** siguiendo PRUEBAS.md.
- MCreator: los comandos de prueba se pueden hacer con *Command* + procedimientos, pero las
  reglas de aparición propias y la sala sellada necesitan código Java.

## ✅ Lista de comprobación
- [ ] `gradlew build` sin errores
- [ ] Mundo nuevo en supervivencia: llegar a la Luna sin comandos
- [ ] Seguir todos los apartados de PRUEBAS.md
- [ ] Probar en servidor dedicado (`gradlew runServer`) con 2 jugadores
