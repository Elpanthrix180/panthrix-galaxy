package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.rocket.RocketTier;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModEntities;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * El cohete en forma de objeto. Clic derecho en el CENTRO de una plataforma de
 * lanzamiento de 3x3 para colocarlo. Guarda el combustible que tenía al recogerlo.
 */
public class PGRocketItem extends Item {

    private static final String FUEL_TAG = "Fuel";
    /** Altura libre necesaria sobre la plataforma (el cohete mide 3 bloques). */
    private static final int REQUIRED_HEIGHT = 3;

    private final RocketTier tier;

    public PGRocketItem(RocketTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public RocketTier getTier() {
        return tier;
    }

    public static int getStoredFuel(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(FUEL_TAG) : 0;
    }

    public static void setStoredFuel(ItemStack stack, int fuel) {
        stack.getOrCreateTag().putInt(FUEL_TAG, fuel);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        Player player = context.getPlayer();

        if (!isLaunchPad3x3(level, center)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.rocket_needs_pad")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        for (int y = 1; y <= REQUIRED_HEIGHT; y++) {
            if (!level.getBlockState(center.above(y)).canBeReplaced()) {
                if (player != null && !level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.rocket_needs_space")
                            .withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }
        }

        if (!level.isClientSide) {
            PGRocketEntity rocket = new PGRocketEntity(ModEntities.ROCKET.get(), level);
            rocket.setTier(tier);
            ItemStack stack = context.getItemInHand();
            rocket.setFuel(getStoredFuel(stack));
            float yaw = player == null ? 0.0f : Math.round(player.getYRot() / 90.0f) * 90.0f;
            rocket.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, yaw, 0.0f);
            level.addFreshEntity(rocket);
            level.playSound(null, center, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, 1.4f);
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** ¿Hay 3x3 bloques de plataforma de lanzamiento con este en el centro? */
    private static boolean isLaunchPad3x3(Level level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.getBlockState(center.offset(dx, 0, dz)).is(ModBlocks.PG_LAUNCH_PAD.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.rocket_destination." + tier.getSerializedName())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.fuel_amount",
                getStoredFuel(stack), tier.getFuelCapacity()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.rocket_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
