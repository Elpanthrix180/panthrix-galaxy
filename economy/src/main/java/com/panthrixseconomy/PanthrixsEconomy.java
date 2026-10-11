package com.panthrixseconomy;

import com.mojang.logging.LogUtils;
import com.panthrixseconomy.config.PEConfig;
import com.panthrixseconomy.registry.PERegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Panthrix's Economy: países, ciudades, pueblos, comercios oficiales y bancos nacionales.
 * Todo lo "oficial" lo crean solo los administradores; los jugadores usan los servicios.
 */
@Mod(PanthrixsEconomy.MODID)
public class PanthrixsEconomy {

    public static final String MODID = "panthrixseconomy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PanthrixsEconomy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PERegistry.register(modBus);
        com.panthrixseconomy.network.PENetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PEConfig.SPEC);
        // Los eventos del juego (PEEvents, PEPermissions) se registran con @Mod.EventBusSubscriber
    }
}
