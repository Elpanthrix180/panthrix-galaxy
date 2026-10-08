# 📖 Libro guía (versión 1.1.0)

## 🎯 Qué hace
La **primera vez** que un jugador entra a un mundo, recibe un libro con las instrucciones del mod
y un mensaje de bienvenida en el chat. Funciona en un jugador y en servidores, y también en mundos
que ya existían (cada jugador lo recibe la próxima vez que entra).

## 🧩 Cómo funciona
| Pieza | Archivo | Para qué |
|---|---|---|
| El libro | `item/PGGuideBook.java` | Crea un **libro escrito normal** de Minecraft con 16 páginas |
| Entregarlo | `event/PGGuideBookEvents.java` | Al entrar (`PlayerLoggedInEvent`) mira si ya lo recibió; si no, lo da |
| `/pgguide` | `event/PGGuideBookEvents.java` | Cualquier jugador puede pedir otra copia |
| Textos | `lang/es_es.json` y `lang/en_us.json` | `book.panthrixsgalaxy.guide.page1` … `page16` |
| Opción | `config/panthrixsgalaxy-common.toml` → `giveGuideBook` | `false` para no darlo |

- **¿Cómo sabe si ya lo dio?** Guarda una marca en los datos del jugador dentro de `PlayerPersisted`,
  una zona que Minecraft conserva al morir y al cambiar de dimensión. Por eso no se repite.
- **¿Por qué sale en mi idioma?** Cada página es un texto *traducible*: el libro guarda la clave
  y tu juego la traduce al abrirlo. Un jugador en español y otro en inglés ven cada uno el suyo.
- **Si el inventario está lleno**, el libro cae al suelo a tus pies.

## ✏️ Cambiar el texto
1. Edita las claves `book.panthrixsgalaxy.guide.pageN` en los dos archivos de idioma.
2. `\n` es un salto de línea. `§l` = negrita, `§o` = cursiva, `§r` = volver a normal.
3. Cada página admite unas **14 líneas de unos 19 caracteres**. Si pones más, se corta.
4. Para añadir páginas, sube también `PAGES` en `PGGuideBook.java`.

> Los libros que ya se entregaron se actualizan solos: guardan la clave, no el texto.

## 🧩 En MCreator
Se hace con un procedimiento en el disparador *Player joins the world*: comprueba una variable
del jugador (lógica, persistente), y si es falsa ejecuta el comando
`/give @s written_book{...}` y la pone a verdadera.

## ✅ Pruebas
- [ ] Mundo nuevo: al entrar recibes el libro y un mensaje.
- [ ] Sal y vuelve a entrar → no recibes otro.
- [ ] Muere → no recibes otro.
- [ ] `/pgguide` → recibes una copia.
- [ ] Cambia el idioma del juego a inglés y abre el libro: sale en inglés.
- [ ] `giveGuideBook = false` en la config → un jugador nuevo no lo recibe.
