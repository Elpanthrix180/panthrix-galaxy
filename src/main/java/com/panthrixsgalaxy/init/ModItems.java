package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.item.PGTestItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Lista de TODOS los objetos del mod.
 *
 * Los objetos de los bloques (BlockItem) NO se escriben aquí: los crea
 * automáticamente ModBlocks.registerBlock(...).
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PanthrixsGalaxy.MOD_ID);

    // ===== OBJETOS DE PRUEBA (Fase 1) =====

    /** PG_Test_Item: objeto de prueba. Clic derecho = mensaje en el chat. */
    public static final RegistryObject<Item> PG_TEST_ITEM = ITEMS.register("pg_test_item",
            () -> new PGTestItem(new Item.Properties().stacksTo(64)));

    // ===== MATERIALES DE LA LUNA (Fase 2) =====

    public static final RegistryObject<Item> PG_RAW_LUNARITE = material("pg_raw_lunarite");
    public static final RegistryObject<Item> PG_LUNARITE_INGOT = material("pg_lunarite_ingot");
    public static final RegistryObject<Item> PG_SELENITE_CRYSTAL = material("pg_selenite_crystal");
    public static final RegistryObject<Item> PG_HELIUM_3 = material("pg_helium_3", Rarity.UNCOMMON);

    // ===== MATERIALES DE MARTE (Fase 2) =====

    public static final RegistryObject<Item> PG_MARTIAN_MINERAL = material("pg_martian_mineral");
    public static final RegistryObject<Item> PG_RAW_MARTIANITE = material("pg_raw_martianite");
    public static final RegistryObject<Item> PG_MARTIANITE_INGOT = material("pg_martianite_ingot");
    public static final RegistryObject<Item> PG_MARTIAN_CRYSTAL = material("pg_martian_crystal");
    public static final RegistryObject<Item> PG_RAW_RUSTED_IRON = material("pg_raw_rusted_iron");

    // ===== MATERIALES DE ASTEROIDES (Fase 2) =====

    public static final RegistryObject<Item> PG_METEORITE_FRAGMENT = material("pg_meteorite_fragment");
    public static final RegistryObject<Item> PG_RAW_ASTEROID_METAL = material("pg_raw_asteroid_metal");
    public static final RegistryObject<Item> PG_ASTEROID_METAL_INGOT = material("pg_asteroid_metal_ingot");
    public static final RegistryObject<Item> PG_RAW_OSMIUM = material("pg_raw_osmium");
    public static final RegistryObject<Item> PG_OSMIUM_INGOT = material("pg_osmium_ingot", Rarity.UNCOMMON);

    // ===== MATERIALES ALIENÍGENAS (Fase 2) =====

    public static final RegistryObject<Item> PG_RAW_XENITE = material("pg_raw_xenite", Rarity.UNCOMMON);
    public static final RegistryObject<Item> PG_XENITE_INGOT = material("pg_xenite_ingot", Rarity.UNCOMMON);
    public static final RegistryObject<Item> PG_ASTRALITE_CRYSTAL = material("pg_astralite_crystal", Rarity.RARE);
    public static final RegistryObject<Item> PG_COSMIC_CRYSTAL = material("pg_cosmic_crystal", Rarity.RARE);
    /** Lo suelta la Reina Alienígena (Fase 18). No arde en lava. */
    public static final RegistryObject<Item> PG_NECRONITE_SHARD = ITEMS.register("pg_necronite_shard",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    /** 4 fragmentos de necronita + 4 lingotes de osmio. No arde en lava. */
    public static final RegistryObject<Item> PG_NECRONITE_INGOT = ITEMS.register("pg_necronite_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // ===== AYUDANTES =====

    /** Material normal (nombre blanco). */
    private static RegistryObject<Item> material(String name) {
        return material(name, Rarity.COMMON);
    }

    /** Material con rareza: UNCOMMON = nombre amarillo, RARE = cian, EPIC = morado. */
    private static RegistryObject<Item> material(String name, Rarity rarity) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().rarity(rarity)));
    }

    private ModItems() {
    }
}
