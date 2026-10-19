package com.laury.laurymod.registry;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.menu.LauryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, LauryMod.MODID);

    public static final RegistryObject<MenuType<LauryMenu>> LAURY = MENUS.register("laury", () -> IForgeMenuType.create(LauryMenu::new));

    private ModMenus() {}
}
