package com.tanny.tannymaid.util;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Frases de Tanny. Siempre dulce, educada y leal con su Master. */
public final class TannySpeech {

    public static final String[] SUMMON = {
            "¡Hola, Master! Soy Tanny, tu maid de aventuras. Desde hoy cuidaré de ti, jejeje ♡",
            "Contrato aceptado~ ¡Encantada, Master! Lucario y yo estamos a tus órdenes ♡"
    };
    public static final String[] FOLLOW = {
            "¡Claro, Master! Voy contigo a donde sea ♡",
            "¡Jajaja, me encanta explorar contigo! Vamos~",
            "¡A la aventura! No me separo de ti, Master."
    };
    public static final String[] STAY = {
            "Entendido, Master. Te espero aquí... no tardes mucho, ¿sí? ♡",
            "Claro, me quedo aquí cuidando todo."
    };
    public static final String[] HOME = {
            "Voy a casa, Master. ¡La dejaré bien linda para cuando vuelvas!",
            "Regreso a casa. Te tendré todo listo ♡"
    };
    public static final String[] SET_HOME = {
            "¡Anotado! Esta será nuestra casa, Master. Qué lindo lugar ♡"
    };
    public static final String[] DEFEND = {
            "Modo defensa activado. ¡Nadie le pone un dedo encima a mi Master!",
            "¡Entendido! Lucario y yo vigilaremos cada rincón."
    };
    public static final String[] FARM = {
            "¡Me encargo del huerto! Cosecharé y volveré a sembrar todo ♡",
            "Claro, Master. Dejaré los cultivos perfectos, jejeje."
    };
    public static final String[] TP = {
            "¡Aquí estoy, Master! ¿Me extrañaste? Jejeje ♡",
            "¡Llegué! Nunca te dejaría solo, Master."
    };
    public static final String[] COMBAT = {
            "¡Cuidado, Master! Yo me encargo.",
            "¡No te acerques a mi Master!",
            "¡Detrás de mí, Master!"
    };
    public static final String[] LUCARIO_OUT = {
            "¡Lucario, te elijo a ti! ¡Protejamos a Master!",
            "¡Sal, Lucario! Es hora de brillar ♡"
    };
    public static final String[] LUCARIO_AURA = {
            "¡Lucario, Esfera Aural!",
            "¡Ahora, Lucario! ¡Esfera Aural!"
    };
    public static final String[] LUCARIO_BACK = {
            "Buen trabajo, Lucario. Descansa ♡",
            "¡Regresa, Lucario! Lo hiciste genial."
    };
    public static final String[] LUCARIO_FAINT = {
            "¡Lucario! ...Descansa un poco, amigo. Pronto volverás a pelear."
    };
    public static final String[] WEAK = {
            "Ay... Master, perdón... necesito descansar un momentito...",
            "Mmm... estoy un poco débil... pero no me iré a ningún lado, Master..."
    };
    public static final String[] RECOVER = {
            "¡Ya estoy mejor! Gracias por esperarme, Master ♡",
            "¡Lista de nuevo! ¿Seguimos con la aventura?"
    };
    public static final String[] FED = {
            "¡Mmm, qué rico! Gracias, Master, eres muy lindo ♡",
            "¡Jajaja, me encanta! Gracias por cuidarme."
    };
    public static final String[] FULL = {
            "Jejeje, ya estoy llena, Master. ¡Pero gracias!"
    };
    public static final String[] AMBIENT = {
            "Master, ¿sabías que Lucario puede sentir el aura de las personas? Dice que la tuya es muy cálida ♡",
            "¡Qué lindo día para una aventura!",
            "Si te cansas, avísame. Yo cargo las cosas, jejeje.",
            "Me pregunto qué Pokémon encontraremos hoy...",
            "Master, si necesitas que ordene tus cofres, solo dímelo ♡",
            "¡Jajaja! Lucario se pone celoso cuando te miro tanto, jejeje."
    };

    private TannySpeech() {}

    public static String pick(RandomSource random, String[] lines) {
        return lines[random.nextInt(lines.length)];
    }

    public static void say(TannyEntity tanny, ServerPlayer player, String[] lines) {
        say(tanny, player, pick(tanny.getRandom(), lines));
    }

    public static void say(TannyEntity tanny, ServerPlayer player, String message) {
        if (player == null) return;
        if (tanny != null && tanny.getNickname() != null) {
            message = message.replace("Master", tanny.getNickname());
        }
        player.sendSystemMessage(Component.literal("§d<Tanny>§r " + message));
        if (tanny != null) {
            tanny.markTalking();
            tanny.level().playSound(null, tanny.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.NEUTRAL, 0.5F, 1.6F);
        }
    }

    public static void system(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("§d[TannyMaid] §7" + message));
    }
}
