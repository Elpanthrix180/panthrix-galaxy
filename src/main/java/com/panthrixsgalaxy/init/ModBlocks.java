package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Lista de TODOS los bloques del mod.
 * Para añadir un bloque nuevo: copia la línea de PG_TEST_BLOCK, cambia el nombre
 * y añade su objeto en ModItems.
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PanthrixsGalaxy.MOD_ID);

    // ===== BLOQUES DE PRUEBA (Fase 1) =====

    /** PG_Test_Block: bloque metálico de prueba que emite un poco de luz. */
    public static final RegistryObject<Block> PG_TEST_BLOCK = BLOCKS.register("pg_test_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 6.0f)          // dureza y resistencia a explosiones
                    .requiresCorrectToolForDrops() // hay que usar pico para que suelte el bloque
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7)));     // emite luz nivel 7

    private ModBlocks() {
    }
}
