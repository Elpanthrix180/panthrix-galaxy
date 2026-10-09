package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModCreativeTabs;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.init.ModFeatures;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import com.panthrixsgalaxy.tool.PGToolTiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Clase principal de Panthrixs Galaxy en Fabric.
 * Fabric la encuentra gracias a "entrypoints → main" de fabric.mod.json.
 *
 * Hace lo mismo que la clase PanthrixsGalaxy de Forge, pero con las herramientas de Fabric.
 * El contenido del mod (bloques, objetos, máquinas...) es el MISMO código para los dos.
 */
public class PanthrixsGalaxyFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Configuración: config/panthrixsgalaxy-common.toml (igual que en Forge)
        PGConfig.COMMON_SPEC.load(FabricLoader.getInstance().getConfigDir().resolve("panthrixsgalaxy-common.toml"));

        PGToolTiers.register();

        // Registros del mod. En Fabric el ORDEN importa: cada cosa necesita las anteriores.
        ModBlocks.BLOCKS.registerAll();
        ModBlockEntities.BLOCK_ENTITIES.registerAll();
        ModEntities.ENTITIES.registerAll();   // antes que los objetos: los huevos necesitan la entidad
        ModItems.ITEMS.registerAll();
        ModMenuTypes.MENUS.registerAll();
        ModRecipes.RECIPE_TYPES.registerAll();
        ModRecipes.RECIPE_SERIALIZERS.registerAll();
        ModFeatures.FEATURES.registerAll();
        ModCreativeTabs.CREATIVE_TABS.registerAll();

        // Criaturas: atributos (vida, daño...) y dónde aparecen solas
        PGFabricMobs.register();

        // Comunicación servidor <-> pantalla y energía compatible con otros mods
        PGNetwork.register();
        PGFabricEnergy.register();
        PGFabricItems.register();   // tolvas y tuberías pueden llenar las máquinas

        // Eventos (comandos, jugadores, traje, mochila...): fabric/PGFabricEvents
        PGFabricEvents.register();

        // El Espacio y los planetas sin aire respirable
        PGAtmosphere.addAirlessDimension(PGPlanets.SPACE);
        PGPlanets.ALL.forEach(planet -> {
            if (!planet.breathable() && planet.dimension() != null) {
                PGAtmosphere.addAirlessDimension(planet.dimension());
            }
        });

        PanthrixsGalaxy.LOGGER.info("[Panthrixs Galaxy] Mod cargado correctamente (Fabric). ¡Preparados para el despegue!");
    }
}
