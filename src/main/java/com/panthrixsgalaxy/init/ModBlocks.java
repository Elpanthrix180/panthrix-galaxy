package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.block.PGOxygenRechargerBlock;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Lista de TODOS los bloques del mod.
 *
 * Cada bloque se registra con registerBlock(...), que además crea automáticamente
 * su objeto (BlockItem) para poder tenerlo en el inventario.
 *
 * Para añadir un bloque nuevo: copia una línea parecida, cambia el nombre y crea
 * su textura, modelo, blockstate, loot table y traducción (mira docs/FASE_2.md).
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PanthrixsGalaxy.MOD_ID);

    // ===== BLOQUES DE PRUEBA (Fase 1) =====

    /** PG_Test_Block: bloque metálico de prueba que emite un poco de luz. */
    public static final RegistryObject<Block> PG_TEST_BLOCK = registerBlock("pg_test_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 6.0f)          // dureza y resistencia a explosiones
                    .requiresCorrectToolForDrops() // hay que usar pico para que suelte el bloque
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7)));     // emite luz nivel 7

    // ===== LUNA (Fase 2) =====

    /** Superficie de la Luna. Se recoge con pala. Material de construcción para bases lunares. */
    public static final RegistryObject<Block> PG_LUNAR_REGOLITH = registerBlock("pg_lunar_regolith",
            () -> new Block(soil(MapColor.COLOR_LIGHT_GRAY, SoundType.SAND)));
    /** Piedra base del subsuelo lunar (donde aparecen las menas). */
    public static final RegistryObject<Block> PG_LUNAR_STONE = registerBlock("pg_lunar_stone",
            () -> new Block(stone(MapColor.STONE, 1.5f, 6.0f, SoundType.STONE)));
    public static final RegistryObject<Block> PG_LUNARITE_ORE = registerBlock("pg_lunarite_ore",
            () -> new DropExperienceBlock(stone(MapColor.STONE, 3.0f, 3.0f, SoundType.STONE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_SELENITE_ORE = registerBlock("pg_selenite_ore",
            () -> new DropExperienceBlock(stone(MapColor.STONE, 3.0f, 3.0f, SoundType.STONE), xp(2, 5)));
    public static final RegistryObject<Block> PG_HELIUM_3_ORE = registerBlock("pg_helium_3_ore",
            () -> new DropExperienceBlock(stone(MapColor.COLOR_LIGHT_GRAY, 2.0f, 3.0f, SoundType.GRAVEL), xp(1, 3)));
    public static final RegistryObject<Block> PG_LUNARITE_BLOCK = registerBlock("pg_lunarite_block",
            () -> new Block(stone(MapColor.COLOR_LIGHT_BLUE, 5.0f, 6.0f, SoundType.METAL)));

    // ===== MARTE (Fase 2) =====

    /** Superficie roja de Marte. Se recoge con pala. */
    public static final RegistryObject<Block> PG_MARTIAN_SOIL = registerBlock("pg_martian_soil",
            () -> new Block(soil(MapColor.COLOR_ORANGE, SoundType.SAND)));
    /** Piedra base del subsuelo marciano. */
    public static final RegistryObject<Block> PG_MARTIAN_STONE = registerBlock("pg_martian_stone",
            () -> new Block(stone(MapColor.TERRACOTTA_RED, 1.5f, 6.0f, SoundType.STONE)));
    public static final RegistryObject<Block> PG_MARTIAN_ORE = registerBlock("pg_martian_ore",
            () -> new DropExperienceBlock(stone(MapColor.TERRACOTTA_RED, 3.0f, 3.0f, SoundType.STONE), xp(1, 3)));
    public static final RegistryObject<Block> PG_MARTIANITE_ORE = registerBlock("pg_martianite_ore",
            () -> new DropExperienceBlock(stone(MapColor.TERRACOTTA_RED, 3.5f, 3.0f, SoundType.STONE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_MARTIAN_CRYSTAL_ORE = registerBlock("pg_martian_crystal_ore",
            () -> new DropExperienceBlock(stone(MapColor.TERRACOTTA_RED, 3.5f, 3.0f, SoundType.STONE), xp(3, 7)));
    public static final RegistryObject<Block> PG_RUSTED_IRON_ORE = registerBlock("pg_rusted_iron_ore",
            () -> new DropExperienceBlock(stone(MapColor.TERRACOTTA_RED, 3.0f, 3.0f, SoundType.STONE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_MARTIANITE_BLOCK = registerBlock("pg_martianite_block",
            () -> new Block(stone(MapColor.COLOR_RED, 5.0f, 6.0f, SoundType.METAL)));

    // ===== ASTEROIDES (Fase 2) =====

    /** Roca que forma los asteroides. */
    public static final RegistryObject<Block> PG_ASTEROID_ROCK = registerBlock("pg_asteroid_rock",
            () -> new Block(stone(MapColor.DEEPSLATE, 2.0f, 6.0f, SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> PG_METEORITE_ORE = registerBlock("pg_meteorite_ore",
            () -> new DropExperienceBlock(stone(MapColor.DEEPSLATE, 4.0f, 6.0f, SoundType.DEEPSLATE), xp(2, 5)));
    public static final RegistryObject<Block> PG_ASTEROID_METAL_ORE = registerBlock("pg_asteroid_metal_ore",
            () -> new DropExperienceBlock(stone(MapColor.DEEPSLATE, 4.0f, 6.0f, SoundType.DEEPSLATE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_OSMIUM_ORE = registerBlock("pg_osmium_ore",
            () -> new DropExperienceBlock(stone(MapColor.DEEPSLATE, 5.0f, 9.0f, SoundType.DEEPSLATE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_ASTEROID_METAL_BLOCK = registerBlock("pg_asteroid_metal_block",
            () -> new Block(stone(MapColor.METAL, 5.0f, 6.0f, SoundType.METAL)));
    public static final RegistryObject<Block> PG_OSMIUM_BLOCK = registerBlock("pg_osmium_block",
            () -> new Block(stone(MapColor.COLOR_BLUE, 7.0f, 12.0f, SoundType.METAL)));

    // ===== ALIENÍGENA (Fase 2) =====

    /** Piedra de los planetas alienígenas. Muy dura. */
    public static final RegistryObject<Block> PG_ALIEN_STONE = registerBlock("pg_alien_stone",
            () -> new Block(stone(MapColor.COLOR_GREEN, 3.0f, 9.0f, SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> PG_XENITE_ORE = registerBlock("pg_xenite_ore",
            () -> new DropExperienceBlock(stone(MapColor.COLOR_GREEN, 6.0f, 12.0f, SoundType.DEEPSLATE), ConstantInt.of(0)));
    public static final RegistryObject<Block> PG_ASTRALITE_ORE = registerBlock("pg_astralite_ore",
            () -> new DropExperienceBlock(stone(MapColor.COLOR_GREEN, 6.0f, 12.0f, SoundType.DEEPSLATE)
                    .lightLevel(state -> 5), xp(4, 8)));
    public static final RegistryObject<Block> PG_COSMIC_CRYSTAL_ORE = registerBlock("pg_cosmic_crystal_ore",
            () -> new DropExperienceBlock(stone(MapColor.COLOR_GREEN, 8.0f, 15.0f, SoundType.AMETHYST)
                    .lightLevel(state -> 9), xp(6, 10)));
    public static final RegistryObject<Block> PG_XENITE_BLOCK = registerBlock("pg_xenite_block",
            () -> new Block(stone(MapColor.COLOR_LIGHT_GREEN, 6.0f, 9.0f, SoundType.METAL)));
    /** Bloque de necronita: tan resistente como la netherita y no se quema en lava. */
    public static final RegistryObject<Block> PG_NECRONITE_BLOCK = registerBlock("pg_necronite_block",
            () -> new Block(stone(MapColor.COLOR_BLACK, 50.0f, 1200.0f, SoundType.NETHERITE_BLOCK)),
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());

    // ===== OXÍGENO (Fase 5) =====

    /** Recargador de oxígeno: clic derecho con una bombona para llenarla (solo donde hay aire). */
    public static final RegistryObject<Block> PG_OXYGEN_RECHARGER = registerBlock("pg_oxygen_recharger",
            () -> new PGOxygenRechargerBlock(stone(MapColor.METAL, 3.5f, 6.0f, SoundType.METAL)
                    .lightLevel(state -> 4)));

    // ===== AYUDANTES =====
    // Pequeños "moldes" para no repetir las mismas propiedades en cada bloque.

    /** Piedra, mena o metal: necesita la herramienta correcta para soltar algo. */
    private static BlockBehaviour.Properties stone(MapColor color, float hardness, float resistance, SoundType sound) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(sound);
    }

    /** Tierra o arena: se puede recoger incluso a mano (la pala es más rápida). */
    private static BlockBehaviour.Properties soil(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(0.6f)
                .sound(sound);
    }

    /** Experiencia que suelta una mena al picarla (entre min y max). */
    private static IntProvider xp(int min, int max) {
        return UniformInt.of(min, max);
    }

    /** Registra un bloque y su objeto con propiedades normales. */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, new Item.Properties());
    }

    /** Registra un bloque y su objeto con propiedades especiales (rareza, resistencia al fuego...). */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, Item.Properties itemProperties) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), itemProperties));
        return registered;
    }

    private ModBlocks() {
    }
}
