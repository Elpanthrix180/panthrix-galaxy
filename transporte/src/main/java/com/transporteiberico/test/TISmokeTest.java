package com.transporteiberico.test;

import com.mojang.authlib.GameProfile;
import com.transporteiberico.TransporteIberico;
import com.transporteiberico.entity.BusEntity;
import com.transporteiberico.registry.TIRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Prueba automática (la usa GitHub con ./gradlew runServer -Psmoke). En un servidor real recién creado:
 * aparece el autobús, cae al suelo, avanza, gira, frena, no atraviesa paredes, sube un escalón,
 * se guarda y se carga, admite un conductor (y solo uno), al bajar no desaparece ni se duplica,
 * y al romperlo suelta su objeto. Escribe "TRANSPORTE SMOKE TEST OK" o los fallos.
 * Jugando normalmente no hace nada.
 */
@Mod.EventBusSubscriber(modid = TransporteIberico.MODID)
public final class TISmokeTest {

    private static final List<String> failures = new ArrayList<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("transporteiberico.smokeTest")) {
            return;
        }
        MinecraftServer server = event.getServer();
        failures.clear();
        try {
            run(server.overworld());
        } catch (Throwable t) {
            failures.add("excepción: " + t);
            TransporteIberico.LOGGER.error("Smoke test", t);
        }
        if (failures.isEmpty()) {
            TransporteIberico.LOGGER.info("[Transporte Ibérico] TRANSPORTE SMOKE TEST OK");
        } else {
            failures.forEach(f -> TransporteIberico.LOGGER.error("[Transporte Ibérico] FALLO: {}", f));
            TransporteIberico.LOGGER.error("[Transporte Ibérico] TRANSPORTE SMOKE TEST FAILED ({} fallos)", failures.size());
        }
        server.halt(false);
    }

    private static void check(boolean ok, String what) {
        if (ok) {
            TransporteIberico.LOGGER.info("[Transporte Ibérico] OK: {}", what);
        } else {
            failures.add(what);
        }
    }

    private static void run(ServerLevel level) {
        check(TIRegistry.BUS.get() != null && TIRegistry.BUS_ITEM.get() != null, "entidad y objeto registrados");

        // Pista de pruebas: suelo de piedra en el cielo, con una pared y un escalón
        int baseY = 250;
        BlockPos origin = new BlockPos(0, baseY, 0);
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 60; z++) {
                level.setBlock(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 2);
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // 1. Aparece y cae al suelo
        BusEntity bus = TIRegistry.BUS.get().create(level);
        check(bus != null, "se puede crear el autobús");
        if (bus == null) {
            return;
        }
        bus.moveTo(0.5, baseY + 3, 0.5, 0.0F, 0.0F);   // mirando hacia +Z
        level.addFreshEntity(bus);
        for (int i = 0; i < 40; i++) {
            bus.tick();
        }
        check(bus.onGround() && Math.abs(bus.getY() - baseY) < 0.01, "cae y se apoya en el suelo (y=" + bus.getY() + ")");

        // 2. Avanza con W en la dirección a la que mira
        double z0 = bus.getZ();
        for (int i = 0; i < 40; i++) {
            bus.simulate(1.0F, 0.0F);
        }
        check(bus.getZ() - z0 > 5.0 && Math.abs(bus.getX() - 0.5) < 0.1, "avanza recto con W (" + (bus.getZ() - z0) + " bloques)");
        check(bus.getSpeed() <= BusEntity.MAX_SPEED, "no supera la velocidad máxima");

        // 3. Gira con A y D
        float yaw0 = bus.getYRot();
        for (int i = 0; i < 10; i++) {
            bus.simulate(1.0F, 1.0F);
        }
        check(bus.getYRot() < yaw0, "A gira a la izquierda");
        float yaw1 = bus.getYRot();
        for (int i = 0; i < 10; i++) {
            bus.simulate(1.0F, -1.0F);
        }
        check(bus.getYRot() > yaw1, "D gira a la derecha");

        // 4. S frena y luego va marcha atrás
        for (int i = 0; i < 60; i++) {
            bus.simulate(-1.0F, 0.0F);
        }
        check(bus.getSpeed() < 0.0F && bus.getSpeed() >= -BusEntity.MAX_REVERSE, "S frena y da marcha atrás");
        for (int i = 0; i < 200; i++) {
            bus.simulate(0.0F, 0.0F);
        }
        check(bus.getSpeed() == 0.0F, "sin pulsar nada se detiene");

        // 5. No atraviesa una pared de 3 bloques; sí sube un escalón de 1
        bus.moveTo(0.5, baseY, 0.5, 0.0F, 0.0F);
        for (int x = -6; x <= 6; x++) {
            for (int y = 0; y < 3; y++) {
                level.setBlock(origin.offset(x, y, 12), Blocks.STONE.defaultBlockState(), 2);
            }
        }
        for (int i = 0; i < 80; i++) {
            bus.simulate(1.0F, 0.0F);
        }
        check(bus.getBoundingBox().maxZ <= 12.01 && bus.getBoundingBox().maxZ > 9.0, "choca con la pared y no la atraviesa (maxZ=" + bus.getBoundingBox().maxZ + ")");
        for (int x = -6; x <= 6; x++) {
            for (int y = 0; y < 3; y++) {
                level.setBlock(origin.offset(x, y, 12), Blocks.AIR.defaultBlockState(), 2);
            }
            level.setBlock(origin.offset(x, 0, 20), Blocks.STONE.defaultBlockState(), 2);   // escalón
            for (int z = 21; z <= 58; z++) {
                level.setBlock(origin.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            }
        }
        for (int i = 0; i < 80; i++) {
            bus.simulate(1.0F, 0.0F);
        }
        check(bus.getY() >= baseY + 0.99, "sube un escalón de 1 bloque (y=" + bus.getY() + ")");

        // 6. Se guarda y se carga igual
        CompoundTag tag = new CompoundTag();
        bus.save(tag);
        var loaded = EntityType.create(tag, level);
        check(loaded.isPresent() && loaded.get() instanceof BusEntity && loaded.get().position().distanceTo(bus.position()) < 0.01
                && Math.abs(loaded.get().getYRot() - bus.getYRot()) < 0.01, "se guarda y se carga con su posición y orientación");

        // 7. Subir, un solo conductor, bajar sin que desaparezca ni se duplique
        FakePlayer driver = FakePlayerFactory.getMinecraft(level);
        driver.moveTo(bus.getX() + 3, bus.getY(), bus.getZ(), 0, 0);
        boolean rode = driver.startRiding(bus, true);
        check(rode && bus.hasPassenger(driver) && bus.getControllingPassenger() == driver, "el jugador se sube y conduce");
        FakePlayer other = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "pasajero_prueba"));
        other.moveTo(bus.getX() - 3, bus.getY(), bus.getZ(), 0, 0);
        check(!other.startRiding(bus, false) && bus.getPassengers().size() == 1, "solo cabe el conductor (sin pasajeros todavía)");
        driver.stopRiding();
        check(!bus.hasPassenger(driver) && !bus.isRemoved(), "al bajar, el autobús sigue ahí");
        check(driver.distanceTo(bus) > 1.0 && driver.getY() < bus.getBoundingBox().maxY, "el jugador baja al lado, no encima");
        AABB area = new AABB(bus.blockPosition()).inflate(64);
        int buses = level.getEntitiesOfClass(BusEntity.class, area).size();
        check(buses == 1, "no se duplica (" + buses + " autobús/es)");

        // 8. Romperlo suelta el objeto
        int itemsBefore = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).size();
        bus.hurt(level.damageSources().playerAttack(driver), 50.0F);
        int itemsAfter = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).size();
        check(bus.isRemoved() && itemsAfter == itemsBefore + 1, "al romperlo desaparece y suelta el objeto");
    }

    private TISmokeTest() {
    }
}
