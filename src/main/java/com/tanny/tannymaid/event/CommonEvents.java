package com.tanny.tannymaid.event;

import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.cobblemon.LucarioCompanion;
import com.tanny.tannymaid.command.TannyCommand;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import com.tanny.tannymaid.util.TannyChat;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = TannyMaid.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {

    private CommonEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TannyCommand.register(event.getDispatcher());
    }

    /** Conversación: si le escribes "Tanny, ..." en el chat, ella te responde. */
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String raw = event.getRawText();
        if (player == null || raw == null || player.getServer() == null) return;
        player.getServer().execute(() -> TannyChat.onChat(player, raw));
    }

    /** Un Lucario de Tanny que quedó guardado de una sesión anterior se elimina al cargar. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!event.getLevel().isClientSide && LucarioCompanion.isTannyPokemon(entity)
                && !LucarioCompanion.isActive(entity.getUUID())) {
            event.setCanceled(true);
        }
    }

    /** El Lucario de Tanny no se puede capturar: la Poké Ball rebota y se devuelve. */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!LucarioCompanion.isTannyPokemon(hit.getEntity())) return;
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide) return;
        String name = projectile.getClass().getName().toLowerCase(Locale.ROOT);
        if (!name.contains("pokeball")) return;
        if (event.isCancelable()) event.setCanceled(true);
        if (projectile instanceof ThrowableItemProjectile throwable) {
            ItemStack ball = throwable.getItem();
            if (!ball.isEmpty()) projectile.spawnAtLocation(ball.copy());
        }
        projectile.discard();
        if (projectile.getOwner() instanceof Player player) {
            player.displayClientMessage(Component.literal("§dEse Lucario pertenece a Tanny. ¡No se puede capturar!"), true);
        }
    }

    /** Si el Master cambia de dimensión, Tanny (en modo seguir/defensa) va con él. */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            bringAlong(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            bringAlong(player);
        }
    }

    private static void bringAlong(ServerPlayer player) {
        TannyEntity tanny = TannyLookup.find(player);
        if (tanny != null && tanny.getMode().follows() && !tanny.isWeakened()) {
            tanny.teleportToOwner(player);
        }
    }
}
