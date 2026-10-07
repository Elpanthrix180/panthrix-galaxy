package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.block.entity.PGCableBlockEntity;
import com.panthrixsgalaxy.block.entity.PGElectricOxygenRechargerBlockEntity;
import com.panthrixsgalaxy.block.entity.PGEnergyCellBlockEntity;
import com.panthrixsgalaxy.block.entity.PGFuelRefineryBlockEntity;
import com.panthrixsgalaxy.block.entity.PGGeneratorBlockEntity;
import com.panthrixsgalaxy.block.entity.PGReactorBlockEntity;
import com.panthrixsgalaxy.block.entity.PGSolarPanelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Tipos de "block entity" (bloques con memoria) del mod.
 * Cada uno une la clase que piensa (PGxxxBlockEntity) con el bloque que la usa.
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PanthrixsGalaxy.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // build(null) es lo normal en Forge
    public static final RegistryObject<BlockEntityType<PGGeneratorBlockEntity>> GENERATOR =
            BLOCK_ENTITIES.register("generator", () -> BlockEntityType.Builder
                    .of(PGGeneratorBlockEntity::new, ModBlocks.PG_GENERATOR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGSolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of(PGSolarPanelBlockEntity::new, ModBlocks.PG_SOLAR_PANEL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGReactorBlockEntity>> REACTOR =
            BLOCK_ENTITIES.register("reactor", () -> BlockEntityType.Builder
                    .of(PGReactorBlockEntity::new, ModBlocks.PG_HELIUM_3_REACTOR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGEnergyCellBlockEntity>> ENERGY_CELL =
            BLOCK_ENTITIES.register("energy_cell", () -> BlockEntityType.Builder
                    .of(PGEnergyCellBlockEntity::new, ModBlocks.PG_ENERGY_CELL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGCableBlockEntity>> CABLE =
            BLOCK_ENTITIES.register("energy_cable", () -> BlockEntityType.Builder
                    .of(PGCableBlockEntity::new, ModBlocks.PG_ENERGY_CABLE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGElectricOxygenRechargerBlockEntity>> ELECTRIC_OXYGEN_RECHARGER =
            BLOCK_ENTITIES.register("electric_oxygen_recharger", () -> BlockEntityType.Builder
                    .of(PGElectricOxygenRechargerBlockEntity::new, ModBlocks.PG_ELECTRIC_OXYGEN_RECHARGER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PGFuelRefineryBlockEntity>> FUEL_REFINERY =
            BLOCK_ENTITIES.register("fuel_refinery", () -> BlockEntityType.Builder
                    .of(PGFuelRefineryBlockEntity::new, ModBlocks.PG_FUEL_REFINERY.get()).build(null));

    private ModBlockEntities() {
    }
}
