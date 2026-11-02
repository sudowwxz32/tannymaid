package com.bdsz.event;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.command.RazaCommand;
import com.bdsz.data.Form;
import com.bdsz.data.PowerLogic;
import com.bdsz.data.SaiyanData;
import com.bdsz.data.SaiyanState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {}

    /** Dentro de un domo protector encendido no aparecen enemigos. */
    @SubscribeEvent
    public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide || e.loadedFromDisk()) return;
        if (e.getEntity() instanceof net.minecraft.world.entity.monster.Enemy
                && com.bdsz.block.ShieldGeneratorBlockEntity.protectedAt(e.getLevel(), e.getEntity().position())) {
            e.setCanceled(true);
        }
    }

    /** Las explosiones no dañan nada dentro de un domo protector. */
    @SubscribeEvent
    public static void explosion(net.minecraftforge.event.level.ExplosionEvent.Detonate e) {
        net.minecraft.world.level.Level lv = e.getLevel();
        e.getAffectedBlocks().removeIf(p -> com.bdsz.block.ShieldGeneratorBlockEntity.protectedAt(lv, p.getCenter()));
        e.getAffectedEntities().removeIf(en -> !(en instanceof net.minecraft.world.entity.monster.Enemy)
                && com.bdsz.block.ShieldGeneratorBlockEntity.protectedAt(lv, en.position()));
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        RazaCommand.register(e.getDispatcher());
    }

    /** Fuerza: tus golpes hacen más daño. Resistencia: recibes menos. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hurt(LivingHurtEvent e) {
        // solo los golpes directos (puñetazos/armas); las ráfagas de ki ya traen su propio daño
        if (e.getSource().getEntity() instanceof ServerPlayer atk && e.getSource().getDirectEntity() == atk) {
            SaiyanState s = SaiyanData.get(atk);
            if (s.saiyan && s.dragonFist > 0) {
                e.setAmount(PowerLogic.dragonFistHit(atk, e.getEntity(), e.getAmount()));
                return;
            }
            if ((s.saiyan || s.fusion > 0) && s.powered()) {
                e.setAmount(e.getAmount() * s.damageMultiplier() + s.punchBonus());
                if (s.activeForm() == Form.ULTRA || s.activeForm() == Form.BLUE || s.activeForm() == Form.GOD) {
                    atk.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM, e.getEntity().getX(), e.getEntity().getY(0.5), e.getEntity().getZ(), 1, 0, 0, 0, 0);
                }
            }
        }
        if (e.getEntity() instanceof ServerPlayer vic) {
            SaiyanState s = SaiyanData.get(vic);
            if ((s.saiyan || s.fusion > 0) && s.powered()) e.setAmount(e.getAmount() * (1.0F - s.resistance()));
        }
    }

    /** Legendario Fase 3: no pierde vida (solo el vacío o /kill pueden con él). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void legendaryImmune(LivingAttackEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            SaiyanState s = SaiyanData.get(p);
            if (s.saiyan && s.transformed && s.activeForm().legendary3()) {
                e.setCanceled(true);
                if (p.tickCount % 4 == 0) {
                    p.serverLevel().sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.3F, 1.0F, 0.3F), 1.5F),
                            p.getX(), p.getY() + 1, p.getZ(), 10, 0.4, 0.6, 0.4, 0);
                }
            }
        }
    }

    /** Ultra Instinto: esquiva automáticamente parte de los ataques. */
    @SubscribeEvent
    public static void attack(LivingAttackEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (com.bdsz.data.UltraDodge.onAttack(p, e.getSource())) e.setCanceled(true);
    }

    /** Último Entrenamiento y Dominio Total solo se pueden craftear con todo desbloqueado hasta Ultra Ego. */
    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        net.minecraft.world.item.Item result = e.getCrafting().getItem();
        boolean ultimate = false, mastery = false;
        if (result == com.bdsz.registry.ModRegistry.ULTIMATE_TRAINING.get()) ultimate = true;
        else if (result == com.bdsz.registry.ModRegistry.TOTAL_MASTERY.get()) mastery = true;
        else if (e.getCrafting().isEmpty()) {
            // shift-clic: el resultado puede venir vacío; se identifica por la rejilla
            net.minecraft.world.Container grid = e.getInventory();
            int maps = 0, copper = 0, iron = 0, center = 0;
            for (int i = 0; i < grid.getContainerSize(); i++) {
                net.minecraft.world.item.Item it = grid.getItem(i).getItem();
                if (it == net.minecraft.world.item.Items.MAP) maps++;
                else if (it == net.minecraft.world.item.Items.COPPER_BLOCK) copper++;
                else if (it == net.minecraft.world.item.Items.IRON_BLOCK) iron++;
                else if (it == com.bdsz.registry.ModRegistry.ULTIMATE_TRAINING.get()) center++;
            }
            if (maps == 1 && copper == 4 && iron == 4) ultimate = true;
            else if (center == 1) mastery = true;
        }
        if (!ultimate && !mastery) return;
        if (p.isCreative() || SaiyanData.get(p).tier >= 5) return;

        final net.minecraft.world.item.Item made = ultimate ? com.bdsz.registry.ModRegistry.ULTIMATE_TRAINING.get()
                                                            : com.bdsz.registry.ModRegistry.TOTAL_MASTERY.get();
        final java.util.List<net.minecraft.world.item.ItemStack> refund = new java.util.ArrayList<>();
        if (ultimate) {
            refund.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MAP));
            refund.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COPPER_BLOCK, 4));
            refund.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_BLOCK, 4));
        } else {
            refund.add(new net.minecraft.world.item.ItemStack(com.bdsz.registry.ModRegistry.ULTIMATE_TRAINING.get()));
            refund.add(new net.minecraft.world.item.ItemStack(com.bdsz.registry.ModRegistry.KI_ORE_ITEM.get(), 7));
            refund.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK));
        }
        p.getServer().execute(() -> {
            boolean removed = false;
            net.minecraft.world.item.ItemStack carried = p.containerMenu.getCarried();
            if (carried.is(made)) { carried.shrink(1); p.containerMenu.setCarried(carried); removed = true; }
            if (!removed) {
                net.minecraft.world.entity.player.Inventory inv = p.getInventory();
                for (int i = inv.getContainerSize() - 1; i >= 0; i--) {
                    if (inv.getItem(i).is(made)) { inv.getItem(i).shrink(1); removed = true; break; }
                }
            }
            if (!removed) return;   // no se encontró el objeto: no se devuelve nada
            for (net.minecraft.world.item.ItemStack r : refund) {
                if (!p.getInventory().add(r.copy())) p.drop(r.copy(), false);
            }
            p.containerMenu.broadcastChanges();
            p.inventoryMenu.broadcastChanges();
            p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§cDebes completar todas las listas hasta el Ultra Ego para hacer este crafteo."), false);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    /** Clic izquierdo sobre un bloque con una Lista de Entrenamiento. */
    @SubscribeEvent
    public static void leftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e) {
        if (e.getItemStack().getItem() instanceof com.bdsz.item.TrainingListItem && e.getEntity() instanceof ServerPlayer p) {
            com.bdsz.item.TrainingListItem.use(p, e.getItemStack());
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        com.bdsz.data.FusionLogic.tick(p);
        com.bdsz.data.UltraDodge.tick(p);
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) return;
        if (s.infiniteKi() && s.ki < s.maxKi()) {
            // Legendario: la energía nunca se acaba
            s.ki = s.maxKi();
            SaiyanData.setQuiet(p, s);
        }
        if (s.powering > 0) PowerLogic.tickPowerUp(p, s);
        else if (s.charging) PowerLogic.tickCharging(p, s);
        if (s.pose != 0 || s.dragonFist > 0 || s.dragonCooldown > 0 || s.attackCooldown > 0) {
            PowerLogic.tickAttacks(p, SaiyanData.get(p));
        }
        if (p.tickCount % 20 == 0) PowerLogic.secondTick(p);
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            if (PowerLogic.totemSave(p)) {
                e.setCanceled(true);
                return;
            }
            SaiyanState s = SaiyanData.get(p);
            if (s.fusion > 0 || s.fusing > 0) {
                com.bdsz.data.FusionLogic.defuse(p, s, false);
                s = SaiyanData.get(p);
            }
            if (s.transformed || s.flight || s.charging || s.powering > 0 || s.ozaru) {
                s.ozaru = false;
                s.transformed = false;
                s.flight = false;
                s.charging = false;
                s.powering = 0;
                SaiyanData.set(p, s);
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            SaiyanState s = SaiyanData.get(p);
            s.charging = false;
            s.powering = 0;
            s.pose = 0;
            s.dragonFist = 0;
            SaiyanData.set(p, s);
            PowerLogic.applyFlight(p, s);
            p.refreshDimensions();
        }
    }

    /** Modo Ozaru: el cuerpo (y su caja de choque) es gigante. Funciona en servidor y cliente. */
    @SubscribeEvent
    public static void size(net.minecraftforge.event.entity.EntityEvent.Size e) {
        if (!(e.getEntity() instanceof net.minecraft.world.entity.player.Player p)) return;
        SaiyanState s;
        if (p.level() == null) return;
        if (p.level().isClientSide) s = com.bdsz.network.ClientStates.get(p.getId());
        else if (p instanceof ServerPlayer sp && sp.connection != null) s = SaiyanData.get(sp);
        else return;
        if (!s.ozaru) return;
        float k = SaiyanState.OZARU_SCALE;
        net.minecraft.world.entity.EntityDimensions d = net.minecraft.world.entity.EntityDimensions.scalable(0.6F * k, 1.8F * k);
        e.setNewSize(d);
        e.setNewEyeHeight(1.62F * k);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            SaiyanState s = SaiyanData.get(p);
            SaiyanData.set(p, s);
            PowerLogic.applyFlight(p, s);
        }
    }

    /** Comer recupera ki. */
    @SubscribeEvent
    public static void ate(LivingEntityUseItemEvent.Finish e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getItem().isEdible()) {
            FoodProperties food = e.getItem().getFoodProperties(p);
            if (food != null) PowerLogic.onAte(p, food.getNutrition(), food.getSaturationModifier() * food.getNutrition());
        }
    }

    @SubscribeEvent
    public static void changeDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) SaiyanData.set(p, SaiyanData.get(p));
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking e) {
        if (e.getTarget() instanceof ServerPlayer target && e.getEntity() instanceof ServerPlayer viewer) {
            SaiyanData.syncTo(target, viewer);
        }
    }

    // ================================================================== fusiones

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) com.bdsz.data.FusionLogic.serverTick(e.getServer());
    }

    /** Si tiras el arete con Q mientras llevas el otro puesto, también sirve para fusionarte. */
    @SubscribeEvent
    public static void toss(net.minecraftforge.event.entity.item.ItemTossEvent e) {
        if (e.getPlayer() instanceof ServerPlayer p && e.getEntity().getItem().is(com.bdsz.registry.ModRegistry.FUSION_EARRING.get())) {
            com.bdsz.data.FusionLogic.track(e.getEntity(), p);
        }
    }

    /** Los mobs de tu misma fusión no te atacan; tus zombis aliados nunca te atacan a ti. */
    @SubscribeEvent
    public static void target(net.minecraftforge.event.entity.living.LivingChangeTargetEvent e) {
        if (!(e.getNewTarget() instanceof ServerPlayer p)) return;
        if (e.getEntity().getTags().contains("bdsz_ally") && e.getEntity().getPersistentData().hasUUID("BdszAllyOf")
                && e.getEntity().getPersistentData().getUUID("BdszAllyOf").equals(p.getUUID())) {
            e.setCanceled(true);
            return;
        }
        SaiyanState s = SaiyanData.get(p);
        if (s.fusion > 0 && com.bdsz.data.Fusion.of(e.getEntity()) == com.bdsz.data.Fusion.byId(s.fusion)) e.setCanceled(true);
    }

    /** Zombis aliados (Horda): buscan enemigos y desaparecen al minuto. */
    @SubscribeEvent
    public static void allyTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent e) {
        if (!(e.getEntity() instanceof net.minecraft.world.entity.Mob m) || m.level().isClientSide || !m.getTags().contains("bdsz_ally")) return;
        if (m.tickCount > 1200) {
            ((net.minecraft.server.level.ServerLevel) m.level()).sendParticles(ParticleTypes.SOUL, m.getX(), m.getY() + 1, m.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
            m.discard();
            return;
        }
        if (m.tickCount % 20 == 0 && (m.getTarget() == null || m.getTarget() instanceof net.minecraft.world.entity.player.Player)) {
            m.setTarget(null);
            m.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, m.getBoundingBox().inflate(16),
                            o -> o != m && o instanceof net.minecraft.world.entity.monster.Enemy && !o.getTags().contains("bdsz_ally"))
                    .stream().min((a, b) -> Double.compare(a.distanceToSqr(m), b.distanceToSqr(m))).ifPresent(m::setTarget);
        }
    }

    /** Creeper: inmune a explosiones. Golpes con habilidades de la fusión (zombi da hambre, gólem lanza por los aires). */
    @SubscribeEvent
    public static void fusionAttack(LivingAttackEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            SaiyanState s = SaiyanData.get(p);
            if (s.fusion == com.bdsz.data.Fusion.CREEPER.ordinal() || s.fusing > 0) e.setCanceled(true);
        }
        if (e.getEntity() instanceof ServerPlayer p && SaiyanData.get(p).fusing > 0) e.setCanceled(true);   // intocable mientras se fusiona
        // dentro del domo protector (o en su borde) nada de afuera te lastima
        if (e.getEntity() instanceof net.minecraft.world.entity.player.Player
                && com.bdsz.block.ShieldGeneratorBlockEntity.shelters(e.getEntity().level(), e.getEntity(),
                e.getSource().getDirectEntity() != null ? e.getSource().getDirectEntity() : e.getSource().getEntity())) {
            e.setCanceled(true);
            return;
        }
        // los de tu misma especie no te hacen daño (zombis, esqueletos y sus flechas, creepers, endermans, wardens, gólems)
        if (e.getEntity() instanceof ServerPlayer p && e.getSource().getEntity() != null) {
            SaiyanState s = SaiyanData.get(p);
            if (s.fusion > 0 && com.bdsz.data.Fusion.of(e.getSource().getEntity()) == com.bdsz.data.Fusion.byId(s.fusion)) e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fusionHit(LivingHurtEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer atk) || e.getSource().getDirectEntity() != atk) return;
        SaiyanState s = SaiyanData.get(atk);
        if (s.fusion == com.bdsz.data.Fusion.ZOMBIE.ordinal()) {
            e.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 200, 1));
        } else if (s.fusion == com.bdsz.data.Fusion.GOLEM.ordinal()) {
            e.getEntity().setDeltaMovement(e.getEntity().getDeltaMovement().add(0, 0.9, 0));
            e.getEntity().hurtMarked = true;
        }
    }

    /** Esqueleto: flechas más fuertes y sin gastar. */
    @SubscribeEvent
    public static void arrowJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide || !(e.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow a)) return;
        if (a.getOwner() instanceof ServerPlayer p && SaiyanData.get(p).fusion == com.bdsz.data.Fusion.SKELETON.ordinal()
                && a.tickCount == 0 && !a.getPersistentData().getBoolean("BdszBoost")) {
            a.getPersistentData().putBoolean("BdszBoost", true);
            a.setBaseDamage(a.getBaseDamage() * 1.6);
        }
    }

    @SubscribeEvent
    public static void arrowLoose(net.minecraftforge.event.entity.player.ArrowLooseEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !p.getAbilities().instabuild && e.hasAmmo()
                && SaiyanData.get(p).fusion == com.bdsz.data.Fusion.SKELETON.ordinal()) {
            p.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW));
        }
    }

    /** Más movilidad al golpear: un pequeño impulso hacia el enemigo. */
    @SubscribeEvent
    public static void lunge(net.minecraftforge.event.entity.player.AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.isShiftKeyDown()) return;
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan && s.fusion == 0) return;
        net.minecraft.world.phys.Vec3 look = p.getLookAngle();
        double k = s.powered() ? 0.42 : 0.28;
        p.setDeltaMovement(p.getDeltaMovement().add(look.x * k, p.onGround() ? 0.08 : look.y * k * 0.5, look.z * k));
        p.hurtMarked = true;
    }
}
