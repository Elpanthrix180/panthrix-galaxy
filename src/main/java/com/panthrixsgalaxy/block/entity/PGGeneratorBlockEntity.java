package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.block.PGEnergyBlock;
import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generador: quema combustible de horno (carbón, madera, bloques de carbón...) y produce energía.
 * Un carbón (1600 ticks) = 64 000 FE.
 * Se le echa combustible con clic derecho o con una tolva.
 */
public class PGGeneratorBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 20_000;
    /** Energía producida por tick mientras quema. */
    public static final int GENERATION = 40;
    /** Máximo que envía a cada máquina vecina por tick. */
    public static final int PUSH = 200;

    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isFuel(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> fuelCapability = LazyOptional.of(() -> fuel);

    private int burnTime;

    public PGGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR.get(), pos, state, CAPACITY, 0, PUSH);
    }

    /** Combustible válido: se quema en un horno y no deja restos (los cubos de lava no valen). */
    private static boolean isFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0 && !stack.hasCraftingRemainingItem();
    }

    @Override
    public void serverTick() {
        boolean wasBurning = burnTime > 0;
        if (burnTime > 0) {
            burnTime--;
            energy.generate(GENERATION);
        }
        // Coger el siguiente combustible si no está quemando y hay sitio para más energía
        if (burnTime <= 0 && energy.getSpace() > 0) {
            ItemStack next = fuel.getStackInSlot(0);
            if (isFuel(next)) {
                burnTime = ForgeHooks.getBurnTime(next, RecipeType.SMELTING);
                fuel.extractItem(0, 1, false);
                setChanged();
            }
        }
        boolean burning = burnTime > 0;
        if (burning != wasBurning && level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(PGEnergyBlock.LIT, burning), 3);
        }
        pushEnergyToNeighbors(PUSH);
    }

    @Override
    protected boolean tryInsertFuel(Player player, ItemStack held) {
        if (!isFuel(held)) {
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
        Component fuelText = stock.isEmpty()
                ? Component.translatable("status.panthrixsgalaxy.no_fuel")
                : Component.translatable("status.panthrixsgalaxy.fuel_stock", stock.getCount(), stock.getHoverName());
        Component state = burnTime > 0
                ? Component.translatable("status.panthrixsgalaxy.producing", GENERATION)
                : Component.translatable("status.panthrixsgalaxy.idle");
        return state.copy().append(" · ").append(fuelText);
    }

    @Override
    public void dropContents() {
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    fuel.getStackInSlot(0));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return fuelCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelCapability.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Fuel", fuel.serializeNBT());
        tag.putInt("BurnTime", burnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("Fuel"));
        burnTime = tag.getInt("BurnTime");
    }
}
