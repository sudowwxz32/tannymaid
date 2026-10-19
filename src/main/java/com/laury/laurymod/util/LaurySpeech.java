package com.laury.laurymod.util;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Frases de Laury: enérgica, risueña, astuta, cariñosa y dura. {p} = nombre del jugador. */
public final class LaurySpeech {

    public static final String[] SUMMON = {
            "¡Jajaja! ¡Por fin alguien con agallas me invoca! Soy Laury, mujer dragón. Desde hoy cuido tu espalda, {p}.",
            "¡Rooaar! ...Jajaja, tranquilo, {p}. Soy Laury. Fuerte, lista y ahora tu compañera. ¿Listo para la aventura?"
    };
    public static final String[] FOLLOW = {"¡Vamos, {p}! ¡Yo voy detrás de ti!", "¡A la carga! Te sigo, {p}.", "¡Jajaja, no me vas a dejar atrás!"};
    public static final String[] STAY = {"Ok, me quedo aquí vigilando. ¡Nada pasa sin que yo lo vea!", "Entendido. Aquí te espero, {p}."};
    public static final String[] TP = {"¡Aquí estoy, {p}! ¿Me necesitabas? Jajaja.", "¡Llegué volando! Literalmente, jaja."};
    public static final String[] COMBAT = {"¡Ja! ¿Eso es todo lo que tienes?", "¡Detrás de mí, {p}! ¡Esto se pone bueno!", "¡Nadie toca a mi compañero!"};
    public static final String[] TO_DRAGON = {"¡Hora de ponerse seria! ¡ROOAAAR!", "¡Siente el poder de un dragón de verdad! ¡Jajaja!"};
    public static final String[] TO_HUMAN = {"Fiuu... de vuelta a dos patas. ¿Me extrañabas así, {p}?", "¡Listo! Forma humana. Las alas pesan menos así, jaja."};
    public static final String[] RIDE = {"¡Agárrate fuerte, {p}! Mira hacia donde quieras volar y avanza. ¡Jajaja!", "¡Súbete! ¡El cielo es nuestro!"};
    public static final String[] WEAK = {"Ugh... me dieron fuerte... Dame un momento, {p}. Ya me levanto...", "Tsk... sigo aquí. Solo necesito recuperar el aliento..."};
    public static final String[] RECOVER = {"¡Jajaja! ¡Laury está de vuelta y con más ganas!", "¡Recuperada al cien! ¿Quién sigue?"};
    public static final String[] FED = {"¡Mmm, delicioso! Gracias, {p}. ¡Energía al máximo!", "¡Jaja, comida! Me conoces bien."};
    public static final String[] AMBIENT = {
            "Oye {p}, ¿sabías que puedo cargar el doble que tú? Jajaja, era broma... o no.",
            "Analizo el terreno... por allá hay buenas vetas de mineral, creo.",
            "¡Qué día tan bonito para volar!",
            "Si te cansas de caminar, ya sabes: /laury dragon y te llevo, jaja.",
            "Me gusta viajar contigo, {p}. Eres raro, pero del bueno, jajaja."
    };

    private LaurySpeech() {}

    public static String pick(RandomSource r, String[] lines) {
        return lines[r.nextInt(lines.length)];
    }

    public static String nameFor(LauryEntity laury, ServerPlayer player) {
        if (laury != null && laury.getNickname() != null) return laury.getNickname();
        return player != null ? player.getGameProfile().getName() : "compañero";
    }

    public static void say(LauryEntity laury, ServerPlayer player, String[] lines) {
        say(laury, player, pick(laury.getRandom(), lines));
    }

    public static void say(LauryEntity laury, ServerPlayer player, String message) {
        if (player == null) return;
        message = message.replace("{p}", nameFor(laury, player));
        player.sendSystemMessage(Component.literal("§9<Laury>§r " + message));
        if (laury != null) {
            laury.markTalking();
            laury.level().playSound(null, laury.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.NEUTRAL, 0.4F, laury.isDragon() ? 0.6F : 1.3F);
        }
    }

    public static void system(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("§9[Laury] §7" + message));
    }
}
