package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Pestañas del modo creativo de Panthrixs Galaxy.
 * Se rellenan solas: todo lo que registres en ModItems o ModBlocks aparece aquí.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PanthrixsGalaxy.MOD_ID);

    /** Pestaña principal: objetos y materiales (todo lo que NO es un bloque ni una herramienta). */
    public static final RegistryObject<CreativeModeTab> PG_MAIN_TAB = CREATIVE_TABS.register("pg_main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_main_tab"))
                    .icon(() -> new ItemStack(ModItems.PG_LUNARITE_INGOT.get()))
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (!(item instanceof BlockItem) && !(item instanceof TieredItem)) {
                            output.accept(item);
                        }
                    }))
                    .build());

    /** Pestaña de bloques: piedras, menas y bloques de almacenamiento. */
    public static final RegistryObject<CreativeModeTab> PG_BLOCKS_TAB = CREATIVE_TABS.register("pg_blocks_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_blocks_tab"))
                    .icon(() -> new ItemStack(ModBlocks.PG_LUNARITE_ORE.get()))
                    .withTabsBefore(PG_MAIN_TAB.getKey())
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (item instanceof BlockItem) {
                            output.accept(item);
                        }
                    }))
                    .build());

    /** Pestaña de herramientas: picos, hachas, palas, azadas y espadas (todo lo que tiene "nivel"). */
    public static final RegistryObject<CreativeModeTab> PG_TOOLS_TAB = CREATIVE_TABS.register("pg_tools_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_tools_tab"))
                    .icon(() -> new ItemStack(ModItems.PG_LUNARITE_PICKAXE.get()))
                    .withTabsBefore(PG_BLOCKS_TAB.getKey())
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (item instanceof TieredItem) {
                            output.accept(item);
                        }
                    }))
                    .build());

    private ModCreativeTabs() {
    }
}
