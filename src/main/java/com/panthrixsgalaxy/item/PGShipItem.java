package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import com.panthrixsgalaxy.entity.ship.ShipTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * La nave espacial en forma de objeto. Clic derecho en el suelo para colocarla.
 * A diferencia del cohete, NO necesita plataforma de lanzamiento: despega en vertical.
 * Guarda su combustible, energía, integridad y bodega al recogerla.
 */
public class PGShipItem extends Item {

    private final ShipTier tier;

    public PGShipItem(ShipTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public ShipTier getTier() {
        return tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos ground = context.getClickedPos();
        Player player = context.getPlayer();

        // La nave mide 3 x 1,5 x 3 bloques: ese hueco tiene que estar libre
        AABB box = new AABB(ground.getX() - 1.0, ground.getY() + 1.0, ground.getZ() - 1.0,
                ground.getX() + 2.0, ground.getY() + 2.5, ground.getZ() + 2.0);
        if (!level.noCollision(box)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.ship_needs_space")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            PGShipEntity ship = PGShipEntity.create(level, this);
            ship.loadFromItem(stack);
            float yaw = player == null ? 0.0f : player.getYRot();
            ship.moveTo(ground.getX() + 0.5, ground.getY() + 1.0, ground.getZ() + 0.5, yaw, 0.0f);
            level.addFreshEntity(ship);
            level.playSound(null, ground, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, 1.2f);
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int fuel = stack.hasTag() ? stack.getTag().getInt("Fuel") : 0;
        int energy = stack.hasTag() ? stack.getTag().getInt("Energy") : 0;
        int integrity = stack.hasTag() && stack.getTag().contains("Integrity")
                ? stack.getTag().getInt("Integrity") : tier.getMaxIntegrity();
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.ship_destination." + tier.getSerializedName())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.fuel_amount", fuel, tier.getFuelCapacity())
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.ship_energy", energy, tier.getEnergyCapacity())
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.ship_integrity", integrity, tier.getMaxIntegrity())
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.ship_cargo", tier.getCargoRows() * 9)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.ship_hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
