package com.transporteiberico.item;

import com.transporteiberico.entity.BusEntity;
import com.transporteiberico.registry.TIRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Objeto "Autobús Urbano Azul": clic derecho sobre el suelo para que aparezca el autobús. */
public class BusItem extends Item {

    public BusItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        BusEntity bus = TIRegistry.BUS.get().create(server);
        if (bus == null) {
            return InteractionResult.FAIL;
        }
        bus.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.getRotation(), 0.0F);
        Player player = ctx.getPlayer();
        if (!server.noCollision(bus, bus.getBoundingBox())) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.transporteiberico.autobus_urbano_azul.no_space")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        server.addFreshEntity(bus);
        server.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        if (player == null || !player.getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.transporteiberico.autobus_urbano_azul.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.transporteiberico.autobus_urbano_azul.controls").withStyle(ChatFormatting.DARK_AQUA));
    }
}
