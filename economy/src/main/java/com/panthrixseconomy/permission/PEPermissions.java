package com.panthrixseconomy.permission;

import com.panthrixseconomy.PanthrixsEconomy;
import com.panthrixseconomy.config.PEConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

/**
 * Permisos de administración, conectados a la API de permisos de Forge.
 *
 * Nodo: panthrixseconomy.admin. Sin mod de permisos, lo tienen los operadores con el nivel
 * de la configuración (2 por defecto). Con LuckPerms u otro, se concede/quita desde allí.
 * TODAS las comprobaciones se hacen en el servidor: el cliente nunca decide.
 */
@Mod.EventBusSubscriber(modid = PanthrixsEconomy.MODID)
public final class PEPermissions {

    public static final PermissionNode<Boolean> ADMIN = new PermissionNode<>(PanthrixsEconomy.MODID, "admin",
            PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.hasPermissions(adminLevel()));

    @SubscribeEvent
    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(ADMIN);
    }

    public static int adminLevel() {
        try {
            return PEConfig.ADMIN_PERMISSION_LEVEL.get();
        } catch (IllegalStateException notLoaded) {
            return 2;
        }
    }

    public static boolean isAdmin(ServerPlayer player) {
        return PermissionAPI.getPermission(player, ADMIN);
    }

    public static boolean isAdmin(Entity entity) {
        return entity instanceof ServerPlayer player && isAdmin(player);
    }

    /** Jugador: nodo de permisos. Consola, bloques de comandos, etc.: nivel de permiso. */
    public static boolean isAdmin(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return isAdmin(player);
        }
        return source.hasPermission(adminLevel());
    }

    private PEPermissions() {
    }
}
