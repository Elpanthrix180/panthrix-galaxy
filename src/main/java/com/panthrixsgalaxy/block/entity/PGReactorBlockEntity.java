package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.block.PGEnergyBlock;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.system.item.PGItemSlots;
import com.panthrixsgalaxy.system.item.PGItemSlotsProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reactor de fusión de helio-3: mucha energía a partir del helio-3 de la Luna.
 * Cada helio-3 dura 20 segundos produciendo 400 FE/t = 160 000 FE.
 * Se alimenta con clic derecho o con tolva.
 */
public class PGReactorBlockEntity extends PGEnergyBlockEntity implements PGItemSlotsProvider {

    public static final int CAPACITY = 200_000;
    public static final int GENERATION = 400;
    public static final int PUSH = 2_000;
    /** Ticks que dura cada helio-3 (400 = 20 segundos). */
    public static final int TICKS_PER_FUEL = 400;

    private final ItemStackHandler fuel = new PGItemSlots(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ModItems.PG_HELIUM_3.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int fuelTicks;

    public PGReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR.get(), pos, state, CAPACITY, 0, PUSH);
    }

    @Override
    public void serverTick() {
        boolean wasRunning = fuelTicks > 0;
        if (fuelTicks > 0) {
            fuelTicks--;
            energy.generate(GENERATION);
        }
        if (fuelTicks <= 0 && energy.getSpace() > 0 && !fuel.getStackInSlot(0).isEmpty()) {
            fuel.extractItem(0, 1, false);
            fuelTicks = TICKS_PER_FUEL;
            setChanged();
        }
        boolean running = fuelTicks > 0;
        if (running != wasRunning && level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(PGEnergyBlock.LIT, running), 3);
        }
        pushEnergyToNeighbors(PUSH);
    }

    @Override
    protected boolean tryInsertFuel(Player player, ItemStack held) {
        if (!held.is(ModItems.PG_HELIUM_3.get())) {
            return false;
        }
        ItemStack rest = fuel.insertItem(0, held.copy(), false);
        int inserted = held.getCount() - rest.getCount();
        if (inserted > 0) {
            if (!player.getAbilities().instabuild) {
                held.shrink(inserted);
            }
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.fuel_added", inserted)
                    .withStyle(ChatFormatting.GOLD), true);
        } else {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.fuel_full")
                    .withStyle(ChatFormatting.RED), true);
        }
        return true;
    }

    @Override
    protected Component getStatus() {
        ItemStack stock = fuel.getStackInSlot(0);
        Component state = fuelTicks > 0
                ? Component.translatable("status.panthrixsgalaxy.producing", GENERATION)
                : Component.translatable("status.panthrixsgalaxy.idle");
        return state.copy().append(" · ").append(stock.isEmpty()
                ? Component.translatable("status.panthrixsgalaxy.no_fuel")
                : Component.translatable("status.panthrixsgalaxy.fuel_stock", stock.getCount(), stock.getHoverName()));
    }

    @Override
    public void dropContents() {
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    fuel.getStackInSlot(0));
        }
    }

    /** Las tolvas y tuberías pueden llenar este hueco. */
    @Override
    public @Nullable PGItemSlots getItemSlots(@Nullable Direction side) {
        return fuel;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Fuel", fuel.serializeNBT());
        tag.putInt("FuelTicks", fuelTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("Fuel"));
        fuelTicks = tag.getInt("FuelTicks");
    }
}
