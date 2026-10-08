package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.block.entity.PGCableBlockEntity;
import com.panthrixsgalaxy.block.entity.PGDiscoveryBlockEntity;
import com.panthrixsgalaxy.block.entity.PGElectricOxygenRechargerBlockEntity;
import com.panthrixsgalaxy.block.entity.PGEnergyCellBlockEntity;
import com.panthrixsgalaxy.block.entity.PGFuelRefineryBlockEntity;
import com.panthrixsgalaxy.block.entity.PGGeneratorBlockEntity;
import com.panthrixsgalaxy.block.entity.PGOxygenDistributorBlockEntity;
import com.panthrixsgalaxy.block.entity.PGOxygenStorageBlockEntity;
import com.panthrixsgalaxy.block.entity.PGReactorBlockEntity;
import com.panthrixsgalaxy.block.entity.PGSolarPanelBlockEntity;
import com.panthrixsgalaxy.block.entity.PGStorageCrateBlockEntity;
import com.panthrixsgalaxy.platform.PGHolder;
import com.panthrixsgalaxy.platform.PGRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Tipos de "block entity" (bloques con memoria) del mod.
 * Cada uno une la clase que piensa (PGxxxBlockEntity) con el bloque que la usa.
 */
public final class ModBlockEntities {

    public static final PGRegistry<BlockEntityType<?>> BLOCK_ENTITIES = PGRegistry.create(Registries.BLOCK_ENTITY_TYPE);

    @SuppressWarnings("DataFlowIssue") // build(null) es lo normal
    public static final PGHolder<BlockEntityType<PGGeneratorBlockEntity>> GENERATOR =
            BLOCK_ENTITIES.register("generator", () -> BlockEntityType.Builder
                    .of(PGGeneratorBlockEntity::new, ModBlocks.PG_GENERATOR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGSolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of(PGSolarPanelBlockEntity::new, ModBlocks.PG_SOLAR_PANEL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGReactorBlockEntity>> REACTOR =
            BLOCK_ENTITIES.register("reactor", () -> BlockEntityType.Builder
                    .of(PGReactorBlockEntity::new, ModBlocks.PG_HELIUM_3_REACTOR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGEnergyCellBlockEntity>> ENERGY_CELL =
            BLOCK_ENTITIES.register("energy_cell", () -> BlockEntityType.Builder
                    .of(PGEnergyCellBlockEntity::new, ModBlocks.PG_ENERGY_CELL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGCableBlockEntity>> CABLE =
            BLOCK_ENTITIES.register("energy_cable", () -> BlockEntityType.Builder
                    .of(PGCableBlockEntity::new, ModBlocks.PG_ENERGY_CABLE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGElectricOxygenRechargerBlockEntity>> ELECTRIC_OXYGEN_RECHARGER =
            BLOCK_ENTITIES.register("electric_oxygen_recharger", () -> BlockEntityType.Builder
                    .of(PGElectricOxygenRechargerBlockEntity::new, ModBlocks.PG_ELECTRIC_OXYGEN_RECHARGER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGFuelRefineryBlockEntity>> FUEL_REFINERY =
            BLOCK_ENTITIES.register("fuel_refinery", () -> BlockEntityType.Builder
                    .of(PGFuelRefineryBlockEntity::new, ModBlocks.PG_FUEL_REFINERY.get()).build(null));

    // ===== Fase 19: bases y estaciones =====

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGOxygenDistributorBlockEntity>> OXYGEN_DISTRIBUTOR =
            BLOCK_ENTITIES.register("oxygen_distributor", () -> BlockEntityType.Builder
                    .of(PGOxygenDistributorBlockEntity::new, ModBlocks.PG_OXYGEN_DISTRIBUTOR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGOxygenStorageBlockEntity>> OXYGEN_STORAGE =
            BLOCK_ENTITIES.register("oxygen_storage", () -> BlockEntityType.Builder
                    .of(PGOxygenStorageBlockEntity::new, ModBlocks.PG_OXYGEN_STORAGE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGStorageCrateBlockEntity>> STORAGE_CRATE =
            BLOCK_ENTITIES.register("storage_crate", () -> BlockEntityType.Builder
                    .of(PGStorageCrateBlockEntity::new, ModBlocks.PG_STORAGE_CRATE.get()).build(null));

    // ===== Fase 22: descubrimientos secretos =====

    @SuppressWarnings("DataFlowIssue")
    public static final PGHolder<BlockEntityType<PGDiscoveryBlockEntity>> DISCOVERY =
            BLOCK_ENTITIES.register("discovery", () -> BlockEntityType.Builder
                    .of(PGDiscoveryBlockEntity::new, ModBlocks.PG_MONOLITH.get(), ModBlocks.PG_ALIEN_ARCHIVE.get()).build(null));

    private ModBlockEntities() {
    }
}
