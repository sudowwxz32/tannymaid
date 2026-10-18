package com.tanny.tannymaid.util;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * /tanny seguro: lleva al Master (y a Tanny) al lugar seguro más cercano:
 * la superficie (si está bajo tierra), su cama, la casa de Tanny o el punto de aparición.
 */
public final class SafePlace {

    private record Candidate(ServerLevel level, Vec3 pos, String name, int priority) {}

    private SafePlace() {}

    public static boolean bringToSafety(TannyEntity tanny, ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        List<Candidate> list = new ArrayList<>();
        ServerLevel here = player.serverLevel();

        // 1) Bajo tierra: la superficie justo arriba
        if (here.dimensionType().hasSkyLight() && !here.canSeeSky(player.blockPosition().above())) {
            BlockPos top = here.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.blockPosition());
            if (here.getFluidState(top.below()).isEmpty() && here.getFluidState(top).isEmpty()) {
                list.add(new Candidate(here, Vec3.atBottomCenterOf(top), "la superficie", 1));
            }
        }

        // 2) La cama / ancla del jugador
        BlockPos bed = player.getRespawnPosition();
        if (bed != null) {
            ServerLevel bedLevel = server.getLevel(player.getRespawnDimension());
            if (bedLevel != null) {
                Optional<Vec3> stand = Player.findRespawnPositionAndUseSpawnBlock(bedLevel, bed,
                        player.getRespawnAngle(), player.isRespawnForced(), true);
                stand.ifPresent(v -> list.add(new Candidate(bedLevel, v, "tu cama", 2)));
            }
        }

        // 3) La casa de Tanny
        if (tanny.getHomePos() != null && tanny.getHomeDim() != null) {
            ServerLevel homeLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tanny.getHomeDim())));
            if (homeLevel != null) {
                Vec3 v = safeSpot(homeLevel, tanny.getHomePos());
                if (v != null) list.add(new Candidate(homeLevel, v, "nuestra casa", 3));
            }
        }

        // 4) El punto de aparición del mundo
        ServerLevel overworld = server.overworld();
        Vec3 spawn = safeSpot(overworld, overworld.getSharedSpawnPos());
        if (spawn != null) list.add(new Candidate(overworld, spawn, "el punto de aparición del mundo", 4));

        if (list.isEmpty()) {
            TannySpeech.say(tanny, player, "Perdón, Amo... no encuentro ningún lugar seguro a dónde llevarte.");
            return false;
        }

        // El más cercano en esta dimensión; si no hay, el de mayor prioridad
        Candidate best = null;
        double bestDist = Double.MAX_VALUE;
        for (Candidate c : list) {
            if (c.level() != here) continue;
            double d = c.pos().distanceToSqr(player.position());
            if (d < 4.0) continue; // ya está ahí
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        if (best == null) {
            for (Candidate c : list) {
                if (c.level() == here && c.pos().distanceToSqr(player.position()) < 4.0) continue;
                if (best == null || c.priority() < best.priority()) best = c;
            }
        }
        if (best == null) {
            TannySpeech.say(tanny, player, "¡Ya estás en un lugar seguro, Amo! Jejeje ♡");
            return false;
        }

        TannySpeech.say(tanny, player, "¡Agárrate de mi mano, Amo! Te llevo a " + best.name() + " ♡");
        poof(here, player.position());
        player.teleportTo(best.level(), best.pos().x, best.pos().y, best.pos().z, player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        poof(best.level(), best.pos());
        best.level().playSound(null, BlockPos.containing(best.pos()), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.3F);

        TannyEntity current = TannyLookup.find(player);
        if (current != null && (current.level() != player.level() || current.distanceToSqr(player) > 16)) {
            current.teleportToOwner(player);
        }
        return true;
    }

    private static void poof(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, pos.x, pos.y + 1, pos.z, 25, 0.5, 0.8, 0.5, 0.05);
    }

    /** Busca un punto con suelo firme y 2 bloques de aire cerca de {@code pos}. */
    @Nullable
    static Vec3 safeSpot(Level level, BlockPos pos) {
        for (int dy = 0; dy <= 10; dy++) {
            for (int sign : new int[] {1, -1}) {
                BlockPos p = pos.above(dy * sign);
                if (isSafe(level, p)) return Vec3.atBottomCenterOf(p);
            }
        }
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        return isSafe(level, top) ? Vec3.atBottomCenterOf(top) : null;
    }

    private static boolean isSafe(Level level, BlockPos p) {
        return level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()
                && level.getFluidState(p).isEmpty()
                && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)
                && level.getFluidState(p.below()).isEmpty();
    }
}
