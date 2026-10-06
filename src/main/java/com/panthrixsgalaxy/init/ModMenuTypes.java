package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.menu.PGBackpackMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Ventanas (menús con huecos) del mod. */
public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PanthrixsGalaxy.MOD_ID);

    /** Ventana de la mochila: inventario + panel de depósitos. */
    public static final RegistryObject<MenuType<PGBackpackMenu>> BACKPACK = MENUS.register("backpack",
            () -> IForgeMenuType.create(PGBackpackMenu::new));

    private ModMenuTypes() {
    }
}
