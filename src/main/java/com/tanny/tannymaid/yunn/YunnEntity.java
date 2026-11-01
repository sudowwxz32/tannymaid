package com.tanny.tannymaid.yunn;

import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.cobblemon.LucarioCompanion;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Yunn, la enfermera Pokémon. No puede morir (se debilita y se recupera), cura y potencia a los
 * Pokémon del Master, le da botiquines de Cobblemon, tiene Pokédex, sabe en qué bioma aparece cada
 * Pokémon, busca la aldea más cercana y pelea con su Cetro Corazón. La acompañan Chansey y Cinderace.
 */
public class YunnEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_WEAK = SynchedEntityData.defineId(YunnEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TALK = SynchedEntityData.defineId(YunnEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STAY = SynchedEntityData.defineId(YunnEntity.class, EntityDataSerializers.BOOLEAN);
    public static final DustParticleOptions PINK = new DustParticleOptions(new Vector3f(1.0F, 0.6F, 0.78F), 1.1F);
    public static final DustParticleOptions MINT = new DustParticleOptions(new Vector3f(0.55F, 1.0F, 0.75F), 1.0F);

    public static final int A_HEAL = 0, A_BOOST = 1, A_KIT = 2, A_DEX = 3, A_WHERE = 4, A_HERE = 5, A_RADAR = 6,
            A_VILLAGE = 7, A_FOLLOW = 8, A_STAY = 9, A_TP = 10, A_CHANSEY = 11, A_CINDERACE = 12, A_HELP = 13;

    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos anchor;
    private int weakTimer;
    private long healReady, boostReady, kitReady, villageReady;
    private int pulseCd;
    final YunnPals pals = new YunnPals(this);

    public YunnEntity(EntityType<? extends YunnEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCustomName(Component.literal("Yunn"));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0.0F);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_SPEED, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WEAK, false);
        entityData.define(DATA_TALK, -1000);
        entityData.define(DATA_STAY, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new WeakGoal());
        goalSelector.addGoal(2, new FightGoal());
        goalSelector.addGoal(3, new FollowGoal());
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new GuardGoal());
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    // ------------------------------------------------------------------ estado

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public boolean isOwner(Entity e) { return e != null && ownerUUID != null && ownerUUID.equals(e.getUUID()); }
    public boolean isWeakened() { return entityData.get(DATA_WEAK); }
    public boolean isStaying() { return entityData.get(DATA_STAY); }

    public boolean isTalking() {
        int now = (int) level().getGameTime();
        int s = entityData.get(DATA_TALK);
        return now - s >= 0 && now - s < 50;
    }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (ownerUUID == null || !(level() instanceof ServerLevel sl)) return null;
        return sl.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    public void init(ServerPlayer owner) {
        ownerUUID = owner.getUUID();
        equipScepter();
    }

    private void equipScepter() {
        if (getMainHandItem().isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.HEART_SCEPTER.get()));
    }

    public void say(@Nullable ServerPlayer p, String text) {
        entityData.set(DATA_TALK, (int) level().getGameTime());
        if (p != null) p.sendSystemMessage(Component.literal("§d[Yunn] §f" + text));
    }

    private static void info(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal(text));
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        ServerPlayer owner = getOwnerPlayer();
        if (isWeakened()) {
            setTarget(null);
            getNavigation().stop();
            if (tickCount % 20 == 0) {
                heal(getMaxHealth() / 60F);
                ((ServerLevel) level()).sendParticles(PINK, getX(), getY() + 1, getZ(), 3, 0.3, 0.3, 0.3, 0);
            }
            if (--weakTimer <= 0) recover(owner);
        } else if (getHealth() < getMaxHealth() && tickCount % (getTarget() == null ? 20 : 40) == 0) {
            heal(2.0F);
        }
        if (getY() < level().getMinBuildHeight() - 4 && owner != null && owner.level() == level()) teleportTo(owner);
        if (owner != null && tickCount % 100 == 0) {
            UUID stored = YunnLookup.getStoredId(owner);
            if (stored != null && !stored.equals(getUUID())) {      // contrato nuevo: esta copia se retira
                pals.recallAll();
                discard();
                return;
            }
            if (stored == null) YunnLookup.setStoredId(owner, getUUID());
        }
        // Pulso Curativo: cura al Master cuando está herido
        if (pulseCd > 0) pulseCd--;
        if (owner != null && !isWeakened() && pulseCd == 0 && owner.level() == level() && distanceToSqr(owner) < 10 * 10
                && owner.getHealth() < owner.getMaxHealth() * 0.7F) {
            pulseCd = 160;
            owner.heal(4.0F);
            swing(InteractionHand.MAIN_HAND);
            ServerLevel sl = (ServerLevel) level();
            sl.sendParticles(MINT, owner.getX(), owner.getY() + 1, owner.getZ(), 18, 0.4, 0.6, 0.4, 0);
            sl.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + 2, owner.getZ(), 3, 0.3, 0.2, 0.3, 0);
            sl.playSound(null, owner.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 0.8F, 1.6F);
            owner.displayClientMessage(Component.literal("§d✚ Yunn te curó con su Cetro Corazón ♡"), true);
        }
        // si el Master se aleja mucho (y no está esperando), va con él
        if (owner != null && !isStaying() && !isWeakened() && (owner.level() != level() || distanceToSqr(owner) > 28 * 28)) {
            teleportTo(owner);
        }
        pals.tick(owner);
    }

    public void teleportTo(ServerPlayer owner) {
        ServerLevel sl = (ServerLevel) owner.level();
        sl.sendParticles(PINK, getX(), getY() + 1, getZ(), 15, 0.4, 0.6, 0.4, 0);
        Vec3 spot = com.tanny.tannymaid.cobblemon.GardevoirCompanion.safeSpot(sl, this, owner);
        if (owner.level() != level()) {
            teleportTo(sl, spot.x, spot.y, spot.z, EnumSet.noneOf(RelativeMovement.class), getYRot(), getXRot());
        } else {
            moveTo(spot.x, spot.y, spot.z, getYRot(), getXRot());
            getNavigation().stop();
        }
        fallDistance = 0;
        sl.sendParticles(PINK, spot.x, spot.y + 1, spot.z, 15, 0.4, 0.6, 0.4, 0);
    }

    // ------------------------------------------------------------------ vida

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isWeakened()) return false;
        Entity a = source.getEntity();
        if (a != null && (isOwner(a) || LucarioCompanion.isTannyPokemon(a) || a instanceof TannyEntity)) return false;
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN)) {
            ServerPlayer o = getOwnerPlayer();
            if (o != null) teleportTo(o);
            return false;
        }
        if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE)) amount *= 0.8F;
        if (amount >= getHealth()) {
            becomeWeak();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && !isRemoved()) {
            dead = false;
            becomeWeak();
            return;
        }
        super.die(source);
    }

    private void becomeWeak() {
        setHealth(1.0F);
        entityData.set(DATA_WEAK, true);
        weakTimer = 1200;
        setTarget(null);
        clearFire();
        say(getOwnerPlayer(), "Ay... necesito descansar un momentito. Chansey, cuídame, ¿sí?");
    }

    private void recover(@Nullable ServerPlayer owner) {
        entityData.set(DATA_WEAK, false);
        setHealth(getMaxHealth());
        say(owner, "¡Ya estoy bien! Gracias por esperarme ♡");
    }

    @Override
    public boolean canAttack(LivingEntity t) {
        if (t == this || isOwner(t) || t instanceof Player || t instanceof TannyEntity || t instanceof YunnEntity
                || LucarioCompanion.isTannyPokemon(t)) return false;
        return super.canAttack(t);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && level() instanceof ServerLevel sl) {
            sl.sendParticles(PINK, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0);
            sl.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 1.0F, 1.4F);
            ServerPlayer o = getOwnerPlayer();
            if (o != null && o.distanceToSqr(this) < 12 * 12) o.heal(1.0F);     // cada golpe del cetro cura un poco al Master
        }
        return hit;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public void onRemovedFromWorld() {
        pals.recallAll();
        super.onRemovedFromWorld();
    }

    // ------------------------------------------------------------------ interacción

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!isOwner(player)) {
            if (!level().isClientSide) player.sendSystemMessage(Component.literal("§d[Yunn] §fHola~ Solo atiendo a mi entrenador, ¡lo siento! ♡"));
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            if (!level().isClientSide) doAction((ServerPlayer) player, isStaying() ? A_FOLLOW : A_STAY, "");
            return InteractionResult.SUCCESS;
        }
        if (level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.tanny.tannymaid.client.YunnScreen.open(getId()));
        }
        return InteractionResult.SUCCESS;
    }

    /** Todas las órdenes (menú, comandos y atajos). */
    public void doAction(ServerPlayer p, int action, String text) {
        if (!isOwner(p)) return;
        if (isWeakened() && action <= A_VILLAGE) {
            say(p, "Perdón... primero necesito recuperarme un poquito (" + weakTimer / 20 + " s).");
            return;
        }
        long now = level().getGameTime();
        switch (action) {
            case A_HEAL -> {
                if (now < healReady) { say(p, "Estoy preparando más medicina, ¡" + (healReady - now) / 20 + " s y listo!"); return; }
                int n = CobblemonBridge.healParty(p);
                if (n < 0) { say(p, "Mmm... no pude revisar a tus Pokémon. ¿Está instalado Cobblemon?"); return; }
                healReady = now + 600;
                swing(InteractionHand.MAIN_HAND);
                ServerLevel sl = (ServerLevel) level();
                sl.sendParticles(MINT, p.getX(), p.getY() + 1, p.getZ(), 30, 0.6, 0.8, 0.6, 0);
                sl.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.8F);
                say(p, n == 0 ? "No llevas Pokémon contigo, ¡pero aquí estaré cuando los necesites!"
                        : "¡Listo! Tus " + n + " Pokémon están sanos y llenos de energía ♡ (vida, estados y PP)");
            }
            case A_BOOST -> {
                if (now < boostReady) { say(p, "Tus Pokémon necesitan descansar antes del próximo entrenamiento (" + (boostReady - now) / 20 + " s)."); return; }
                List<String> res = CobblemonBridge.boostParty(p, getRandom());
                if (res.isEmpty()) { say(p, "No tienes Pokémon en tu equipo para entrenar."); return; }
                boostReady = now + 12000;
                say(p, "¡Masaje vitamínico y entrenamiento especial de Yunn! ✦");
                for (String r : res) info(p, "§7  • §f" + r);
            }
            case A_KIT -> giveKit(p, now);
            case A_DEX -> {
                if (text.isBlank()) { say(p, "Dime el nombre de un Pokémon y te leo su Pokédex ♡"); return; }
                List<String> dex = CobblemonBridge.pokedex(text.trim());
                if (dex == null) { say(p, "Mmm... no encuentro a \"" + text + "\" en mi Pokédex. ¿Lo escribiste en inglés, como pikachu?"); return; }
                say(p, "Pokédex de Yunn ✦");
                for (String l : dex) info(p, "  " + l);
            }
            case A_WHERE -> {
                if (text.isBlank()) { say(p, "¿Qué Pokémon buscas? Te digo dónde aparece ♡"); return; }
                List<String> w = CobblemonBridge.whereSpawns(text.trim());
                if (w == null) { say(p, "No puedo leer el mapa de apariciones ahora mismo..."); return; }
                if (w.isEmpty()) { say(p, "No encontré dónde aparece \"" + text + "\" en estado salvaje. Quizá se obtiene evolucionando ♡"); return; }
                say(p, "Mapa Pokémon: " + text + " aparece en…");
                int k = 0;
                for (String l : w) { if (k++ >= 8) break; info(p, "§7  • " + l); }
            }
            case A_HERE -> {
                List<String> h = CobblemonBridge.spawnsHere(p);
                if (h == null) { say(p, "No puedo leer el mapa de apariciones ahora mismo..."); return; }
                String biome = p.level().getBiome(p.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("?");
                say(p, "En este bioma (" + biome + ") pueden aparecer " + h.size() + " especies:");
                info(p, "§f  " + (h.isEmpty() ? "ninguna por aquí..." : String.join(", ", h.subList(0, Math.min(60, h.size())))
                        + (h.size() > 60 ? "…" : "")));
            }
            case A_RADAR -> radar(p);
            case A_VILLAGE -> village(p, now);
            case A_FOLLOW -> {
                entityData.set(DATA_STAY, false);
                anchor = null;
                say(p, "¡Voy contigo! Si alguien se lastima, yo me encargo ♡");
            }
            case A_STAY -> {
                entityData.set(DATA_STAY, true);
                anchor = blockPosition();
                getNavigation().stop();
                say(p, "Te espero aquí. ¡Vuelve sano y salvo!");
            }
            case A_TP -> {
                entityData.set(DATA_STAY, false);
                teleportTo(p);
                say(p, "¡Aquí estoy! ¿Alguien necesita una enfermera?");
            }
            case A_CHANSEY -> say(p, pals.toggleChansey() ? "¡Chansey, sal a ayudar!" : "Chansey, descansa en tu Poké Ball ♡");
            case A_CINDERACE -> say(p, pals.toggleCinderace() ? "¡Cinderace, protege a mi entrenador!" : "Buen trabajo, Cinderace. ¡Regresa!");
            default -> help(p);
        }
    }

    private static final Object[][] KIT = {
            {"potion", 3}, {"super_potion", 2}, {"hyper_potion", 1}, {"full_heal", 1}, {"revive", 1}, {"ether", 1},
            {"rare_candy", 1}, {"exp_candy_s", 2}
    };
    private static final String[] VITAMINS = {"hp_up", "protein", "iron", "calcium", "zinc", "carbos"};

    private void giveKit(ServerPlayer p, long now) {
        if (!CobblemonBridge.isLoaded()) { say(p, "Sin Cobblemon no tengo medicinas Pokémon que darte..."); return; }
        if (now < kitReady) { say(p, "El próximo botiquín estará listo en " + (kitReady - now) / 20 + " s ♡"); return; }
        List<String> given = new ArrayList<>();
        for (Object[] k : KIT) {
            ItemStack s = CobblemonBridge.item((String) k[0], (Integer) k[1]);
            if (s == null) continue;
            given.add(s.getCount() + "× " + s.getHoverName().getString());
            if (!p.getInventory().add(s)) p.drop(s, false);
        }
        ItemStack vit = CobblemonBridge.item(VITAMINS[getRandom().nextInt(VITAMINS.length)], 1);
        if (vit != null) {
            given.add("1× " + vit.getHoverName().getString());
            if (!p.getInventory().add(vit)) p.drop(vit, false);
        }
        if (given.isEmpty()) { say(p, "No encuentro medicinas en esta versión de Cobblemon..."); return; }
        kitReady = now + 12000;
        swing(InteractionHand.MAIN_HAND);
        say(p, "¡Tu botiquín Pokémon! ✚ " + String.join(", ", given) + ". ¡Úsalo con cariño!");
    }

    private void radar(ServerPlayer p) {
        List<Mob> found = level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(64, 32, 64),
                m -> m.isAlive() && CobblemonBridge.isPokemonEntity(m) && !LucarioCompanion.isTannyPokemon(m) && CobblemonBridge.isWild(m));
        found.sort((a, b) -> Double.compare(a.distanceToSqr(p), b.distanceToSqr(p)));
        if (found.isEmpty()) { say(p, "Mi radar no detecta Pokémon salvajes cerca (64 bloques)."); return; }
        say(p, "Radar Pokémon: " + found.size() + " Pokémon salvajes cerca ✦");
        for (int i = 0; i < Math.min(8, found.size()); i++) {
            Mob m = found.get(i);
            info(p, "§7  • §f" + m.getName().getString() + " §7a §e" + (int) Math.sqrt(m.distanceToSqr(p)) + " m §7hacia el §e" + dir(p, m.getX(), m.getZ()));
        }
    }

    private void village(ServerPlayer p, long now) {
        if (now < villageReady) { say(p, "Estoy revisando el mapa... pregúntame en " + (villageReady - now) / 20 + " s."); return; }
        villageReady = now + 400;
        BlockPos v = ((ServerLevel) p.level()).findNearestMapStructure(StructureTags.VILLAGE, p.blockPosition(), 64, false);
        if (v == null) { say(p, "No encuentro ninguna aldea cerca en esta dimensión..."); return; }
        int dist = (int) Math.sqrt(p.blockPosition().distSqr(new BlockPos(v.getX(), p.getBlockY(), v.getZ())));
        say(p, "La aldea más cercana está a §e" + dist + " bloques§f hacia el §e" + dir(p, v.getX(), v.getZ()) + "§f (X " + v.getX()
                + ", Z " + v.getZ() + "). Si tiene Centro Pokémon, ahí está la máquina curativa ♡");
    }

    private static String dir(Player p, double x, double z) {
        double ang = Math.toDegrees(Math.atan2(-(x - p.getX()), z - p.getZ()));
        String[] d = {"sur", "suroeste", "oeste", "noroeste", "norte", "noreste", "este", "sureste"};
        return d[(int) Math.floorMod(Math.round(ang / 45.0), 8L)];
    }

    private void help(ServerPlayer p) {
        info(p, "§d✚ Yunn, enfermera Pokémon — órdenes:");
        info(p, "§f/yunn curar§7 – cura a todo tu equipo (vida, estados, PP)");
        info(p, "§f/yunn potenciar§7 – amistad, EVs y un IV al máximo para tu equipo");
        info(p, "§f/yunn botiquin§7 – pociones, revivir, caramelos y vitaminas");
        info(p, "§f/yunn pokedex <pokémon>§7 – ficha de Pokédex");
        info(p, "§f/yunn donde <pokémon>§7 – en qué bioma aparece");
        info(p, "§f/yunn aqui§7 – qué Pokémon aparecen en tu bioma · §f/yunn radar§7 – Pokémon salvajes cerca");
        info(p, "§f/yunn aldea§7 – la aldea más cercana (Centro Pokémon)");
        info(p, "§f/yunn seguir|quedarse|tp§7 · §f/yunn chansey|cinderace§7 – sacar o guardar a sus Pokémon");
        info(p, "Clic derecho: su menú · Shift + clic derecho: seguir / quedarse");
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putBoolean("Weak", isWeakened());
        tag.putInt("WeakTimer", weakTimer);
        tag.putBoolean("Stay", isStaying());
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
        pals.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entityData.set(DATA_WEAK, tag.getBoolean("Weak"));
        weakTimer = tag.getInt("WeakTimer");
        entityData.set(DATA_STAY, tag.getBoolean("Stay"));
        anchor = tag.contains("Anchor") ? BlockPos.of(tag.getLong("Anchor")) : null;
        pals.load(tag);
        setCustomName(Component.literal("Yunn"));
        equipScepter();
    }

    // ------------------------------------------------------------------ IA

    private class WeakGoal extends Goal {
        WeakGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)); }
        @Override public boolean canUse() { return isWeakened(); }
    }

    private class FollowGoal extends Goal {
        private int repath;
        FollowGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (isWeakened() || getTarget() != null) return false;
            if (isStaying()) return anchor != null && anchor.distToCenterSqr(position()) > 9;
            ServerPlayer o = getOwnerPlayer();
            return o != null && o.level() == level() && distanceToSqr(o) > 3.5 * 3.5;
        }

        @Override
        public void tick() {
            if (--repath > 0) return;
            repath = 8;
            if (isStaying() && anchor != null) {
                getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 1.1D);
                return;
            }
            ServerPlayer o = getOwnerPlayer();
            if (o != null) getNavigation().moveTo(o, distanceToSqr(o) > 10 * 10 ? 1.4D : 1.15D);
        }
    }

    private class FightGoal extends Goal {
        private int cooldown, repath;
        FightGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            LivingEntity t = getTarget();
            return t != null && t.isAlive() && !isWeakened();
        }

        @Override
        public void start() { setAggressive(true); }

        @Override
        public void stop() { setAggressive(false); getNavigation().stop(); }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            if (cooldown > 0) cooldown--;
            getLookControl().setLookAt(t, 30, 30);
            if (--repath <= 0) {
                repath = 6;
                getNavigation().moveTo(t, 1.3D);
            }
            double reach = getBbWidth() * 2.0 * getBbWidth() * 2.0 + t.getBbWidth() + 1.8;
            if (distanceToSqr(t) <= reach && cooldown <= 0) {
                swing(InteractionHand.MAIN_HAND);
                doHurtTarget(t);
                cooldown = 14;
            }
        }
    }

    /** Defiende al Master: a quien lo ataca, a quien él ataca y a los hostiles que se le acercan. */
    private class GuardGoal extends TargetGoal {
        private int scan;
        private LivingEntity pick;
        GuardGoal() { super(YunnEntity.this, false); setFlags(EnumSet.of(Flag.TARGET)); }

        @Override
        public boolean canUse() {
            if (isWeakened() || --scan > 0) return false;
            scan = 10;
            ServerPlayer o = getOwnerPlayer();
            if (o == null || o.level() != level()) return false;
            LivingEntity a = o.getLastHurtByMob();
            if (a != null && o.tickCount - o.getLastHurtByMobTimestamp() < 100 && canAttack(a)) { pick = a; return true; }
            LivingEntity b = o.getLastHurtMob();
            if (b != null && o.tickCount - o.getLastHurtMobTimestamp() < 100 && canAttack(b) && !(b instanceof Player)) { pick = b; return true; }
            List<Mob> near = level().getEntitiesOfClass(Mob.class, o.getBoundingBox().inflate(10, 5, 10),
                    m -> m instanceof Enemy && m.isAlive() && canAttack(m)
                            && !(m instanceof NeutralMob n && !n.isAngry() && m.getTarget() != o));
            pick = near.stream().min((x, y) -> Double.compare(x.distanceToSqr(o), y.distanceToSqr(o))).orElse(null);
            return pick != null;
        }

        @Override
        public void start() {
            setTarget(pick);
            targetMob = pick;
            super.start();
        }
    }
}
