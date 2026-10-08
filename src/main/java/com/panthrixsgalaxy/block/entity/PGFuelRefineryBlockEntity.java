package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.block.PGEnergyBlock;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.item.PGItemSlots;
import com.panthrixsgalaxy.system.item.PGItemSlotsProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Refinería de combustible: convierte materiales en combustible de cohete usando energía.
 *
 *   Carbón / carbón vegetal = 100 mB     Polvo de blaze = 250 mB     Vara de blaze = 500 mB
 *   Bloque de carbón        = 900 mB     Helio-3        = 500 mB
 *   Mineral marciano        = 150 mB  (metano atrapado en la roca: para repostar en Marte)
 *
 * Gasta 40 FE por tick mientras refina (2 mB por tick = 40 mB por segundo).
 * Se le echan materiales con clic derecho o con tolva.
 * Clic derecho con un bidón: lo llena. Con la mano vacía: llena el depósito de combustible de la mochila.
 */
public class PGFuelRefineryBlockEntity extends PGEnergyBlockEntity implements PGItemSlotsProvider {

    public static final int CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int FUEL_CAPACITY = 10_000;
    public static final int ENERGY_PER_TICK = 40;
    public static final int FUEL_PER_TICK = 2;

    private final ItemStackHandler input = new PGItemSlots(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return fuelValue(stack) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Combustible ya refinado (mB). */
    private int fuel;
    /** Combustible que falta por sacar del material que se está procesando (mB). */
    private int pending;

    public PGFuelRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUEL_REFINERY.get(), pos, state, CAPACITY, MAX_RECEIVE, 0);
    }

    /** Cuánto combustible da cada material (0 = no sirve). */
    public static int fuelValue(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 100;
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return 900;
        }
        if (stack.is(Items.BLAZE_POWDER)) {
            return 250;
        }
        if (stack.is(Items.BLAZE_ROD) || stack.is(ModItems.PG_HELIUM_3.get())) {
            return 500;
        }
        if (stack.is(ModItems.PG_MARTIAN_MINERAL.get())) {
            return 150;
        }
        return 0;
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }
        boolean working = false;
        if (pending <= 0 && fuel < FUEL_CAPACITY) {
            ItemStack next = input.getStackInSlot(0);
            int value = fuelValue(next);
            if (value > 0) {
                pending = value;
                input.extractItem(0, 1, false);
            }
        }
        if (pending > 0 && fuel < FUEL_CAPACITY && energy.getEnergyStored() >= ENERGY_PER_TICK) {
            int produced = Math.min(FUEL_PER_TICK, Math.min(pending, FUEL_CAPACITY - fuel));
            energy.take(ENERGY_PER_TICK);
            pending -= produced;
            fuel += produced;
            working = true;
            setChanged();
        }
        if (getBlockState().getValue(PGEnergyBlock.LIT) != working) {
            level.setBlock(worldPosition, getBlockState().setValue(PGEnergyBlock.LIT, working), 3);
        }
    }

    @Override
    public InteractionResult use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PGFuelCanisterItem canister) {
            int moved = Math.min(fuel, canister.getCapacity() - PGFuelCanisterItem.getFuel(held));
            if (moved > 0) {
                PGFuelCanisterItem.setFuel(held, PGFuelCanisterItem.getFuel(held) + moved);
                fuel -= moved;
                setChanged();
                level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
            message(player, Component.translatable("message.panthrixsgalaxy.canister_filled",
                    PGFuelCanisterItem.getFuel(held), canister.getCapacity()));
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty()) {
            ItemStack backpack = PGBackpackSlot.getEquipped(player);
            if (backpack.getItem() instanceof PGBackpackItem item) {
                int current = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.FUEL);
                int moved = Math.min(fuel, item.getCapacity(PGBackpackItem.Tank.FUEL) - current);
                if (moved > 0) {
                    PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.FUEL, current + moved);
                    fuel -= moved;
                    setChanged();
                    message(player, Component.translatable("message.panthrixsgalaxy.backpack_fuel_filled", moved));
                }
            }
        }
        // Materiales, baterías e información: igual que las demás máquinas
        return super.use(player, hand);
    }

    @Override
    protected boolean tryInsertFuel(Player player, ItemStack held) {
        if (fuelValue(held) <= 0) {
            return false;
        }
        ItemStack rest = input.insertItem(0, held.copy(), false);
        int inserted = held.getCount() - rest.getCount();
        if (inserted > 0 && !player.getAbilities().instabuild) {
            held.shrink(inserted);
        }
        message(player, Component.translatable(inserted > 0
                ? "message.panthrixsgalaxy.refinery_input_added"
                : "message.panthrixsgalaxy.fuel_full", inserted));
        return true;
    }

    private void message(Player player, Component text) {
        player.displayClientMessage(text.copy().withStyle(ChatFormatting.GOLD), true);
    }

    @Override
    protected Component getStatus() {
        ItemStack stock = input.getStackInSlot(0);
        Component material = stock.isEmpty()
                ? Component.translatable("status.panthrixsgalaxy.no_fuel")
                : Component.translatable("status.panthrixsgalaxy.fuel_stock", stock.getCount(), stock.getHoverName());
        return Component.translatable("status.panthrixsgalaxy.refinery", fuel, FUEL_CAPACITY).copy()
                .append(" · ").append(material);
    }

    @Override
    public void dropContents() {
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    input.getStackInSlot(0));
        }
    }

    /** Las tolvas y tuberías pueden llenar este hueco. */
    @Override
    public @Nullable PGItemSlots getItemSlots(@Nullable Direction side) {
        return input;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Input", input.serializeNBT());
        tag.putInt("Fuel", fuel);
        tag.putInt("Pending", pending);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        input.deserializeNBT(tag.getCompound("Input"));
        fuel = tag.getInt("Fuel");
        pending = tag.getInt("Pending");
    }
}
