package com.panthrixsgalaxy.system.oxygen;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.armor.PGSpaceSuitItem;
import com.panthrixsgalaxy.init.ModDamageTypes;
import com.panthrixsgalaxy.network.OxygenSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El "corazón" del sistema de oxígeno. Se ejecuta una vez por segundo para cada jugador:
 *
 *   ¿Hay aire?  sí -> no pasa nada.
 *               no -> ¿casco + oxígeno?  sí -> gasta 1 de oxígeno.
 *                     ¿casco + agua y energía en la mochila? sí -> oxígeno de emergencia (Fase 7B).
 *                                        no -> advertencia, 5 s de margen y después daño.
 *
 * Además (Fase 12): sin aire tampoco hay protección térmica. Sin las 4 piezas del traje
 * espacial, la temperatura extrema hace ½ corazón de daño cada 2 segundos.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGOxygenEvents {

    /** Oxígeno que gasta el traje por segundo. */
    public static final int CONSUMPTION_PER_SECOND = 1;
    /** Por debajo de esto se avisa de "oxígeno bajo" (60 = 1 minuto). */
    public static final int LOW_OXYGEN = 60;
    /** Segundos de margen antes de empezar a hacer daño. */
    public static final int GRACE_SECONDS = 5;
    /** A partir de estos segundos sin respirar el daño se duplica. */
    public static final int STRONG_DAMAGE_SECONDS = 15;

    /** Dato guardado en el jugador: segundos que lleva sin poder respirar. */
    private static final String NO_AIR_SECONDS_TAG = "pg_no_air_seconds";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Solo en el servidor, al final del tick y una vez por segundo (20 ticks)
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        boolean airless = PGAtmosphere.isAirlessFor(player);
        int noAirSeconds = data.getInt(NO_AIR_SECONDS_TAG);
        OxygenState state;

        if (!airless || player.isCreative() || player.isSpectator()) {
            // Hay aire (o el jugador está en creativo): todo bien
            noAirSeconds = 0;
            state = airless ? OxygenState.OK : OxygenState.BREATHABLE;
        } else {
            boolean hasHelmet = PGSpaceSuitItem.hasSpaceHelmet(player);
            int oxygen = OxygenHelper.getTotalOxygen(player);

            if (hasHelmet && oxygen > 0) {
                // Respira gracias al traje
                OxygenHelper.consume(player, CONSUMPTION_PER_SECOND);
                noAirSeconds = 0;
                state = oxygen - CONSUMPTION_PER_SECOND < LOW_OXYGEN ? OxygenState.LOW : OxygenState.OK;
            } else if (hasHelmet && OxygenHelper.tryEmergencyElectrolysis(player)) {
                // Sin oxígeno, pero la mochila lo fabrica con agua + energía
                noAirSeconds = 0;
                state = OxygenState.EMERGENCY;
            } else {
                // No puede respirar
                state = hasHelmet ? OxygenState.NO_OXYGEN : OxygenState.NO_HELMET;
                if (noAirSeconds == 0) {
                    // Primer segundo sin aire: pitido de alarma
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1.0f, 0.5f);
                }
                noAirSeconds++;
                if (noAirSeconds > GRACE_SECONDS) {
                    float damage = noAirSeconds > STRONG_DAMAGE_SECONDS ? 2.0f : 1.0f;
                    player.hurt(ModDamageTypes.noOxygen(player.level()), damage);
                }
            }
        }

        data.putInt(NO_AIR_SECONDS_TAG, noAirSeconds);

        // Temperatura extrema: hace falta el traje completo
        boolean temperatureDanger = airless && !player.isCreative() && !player.isSpectator()
                && !PGSpaceSuitItem.hasFullSpaceSuit(player);
        if (temperatureDanger && player.tickCount % 40 == 0) {
            player.hurt(ModDamageTypes.extremeTemperature(player.level()), 1.0f);
        }

        int graceLeft = Math.max(0, GRACE_SECONDS - noAirSeconds);
        PGNetwork.sendToPlayer(player, new OxygenSyncPacket(state,
                OxygenHelper.getTotalOxygen(player), OxygenHelper.getTotalCapacity(player), graceLeft, temperatureDanger));
    }

    private PGOxygenEvents() {
    }
}
