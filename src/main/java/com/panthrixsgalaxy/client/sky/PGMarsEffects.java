package com.panthrixsgalaxy.client.sky;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Aspecto de Marte: el cielo normal de Minecraft (con día, noche, Sol, estrellas y una
 * luna que hace de Fobos) pero con los colores anaranjados del bioma marciano y sin nubes.
 * Se une a la dimensión por "effects": "panthrixsgalaxy:mars".
 */
public class PGMarsEffects extends DimensionSpecialEffects {

    public PGMarsEffects() {
        // sin nubes (NaN), tiene suelo, cielo normal, sin luz forzada, sin luz constante
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    /** Igual que en la Tierra: la niebla se oscurece de noche. */
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(brightness * 0.94f + 0.06f, brightness * 0.94f + 0.06f, brightness * 0.91f + 0.09f);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}
