package com.panthrixsgalaxy.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * CONFIGURACIÓN del mod (Fase 23). Se puede cambiar sin tocar el código, editando los archivos
 * que Forge crea al arrancar el juego por primera vez:
 *
 *   config/panthrixsgalaxy-common.toml  → reglas del juego (oxígeno, estaciones, combate, rendimiento)
 *   config/panthrixsgalaxy-client.toml  → solo lo que se ve en TU pantalla (paneles, efectos)
 *
 * En multijugador manda el archivo "common" del SERVIDOR.
 */
public final class PGConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // ===== Común (reglas del juego) =====
    public static ForgeConfigSpec.IntValue oxygenPerSecond;
    public static ForgeConfigSpec.IntValue oxygenGraceSeconds;
    public static ForgeConfigSpec.BooleanValue temperatureDamage;
    public static ForgeConfigSpec.IntValue maxRoomVolume;
    public static ForgeConfigSpec.IntValue distributorBaseCost;
    public static ForgeConfigSpec.IntValue distributorBlocksPerFe;
    public static ForgeConfigSpec.BooleanValue marsDustStorms;
    public static ForgeConfigSpec.DoubleValue laserDamageMultiplier;
    public static ForgeConfigSpec.DoubleValue bossHealthMultiplier;
    public static ForgeConfigSpec.DoubleValue shipFuelMultiplier;
    public static ForgeConfigSpec.IntValue cableNetworkRefreshTicks;
    public static ForgeConfigSpec.IntValue distributorPlayerRange;

    // ===== Cliente (pantalla) =====
    public static ForgeConfigSpec.BooleanValue showOxygenHud;
    public static ForgeConfigSpec.BooleanValue showVehicleHud;
    public static ForgeConfigSpec.BooleanValue cameraShake;
    public static ForgeConfigSpec.IntValue stormParticles;
    public static ForgeConfigSpec.BooleanValue venusFog;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.comment("Oxígeno y supervivencia").push("oxygen");
        oxygenPerSecond = common.comment("Oxígeno que gasta el traje cada segundo (1 = normal, 0 = nunca se gasta)")
                .defineInRange("oxygenPerSecond", 1, 0, 10);
        oxygenGraceSeconds = common.comment("Segundos sin aire antes de empezar a recibir daño")
                .defineInRange("graceSeconds", 5, 0, 60);
        temperatureDamage = common.comment("¿La temperatura extrema hace daño sin el traje completo?")
                .define("temperatureDamage", true);
        common.pop();

        common.comment("Bases y estaciones espaciales (Fase 19)").push("station");
        maxRoomVolume = common.comment("Tamaño máximo de una sala sellada (bloques de aire por distribuidor)")
                .defineInRange("maxRoomVolume", 2048, 64, 16384);
        distributorBaseCost = common.comment("Energía fija que gasta el distribuidor (FE por tick)")
                .defineInRange("distributorBaseCost", 5, 0, 1000);
        distributorBlocksPerFe = common.comment("El distribuidor gasta 1 FE/t más por cada tantos bloques de sala")
                .defineInRange("distributorBlocksPerFe", 40, 1, 10000);
        common.pop();

        common.comment("Planetas").push("planets");
        marsDustStorms = common.comment("¿Hay tormentas de polvo en Marte?").define("marsDustStorms", true);
        common.pop();

        common.comment("Combate y vehículos").push("gameplay");
        laserDamageMultiplier = common.comment("Multiplica el daño de todos los rayos láser (1.0 = normal)")
                .defineInRange("laserDamageMultiplier", 1.0, 0.1, 10.0);
        bossHealthMultiplier = common.comment("Multiplica la vida de los jefes (1.0 = normal; 2.0 para jugar en grupo)")
                .defineInRange("bossHealthMultiplier", 1.0, 0.1, 10.0);
        shipFuelMultiplier = common.comment("Multiplica el combustible que gasta la nave (0.5 = la mitad)")
                .defineInRange("shipFuelMultiplier", 1.0, 0.0, 10.0);
        common.pop();

        common.comment("Rendimiento (Fase 23)").push("performance");
        cableNetworkRefreshTicks = common.comment("Cada cuántos ticks se vuelve a recorrer una red de cables aunque no cambie")
                .defineInRange("cableNetworkRefreshTicks", 100, 10, 1200);
        distributorPlayerRange = common.comment("El distribuidor solo trabaja si hay un jugador a menos de estos bloques")
                .defineInRange("distributorPlayerRange", 96, 16, 512);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.comment("Lo que se ve en tu pantalla").push("display");
        showOxygenHud = client.comment("Mostrar el indicador de oxígeno").define("showOxygenHud", true);
        showVehicleHud = client.comment("Mostrar el panel del cohete y de la nave").define("showVehicleHud", true);
        cameraShake = client.comment("Temblor de la cámara al despegar").define("cameraShake", true);
        stormParticles = client.comment("Partículas de polvo de las tormentas de Marte (0 = ninguna, 14 = normal)")
                .defineInRange("stormParticles", 14, 0, 40);
        venusFog = client.comment("Niebla espesa en Venus").define("venusFog", true);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private PGConfig() {
    }
}
