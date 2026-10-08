package com.panthrixsgalaxy.fabric.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.PGClientRegistrations;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.network.BackpackSyncPacket;
import com.panthrixsgalaxy.network.OxygenSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Parte de PANTALLA del mod en Fabric ("entrypoints → client" de fabric.mod.json).
 * Nunca se carga en un servidor dedicado.
 */
public class PGFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PGConfig.CLIENT_SPEC.load(FabricLoader.getInstance().getConfigDir().resolve("panthrixsgalaxy-client.toml"));

        // Paquetes: servidor -> pantalla, y cómo enviar de la pantalla al servidor
        ClientPlayNetworking.registerGlobalReceiver(id("oxygen_sync"), (client, listener, buf, sender) -> {
            OxygenSyncPacket packet = new OxygenSyncPacket(buf);
            client.execute(packet::handleOnClient);
        });
        ClientPlayNetworking.registerGlobalReceiver(id("backpack_sync"), (client, listener, buf, sender) -> {
            BackpackSyncPacket packet = new BackpackSyncPacket(buf);
            client.execute(packet::handleOnClient);
        });
        PGNetwork.setClientSender(ClientPlayNetworking::send);

        // Quién dibuja cada entidad y sus modelos 3D (la lista es común con Forge)
        PGClientRegistrations.entityRenderers(new PGClientRegistrations.EntityRenderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> renderer) {
                EntityRendererRegistry.register(type, renderer);
            }
        });
        PGClientRegistrations.layerDefinitions((location, definition) ->
                EntityModelLayerRegistry.registerModelLayer(location, definition::get));

        // Ventanas y cielos de los planetas
        PGClientRegistrations.menuScreens();
        PGClientRegistrations.dimensionEffects(DimensionRenderingRegistry::registerDimensionEffects);

        // Espadas láser: el modelo cambia a "con hoja" cuando están encendidas
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof PGLaserSwordItem) {
                FabricModelPredicateProviderRegistry.register(entry.get(), id("active"),
                        (stack, level, entity, seed) -> PGLaserSwordItem.isActive(stack) ? 1.0f : 0.0f);
            }
        });
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, name);
    }
}
