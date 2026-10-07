package com.panthrixsgalaxy.planet;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Un cuerpo celeste (planeta, luna...). Toda la información de un planeta está aquí,
 * así añadir uno nuevo no obliga a cambiar el resto del mod.
 *
 * @param id            nombre interno ("moon"): se usa en traducciones y texturas
 * @param dimension     su dimensión en Minecraft (null = todavía no existe)
 * @param requiredReach nivel de cohete necesario para llegar (ver RocketTier)
 * @param spaceOffset   dónde está en el Espacio, contando desde el punto de llegada desde la Tierra
 * @param radius        tamaño visual en el cielo del Espacio (en bloques)
 * @param entryDistance a qué distancia del centro empieza el descenso al planeta
 * @param gravity       gravedad (1.0 = la de la Tierra)
 * @param breathable    ¿tiene aire respirable?
 * @param exitHeight    altura a la que un cohete que despega del planeta sale al Espacio
 * @param texture       imagen que se dibuja en el cielo del Espacio
 */
public record PGPlanet(String id,
                       @Nullable ResourceKey<Level> dimension,
                       int requiredReach,
                       Vec3 spaceOffset,
                       float radius,
                       float entryDistance,
                       double gravity,
                       boolean breathable,
                       int exitHeight,
                       ResourceLocation texture) {

    public String getTranslationKey() {
        return "planet.panthrixsgalaxy." + id;
    }

    /** ¿Hace falta plataforma de lanzamiento para despegar? Solo con gravedad fuerte. */
    public boolean needsLaunchPad() {
        return gravity >= 0.5;
    }

    /** ¿Ya se puede aterrizar en él? */
    public boolean isLandable() {
        return dimension != null;
    }
}
