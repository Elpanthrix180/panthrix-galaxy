package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.block.PGEnergyBlock;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.oxygen.Electrolysis;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Recargador de oxígeno ELÉCTRICO: agua + energía -> oxígeno (electrólisis).
 * Funciona en cualquier sitio, también en la Luna y en Marte.
 *
 *   - Energía: por cable o de un generador/celda pegados.
 *   - Agua: clic derecho con un cubo de agua, o la bombea sola si tiene agua al lado.
 *   - Clic derecho con una bombona o mochila en la mano: la llena de oxígeno.
 *   - Clic derecho con la mano vacía: llena el oxígeno Y el depósito de agua de la mochila equipada.
 */
public class PGElectricOxygenRechargerBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 50_000;
    public static final int MAX_RECEIVE = 2_000;
    public static final int WATER_CAPACITY = 8_000;
    /** Agua que bombea cada segundo si tiene una fuente de agua al lado. */
    public static final int PUMP_PER_SECOND = 200;

    private int water;

    public PGElectricOxygenRechargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_OXYGEN_RECHARGER.get(), pos, state, CAPACITY, MAX_RECEIVE, 0);
    }

    @Override
    public void serverTick() {
        if (level == null || level.getGameTime() % 20 != 0) {
            return;
        }
        // Bomba: si hay agua al lado (fuente), se llena poco a poco
        if (water < WATER_CAPACITY && hasAdjacentWater()) {
            water = Math.min(WATER_CAPACITY, water + PUMP_PER_SECOND);
            setChanged();
        }
        // Luz encendida = listo para producir oxígeno
        boolean ready = water >= Electrolysis.WATER_PER_OXYGEN && energy.getEnergyStored() >= Electrolysis.ENERGY_PER_OXYGEN;
        if (getBlockState().getValue(PGEnergyBlock.LIT) != ready) {
            level.setBlock(worldPosition, getBlockState().setValue(PGEnergyBlock.LIT, ready), 3);
        }
    }

    private boolean hasAdjacentWater() {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FluidState fluid = level.getFluidState(worldPosition.relative(direction));
            if (fluid.isSource() && fluid.is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Cubo de agua -> depósito de agua de la máquina
        if (held.is(Items.WATER_BUCKET)) {
            if (water + 1000 <= WATER_CAPACITY) {
                water += 1000;
                setChanged();
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                info(player, Component.translatable("message.panthrixsgalaxy.water_added", water, WATER_CAPACITY));
            } else {
                info(player, Component.translatable("message.panthrixsgalaxy.water_full"));
            }
            return InteractionResult.CONSUME;
        }

        // ¿Qué llenamos? Lo de la mano, o la mochila equipada si la mano está vacía
        ItemStack target = held.isEmpty() ? PGBackpackSlot.getEquipped(player) : held;
        if (target.getItem() instanceof PGOxygenTankItem || target.getItem() instanceof PGBackpackItem) {
            int oxygen = fillOxygen(target);
            int waterMoved = target.getItem() instanceof PGBackpackItem ? fillBackpackWater(target) : 0;
            if (oxygen > 0 || waterMoved > 0) {
                level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8f, 1.4f);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, 12, 0.2, 0.1, 0.2, 0.02);
                }
                info(player, Component.translatable("message.panthrixsgalaxy.electrolysis_done", oxygen, waterMoved));
            } else {
                info(player, Component.translatable(water < Electrolysis.WATER_PER_OXYGEN
                        ? "message.panthrixsgalaxy.need_water"
                        : energy.getEnergyStored() < Electrolysis.ENERGY_PER_OXYGEN
                        ? "message.panthrixsgalaxy.need_energy"
                        : "message.panthrixsgalaxy.tank_already_full"));
            }
            return InteractionResult.CONSUME;
        }

        // Lo demás (baterías, información...) igual que cualquier máquina
        return super.use(player, hand);
    }

    /** Produce oxígeno con agua y energía y lo mete en la bombona o mochila. Devuelve las unidades producidas. */
    private int fillOxygen(ItemStack target) {
        int current;
        int capacity;
        if (target.getItem() instanceof PGOxygenTankItem tank) {
            current = PGOxygenTankItem.getOxygen(target);
            capacity = tank.getCapacity();
        } else {
            current = PGBackpackItem.getTank(target, PGBackpackItem.Tank.OXYGEN);
            capacity = ((PGBackpackItem) target.getItem()).getCapacity(PGBackpackItem.Tank.OXYGEN);
        }
        int amount = Math.min(capacity - current,
                Math.min(water / Electrolysis.WATER_PER_OXYGEN, energy.getEnergyStored() / Electrolysis.ENERGY_PER_OXYGEN));
        if (amount <= 0) {
            return 0;
        }
        water -= amount * Electrolysis.WATER_PER_OXYGEN;
        energy.take(amount * Electrolysis.ENERGY_PER_OXYGEN);
        if (target.getItem() instanceof PGOxygenTankItem) {
            PGOxygenTankItem.setOxygen(target, current + amount);
        } else {
            PGBackpackItem.setTank(target, PGBackpackItem.Tank.OXYGEN, current + amount);
        }
        setChanged();
        return amount;
    }

    /** Pasa el agua que sobre al depósito de agua de la mochila (para el oxígeno de emergencia). */
    private int fillBackpackWater(ItemStack backpack) {
        int current = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.WATER);
        int capacity = ((PGBackpackItem) backpack.getItem()).getCapacity(PGBackpackItem.Tank.WATER);
        int moved = Math.min(water, capacity - current);
        if (moved > 0) {
            water -= moved;
            PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.WATER, current + moved);
            setChanged();
        }
        return moved;
    }

    private void info(Player player, Component text) {
        player.displayClientMessage(text.copy().withStyle(ChatFormatting.AQUA), true);
    }

    @Override
    protected Component getStatus() {
        return Component.translatable("status.panthrixsgalaxy.water", water, WATER_CAPACITY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Water", water);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        water = tag.getInt("Water");
    }
}
