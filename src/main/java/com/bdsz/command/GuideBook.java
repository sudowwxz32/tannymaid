package com.bdsz.command;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Libro "Guía del Saiyajin": controles, bloques de fases y crafteos de las Listas de Entrenamiento. */
public final class GuideBook {
    private GuideBook() {}

    private static final String[] PAGES = {
            "§6§lGuía del Saiyajin§r\n\n§0Bienvenido, guerrero.\n\nTu poder se libera en §l4 bloques§r§0. Al principio solo tienes el §cKaioken§0.\n\nEntrena, craftea las §6Listas de Entrenamiento§0 y desbloquea fases más poderosas.",
            "§l§1Controles§r\n\n§0K §8- menú de fases\n§0P §8- transformarse\n§0U §8- aura\n§0I §8- volar\n§0H §8- recargar ki (otra vez H para parar)\n§0B, R, G, Z §8- ataques\n\n§8El ki también se recupera comiendo.",
            "§l§1Los bloques§r\n\n§0§lBloque 1:§r§0 Kaioken\n§0§lBloque 2:§r§0 Falso, Fase 1\n§0§lBloque 3:§r§0 Fase 2, Fase 3, Dios\n§0§lBloque 4:§r§0 Blue, Ultra Instinto\n\n§5§lBloque Especial:§r§0\nSuper Saiyajin Fase 4\nUltra Ego",
            "§l§6Lista de Entrenamiento I§r\n§8(desbloquea el Bloque 2)\n\n§0Crafteo sin forma:\n- 1 lingote de oro\n- 1 mapa vacío\n- 2 varas de blaze\n\n§0Con la lista en la mano haz §lclic izquierdo§r§0.",
            "§l§6Lista de Entrenamiento II§r\n§8(desbloquea el Bloque 3)\n\n§0Crafteo sin forma:\n- Lista de Entrenamiento I\n- 1 diamante\n- 1 polvo de redstone\n\n§0Clic izquierdo con ella en la mano.",
            "§l§6Lista de Entrenamiento III§r\n§8(desbloquea el Bloque 4)\n\n§0Crafteo sin forma:\n- Lista de Entrenamiento II\n- 1 bloque de hierro\n- 1 bloque de diamante\n\n§0Clic izquierdo con ella en la mano.",
            "§l§5Lista de Entrenamiento IV§r\n§8(desbloquea el Bloque Especial)\n\n§0Crafteo sin forma:\n- Lista de Entrenamiento III\n- 2 bloques de hierro\n- 2 diamantes\n\n§0Clic izquierdo con ella en la mano.",
            "§l§dDominio Total§r\n§8(tiempo infinito)\n\n§0Crafteo en mesa:\nLista de Entrenamiento IV en el centro, rodeada de 8 diamantes.\n\n§0Con Kaioken, de la Fase 3 al Ultra Ego el límite es de 40 minutos.",
            "§l§1Ataques§r\n\n§0R §8- Kamehameha (80 ki)\n§0G §8- Big Bang Attack (60 ki)\n§0Z §8- Puño del Dragón (200 ki): todo se oscurece, un dragón te envuelve y tu siguiente golpe es devastador. Espera de 3 minutos; una semilla del ermitaño la recupera.\n§0B §8- ráfaga de ki (12 ki)",
            "§l§1Armaduras de Ki§r\n\n§0Busca §5Mineral de Ki§0 a la altura del diamante. Fúndelo en el horno: sale un §8Lingote de Ki Negro§0.\nLingote negro + tinte naranja o azul = lingote de color.\nPieza de armadura de diamante en el centro + 8 lingotes alrededor.",
            "§l§1Consejos§r\n\n§0- Cada transformación tiene un §ltiempo límite§r§0 (barra dorada). El Kaioken lo reduce.\n- Las §2Semillas del Ermitaño§0 lo recuperan todo. Están en cofres de aldeas y se pueden plantar.\n- Desbloquea los bloques en orden."
    };

    public static void give(ServerPlayer p) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "Guía del Saiyajin");
        tag.putString("author", "Block Dragon Super Z");
        tag.putBoolean("resolved", true);
        ListTag pages = new ListTag();
        for (String page : PAGES) pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page))));
        tag.put("pages", pages);
        if (!p.getInventory().add(book)) p.drop(book, false);
    }
}
