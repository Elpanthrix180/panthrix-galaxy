package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import com.panthrixsgalaxy.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
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
 * Baliza alienígena: una señal hecha con tecnología alienígena. Clic derecho en el suelo
 * de OTRO PLANETA (no en la Tierra) y... la REINA ALIENÍGENA acude a la llamada.
 *
 * La Reina también vive en su COLMENA, en el planeta alienígena Xenoria (Fase 21).
 */
public class PGAlienBeaconItem extends Item {

    private static final double ONE_QUEEN_RADIUS = 128.0;

    public PGAlienBeaconItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER || level.dimension() == Level.END) {
            tell(player, "message.panthrixsgalaxy.beacon_not_here");
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().above();
        AABB area = new AABB(pos).inflate(ONE_QUEEN_RADIUS);
        if (!level.getEntitiesOfClass(PGAlienQueenEntity.class, area).isEmpty()) {
            tell(player, "message.panthrixsgalaxy.beacon_queen_present");
            return InteractionResult.FAIL;
        }
        PGAlienQueenEntity queen = ModEntities.ALIEN_QUEEN.get().create(server);
        if (queen == null) {
            return InteractionResult.FAIL;
        }
        // Un rayo (solo efecto, no quema nada) y aparece la Reina
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(server);
        if (lightning != null) {
            lightning.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            lightning.setVisualOnly(true);
            server.addFreshEntity(lightning);
        }
        queen.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player == null ? 0.0f : player.getYRot() + 180.0f, 0.0f);
        queen.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        server.addFreshEntity(queen);
        for (Player nearby : server.getEntitiesOfClass(Player.class, new AABB(pos).inflate(64.0))) {
            nearby.displayClientMessage(Component.translatable("message.panthrixsgalaxy.queen_arrives")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        }
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static void tell(@Nullable Player player, String key) {
        if (player != null) {
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.alien_beacon").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.alien_beacon_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
