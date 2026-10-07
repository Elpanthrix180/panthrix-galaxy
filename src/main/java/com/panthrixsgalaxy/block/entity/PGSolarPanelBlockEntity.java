package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Panel solar: Sol -> panel -> energía.
 *
 * Produce hasta 15 FE/t a mediodía con el cielo despejado. Produce menos al amanecer,
 * al atardecer y con lluvia, y nada de noche o bajo techo.
 * En lugares SIN ATMÓSFERA (Luna, espacio) produce el DOBLE: no hay aire que filtre la luz.
 */
public class PGSolarPanelBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 10_000;
    public static final int MAX_GENERATION = 15;
    public static final int PUSH = 100;

    /** Producción actual (se recalcula cada segundo). */
    private int currentGeneration;

    public PGSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state, CAPACITY, 0, PUSH);
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }
        if (level.getGameTime() % 20 == 0) {
            currentGeneration = calculateGeneration();
        }
        if (currentGeneration > 0) {
            energy.generate(currentGeneration);
        }
        pushEnergyToNeighbors(PUSH);
    }

    /** Cuánta energía produce ahora según la luz del sol que recibe. */
    private int calculateGeneration() {
        BlockPos above = worldPosition.above();
        // En dimensiones con hora fija (la Luna) Minecraft nunca dice "es de día": lo comprobamos a mano
        boolean day = level.dimensionType().hasFixedTime() ? level.getSkyDarken() < 4 : level.isDay();
        if (!level.dimensionType().hasSkyLight() || !day || !level.canSeeSky(above)) {
            return 0;
        }
        // Luz del cielo (0-15) menos lo que oscurece la hora del día y la lluvia
        int sunlight = level.getBrightness(LightLayer.SKY, above) - level.getSkyDarken();
        if (sunlight <= 0) {
            return 0;
        }
        float generation = MAX_GENERATION * sunlight / 15.0f;
        if (PGAtmosphere.isAirlessDimension(level)) {
            generation *= 2.0f;
        }
        return Math.max(1, Math.round(generation));
    }

    @Override
    protected Component getStatus() {
        return currentGeneration > 0
                ? Component.translatable("status.panthrixsgalaxy.producing", currentGeneration)
                : Component.translatable("status.panthrixsgalaxy.no_sun");
    }
}
