# 📦 Panthrix Pack

Modpack para **Minecraft 1.20.1 + Forge 47.3.0** con los tres mods del proyecto:

| Mod | Qué añade |
|---|---|
| 💰 Panthrix's Economy | países, bancos, **cajeros automáticos y tarjeta bancaria**, comercios, empleos, la Gran Plaza |
| 🚀 Panthrix's Galaxy | exploración espacial |
| 🚌 Transporte Ibérico | el Autobús Urbano Azul conducible |

GitHub lo construye solo (`modpack/build_pack.py`) en cada cambio y lo añade a las Releases:

- `Panthrix-Pack-1.0.0.mrpack` → **Modrinth App**, **Prism Launcher**, ATLauncher…
- `Panthrix-Pack-1.0.0.zip` → **CurseForge App**

## Instalarlo (Windows 11)

**CurseForge App:** Minecraft → **Crear perfil personalizado** → enlace pequeño **"Importar"** (arriba a la
derecha) → elige `Panthrix-Pack-1.0.0.zip`. La app instala Minecraft 1.20.1 y Forge sola. Pulsa **Jugar**.

**Modrinth App:** botón **+** (crear instancia) → pestaña **"Desde archivo"** → elige `Panthrix-Pack-1.0.0.mrpack`.

**Prism Launcher:** **Añadir instancia** → **Importar** → elige el `.mrpack` (o el `.zip`).

**Sin lanzador especial:** abre el `.zip`, entra en `overrides/mods` y copia los tres `.jar` en tu carpeta
`%appdata%\.minecraft\mods` (con Forge 1.20.1 instalado).

Para cambiar la versión del pack, edita `modpack/pack.properties`.
