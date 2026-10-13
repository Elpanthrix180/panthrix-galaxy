package com.panthrixseconomy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Tarjeta bancaria personal. Solo funciona para su titular. */
public class BankCardItem extends Item {

    public BankCardItem(Properties props) {
        super(props);
    }

    public static ItemStack forPlayer(Item item, Player owner) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("Owner", owner.getUUID());
        tag.putString("OwnerName", owner.getGameProfile().getName());
        return stack;
    }

    public static boolean belongsTo(ItemStack stack, Player player) {
        return stack.getItem() instanceof BankCardItem && stack.hasTag() && stack.getTag().hasUUID("Owner")
                && stack.getTag().getUUID("Owner").equals(player.getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("OwnerName")) {
            tooltip.add(Component.translatable("item.panthrixseconomy.bank_card.owner", stack.getTag().getString("OwnerName"))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.panthrixseconomy.bank_card.blank").withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("item.panthrixseconomy.bank_card.help").withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}
