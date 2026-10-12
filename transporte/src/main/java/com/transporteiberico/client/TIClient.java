package com.transporteiberico.client;

import com.transporteiberico.TransporteIberico;
import com.transporteiberico.registry.TIRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Solo en el cliente (el juego con pantalla): registra el modelo y el dibujo del autobús. */
@Mod.EventBusSubscriber(modid = TransporteIberico.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TIClient {

    public static final ModelLayerLocation BUS_LAYER =
            new ModelLayerLocation(new ResourceLocation(TransporteIberico.MODID, "autobus_urbano_azul"), "main");

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BUS_LAYER, BusModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TIRegistry.BUS.get(), BusRenderer::new);
    }

    private TIClient() {
    }
}
