package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyItem;
import com.panthrixsgalaxy.system.energy.PGEnergyProvider;
import com.panthrixsgalaxy.system.item.PGItemSlots;
import com.panthrixsgalaxy.system.item.PGItemSlotsProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Solo Forge: ofrece la energía y los huecos del mod como "capabilities" de Forge,
 * para que cables, tuberías y máquinas de OTROS mods puedan usarlos.
 *
 * Las máquinas y objetos del mod no saben nada de Forge: solo dicen que tienen energía
 * (PGEnergyProvider, PGEnergyItem) o huecos (PGItemSlotsProvider). Aquí se "enchufan".
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGForgeCapabilities {

    private static final ResourceLocation ID = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "machine");

    @SubscribeEvent
    public static void attachToBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        if (blockEntity instanceof PGEnergyProvider || blockEntity instanceof PGItemSlotsProvider) {
            BlockEntityProvider provider = new BlockEntityProvider(blockEntity);
            event.addCapability(ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void attachToItem(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof PGEnergyItem item) {
            LazyOptional<IEnergyStorage> energy =
                    LazyOptional.of(() -> new ForgeEnergyAdapter.ToForge(item.createEnergyHandler(stack)));
            event.addCapability(ID, new ICapabilityProvider() {
                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                    return ForgeCapabilities.ENERGY.orEmpty(capability, energy);
                }
            });
        }
    }

    /** Las capabilities de una máquina, una por cada lado (y otra para "sin lado"). */
    private static final class BlockEntityProvider implements ICapabilityProvider {

        private final BlockEntity blockEntity;
        private final Map<Direction, LazyOptional<IEnergyStorage>> energy = new HashMap<>();
        private final Map<Direction, LazyOptional<IItemHandler>> items = new HashMap<>();

        BlockEntityProvider(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == ForgeCapabilities.ENERGY && blockEntity instanceof PGEnergyProvider provider) {
                return energy.computeIfAbsent(side, s -> {
                    PGEnergyHandler handler = provider.getEnergyHandler(s);
                    return handler == null ? LazyOptional.empty() : LazyOptional.of(() -> new ForgeEnergyAdapter.ToForge(handler));
                }).cast();
            }
            if (capability == ForgeCapabilities.ITEM_HANDLER && blockEntity instanceof PGItemSlotsProvider provider) {
                return items.computeIfAbsent(side, s -> {
                    PGItemSlots slots = provider.getItemSlots(s);
                    return slots == null ? LazyOptional.empty() : LazyOptional.of(() -> new ItemSlotsHandler(slots));
                }).cast();
            }
            return LazyOptional.empty();
        }

        void invalidate() {
            energy.values().forEach(LazyOptional::invalidate);
            items.values().forEach(LazyOptional::invalidate);
        }
    }

    /** Los huecos del mod vistos como un IItemHandler de Forge (para tolvas y tuberías). */
    private record ItemSlotsHandler(PGItemSlots slots) implements IItemHandler {

        @Override
        public int getSlots() {
            return slots.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slots.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slots.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slots.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slots.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slots.isItemValid(slot, stack);
        }
    }

    private PGForgeCapabilities() {
    }
}
