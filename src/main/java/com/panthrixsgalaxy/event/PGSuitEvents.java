package com.panthrixsgalaxy.event;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.armor.PGSpaceSuitItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Eventos del traje espacial.
 * Forge llama a estos métodos automáticamente cuando ocurre algo en el juego
 * (gracias a @Mod.EventBusSubscriber).
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGSuitEvents {

    /**
     * Cuando un jugador se pone una pieza del traje y con ella completa las 4,
     * aparece un aviso sobre la barra de objetos y suena un pitido.
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        boolean putOnSuitPiece = PGSpaceSuitItem.isSuitPiece(event.getTo());
        if (putOnSuitPiece && PGSpaceSuitItem.hasFullSpaceSuit(player)) {
            player.displayClientMessage(
                    Component.translatable("message.panthrixsgalaxy.space_suit_complete").withStyle(ChatFormatting.GREEN),
                    true); // true = mensaje sobre la barra de objetos, no en el chat
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
        }
    }

    private PGSuitEvents() {
    }
}
