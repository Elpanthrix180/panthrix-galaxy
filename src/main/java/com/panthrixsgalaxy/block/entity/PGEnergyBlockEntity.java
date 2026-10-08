package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.energy.EnergyHelper;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyProvider;
import com.panthrixsgalaxy.system.energy.PGEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Base de todas las máquinas con energía (generador, panel solar, reactor, celda...).
 *
 * Una "block entity" es un bloque con memoria: guarda datos (la energía) y puede
 * hacer cosas cada tick (20 veces por segundo).
 *
 * Lo común a todas:
 *   - Un almacén de energía que se guarda al salir del mundo.
 *   - La energía se ofrece a otros mods y cables (PGEnergyProvider: Forge y Fabric la conectan).
 *   - Clic derecho: cargar el objeto de la mano, o con la mano vacía ver información
 *     y cargar la mochila equipada. Mayús + clic derecho: descargar el objeto en la máquina.
 */
public abstract class PGEnergyBlockEntity extends BlockEntity implements PGEnergyProvider {

    protected final PGEnergyStorage energy;

    protected PGEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  int capacity, int maxReceive, int maxExtract) {
        super(type, pos, state);
        this.energy = new PGEnergyStorage(capacity, maxReceive, maxExtract, this::setChanged);
    }

    /** Lo que hace la máquina cada tick (solo en el servidor). */
    public abstract void serverTick();

    /** Texto de estado para el mensaje de información ("Produciendo 15 FE/t", etc.). */
    protected abstract Component getStatus();

    /** Si la máquina acepta el objeto de la mano como combustible. Devuelve true si lo ha gestionado. */
    protected boolean tryInsertFuel(Player player, ItemStack held) {
        return false;
    }

    /** Suelta al suelo lo que tenga dentro cuando se rompe el bloque. */
    public void dropContents() {
    }

    public PGEnergyStorage getEnergy() {
        return energy;
    }

    // ===== Clic derecho =====

    public InteractionResult use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (tryInsertFuel(player, held)) {
            return InteractionResult.CONSUME;
        }

        if (!held.isEmpty() && EnergyHelper.holdsEnergy(held)) {
            int moved;
            if (player.isShiftKeyDown()) {
                moved = energy.canReceive() ? EnergyHelper.dischargeItem(energy, held) : 0;
                message(player, Component.translatable("message.panthrixsgalaxy.energy_discharged", moved));
            } else {
                moved = EnergyHelper.chargeItem(energy, held);
                message(player, Component.translatable("message.panthrixsgalaxy.energy_charged", moved));
            }
            if (moved > 0) {
                playSound(1.4f);
            }
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            // Mano vacía: cargar la mochila equipada y mostrar información
            int moved = EnergyHelper.chargeItem(energy, PGBackpackSlot.getEquipped(player));
            if (moved > 0) {
                playSound(1.2f);
                message(player, Component.translatable("message.panthrixsgalaxy.backpack_energy_charged", moved));
            }
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.energy_info",
                            getBlockState().getBlock().getName(), energy.getEnergyStored(), energy.getMaxEnergyStored())
                    .withStyle(ChatFormatting.YELLOW), false);
            player.displayClientMessage(getStatus().copy().withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void message(Player player, Component text) {
        player.displayClientMessage(text.copy().withStyle(ChatFormatting.YELLOW), true);
    }

    private void playSound(float pitch) {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.4f, pitch);
        }
    }

    // ===== Enviar energía a las máquinas de al lado =====

    /** Ofrece energía a todos los bloques vecinos que puedan recibirla (como máximo maxPerTick a cada uno). */
    protected void pushEnergyToNeighbors(int maxPerTick) {
        if (level == null || energy.getEnergyStored() <= 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            PGEnergyHandler target = EnergyHelper.findBlockEnergy(level, worldPosition.relative(direction), direction.getOpposite());
            if (target != null && target.canReceive() && energy.getEnergyStored() > 0) {
                int offered = Math.min(maxPerTick, energy.getEnergyStored());
                int accepted = target.receiveEnergy(offered, false);
                energy.take(accepted);
            }
        }
    }

    // ===== Energía para otros bloques y guardado =====

    /** La misma energía por todos los lados. */
    @Override
    public @Nullable PGEnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergy(tag.getInt("Energy"));
    }
}
