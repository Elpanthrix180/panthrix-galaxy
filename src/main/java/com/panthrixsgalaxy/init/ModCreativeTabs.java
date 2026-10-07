package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGBatteryItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGRocketItem;
import com.panthrixsgalaxy.item.PGShipItem;
import com.panthrixsgalaxy.weapon.PGLaserItem;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
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

    /** Pestaña principal: objetos y materiales (todo lo que NO es un bloque ni equipo). */
    public static final RegistryObject<CreativeModeTab> PG_MAIN_TAB = CREATIVE_TABS.register("pg_main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_main_tab"))
                    .icon(() -> new ItemStack(ModItems.PG_LUNARITE_INGOT.get()))
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (item instanceof PGOxygenTankItem tank) {
                            output.accept(tank.createFull()); // en creativo, las bombonas salen llenas
                        } else if (item instanceof PGFuelCanisterItem canister) {
                            output.accept(new ItemStack(canister));  // vacío
                            output.accept(canister.createFull());     // y lleno
                        } else if (item instanceof PGBatteryItem battery) {
                            output.accept(new ItemStack(battery));  // vacía
                            output.accept(battery.createFull());     // y llena
                        } else if (!(item instanceof BlockItem) && !isEquipment(item)) {
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

    /** Pestaña de equipo: herramientas, armas y trajes. */
    public static final RegistryObject<CreativeModeTab> PG_TOOLS_TAB = CREATIVE_TABS.register("pg_tools_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.panthrixsgalaxy.pg_tools_tab"))
                    .icon(() -> new ItemStack(ModItems.PG_LUNARITE_PICKAXE.get()))
                    .withTabsBefore(PG_BLOCKS_TAB.getKey())
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (item instanceof PGLaserItem laser) {
                            output.accept(new ItemStack(laser)); // descargada
                            output.accept(laser.createFull());    // y cargada
                        } else if (item instanceof PGLaserSwordItem sword) {
                            output.accept(new ItemStack(sword));
                            output.accept(sword.createFull());
                        } else if (isEquipment(item)) {
                            output.accept(item);
                        }
                    }))
                    .build());

    /** Equipo = herramientas y armas (tienen "nivel"), piezas de armadura y mochilas. */
    private static boolean isEquipment(Item item) {
        return item instanceof TieredItem || item instanceof ArmorItem || item instanceof PGBackpackItem
                || item instanceof PGRocketItem || item instanceof PGShipItem || item instanceof PGLaserItem;
    }

    private ModCreativeTabs() {
    }
}
