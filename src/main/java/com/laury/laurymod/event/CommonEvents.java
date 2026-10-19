package com.laury.laurymod.event;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.command.LauryCommand;
import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.item.DragonWeapon;
import com.laury.laurymod.registry.ModItems;
import com.laury.laurymod.util.LauryChat;
import com.laury.laurymod.util.LauryLookup;
import com.laury.laurymod.util.LaurySpeech;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LauryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {

    private CommonEvents() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        LauryCommand.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        ServerPlayer p = e.getPlayer();
        String raw = e.getRawText();
        if (p == null || raw == null || p.getServer() == null) return;
        p.getServer().execute(() -> LauryChat.onChat(p, raw));
    }

    /** Las espadas de dragón solo las puede usar Laury. */
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent e) {
        Player p = e.getEntity();
        if (p.getMainHandItem().getItem() instanceof DragonWeapon) {
            e.setCanceled(true);
            p.displayClientMessage(Component.literal("§6Esta espada de dragón solo responde a Laury."), true);
        }
    }

    /** Laury (o sus flechas) nunca dañan a su compañero. */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent e) {
        Entity src = e.getSource().getEntity();
        if (src instanceof LauryEntity laury && laury.isOwner(e.getEntity())) e.setCanceled(true);
    }

    /** Solo puede existir una Laury: si ya tienes una, el cuerno crafteado se devuelve en materiales. */
    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (!e.getCrafting().is(ModItems.SUMMON_HORN.get()) && !isHornRecipe(e.getInventory())) return;
        if (!LauryLookup.isBound(p)) return;
        p.getServer().execute(() -> {
            if (!removeOneHorn(p)) return;
            give(p, new ItemStack(Items.LAPIS_LAZULI, 2));
            give(p, new ItemStack(Items.DIAMOND));
            give(p, new ItemStack(Items.GOLD_INGOT));
            give(p, new ItemStack(Items.GOAT_HORN));
            LaurySpeech.system(p, "Solo puede existir una Laury, y ya está contigo. Te devolví los materiales.");
        });
    }

    private static boolean isHornRecipe(net.minecraft.world.Container grid) {
        boolean horn = false, lapis = false, diamond = false, gold = false;
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            horn |= s.is(Items.GOAT_HORN);
            lapis |= s.is(Items.LAPIS_LAZULI);
            diamond |= s.is(Items.DIAMOND);
            gold |= s.is(Items.GOLD_INGOT);
        }
        return horn && lapis && diamond && gold;
    }

    private static boolean removeOneHorn(ServerPlayer p) {
        ItemStack carried = p.containerMenu.getCarried();
        if (carried.is(ModItems.SUMMON_HORN.get())) {
            carried.shrink(1);
            return true;
        }
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(ModItems.SUMMON_HORN.get())) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    @SubscribeEvent
    public static void onChangeDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) bring(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) bring(p);
    }

    private static void bring(ServerPlayer p) {
        LauryEntity l = LauryLookup.find(p);
        if (l != null && l.getMode() == LauryEntity.MODE_FOLLOW && !l.isWeakened() && p.getVehicle() != l) l.teleportToOwner(p);
    }
}
