"""
Construye el modpack "Panthrix Pack" a partir de los .jar ya compilados.

  python modpack/build_pack.py <carpeta-con-los-jar> <carpeta-de-salida>

Genera dos archivos que se importan directamente:
  - Panthrix-Pack-<versión>.mrpack  -> Modrinth App, Prism Launcher, ATLauncher...
  - Panthrix-Pack-<versión>.zip     -> CurseForge App (Crear perfil -> Importar)

Los mods van dentro del pack (carpeta overrides/mods), así que no hace falta internet
para descargarlos ni que estén publicados en ninguna web.
"""
import json
import pathlib
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent
# Mods de Forge que forman el pack (el .jar de Fabric de Galaxy NO va: es otro cargador)
WANTED = ("panthrixseconomy-", "panthrixsgalaxy-1.20.1-", "transporteiberico-")


def props():
    out = {}
    for line in (ROOT / "pack.properties").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            out[k.strip()] = v.strip()
    return out


def main():
    jars_dir = pathlib.Path(sys.argv[1])
    out_dir = pathlib.Path(sys.argv[2])
    out_dir.mkdir(parents=True, exist_ok=True)
    p = props()
    jars = sorted(j for j in jars_dir.rglob("*.jar")
                  if j.name.startswith(WANTED) and not j.name.endswith(("-sources.jar", "-slim.jar")))
    missing = [w for w in WANTED if not any(j.name.startswith(w) for j in jars)]
    if missing:
        sys.exit(f"Faltan mods para el pack: {missing}")
    readme = (ROOT / "LEEME.txt").read_text(encoding="utf-8")
    base = f"Panthrix-Pack-{p['version']}"

    # ---- Modrinth (.mrpack) ----
    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": p["version"],
        "name": p["name"],
        "summary": p["summary"],
        "files": [],
        "dependencies": {"minecraft": p["minecraft"], "forge": p["forge"]},
    }
    with zipfile.ZipFile(out_dir / f"{base}.mrpack", "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("modrinth.index.json", json.dumps(index, indent=2, ensure_ascii=False))
        for j in jars:
            z.write(j, f"overrides/mods/{j.name}")
        z.writestr("overrides/LEEME-Panthrix-Pack.txt", readme)

    # ---- CurseForge (.zip) ----
    manifest = {
        "minecraft": {"version": p["minecraft"], "modLoaders": [{"id": f"forge-{p['forge']}", "primary": True}]},
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": p["name"],
        "version": p["version"],
        "author": p["author"],
        "files": [],
        "overrides": "overrides",
    }
    modlist = "<ul>\n" + "".join(f"<li>{j.name}</li>\n" for j in jars) + "</ul>\n"
    with zipfile.ZipFile(out_dir / f"{base}.zip", "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("manifest.json", json.dumps(manifest, indent=2, ensure_ascii=False))
        z.writestr("modlist.html", modlist)
        for j in jars:
            z.write(j, f"overrides/mods/{j.name}")
        z.writestr("overrides/LEEME-Panthrix-Pack.txt", readme)

    print(f"Pack {p['name']} {p['version']} con {len(jars)} mods:")
    for j in jars:
        print("  -", j.name)


if __name__ == "__main__":
    main()
