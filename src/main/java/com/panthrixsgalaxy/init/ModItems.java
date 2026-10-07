package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.armor.PGArmorMaterials;
import com.panthrixsgalaxy.armor.PGSpaceSuitItem;
import com.panthrixsgalaxy.entity.rocket.RocketTier;
import com.panthrixsgalaxy.entity.ship.ShipTier;
import com.panthrixsgalaxy.item.PGAlienBeaconItem;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGBatteryItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGRocketItem;
import com.panthrixsgalaxy.item.PGShipItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.item.PGTestItem;
import com.panthrixsgalaxy.weapon.LaserSwordTier;
import com.panthrixsgalaxy.weapon.LaserTier;
import com.panthrixsgalaxy.weapon.PGLaserItem;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import com.panthrixsgalaxy.tool.PGToolTiers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

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

    // ===== HERRAMIENTAS (Fase 3) =====
    // Los números son (daño, velocidad de ataque) igual que en Minecraft normal.
    // El daño final = 1 + daño extra del nivel (PGToolTiers) + este número.

    // --- Lunarita (Luna): mejor que hierro ---
    public static final RegistryObject<Item> PG_LUNARITE_SWORD = ITEMS.register("pg_lunarite_sword",
            () -> new SwordItem(PGToolTiers.LUNARITE, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> PG_LUNARITE_PICKAXE = ITEMS.register("pg_lunarite_pickaxe",
            () -> new PickaxeItem(PGToolTiers.LUNARITE, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> PG_LUNARITE_AXE = ITEMS.register("pg_lunarite_axe",
            () -> new AxeItem(PGToolTiers.LUNARITE, 6.0f, -3.1f, new Item.Properties()));
    public static final RegistryObject<Item> PG_LUNARITE_SHOVEL = ITEMS.register("pg_lunarite_shovel",
            () -> new ShovelItem(PGToolTiers.LUNARITE, 1.5f, -3.0f, new Item.Properties()));
    public static final RegistryObject<Item> PG_LUNARITE_HOE = ITEMS.register("pg_lunarite_hoe",
            () -> new HoeItem(PGToolTiers.LUNARITE, -2, -1.0f, new Item.Properties()));

    // --- Marteíta (Marte): mejor que diamante ---
    public static final RegistryObject<Item> PG_MARTIANITE_SWORD = ITEMS.register("pg_martianite_sword",
            () -> new SwordItem(PGToolTiers.MARTIANITE, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> PG_MARTIANITE_PICKAXE = ITEMS.register("pg_martianite_pickaxe",
            () -> new PickaxeItem(PGToolTiers.MARTIANITE, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> PG_MARTIANITE_AXE = ITEMS.register("pg_martianite_axe",
            () -> new AxeItem(PGToolTiers.MARTIANITE, 5.0f, -3.0f, new Item.Properties()));
    public static final RegistryObject<Item> PG_MARTIANITE_SHOVEL = ITEMS.register("pg_martianite_shovel",
            () -> new ShovelItem(PGToolTiers.MARTIANITE, 1.5f, -3.0f, new Item.Properties()));
    public static final RegistryObject<Item> PG_MARTIANITE_HOE = ITEMS.register("pg_martianite_hoe",
            () -> new HoeItem(PGToolTiers.MARTIANITE, -3, 0.0f, new Item.Properties()));

    // --- Osmio (asteroides): mejor que netherita ---
    public static final RegistryObject<Item> PG_OSMIUM_SWORD = ITEMS.register("pg_osmium_sword",
            () -> new SwordItem(PGToolTiers.OSMIUM, 3, -2.4f, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PG_OSMIUM_PICKAXE = ITEMS.register("pg_osmium_pickaxe",
            () -> new PickaxeItem(PGToolTiers.OSMIUM, 1, -2.8f, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PG_OSMIUM_AXE = ITEMS.register("pg_osmium_axe",
            () -> new AxeItem(PGToolTiers.OSMIUM, 5.0f, -3.0f, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PG_OSMIUM_SHOVEL = ITEMS.register("pg_osmium_shovel",
            () -> new ShovelItem(PGToolTiers.OSMIUM, 1.5f, -3.0f, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PG_OSMIUM_HOE = ITEMS.register("pg_osmium_hoe",
            () -> new HoeItem(PGToolTiers.OSMIUM, -4, 0.0f, new Item.Properties().rarity(Rarity.UNCOMMON)));

    // --- Xenita (alienígena): el mejor nivel. No arde en lava ---
    public static final RegistryObject<Item> PG_XENITE_SWORD = ITEMS.register("pg_xenite_sword",
            () -> new SwordItem(PGToolTiers.XENITE, 3, -2.4f, new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> PG_XENITE_PICKAXE = ITEMS.register("pg_xenite_pickaxe",
            () -> new PickaxeItem(PGToolTiers.XENITE, 1, -2.8f, new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> PG_XENITE_AXE = ITEMS.register("pg_xenite_axe",
            () -> new AxeItem(PGToolTiers.XENITE, 5.0f, -3.0f, new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> PG_XENITE_SHOVEL = ITEMS.register("pg_xenite_shovel",
            () -> new ShovelItem(PGToolTiers.XENITE, 1.5f, -3.0f, new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> PG_XENITE_HOE = ITEMS.register("pg_xenite_hoe",
            () -> new HoeItem(PGToolTiers.XENITE, -5, 0.0f, new Item.Properties().rarity(Rarity.RARE).fireResistant()));

    // ===== TRAJE ESPACIAL (Fase 4) =====
    // Se fabrica con materiales de la Tierra, para poder hacer el primer viaje a la Luna.

    /** Tela espacial: lana + cuero + cuerda. */
    public static final RegistryObject<Item> PG_SPACE_FABRIC = material("pg_space_fabric");
    /** Placa reforzada: hierro + cobre. También repara el traje en el yunque. */
    public static final RegistryObject<Item> PG_REINFORCED_PLATE = material("pg_reinforced_plate");
    /** Visor del casco: paneles de cristal + oro. */
    public static final RegistryObject<Item> PG_HELMET_VISOR = material("pg_helmet_visor");
    /**
     * Guantes espaciales. Minecraft no tiene ranura para guantes, así que se usan
     * para fabricar la pechera y se ven puestos en las manos del traje.
     */
    public static final RegistryObject<Item> PG_SPACE_GLOVES = material("pg_space_gloves");

    /** PG_Space_Helmet: la pieza más importante. Permitirá respirar fuera de la Tierra (Fase 5). */
    public static final RegistryObject<Item> PG_SPACE_HELMET = ITEMS.register("pg_space_helmet",
            () -> new PGSpaceSuitItem(PGArmorMaterials.SPACE_SUIT, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> PG_SPACE_CHESTPLATE = ITEMS.register("pg_space_chestplate",
            () -> new PGSpaceSuitItem(PGArmorMaterials.SPACE_SUIT, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> PG_SPACE_LEGGINGS = ITEMS.register("pg_space_leggings",
            () -> new PGSpaceSuitItem(PGArmorMaterials.SPACE_SUIT, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> PG_SPACE_BOOTS = ITEMS.register("pg_space_boots",
            () -> new PGSpaceSuitItem(PGArmorMaterials.SPACE_SUIT, ArmorItem.Type.BOOTS, new Item.Properties()));

    // ===== OXÍGENO (Fase 5) =====
    // 1 unidad de oxígeno = 1 segundo respirando. Se fabrican vacías: llénalas en el recargador.

    /** PG_Oxygen_Tank: 10 minutos de oxígeno. */
    public static final RegistryObject<Item> PG_OXYGEN_TANK = ITEMS.register("pg_oxygen_tank",
            () -> new PGOxygenTankItem(600, new Item.Properties()));
    /** Bombona grande: 30 minutos. */
    public static final RegistryObject<Item> PG_LARGE_OXYGEN_TANK = ITEMS.register("pg_large_oxygen_tank",
            () -> new PGOxygenTankItem(1800, new Item.Properties()));
    /** Tanque espacial: 100 minutos. Necesita lunarita (se fabrica tras llegar a la Luna). */
    public static final RegistryObject<Item> PG_SPACE_OXYGEN_TANK = ITEMS.register("pg_space_oxygen_tank",
            () -> new PGOxygenTankItem(6000, new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ===== MOCHILAS (Fase 6) =====
    // PGBackpackItem(filas, oxígeno, energía FE, combustible mB, agua mB, modelo en la espalda, propiedades)

    /** PG_Space_Backpack: 9 huecos. Materiales de la Tierra. */
    public static final RegistryObject<Item> PG_SPACE_BACKPACK = ITEMS.register("pg_space_backpack",
            () -> new PGBackpackItem(1, 1200, 5000, 2000, 2000, "pg_space_backpack", new Item.Properties()));
    /** Mochila avanzada: 18 huecos. Necesita lunarita y selenita (Luna). */
    public static final RegistryObject<Item> PG_ADVANCED_SPACE_BACKPACK = ITEMS.register("pg_advanced_space_backpack",
            () -> new PGBackpackItem(2, 3000, 20000, 4000, 4000, "pg_advanced_space_backpack",
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** Mochila tecnológica: 27 huecos. Necesita marteíta y cristal marciano (Marte). */
    public static final RegistryObject<Item> PG_TECH_SPACE_BACKPACK = ITEMS.register("pg_tech_space_backpack",
            () -> new PGBackpackItem(3, 6000, 50000, 8000, 8000, "pg_tech_space_backpack",
                    new Item.Properties().rarity(Rarity.RARE)));
    /** Mochila experimental: 36 huecos. Necesita xenita, osmio y cristal cósmico. No arde en lava. */
    public static final RegistryObject<Item> PG_EXPERIMENTAL_SPACE_BACKPACK = ITEMS.register("pg_experimental_space_backpack",
            () -> new PGBackpackItem(4, 12000, 200000, 16000, 16000, "pg_experimental_space_backpack",
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // ===== ENERGÍA (Fase 7) =====
    // PGBatteryItem(capacidad FE, máximo por transferencia, propiedades)

    /** Batería básica: 50 000 FE. Cobre y redstone (Tierra). */
    public static final RegistryObject<Item> PG_BASIC_BATTERY = ITEMS.register("pg_basic_battery",
            () -> new PGBatteryItem(50_000, 10_000, new Item.Properties()));
    /** Batería avanzada: 250 000 FE. Lunarita y selenita (Luna). */
    public static final RegistryObject<Item> PG_ADVANCED_BATTERY = ITEMS.register("pg_advanced_battery",
            () -> new PGBatteryItem(250_000, 50_000, new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ===== COHETES (Fase 9) =====

    /** Componentes del cohete (se fabrican en el Banco de Ingeniería, categoría Cohetes). */
    public static final RegistryObject<Item> PG_ROCKET_HULL = material("pg_rocket_hull");
    public static final RegistryObject<Item> PG_ROCKET_ENGINE = material("pg_rocket_engine");
    public static final RegistryObject<Item> PG_ROCKET_FUEL_TANK = material("pg_rocket_fuel_tank");
    public static final RegistryObject<Item> PG_ROCKET_NOSE_CONE = material("pg_rocket_nose_cone");
    public static final RegistryObject<Item> PG_ROCKET_FINS = material("pg_rocket_fins");

    /** Bidón de combustible: 1000 mB. */
    public static final RegistryObject<Item> PG_FUEL_CANISTER = ITEMS.register("pg_fuel_canister",
            () -> new PGFuelCanisterItem(1_000, new Item.Properties()));

    /** PG_Space_Rocket básico: Tierra -> Luna. */
    public static final RegistryObject<Item> PG_BASIC_ROCKET = ITEMS.register("pg_basic_rocket",
            () -> new PGRocketItem(RocketTier.BASIC, new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** Cohete avanzado: Tierra -> Marte. Lleva lunarita. */
    public static final RegistryObject<Item> PG_ADVANCED_ROCKET = ITEMS.register("pg_advanced_rocket",
            () -> new PGRocketItem(RocketTier.ADVANCED, new Item.Properties().rarity(Rarity.RARE)));

    // ===== NAVES ESPACIALES (Fase 14) =====

    /** Componentes de la nave (Banco de Ingeniería, categoría Naves). */
    public static final RegistryObject<Item> PG_SHIP_HULL = material("pg_ship_hull", Rarity.UNCOMMON);
    public static final RegistryObject<Item> PG_ION_ENGINE = material("pg_ion_engine", Rarity.UNCOMMON);
    public static final RegistryObject<Item> PG_SHIP_COCKPIT = material("pg_ship_cockpit", Rarity.UNCOMMON);

    /** PG_Space_Ship: vuela libre; Luna, Marte y asteroides. */
    public static final RegistryObject<Item> PG_SPACE_SHIP = ITEMS.register("pg_space_ship",
            () -> new PGShipItem(ShipTier.SHIP, new Item.Properties().rarity(Rarity.RARE)));
    /** Nave avanzada: planetas exteriores y alienígenas. Lleva metal de asteroide y osmio. */
    public static final RegistryObject<Item> PG_ADVANCED_SPACE_SHIP = ITEMS.register("pg_advanced_space_ship",
            () -> new PGShipItem(ShipTier.ADVANCED, new Item.Properties().rarity(Rarity.EPIC)));

    // ===== ARMAS LÁSER (Fase 15) =====

    /** Componentes del láser (Banco de Ingeniería, categoría Láseres). */
    public static final RegistryObject<Item> PG_FOCUSING_LENS = material("pg_focusing_lens");
    public static final RegistryObject<Item> PG_LASER_EMITTER = material("pg_laser_emitter", Rarity.UNCOMMON);

    /** 🔴 Pistola láser roja: la básica. Lunarita (Luna). */
    public static final RegistryObject<Item> PG_LASER_PISTOL = ITEMS.register("pg_laser_pistol",
            () -> new PGLaserItem(LaserTier.PISTOL, new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** 🟢 Verde: gasta poca energía. Lunarita y esmeralda. */
    public static final RegistryObject<Item> PG_GREEN_LASER_PISTOL = ITEMS.register("pg_green_laser_pistol",
            () -> new PGLaserItem(LaserTier.GREEN, new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** 🔵 Azul: más daño. Marteíta y cristal marciano. */
    public static final RegistryObject<Item> PG_BLUE_LASER_PISTOL = ITEMS.register("pg_blue_laser_pistol",
            () -> new PGLaserItem(LaserTier.BLUE, new Item.Properties().rarity(Rarity.RARE)));
    /** 🟣 Púrpura: perfora armaduras. Osmio y astralita (asteroides). */
    public static final RegistryObject<Item> PG_PURPLE_LASER_PISTOL = ITEMS.register("pg_purple_laser_pistol",
            () -> new PGLaserItem(LaserTier.PURPLE, new Item.Properties().rarity(Rarity.RARE)));
    /** 🟡 Dorada: ráfaga rápida. Xenita, oro y tecnología alienígena. */
    public static final RegistryObject<Item> PG_GOLD_LASER_PISTOL = ITEMS.register("pg_gold_laser_pistol",
            () -> new PGLaserItem(LaserTier.GOLD, new Item.Properties().rarity(Rarity.EPIC)));
    /** ⚪ Blanca: extremadamente poderosa. Cristal cósmico, necronita y tecnología alienígena. */
    public static final RegistryObject<Item> PG_WHITE_LASER_PISTOL = ITEMS.register("pg_white_laser_pistol",
            () -> new PGLaserItem(LaserTier.WHITE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    /** Rifle láser: potente y atraviesa enemigos. Marteíta y cristal marciano (Marte). */
    public static final RegistryObject<Item> PG_LASER_RIFLE = ITEMS.register("pg_laser_rifle",
            () -> new PGLaserItem(LaserTier.RIFLE, new Item.Properties().rarity(Rarity.RARE)));

    // ===== ESPADAS LÁSER (Fase 16) =====

    /** Empuñadura: la base de todas las espadas láser. */
    public static final RegistryObject<Item> PG_LASER_SWORD_HILT = material("pg_laser_sword_hilt", Rarity.UNCOMMON);

    /** Hoja azul (selenita, Luna). */
    public static final RegistryObject<Item> PG_BLUE_LASER_SWORD = ITEMS.register("pg_blue_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.BLUE, new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** Hoja roja (cristal marciano, Marte): quema. */
    public static final RegistryObject<Item> PG_RED_LASER_SWORD = ITEMS.register("pg_red_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.RED, new Item.Properties().rarity(Rarity.RARE)));
    /** Hoja verde (selenita y esmeralda): gasta muy poca energía. */
    public static final RegistryObject<Item> PG_GREEN_LASER_SWORD = ITEMS.register("pg_green_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.GREEN, new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** Hoja púrpura (osmio y astralita): perfora armaduras. */
    public static final RegistryObject<Item> PG_PURPLE_LASER_SWORD = ITEMS.register("pg_purple_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.PURPLE, new Item.Properties().rarity(Rarity.RARE)));
    /** Hoja dorada (xenita, oro y tecnología alienígena): muy rápida. */
    public static final RegistryObject<Item> PG_GOLD_LASER_SWORD = ITEMS.register("pg_gold_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.GOLD, new Item.Properties().rarity(Rarity.EPIC)));
    /** Hoja blanca (cristal cósmico y tecnología alienígena): muy fuerte, hace brillar al enemigo. */
    public static final RegistryObject<Item> PG_WHITE_LASER_SWORD = ITEMS.register("pg_white_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.WHITE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    /** ⚫ Hoja NEGRA: extremadamente rara. Necronita (de la Reina alienígena) y tecnología alienígena. */
    public static final RegistryObject<Item> PG_BLACK_LASER_SWORD = ITEMS.register("pg_black_laser_sword",
            () -> new PGLaserSwordItem(LaserSwordTier.BLACK, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // ===== CRIATURAS (Fase 17) =====

    /** Quitina alienígena: caparazón de crawlers, escorpiones y gusanos. */
    public static final RegistryObject<Item> PG_ALIEN_CHITIN = material("pg_alien_chitin");
    /** Tecnología alienígena: la sueltan los aliens. Para la tecnología más avanzada. */
    public static final RegistryObject<Item> PG_ALIEN_TECH = material("pg_alien_tech", Rarity.RARE);

    // Huevos de aparición (color de fondo, color de las manchas)
    public static final RegistryObject<Item> PG_LUNAR_CRAWLER_SPAWN_EGG = egg("lunar_crawler", ModEntities.LUNAR_CRAWLER, 0xB8BCC4, 0x5A6070);
    public static final RegistryObject<Item> PG_LUNAR_SCORPION_SPAWN_EGG = egg("lunar_scorpion", ModEntities.LUNAR_SCORPION, 0xD8DCE4, 0x6A8CC8);
    public static final RegistryObject<Item> PG_MARTIAN_WORM_SPAWN_EGG = egg("martian_worm", ModEntities.MARTIAN_WORM, 0xA0522D, 0xF0C080);
    public static final RegistryObject<Item> PG_MARTIAN_SCORPION_SPAWN_EGG = egg("martian_scorpion", ModEntities.MARTIAN_SCORPION, 0xB03A20, 0x401810);
    public static final RegistryObject<Item> PG_MARTIAN_CRAWLER_SPAWN_EGG = egg("martian_crawler", ModEntities.MARTIAN_CRAWLER, 0x8A3A22, 0xE0703A);
    public static final RegistryObject<Item> PG_ALIEN_EXPLORER_SPAWN_EGG = egg("alien_explorer", ModEntities.ALIEN_EXPLORER, 0x7ACC6A, 0xE0E0E0);
    public static final RegistryObject<Item> PG_ALIEN_SOLDIER_SPAWN_EGG = egg("alien_soldier", ModEntities.ALIEN_SOLDIER, 0x4A8A40, 0x303848);
    public static final RegistryObject<Item> PG_ALIEN_CREATURE_SPAWN_EGG = egg("alien_creature", ModEntities.ALIEN_CREATURE, 0x5A3A7A, 0xC0FF60);
    public static final RegistryObject<Item> PG_ALIEN_PREDATOR_SPAWN_EGG = egg("alien_predator", ModEntities.ALIEN_PREDATOR, 0x2A3020, 0xB04AFF);

    // ===== JEFES (Fase 18) =====

    /** Baliza alienígena: invoca a la Reina alienígena en otro planeta. */
    public static final RegistryObject<Item> PG_ALIEN_BEACON = ITEMS.register("pg_alien_beacon",
            () -> new PGAlienBeaconItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PG_ALIEN_QUEEN_SPAWN_EGG = egg("alien_queen", ModEntities.ALIEN_QUEEN, 0x2A1A3A, 0x9CFF3A);

    // ===== AYUDANTES =====

    /** Huevo de aparición de una criatura. */
    private static RegistryObject<Item> egg(String mob, Supplier<? extends EntityType<? extends Mob>> type, int background, int spots) {
        return ITEMS.register("pg_" + mob + "_spawn_egg", () -> new ForgeSpawnEggItem(type, background, spots, new Item.Properties()));
    }

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
