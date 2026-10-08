package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.command.PGCommands;
import com.panthrixsgalaxy.command.PGTestCommands;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.mob.PGMobSetup;
import com.panthrixsgalaxy.event.PGGuideBookEvents;
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
import com.panthrixsgalaxy.platform.PGPlatform;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import com.panthrixsgalaxy.tool.PGToolTiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;

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

        // Atributos de las criaturas (vida, daño...)
        PGMobSetup.attributes(FabricDefaultAttributeRegistry::register);

        // Comunicación servidor <-> pantalla y energía compatible con otros mods
        PGNetwork.register();
        PGFabricEnergy.register();
        PGFabricItems.register();   // tolvas y tuberías pueden llenar las máquinas

        // Comandos (/pgtp, /pgkit, /pgrefill, /pgvacuum, /pgguide) y libro guía al entrar
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PGCommands.register(dispatcher);
            PGTestCommands.register(dispatcher);
            PGGuideBookEvents.register(dispatcher);
        });
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                PGGuideBookEvents.onPlayerLogin(listener.player));

        // El Espacio y los planetas sin aire respirable
        PGAtmosphere.addAirlessDimension(PGPlanets.SPACE);
        PGPlanets.ALL.forEach(planet -> {
            if (!planet.breathable() && planet.dimension() != null) {
                PGAtmosphere.addAirlessDimension(planet.dimension());
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PGSealedRooms.clearAll());

        // Al reaparecer (o volver del End) Minecraft crea un jugador nuevo: se copian sus datos del mod.
        // (De momento la mochila se conserva siempre; en la Fase F3 caerá al suelo al morir, como en Forge.)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CompoundTag newData = PGPlatform.getPersistentData(newPlayer);
            if (alive) {
                newData.merge(PGPlatform.getPersistentData(oldPlayer).copy());
            } else {
                newData.put(PGPlayerData.PERSISTED_TAG, PGPlatform.getPersistedData(oldPlayer).copy());
            }
            PGBackpackSlot.setEquipped(newPlayer, PGBackpackSlot.getEquipped(oldPlayer));
        });

        PanthrixsGalaxy.LOGGER.info("[Panthrixs Galaxy] Mod cargado correctamente (Fabric). ¡Preparados para el despegue!");
    }
}
