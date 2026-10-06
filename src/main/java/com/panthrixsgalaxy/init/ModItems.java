package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.item.PGTestItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Lista de TODOS los objetos del mod (incluidos los objetos de los bloques).
 * Todo lo registrado aquí aparece automáticamente en la pestaña creativa del mod.
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PanthrixsGalaxy.MOD_ID);

    // ===== OBJETOS DE PRUEBA (Fase 1) =====

    /** PG_Test_Item: objeto de prueba. Clic derecho = mensaje en el chat. */
    public static final RegistryObject<Item> PG_TEST_ITEM = ITEMS.register("pg_test_item",
            () -> new PGTestItem(new Item.Properties().stacksTo(64)));

    // ===== OBJETOS DE BLOQUES =====
    // Cada bloque necesita un "BlockItem" para poder tenerlo en el inventario.

    public static final RegistryObject<Item> PG_TEST_BLOCK = ITEMS.register("pg_test_block",
            () -> new BlockItem(ModBlocks.PG_TEST_BLOCK.get(), new Item.Properties()));

    private ModItems() {
    }
}
