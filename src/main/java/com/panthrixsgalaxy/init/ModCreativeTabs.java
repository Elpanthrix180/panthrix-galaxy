package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Pestañas del modo creativo de Panthrixs Galaxy.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PanthrixsGalaxy.MOD_ID);

    /** Pestaña principal: muestra todos los objetos registrados en ModItems. */
    public static final RegistryObject<CreativeModeTab> PG_MAIN_TAB = CREATIVE_TABS.register("pg_main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_main_tab"))
                    .icon(() -> new ItemStack(ModItems.PG_TEST_ITEM.get()))
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabs() {
    }
}
