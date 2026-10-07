package com.panthrixsgalaxy.advancement;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import com.panthrixsgalaxy.entity.mob.PGAliens;
import com.panthrixsgalaxy.entity.mob.PGCrawlerEntity;
import com.panthrixsgalaxy.entity.mob.PGMartianWormEntity;
import com.panthrixsgalaxy.entity.mob.PGScorpionEntity;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.weapon.PGLaserItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/**
 * Logros que Minecraft no sabe comprobar solo (Fase 22):
 *
 *   "Astronauta"          estar 5 minutos (en total) en la Luna
 *   "Primer contacto"     ver de cerca una criatura marciana
 *   "Espectro de colores" tener 3 pistolas láser de colores distintos
 *   "Cazador marciano"    matar 10 criaturas marcianas
 *   "Depredador espacial" matar 50 criaturas de otros mundos
 *   "Más allá de la Tierra" visitar 3 cuerpos celestes
 *
 * Los contadores se guardan en el jugador, en la parte que NO se pierde al morir ("PlayerPersisted").
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGAdvancementEvents {

    private static final int MOON_TICKS_NEEDED = 5 * 60 * 20;
    private static final String MOON_TICKS = "pg_moon_ticks";
    private static final String MARTIAN_KILLS = "pg_martian_kills";
    private static final String SPACE_KILLS = "pg_space_kills";
    private static final String VISITED = "pg_visited_bodies";

    /** Datos que se conservan al morir. */
    private static CompoundTag persisted(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0) {
            return;
        }
        // Astronauta: tiempo en la Luna
        if (player.level().dimension().equals(PGPlanets.MOON_LEVEL) && player.isAlive()) {
            CompoundTag data = persisted(player);
            int ticks = data.getInt(MOON_TICKS) + 40;
            data.putInt(MOON_TICKS, ticks);
            if (ticks >= MOON_TICKS_NEEDED) {
                PGAdvancements.award(player, "astronaut");
            }
        }
        // Primer contacto: una criatura marciana a la vista
        if (player.level().dimension().equals(PGPlanets.MARS_LEVEL)) {
            for (LivingEntity creature : player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(16.0), PGAdvancementEvents::isMartianCreature)) {
                if (player.hasLineOfSight(creature)) {
                    PGAdvancements.award(player, "mars_first_contact");
                    break;
                }
            }
        }
        // Espectro de colores: 3 armas láser distintas en el inventario
        Set<Object> tiers = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PGLaserItem laser) {
                tiers.add(laser.getTier());
            }
        }
        if (tiers.size() >= 3) {
            PGAdvancements.award(player, "color_spectrum");
        }
    }

    private static boolean isMartianCreature(Entity entity) {
        return (entity instanceof PGCrawlerEntity crawler && crawler.isMartian())
                || (entity instanceof PGScorpionEntity scorpion && scorpion.isMartian())
                || (entity instanceof PGMartianWormEntity worm && worm.isEmerged());
    }

    /** ¿Es una criatura de otro mundo (cualquiera del mod)? */
    private static boolean isSpaceCreature(Entity entity) {
        return entity instanceof PGCrawlerEntity || entity instanceof PGScorpionEntity
                || entity instanceof PGMartianWormEntity || PGAliens.isAlien(entity) || entity instanceof PGAlienQueenEntity;
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Entity victim = event.getEntity();
        CompoundTag data = persisted(player);
        if (isMartianCreature(victim) || (victim instanceof PGMartianWormEntity)) {
            int kills = data.getInt(MARTIAN_KILLS) + 1;
            data.putInt(MARTIAN_KILLS, kills);
            if (kills >= 10) {
                PGAdvancements.award(player, "martian_hunter");
            }
        }
        if (isSpaceCreature(victim)) {
            int kills = data.getInt(SPACE_KILLS) + 1;
            data.putInt(SPACE_KILLS, kills);
            if (kills >= 50) {
                PGAdvancements.award(player, "space_predator");
            }
        }
    }

    /** Más allá de la Tierra: 3 cuerpos celestes distintos (cualquier planeta, luna o el cinturón). */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PGPlanet planet = PGPlanets.fromDimension(event.getTo());
        if (planet == null || planet == PGPlanets.EARTH) {
            return;
        }
        CompoundTag data = persisted(player);
        ListTag visited = data.getList(VISITED, Tag.TAG_STRING);
        StringTag id = StringTag.valueOf(planet.id());
        if (!visited.contains(id)) {
            visited.add(id);
            data.put(VISITED, visited);
        }
        if (visited.size() >= 3) {
            PGAdvancements.award(player, "beyond_earth");
        }
    }

    private PGAdvancementEvents() {
    }
}
