package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.menu.PGBackpackMenu;
import com.panthrixsgalaxy.menu.PGEngineeringBenchMenu;
import com.panthrixsgalaxy.menu.PGShipMenu;
import com.panthrixsgalaxy.platform.PGHolder;
import com.panthrixsgalaxy.platform.PGPlatform;
import com.panthrixsgalaxy.platform.PGRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

/** Ventanas (menús con huecos) del mod. */
public final class ModMenuTypes {

    public static final PGRegistry<MenuType<?>> MENUS = PGRegistry.create(Registries.MENU);

    /** Ventana de la mochila: inventario + panel de depósitos. */
    public static final PGHolder<MenuType<PGBackpackMenu>> BACKPACK = MENUS.register("backpack",
            () -> PGPlatform.menuType(PGBackpackMenu::new));

    /** Ventana del Banco de Ingeniería Espacial. */
    public static final PGHolder<MenuType<PGEngineeringBenchMenu>> ENGINEERING_BENCH = MENUS.register("engineering_bench",
            () -> PGPlatform.menuType(PGEngineeringBenchMenu::new));

    /** Panel de control y bodega de la nave espacial (Fase 14). */
    public static final PGHolder<MenuType<PGShipMenu>> SHIP = MENUS.register("ship",
            () -> PGPlatform.menuType(PGShipMenu::new));

    private ModMenuTypes() {
    }
}
