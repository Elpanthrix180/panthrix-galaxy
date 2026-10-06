package com.panthrixsgalaxy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Objeto de prueba. Sirve para comprobar que el código Java del mod se ejecuta:
 * al hacer clic derecho muestra un mensaje en el chat.
 */
public class PGTestItem extends Item {

    public PGTestItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Solo en el servidor, para que el mensaje no salga duplicado
        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.translatable("message.panthrixsgalaxy.test_item_used").withStyle(ChatFormatting.AQUA),
                    false);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.pg_test_item").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
