package com.transporteiberico.registry;

import com.transporteiberico.TransporteIberico;
import com.transporteiberico.entity.BusEntity;
import com.transporteiberico.item.BusItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registro de la entidad del autobús y del objeto que lo invoca. */
public final class TIRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TransporteIberico.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TransporteIberico.MODID);

    /** Caja de choque de 3×3 bloques (Minecraft solo admite cajas cuadradas que no giran). */
    public static final RegistryObject<EntityType<BusEntity>> BUS = ENTITIES.register("autobus_urbano_azul",
            () -> EntityType.Builder.<BusEntity>of(BusEntity::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F)
                    .clientTrackingRange(10)
                    .build("autobus_urbano_azul"));

    public static final RegistryObject<Item> BUS_ITEM = ITEMS.register("autobus_urbano_azul",
            () -> new BusItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(TIRegistry::onCreativeTabs);
    }

    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BUS_ITEM);
        }
    }

    private TIRegistry() {
    }
}
