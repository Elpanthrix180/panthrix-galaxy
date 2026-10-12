package com.transporteiberico;

import com.mojang.logging.LogUtils;
import com.transporteiberico.registry.TIRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Transporte Ibérico: vehículos inspirados en España.
 * Versión 1.0.0: el Autobús Urbano Azul.
 */
@Mod(TransporteIberico.MODID)
public class TransporteIberico {

    public static final String MODID = "transporteiberico";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TransporteIberico() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TIRegistry.register(modBus);
    }
}
