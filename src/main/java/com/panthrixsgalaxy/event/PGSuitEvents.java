package com.panthrixsgalaxy.event;

import com.panthrixsgalaxy.armor.PGSpaceSuitItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Eventos del traje espacial.
 * Forge llama a estos métodos automáticamente cuando ocurre algo en el juego
 * (gracias a @Mod.EventBusSubscriber).
 */
public final class PGSuitEvents {

    /**
     * Cuando un jugador se pone una pieza del traje y con ella completa las 4,
     * aparece un aviso sobre la barra de objetos y suena un pitido.
     */
    /** Cuando una criatura cambia lo que lleva puesto (lo llaman Forge y Fabric). */
    public static void onEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack to) {
        if (!(entity instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (slot.getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        boolean putOnSuitPiece = PGSpaceSuitItem.isSuitPiece(to);
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
