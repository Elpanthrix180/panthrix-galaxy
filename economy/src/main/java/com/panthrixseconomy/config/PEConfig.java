package com.panthrixseconomy.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** config/panthrixseconomy-common.toml */
public final class PEConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue ADMIN_PERMISSION_LEVEL;
    public static final ForgeConfigSpec.IntValue TOWN_RADIUS;
    public static final ForgeConfigSpec.IntValue CITY_RADIUS;
    public static final ForgeConfigSpec.BooleanValue GENERATE_PLAZA;
    public static final ForgeConfigSpec.BooleanValue GENERATE_PLAZA_IN_EXISTING_WORLDS;
    public static final ForgeConfigSpec.IntValue PLAZA_SEARCH_RADIUS;
    public static final ForgeConfigSpec.BooleanValue TELEPORT_NEW_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue SET_WORLD_SPAWN_TO_PLAZA;
    public static final ForgeConfigSpec.IntValue MAX_HISTORY;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("permisos");
        ADMIN_PERMISSION_LEVEL = b.comment(
                        "Nivel de operador necesario para administrar la economía (crear países, pueblos, comercios...).",
                        "2 = operadores normales. Si usas un mod de permisos (LuckPerms...), el nodo es 'panthrixseconomy.admin'.")
                .defineInRange("nivelAdministrador", 2, 1, 4);
        b.pop();

        b.push("territorio");
        TOWN_RADIUS = b.comment("Radio (bloques) de un pueblo nuevo").defineInRange("radioPueblo", 64, 8, 1024);
        CITY_RADIUS = b.comment("Radio (bloques) de una ciudad nueva").defineInRange("radioCiudad", 128, 8, 2048);
        b.pop();

        b.push("granPlaza");
        GENERATE_PLAZA = b.comment("Construir la Gran Plaza al crear un mundo nuevo").define("generar", true);
        GENERATE_PLAZA_IN_EXISTING_WORLDS = b.comment(
                        "Construirla también en mundos que ya existían antes de instalar el mod (por defecto no)")
                .define("generarEnMundosExistentes", false);
        PLAZA_SEARCH_RADIUS = b.comment("Distancia máxima desde el spawn para buscar un sitio seguro")
                .defineInRange("radioBusqueda", 192, 0, 1024);
        TELEPORT_NEW_PLAYERS = b.comment(
                        "Llevar a la plaza a cada jugador la PRIMERA vez que entra (nunca después de morir o reconectar)")
                .define("llevarJugadoresNuevos", true);
        SET_WORLD_SPAWN_TO_PLAZA = b.comment(
                        "Mover también el spawn del mundo a la plaza (afecta a reapariciones sin cama). Por defecto no.")
                .define("moverSpawnDelMundo", false);
        b.pop();

        b.push("banco");
        MAX_HISTORY = b.comment("Movimientos bancarios guardados como máximo").defineInRange("historialMaximo", 5000, 100, 100000);
        b.pop();
        SPEC = b.build();
    }

    private PEConfig() {
    }
}
