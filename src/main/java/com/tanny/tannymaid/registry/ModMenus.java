package com.tanny.tannymaid.registry;

import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.menu.TannyMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TannyMaid.MODID);

    public static final RegistryObject<MenuType<TannyMenu>> TANNY =
            MENUS.register("tanny", () -> IForgeMenuType.create(TannyMenu::new));

    private ModMenus() {}
}
