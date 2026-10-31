package com.tanny.tannymaid.entity;

import com.tanny.tannymaid.cobblemon.LucarioCompanion;
import com.tanny.tannymaid.entity.goal.*;
import com.tanny.tannymaid.registry.ModItems;
import com.tanny.tannymaid.util.ChestOrganizer;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.network.NetworkHooks;
import com.tanny.tannymaid.menu.TannyMenu;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import com.tanny.tannymaid.util.TannyChat;

/**
 * Tanny: maid de aventuras. No puede morir (se debilita), tiene inventario propio,
 * sigue órdenes, defiende a su Master, cosecha, ordena cofres y pelea junto a su Lucario.
 */
public class TannyEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(TannyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WEAKENED =
            SynchedEntityData.defineId(TannyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TALK_START =
            SynchedEntityData.defineId(TannyEntity.class, EntityDataSerializers.INT);

    public static final int WEAKENED_TICKS = 1200; // 60 segundos
    private static final int TALK_TICKS = 50;

    private final SimpleContainer inventory = new SimpleContainer(27);
    /** Barra de herramientas: 0 pico, 1 pala, 2 hacha, 3 espada, 4 escudo. */
    private final SimpleContainer tools = new SimpleContainer(TannyMenu.TOOL_COUNT);
    /** Armadura: 0 casco, 1 pechera, 2 pantalones, 3 botas. */
    private final SimpleContainer armor = new SimpleContainer(4);
    /** Bolsa de Poké Balls (solo acepta Poké Balls). */
    private final SimpleContainer balls = new SimpleContainer(27);
    public static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private long berryReadyAt;
    @Nullable private ServerPlayer cachedOwner;
    private int cachedOwnerTick = -1;

    // ---- recolección
    @Nullable private TannyResource gatherResource;
    private int gatherTarget;
    private int gatherDone;
    private final Set<BlockPos> gatherSkip = new HashSet<>();
    private long breadReadyAt;
    private long safeReadyAt;
    private final LucarioCompanion lucario = new LucarioCompanion(this);

    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos homePos;
    @Nullable private String homeDim;
    @Nullable private BlockPos anchorPos; // punto para "quedarse" y "granja"
    private boolean collecting = true;
    private int weakenedTimer;
    private long lastCombatSpeech = -10000L;

    // ---- conversación
    private record Pending(int at, String text) {}
    private final Deque<Pending> pendingLines = new ArrayDeque<>();
    private final Map<String, Integer> lastPicks = new HashMap<>();
    private final List<String> likes = new ArrayList<>();
    @Nullable private String nickname;
    @Nullable private String expecting;
    private int expectTicks;
    private int chatWindow;
    private int worryCooldown;

    public TannyEntity(EntityType<? extends TannyEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
        this.xpReward = 0;
        this.setCustomName(Component.literal("Tanny"));
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
        this.tools.addListener(c -> updateEquipment());
        this.armor.addListener(c -> updateArmor());
        for (EquipmentSlot slot : EquipmentSlot.values()) this.setDropChance(slot, 0.0F);
    }

    public static final double BASE_HEALTH = 60.0D, BASE_DAMAGE = 7.0D, BASE_ARMOR = 6.0D;

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ATTACK_SPEED, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.ARMOR, BASE_ARMOR);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MODE, TannyMode.FOLLOW.ordinal());
        this.entityData.define(DATA_WEAKENED, false);
        this.entityData.define(DATA_TALK_START, -1000);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TannyWeakenedGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TannyCombatGoal(this));
        this.goalSelector.addGoal(2, new TannyChatGoal(this));
        this.goalSelector.addGoal(3, new TannyGatherGoal(this));
        this.goalSelector.addGoal(3, new TannyFollowOwnerGoal(this, 1.1D, 4.5F, 2.2F));
        this.goalSelector.addGoal(3, new TannyStayGoal(this));
        this.goalSelector.addGoal(4, new TannyFarmGoal(this));
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new TannyCollectItemsGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new TannyOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new TannyOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new TannyDefendGoal(this));
    }

    // ------------------------------------------------------------------ dueño

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    public boolean isOwner(Entity entity) {
        return entity != null && ownerUUID != null && ownerUUID.equals(entity.getUUID());
    }

    /** Solo en servidor; busca al dueño en cualquier dimensión. */
    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (ownerUUID == null || !(level() instanceof ServerLevel sl)) return null;
        // Optimización: los objetivos (goals) la piden muchas veces por tick; se busca una sola vez por tick.
        if (cachedOwnerTick != tickCount || (cachedOwner != null && cachedOwner.isRemoved())) {
            cachedOwner = sl.getServer().getPlayerList().getPlayer(ownerUUID);
            cachedOwnerTick = tickCount;
        }
        return cachedOwner;
    }

    // ------------------------------------------------------------------ estado

    public TannyMode getMode() {
        return TannyMode.byId(this.entityData.get(DATA_MODE));
    }

    public void setMode(TannyMode mode) {
        this.entityData.set(DATA_MODE, mode.ordinal());
        this.getNavigation().stop();
    }

    public boolean isWeakened() {
        return this.entityData.get(DATA_WEAKENED);
    }

    public boolean isTalking() {
        int start = this.entityData.get(DATA_TALK_START);
        int now = (int) this.level().getGameTime();
        return now - start >= 0 && now - start < TALK_TICKS;
    }

    public void markTalking() {
        this.entityData.set(DATA_TALK_START, (int) this.level().getGameTime());
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public LucarioCompanion getLucario() {
        return lucario;
    }

    public boolean isCollecting() {
        return collecting;
    }

    @Nullable
    public BlockPos getAnchor() {
        TannyMode mode = getMode();
        if (mode == TannyMode.HOME) {
            if (homePos != null && homeDim != null && homeDim.equals(level().dimension().location().toString())) {
                return homePos;
            }
            return null;
        }
        if (mode == TannyMode.STAY || mode == TannyMode.FARM) {
            return anchorPos;
        }
        return null;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        ServerPlayer owner = getOwnerPlayer();

        if (isWeakened()) {
            setTarget(null);
            getNavigation().stop();
            weakenedTimer--;
            if (tickCount % 20 == 0) {
                heal(getMaxHealth() / (WEAKENED_TICKS / 20.0F));
                ((ServerLevel) level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        getX(), getY() + 1.0, getZ(), 2, 0.3, 0.3, 0.3, 0.0);
            }
            if (isInDanger()) {
                teleportToSafety();
            }
            if (weakenedTimer <= 0) {
                recover(owner);
            }
        } else if (getHealth() < getMaxHealth()) {
            // Regeneración: 1 ♥ cada 2 s peleando y cada 1 s descansando
            int every = getTarget() == null ? 20 : 40;
            if (tickCount % every == 0) heal(2.0F);
        }

        if (getY() < level().getMinBuildHeight() - 8) {
            teleportToSafety();
        }

        if (owner != null && tickCount % 100 == 0) {
            UUID stored = TannyLookup.getStoredId(owner);
            if (stored != null && !stored.equals(getUUID())) {
                // El Master firmó un contrato nuevo: esta copia antigua se retira.
                lucario.recall(false);
                discard();
                return;
            }
            if (stored == null) {
                TannyLookup.setStoredId(owner, getUUID());
            }
            TannyLookup.storeLastPos(owner, this);
        }

        lucario.tick();

        // Conversación: frases en cola (con pausa para que parezca que escribe)
        if (chatWindow > 0) chatWindow--;
        if (expectTicks > 0 && --expectTicks == 0) expecting = null;
        if (worryCooldown > 0) worryCooldown--;
        int spoken = 0;
        while (!pendingLines.isEmpty() && pendingLines.peekFirst().at() <= tickCount && spoken++ < 2) {
            TannySpeech.say(this, owner, pendingLines.pollFirst().text());
        }

        if (owner != null && !isWeakened() && owner.level() == level() && distanceToSqr(owner) < 24 * 24) {
            if (worryCooldown == 0 && tickCount % 20 == 0 && owner.isAlive()
                    && owner.getHealth() <= owner.getMaxHealth() * 0.3F) {
                worryCooldown = 1200;
                TannyChat.worryAboutOwner(this, owner);
            } else if (getTarget() == null && chatWindow == 0 && distanceToSqr(owner) < 64
                    && getRandom().nextInt(3600) == 0) {
                queueSay(TannyChat.ambientLine(this, owner), 0);
            }
        }
    }

    // ------------------------------------------------------------------ conversación

    /** Pone una frase en cola; se dice tras {@code delay} ticks después de la anterior. */
    public void queueSay(String text, int delay) {
        int base = pendingLines.isEmpty() ? tickCount : Math.max(tickCount, pendingLines.peekLast().at());
        pendingLines.addLast(new Pending(base + delay, text));
    }

    public boolean hasPendingLines() {
        return !pendingLines.isEmpty();
    }

    public void openChatWindow() {
        chatWindow = 900; // 45 s sin tener que repetir "Tanny"
    }

    public void closeChatWindow() {
        chatWindow = 0;
        expecting = null;
    }

    public boolean isChatting() {
        return chatWindow > 0;
    }

    @Nullable
    public String getExpecting() {
        return expecting;
    }

    public void expect(@Nullable String topic) {
        expecting = topic;
        expectTicks = topic == null ? 0 : 700;
    }

    @Nullable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@Nullable String nickname) {
        this.nickname = nickname;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void addLike(String like) {
        likes.remove(like);
        likes.add(like);
        while (likes.size() > 8) likes.remove(0);
    }

    /** Elige una frase evitando repetir la última usada para esa misma clave. */
    public String pickLine(String key, String[] lines) {
        if (lines.length == 1) return lines[0];
        int last = lastPicks.getOrDefault(key, -1);
        int i = getRandom().nextInt(lines.length);
        if (i == last) i = (i + 1 + getRandom().nextInt(lines.length - 1)) % lines.length;
        lastPicks.put(key, i);
        return lines[i];
    }

    // ------------------------------------------------------------------ combate y vida

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return false;
        }
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            teleportToSafety();
        }
        if (isWeakened()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && (isOwner(attacker) || LucarioCompanion.isTannyPokemon(attacker))) {
            return false;
        }
        if (isInvulnerableTo(source)) {
            return false;
        }
        ItemStack shield = tools.getItem(4);
        // Escudo en alto (lo levanta de verdad en combate) o un bloqueo rápido de reflejo (25 %)
        boolean raised = isBlocking() && isDamageSourceBlocked(source);
        if (!shield.isEmpty() && attacker != null && !source.is(DamageTypeTags.BYPASSES_SHIELD)
                && (raised || getRandom().nextFloat() < 0.25F)) {
            // ¡Bloqueo con escudo!
            level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.9F + getRandom().nextFloat() * 0.2F);
            shield.hurtAndBreak(1 + (int) amount, this, e -> e.broadcastBreakEvent(EquipmentSlot.OFFHAND));
            tools.setChanged();
            if (attacker instanceof LivingEntity living) {
                living.knockback(0.5D, getX() - living.getX(), getZ() - living.getZ());
            }
            return false;
        }
        // Resistencia: recibe un 20 % menos de daño
        if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE)) amount *= 0.8F;
        if (amount >= getHealth()) {
            becomeWeakened();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        // Tanny nunca muere: si algo la deja a 0, solo se debilita.
        if (!level().isClientSide && !isRemoved()) {
            this.dead = false;
            becomeWeakened();
            return;
        }
        super.die(source);
    }

    public void becomeWeakened() {
        setHealth(1.0F);
        this.entityData.set(DATA_WEAKENED, true);
        weakenedTimer = WEAKENED_TICKS;
        setTarget(null);
        setAggressive(false);
        getNavigation().stop();
        clearFire();
        lucario.recall(false);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 15, 0.4, 0.4, 0.4, 0.02);
        }
        if (isInDanger()) {
            teleportToSafety();
        }
        TannySpeech.say(this, getOwnerPlayer(), TannySpeech.WEAK);
    }

    public void recover(@Nullable ServerPlayer owner) {
        this.entityData.set(DATA_WEAKENED, false);
        weakenedTimer = 0;
        setHealth(getMaxHealth());
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 6, 0.4, 0.3, 0.4, 0.02);
        }
        TannySpeech.say(this, owner, TannySpeech.RECOVER);
    }

    private boolean isInDanger() {
        return isInLava() || isOnFire() || getY() < level().getMinBuildHeight() + 1;
    }

    private void teleportToSafety() {
        clearFire();
        setDeltaMovement(0, 0, 0);
        fallDistance = 0;
        ServerPlayer owner = getOwnerPlayer();
        if (homePos != null && homeDim != null && homeDim.equals(level().dimension().location().toString())) {
            moveTo(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5, getYRot(), getXRot());
        } else if (owner != null && owner.level() == level()) {
            moveTo(owner.getX(), owner.getY(), owner.getZ(), getYRot(), getXRot());
        } else if (level() instanceof ServerLevel sl) {
            BlockPos spawn = sl.getSharedSpawnPos();
            moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, getYRot(), getXRot());
        }
        getNavigation().stop();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target == this || isOwner(target) || target instanceof TannyEntity || LucarioCompanion.isTannyPokemon(target)) {
            return false;
        }
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isWeakened() || !canAttack(target))) {
            return;
        }
        LivingEntity previous = getTarget();
        super.setTarget(target);
        if (target != null && previous == null && !level().isClientSide) {
            long now = level().getGameTime();
            if (now - lastCombatSpeech > 400) {
                lastCombatSpeech = now;
                TannySpeech.say(this, getOwnerPlayer(), TannySpeech.COMBAT);
            }
        }
    }

    /** ¿Es un mob hostil contra el que Tanny debe proteger al Master? */
    public boolean isValidDefendTarget(Mob mob, @Nullable Player owner) {
        if (!mob.isAlive() || mob == this || LucarioCompanion.isTannyPokemon(mob)) return false;
        if (!(mob instanceof Enemy)) return false;
        if (mob instanceof NeutralMob neutral && !neutral.isAngry()
                && (owner == null || mob.getTarget() != owner) && mob.getTarget() != this) {
            return false; // endermans y piglins zombificados tranquilos se dejan en paz
        }
        return canAttack(mob);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void onRemovedFromWorld() {
        lucario.recall(false);
        super.onRemovedFromWorld();
    }

    // ------------------------------------------------------------------ interacción

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer sp = (ServerPlayer) player;
        if (!isOwner(player)) {
            TannySpeech.say(this, sp, "Lo siento, solo sigo las órdenes de mi Master ♡");
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (isHealingItem(stack)) {
            feed(sp, stack);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            cycleMode(sp);
            return InteractionResult.CONSUME;
        }
        openInventory(sp);
        return InteractionResult.CONSUME;
    }

    public static boolean isHealingItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.isEdible() || stack.is(Items.POTION) || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.CAKE)) return true;
        return isCobblemonHealing(stack);
    }

    private static boolean isCobblemonHealing(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"cobblemon".equals(id.getNamespace())) return false;
        String path = id.getPath();
        return path.contains("potion") || path.contains("berry") || path.contains("revive")
                || path.contains("elixir") || path.contains("ether");
    }

    private void feed(ServerPlayer player, ItemStack stack) {
        boolean special = stack.is(Items.POTION) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.CAKE) || isCobblemonHealing(stack);
        if (!isWeakened() && getHealth() >= getMaxHealth()) {
            TannySpeech.say(this, player, TannySpeech.FULL);
            return;
        }
        Item item = stack.getItem();
        if (isWeakened()) {
            if (special) {
                recover(player);
            } else {
                FoodProperties food = item.getFoodProperties();
                int nutrition = food != null ? food.getNutrition() : 4;
                weakenedTimer = Math.max(0, weakenedTimer - nutrition * 80);
                heal(nutrition);
                TannySpeech.say(this, player, "Gracias, Master... ya me siento un poquito mejor ♡");
            }
        } else {
            if (special) {
                setHealth(getMaxHealth());
            } else {
                FoodProperties food = item.getFoodProperties();
                heal(food != null ? food.getNutrition() * 1.5F : 6.0F);
            }
            TannySpeech.say(this, player, TannySpeech.FED);
        }
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 1.2F);
        ((ServerLevel) level()).sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 4, 0.3, 0.2, 0.3, 0.02);
        if (!player.getAbilities().instabuild) {
            boolean bottle = stack.is(Items.POTION) || stack.is(Items.HONEY_BOTTLE);
            stack.shrink(1);
            if (bottle) {
                ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(empty)) player.drop(empty, false);
            }
        }
    }

    private void cycleMode(ServerPlayer player) {
        TannyMode next = switch (getMode()) {
            case FOLLOW -> TannyMode.STAY;
            case STAY -> TannyMode.DEFEND;
            case DEFEND -> TannyMode.FARM;
            default -> TannyMode.FOLLOW;
        };
        switch (next) {
            case STAY -> commandStay(player);
            case DEFEND -> commandDefend(player);
            case FARM -> commandFarm(player);
            default -> commandFollow(player);
        }
        player.displayClientMessage(Component.literal("§dModo de Tanny: §f" + next.displayName()), true);
    }

    public void openInventory(ServerPlayer player) {
        openInventory(player, TannyMenu.PAGE_GEAR);
    }

    /** Abre el menú de Tanny en una pestaña: equipo (herramientas, armadura, mochila) o Poké Balls. */
    public void openInventory(ServerPlayer player, int page) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, playerInv, p) -> new TannyMenu(id, playerInv, page, inventory, tools, armor, balls, this),
                Component.literal("Tanny ♡")), buf -> {
            buf.writeVarInt(getId());
            buf.writeVarInt(page);
        });
    }

    public SimpleContainer getTools() {
        return tools;
    }

    public SimpleContainer getArmor() {
        return armor;
    }

    public SimpleContainer getBalls() {
        return balls;
    }

    /** Pone la espada en la mano y el escudo en la otra (salvo mientras trabaja). */
    public void updateEquipment() {
        if (level().isClientSide || isGathering()) return;
        setItemSlot(EquipmentSlot.MAINHAND, tools.getItem(3));
        setItemSlot(EquipmentSlot.OFFHAND, tools.getItem(4));
    }

    /** Se pone la armadura de sus ranuras (le da protección real y se ve puesta). */
    public void updateArmor() {
        if (level().isClientSide) return;
        for (int i = 0; i < 4; i++) setItemSlot(ARMOR_SLOTS[i], armor.getItem(i));
    }

    // ------------------------------------------------------------------ armas de verdad

    public void holdInMainHand(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    /** Espada; si no tiene, el hacha; si no, el pico. */
    public ItemStack getMeleeWeapon() {
        for (int slot : new int[] {3, 2, 0}) {
            ItemStack s = tools.getItem(slot);
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getBow() {
        return tools.getItem(TannyMenu.BOW_SLOT);
    }

    /** Ticks entre golpes según la velocidad de ataque del arma (espada ≈ 12, hacha ≈ 20). */
    public int attackInterval() {
        double speed = getAttributeValue(Attributes.ATTACK_SPEED);
        return (int) Math.max(8, Math.min(30, Math.round(20.0 / Math.max(0.5, speed))));
    }

    private boolean infiniteArrows() {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS, getBow()) > 0;
    }

    private int findArrowSlot() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() instanceof net.minecraft.world.item.ArrowItem) return i;
        }
        return -1;
    }

    public boolean hasBowReady() {
        return getBow().getItem() instanceof net.minecraft.world.item.BowItem && (infiniteArrows() || findArrowSlot() >= 0);
    }

    /** No dispara si el Master (o Gardevoir/Lucario) está en la línea de tiro. */
    public boolean isShotSafe(LivingEntity target) {
        ServerPlayer owner = getOwnerPlayer();
        net.minecraft.world.phys.Vec3 a = getEyePosition(), b = target.position().add(0, target.getBbHeight() * 0.5, 0);
        for (LivingEntity friend : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24.0),
                e -> e == owner || LucarioCompanion.isTannyPokemon(e))) {
            net.minecraft.world.phys.Vec3 p = friend.position().add(0, friend.getBbHeight() * 0.5, 0);
            net.minecraft.world.phys.Vec3 ab = b.subtract(a);
            double t = Math.max(0, Math.min(1, p.subtract(a).dot(ab) / Math.max(1e-6, ab.lengthSqr())));
            if (a.add(ab.scale(t)).distanceToSqr(p) < 1.4 * 1.4) return false;
        }
        return true;
    }

    /** Dispara una flecha real con su arco (gasta flechas de la mochila salvo con Infinidad). */
    public void shootArrow(LivingEntity target, float power) {
        ItemStack bow = getBow();
        if (!(bow.getItem() instanceof net.minecraft.world.item.BowItem bowItem)) return;
        int slot = findArrowSlot();
        boolean infinite = infiniteArrows();
        ItemStack ammo = slot >= 0 ? inventory.getItem(slot).copyWithCount(1) : new ItemStack(Items.ARROW);
        if (slot < 0 && !infinite) return;
        net.minecraft.world.entity.projectile.AbstractArrow arrow =
                net.minecraft.world.entity.projectile.ProjectileUtil.getMobArrow(this, ammo, power);
        arrow = bowItem.customArrow(arrow);
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + d * 0.18, dz, 1.7F * Math.max(0.6F, power), 2.0F);
        arrow.setBaseDamage(arrow.getBaseDamage() + 1.5D);
        boolean free = infinite && ammo.is(Items.ARROW);
        arrow.pickup = free ? net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY
                : net.minecraft.world.entity.projectile.AbstractArrow.Pickup.ALLOWED;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F,
                1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
        swing(InteractionHand.MAIN_HAND);
        bow.hurtAndBreak(1, this, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        if (!free && slot >= 0) {
            inventory.getItem(slot).shrink(1);
            inventory.setChanged();
        }
        tools.setChanged();
    }

    /** ¿Enemigo peligroso que Tanny derriba en dos golpes? (los jefes no: ahí solo pega fuerte). */
    private static boolean isBoss(LivingEntity e) {
        return e instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || e instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || e instanceof net.minecraft.world.entity.monster.warden.Warden
                || e instanceof net.minecraft.world.entity.monster.ElderGuardian;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        float knock = (float) getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity living) {
            damage += net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(getMainHandItem(), living.getMobType());
            knock += net.minecraft.world.item.enchantment.EnchantmentHelper.getKnockbackBonus(this);
            if (living instanceof Enemy) {
                if (isBoss(living)) {
                    damage *= 1.3F;                                         // fuerte, pero sin exagerar
                } else {
                    // Fuerza: derriba a los enemigos peligrosos en dos golpes (máx. 40 por golpe)
                    damage = Math.max(damage, Math.min(40.0F, living.getMaxHealth() * 0.6F + 1.0F));
                }
            }
        }
        int fire = net.minecraft.world.item.enchantment.EnchantmentHelper.getFireAspect(this);
        if (fire > 0) target.setSecondsOnFire(fire * 4);
        boolean hit = target.hurt(damageSources().mobAttack(this), damage);
        if (hit) {
            if (knock > 0 && target instanceof LivingEntity living) {
                living.knockback(knock * 0.5F, Mth.sin(getYRot() * ((float) Math.PI / 180F)), -Mth.cos(getYRot() * ((float) Math.PI / 180F)));
                setDeltaMovement(getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }
            doEnchantDamageEffects(this, target);
            setLastHurtMob(target);
            ItemStack weapon = getMainHandItem();
            if (!weapon.isEmpty() && weapon.isDamageableItem()) {
                weapon.hurtAndBreak(1, this, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                tools.setChanged();
            }
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.6), target.getZ(), 1, 0, 0, 0, 0);
            }
            level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 0.8F, 1.1F);
        }
        return hit;
    }

    // ------------------------------------------------------------------ bayas

    private static final Object[][] BERRIES = {
            {"oran_berry", 8}, {"sitrus_berry", 4}, {"lum_berry", 2}, {"leppa_berry", 2},
            {"pecha_berry", 2}, {"cheri_berry", 2}, {"chesto_berry", 2}, {"rawst_berry", 2},
            {"aspear_berry", 2}, {"persim_berry", 2}
    };

    /** /tanny bayas: Tanny le da al Master bayas curativas de Cobblemon (cada 5 minutos). */
    public void commandBerries(ServerPlayer player) {
        if (!com.tanny.tannymaid.cobblemon.CobblemonBridge.isLoaded()) {
            TannySpeech.say(this, player, "Amo, sin Cobblemon no crecen bayas en mi huerto... lo siento ♡");
            return;
        }
        long now = level().getGameTime();
        if (now < berryReadyAt) {
            TannySpeech.say(this, player, "Las bayas todavía están madurando, Amo. Faltan " + (berryReadyAt - now) / 20 + " segundos ♡");
            return;
        }
        int given = 0;
        for (Object[] b : BERRIES) {
            ItemStack stack = com.tanny.tannymaid.cobblemon.CobblemonBridge.item((String) b[0], (Integer) b[1]);
            if (stack == null) continue;
            given += stack.getCount();
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        if (given == 0) {
            TannySpeech.say(this, player, "Mmm... no encuentro bayas en esta versión de Cobblemon, Amo.");
            return;
        }
        berryReadyAt = now + 6000;
        swing(InteractionHand.MAIN_HAND);
        level().playSound(null, blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.NEUTRAL, 1.0F, 1.1F);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 4, 0.3, 0.2, 0.3, 0.02);
        }
        TannySpeech.say(this, player, "¡Aquí tienes, Amo! " + given + " bayas recién cortadas: Aranja y Zidra para curar, "
                + "Ziuela y Zanama para los estados... ¡cuida mucho a tu equipo! ♡");
    }

    @Nullable
    public BlockPos getHomeInThisLevel() {
        if (homePos != null && homeDim != null && homeDim.equals(level().dimension().location().toString())) return homePos;
        return null;
    }

    @Nullable
    public BlockPos getHomePos() {
        return homePos;
    }

    @Nullable
    public String getHomeDim() {
        return homeDim;
    }

    // ------------------------------------------------------------------ recolectar

    public boolean isGathering() {
        return gatherResource != null;
    }

    @Nullable
    public TannyResource getGatherResource() {
        return gatherResource;
    }

    public boolean isGatherBlockSkipped(BlockPos pos) {
        return gatherSkip.contains(pos);
    }

    public void skipGatherBlock(BlockPos pos) {
        gatherSkip.add(pos.immutable());
    }

    public void commandGather(ServerPlayer player, TannyResource res, int amount) {
        if (isWeakened()) {
            TannySpeech.say(this, player, "Perdón, Amo... primero necesito recuperarme.");
            return;
        }
        if (res.needsTool && tools.getItem(res.toolSlot).isEmpty()) {
            TannySpeech.say(this, player, "Amo, para sacar " + res.display + " necesito " + res.toolName()
                    + ". Pónmelo en mi barra de herramientas, por favor ♡");
            return;
        }
        gatherResource = res;
        gatherTarget = Math.max(1, Math.min(64, amount));
        gatherDone = 0;
        gatherSkip.clear();
        getNavigation().stop();
        TannySpeech.say(this, player, "¡Entendido, Amo! Voy por " + gatherTarget + " de " + res.display + ". ¡Ya vuelvo! ♡");
    }

    public void onGatheredBlock() {
        gatherDone++;
        if (gatherDone >= gatherTarget) {
            finishGather(true, "done");
        } else if (!hasFreeSlot()) {
            finishGather(false, "full");
        }
    }

    private boolean hasFreeSlot() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public void finishGather(boolean success, String reason) {
        TannyResource res = gatherResource;
        int done = gatherDone;
        gatherResource = null;
        gatherSkip.clear();
        updateEquipment();
        if (res == null) return;
        ServerPlayer owner = getOwnerPlayer();
        String msg = switch (reason) {
            case "done" -> "¡Listo, Amo! Recolecté " + done + " de " + res.display + ". Está en mi mochila ♡";
            case "full" -> "Amo, mi mochila está llena. Alcancé a juntar " + done + " de " + res.display + ".";
            case "no_tool" -> "Amo, necesito " + res.toolName() + " para seguir sacando " + res.display
                    + ". Llevo " + done + ".";
            case "weak_tool" -> "Amo, mi pico no sirve para " + res.display + "... necesito uno mejor (de piedra o más). Llevo " + done + ".";
            case "stopped" -> "Está bien, Amo, dejo de recolectar. Junté " + done + " de " + res.display + ".";
            default -> done == 0
                    ? "Amo, no encuentro " + res.display + " cerca de aquí... ¿vamos a otro lugar?"
                    : "Solo encontré " + done + " de " + res.display + " por aquí, Amo. ¡Lo guardé en mi mochila!";
        };
        TannySpeech.say(this, owner, msg);
    }

    public void commandStopWork(ServerPlayer player) {
        if (isGathering()) {
            finishGather(false, "stopped");
        } else {
            TannySpeech.say(this, player, "No estoy haciendo nada ahora, Amo. ¡Dime qué necesitas! ♡");
        }
    }

    // ------------------------------------------------------------------ pan y lugar seguro

    public void commandBread(ServerPlayer player) {
        long now = level().getGameTime();
        if (now < breadReadyAt) {
            long secs = (breadReadyAt - now) / 20;
            TannySpeech.say(this, player, "El pan todavía está en el horno, Amo. Estará listo en " + secs + " segundos ♡");
            return;
        }
        breadReadyAt = now + 6000; // 5 minutos
        ItemStack bread = new ItemStack(Items.BREAD, 24);
        if (!player.getInventory().add(bread)) player.drop(bread, false);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 4, 0.3, 0.2, 0.3, 0.02);
        }
        TannySpeech.say(this, player, "¡Pan recién horneado, Amo! Aquí tienes 24. ¡Buen provecho! ♡");
    }

    public void commandSafe(ServerPlayer player) {
        long now = level().getGameTime();
        if (now < safeReadyAt) {
            TannySpeech.say(this, player, "Dame un momentito para recuperar fuerzas, Amo... (" + (safeReadyAt - now) / 20 + " s)");
            return;
        }
        if (com.tanny.tannymaid.util.SafePlace.bringToSafety(this, player)) {
            safeReadyAt = now + 600; // 30 s
        }
    }

    // ------------------------------------------------------------------ órdenes

    public void commandFollow(ServerPlayer player) {
        setMode(TannyMode.FOLLOW);
        TannySpeech.say(this, player, TannySpeech.FOLLOW);
    }

    public void commandStay(ServerPlayer player) {
        anchorPos = blockPosition();
        setMode(TannyMode.STAY);
        TannySpeech.say(this, player, TannySpeech.STAY);
    }

    public void commandSetHome(ServerPlayer player) {
        homePos = player.blockPosition();
        homeDim = player.level().dimension().location().toString();
        TannySpeech.say(this, player, TannySpeech.SET_HOME);
    }

    public void commandHome(ServerPlayer player) {
        if (homePos == null || homeDim == null) {
            TannySpeech.say(this, player, "Aún no tenemos casa, Master. Párate donde quieras y usa /tanny sethome ♡");
            return;
        }
        setMode(TannyMode.HOME);
        TannySpeech.say(this, player, TannySpeech.HOME);
        boolean sameDim = homeDim.equals(level().dimension().location().toString());
        if (!sameDim || distanceToSqr(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5) > 64 * 64) {
            ServerLevel target = player.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, new ResourceLocation(homeDim)));
            if (target != null) {
                spawnPoof();
                this.teleportTo(target, homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5,
                        EnumSet.noneOf(RelativeMovement.class), getYRot(), getXRot());
            }
        }
    }

    public void commandTp(ServerPlayer player) {
        if (!getMode().follows()) {
            setMode(TannyMode.FOLLOW);
        }
        teleportToOwner(player);
        TannySpeech.say(this, player, TannySpeech.TP);
    }

    public void commandOrganize(ServerPlayer player) {
        int count = ChestOrganizer.organize((ServerLevel) level(), blockPosition(), 8);
        if (count == 0) {
            TannySpeech.say(this, player, "Mmm... no veo cofres ni barriles cerca de mí, Master.");
            return;
        }
        swing(InteractionHand.MAIN_HAND);
        ((ServerLevel) level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1, getZ(), 12, 1.5, 0.8, 1.5, 0.0);
        TannySpeech.say(this, player, "¡Listo! Ordené " + count + " cofre(s). Todo en su lugar, como debe ser ♡");
    }

    public void commandFarm(ServerPlayer player) {
        anchorPos = blockPosition();
        setMode(TannyMode.FARM);
        TannySpeech.say(this, player, TannySpeech.FARM);
    }

    public void commandDefend(ServerPlayer player) {
        setMode(TannyMode.DEFEND);
        TannySpeech.say(this, player, TannySpeech.DEFEND);
    }

    public void commandCollect(ServerPlayer player) {
        collecting = !collecting;
        TannySpeech.say(this, player, collecting
                ? "¡Recogeré lo que encuentre en el suelo, Master!"
                : "Entendido, ya no recogeré cosas del suelo.");
    }

    public void commandStatus(ServerPlayer player) {
        TannySpeech.system(player, String.format("Vida: %.0f/%.0f%s | Modo: %s | Recoger objetos: %s | Lucario: %s",
                getHealth(), getMaxHealth(), isWeakened() ? " (debilitada, " + weakenedTimer / 20 + " s)" : "",
                getMode().displayName(), collecting ? "sí" : "no", lucario.status()));
        TannySpeech.system(player, "Gardevoir: " + com.tanny.tannymaid.cobblemon.GardevoirCompanion.status(player)
                + " | Avisos: " + com.tanny.tannymaid.util.PokemonWatch.describe(player));
        TannySpeech.system(player, "Posición: " + blockPosition().getX() + " " + blockPosition().getY() + " "
                + blockPosition().getZ() + " (" + level().dimension().location() + ")"
                + (homePos != null ? " | Casa: " + homePos.getX() + " " + homePos.getY() + " " + homePos.getZ() : ""));
    }

    public void commandDismiss(ServerPlayer player) {
        TannySpeech.say(this, player, "Está bien, Master... Te dejo el contrato. Llámame cuando me necesites ♡");
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.removeItemNoUpdate(i);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        for (SimpleContainer c : new SimpleContainer[] {tools, armor, balls}) {
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack stack = c.removeItemNoUpdate(i);
                if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) setItemSlot(slot, ItemStack.EMPTY);
        ItemStack contract = new ItemStack(ModItems.TANNY_CONTRACT.get());
        if (!player.getInventory().add(contract)) player.drop(contract, false);
        TannyLookup.setStoredId(player, null);
        lucario.recall(false);
        spawnPoof();
        discard();
    }

    // ------------------------------------------------------------------ teletransporte

    public void teleportToOwner(ServerPlayer owner) {
        if (isRemoved()) return;
        spawnPoof();
        if (owner.level() != level()) {
            this.teleportTo((ServerLevel) owner.level(), owner.getX(), owner.getY(), owner.getZ(),
                    EnumSet.noneOf(RelativeMovement.class), getYRot(), getXRot());
        } else {
            teleportNear(owner);
        }
    }

    public void teleportNear(LivingEntity target) {
        BlockPos base = target.blockPosition();
        for (int i = 0; i < 12; i++) {
            int dx = getRandom().nextInt(5) - 2;
            int dz = getRandom().nextInt(5) - 2;
            if (Math.abs(dx) < 1 && Math.abs(dz) < 1) continue;
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos pos = base.offset(dx, dy, dz);
                if (canStandAt(pos)) {
                    moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, getYRot(), getXRot());
                    getNavigation().stop();
                    return;
                }
            }
        }
        moveTo(target.getX(), target.getY(), target.getZ(), getYRot(), getXRot());
        getNavigation().stop();
    }

    private boolean canStandAt(BlockPos pos) {
        BlockPathTypes type = WalkNodeEvaluator.getBlockPathTypeStatic(level(), pos.mutable());
        if (type != BlockPathTypes.WALKABLE) return false;
        BlockPos delta = pos.subtract(blockPosition());
        return level().noCollision(this, getBoundingBox().move(delta.getX(), delta.getY(), delta.getZ()));
    }

    private void spawnPoof() {
        if (level() instanceof ServerLevel sl) {
            ParticleOptions p = ParticleTypes.CHERRY_LEAVES;
            sl.sendParticles(p, getX(), getY() + 1, getZ(), 20, 0.4, 0.6, 0.4, 0.05);
            sl.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.6F, 1.4F);
        }
    }

    // ------------------------------------------------------------------ inventario helpers

    public boolean canStore(ItemStack stack) {
        if (com.tanny.tannymaid.cobblemon.CobblemonBridge.isPokeBall(stack) && balls.canAddItem(stack)) return true;
        return inventory.canAddItem(stack);
    }

    public void storeOrDrop(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (com.tanny.tannymaid.cobblemon.CobblemonBridge.isPokeBall(stack)) {
            stack = balls.addItem(stack);   // las Poké Balls van a su bolsa especial
            if (stack.isEmpty()) return;
        }
        ItemStack rest = inventory.addItem(stack);
        if (!rest.isEmpty()) {
            spawnAtLocation(rest);
        }
    }

    public boolean consumeItem(Item item) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack s = inventory.getItem(i);
            if (s.is(item)) {
                s.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putInt("Mode", getMode().ordinal());
        tag.putBoolean("Weakened", isWeakened());
        tag.putInt("WeakenedTimer", weakenedTimer);
        tag.putBoolean("Collecting", collecting);
        if (homePos != null) tag.putLong("HomePos", homePos.asLong());
        if (homeDim != null) tag.putString("HomeDim", homeDim);
        if (anchorPos != null) tag.putLong("AnchorPos", anchorPos.asLong());
        tag.put("Inventory", inventory.createTag());
        tag.put("Tools", saveSlots(tools));
        tag.put("Armor", saveSlots(armor));
        tag.put("Balls", saveSlots(balls));
        if (nickname != null) tag.putString("Nickname", nickname);
        ListTag likeTags = new ListTag();
        for (String like : likes) likeTags.add(StringTag.valueOf(like));
        tag.put("Likes", likeTags);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.entityData.set(DATA_MODE, TannyMode.byId(tag.getInt("Mode")).ordinal());
        this.entityData.set(DATA_WEAKENED, tag.getBoolean("Weakened"));
        weakenedTimer = tag.getInt("WeakenedTimer");
        collecting = !tag.contains("Collecting") || tag.getBoolean("Collecting");
        homePos = tag.contains("HomePos") ? BlockPos.of(tag.getLong("HomePos")) : null;
        homeDim = tag.contains("HomeDim") ? tag.getString("HomeDim") : null;
        anchorPos = tag.contains("AnchorPos") ? BlockPos.of(tag.getLong("AnchorPos")) : null;
        inventory.fromTag(tag.getList("Inventory", Tag.TAG_COMPOUND));
        loadTools(tag.getList("Tools", Tag.TAG_COMPOUND));
        // mundos de versiones anteriores: sube su vida, daño y armadura base a los nuevos valores
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(BASE_HEALTH);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE);
        getAttribute(Attributes.ARMOR).setBaseValue(BASE_ARMOR);
        loadSlots(armor, tag.getList("Armor", Tag.TAG_COMPOUND));
        loadSlots(balls, tag.getList("Balls", Tag.TAG_COMPOUND));
        updateArmor();
        nickname = tag.contains("Nickname") ? tag.getString("Nickname") : null;
        likes.clear();
        ListTag likeTags = tag.getList("Likes", Tag.TAG_STRING);
        for (int i = 0; i < likeTags.size(); i++) likes.add(likeTags.getString(i));
        this.setCustomName(Component.literal("Tanny"));
    }

    /** Carga la barra de herramientas respetando la posición de cada ranura. */
    private void loadTools(ListTag list) {
        for (int i = 0; i < tools.getContainerSize(); i++) tools.setItem(i, ItemStack.EMPTY);
        // SimpleContainer.createTag() no guarda el índice: se guarda en orden y sin huecos,
        // así que usamos un formato propio con índice.
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            int slot = item.contains("TannySlot") ? item.getInt("TannySlot") : -1;
            ItemStack stack = ItemStack.of(item);
            if (slot >= 0 && slot < tools.getContainerSize()) {
                tools.setItem(slot, stack);
            } else {
                for (int j = 0; j < tools.getContainerSize(); j++) {
                    if (tools.getItem(j).isEmpty() && TannyMenu.fitsToolSlot(j, stack)) {
                        tools.setItem(j, stack);
                        break;
                    }
                }
            }
        }
        updateEquipment();
    }

    private static void loadSlots(SimpleContainer c, ListTag list) {
        for (int i = 0; i < c.getContainerSize(); i++) c.setItem(i, ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            int slot = item.getInt("TannySlot");
            if (slot >= 0 && slot < c.getContainerSize()) c.setItem(slot, ItemStack.of(item));
        }
    }

    private static ListTag saveSlots(SimpleContainer tools) {
        ListTag list = new ListTag();
        for (int i = 0; i < tools.getContainerSize(); i++) {
            ItemStack stack = tools.getItem(i);
            if (stack.isEmpty()) continue;
            CompoundTag item = new CompoundTag();
            stack.save(item);
            item.putInt("TannySlot", i);
            list.add(item);
        }
        return list;
    }

    /** Usado por el contrato al invocarla por primera vez. */
    public void initFromContract(ServerPlayer owner) {
        setOwner(owner);
        homePos = owner.blockPosition();
        homeDim = owner.level().dimension().location().toString();
        anchorPos = blockPosition();
        setMode(TannyMode.FOLLOW);
    }
}
