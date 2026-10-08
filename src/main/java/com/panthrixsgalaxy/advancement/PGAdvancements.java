package com.panthrixsgalaxy.advancement;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Conceder logros desde Java.
 *
 * La mayoría de los logros del mod son JSON normales (data/panthrixsgalaxy/advancements/):
 * "consigue este objeto", "entra en esta dimensión", "mata a este mob"... Minecraft los comprueba solo.
 *
 * Los que Minecraft NO sabe comprobar (estar 5 minutos en la Luna, contar 10 muertes, visitar
 * 3 planetas cualesquiera, construir una base...) tienen en su JSON el criterio "minecraft:impossible"
 * y los concede este código con award(...).
 */
public final class PGAdvancements {

    /** Concede un logro del mod. Devuelve true si lo acaba de conseguir (no si ya lo tenía). */
    public static boolean award(ServerPlayer player, String id) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(new ResourceLocation(PanthrixsGalaxy.MOD_ID, id));
        if (advancement == null) {
            return false;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return false;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
        return true;
    }

    private PGAdvancements() {
    }
}
