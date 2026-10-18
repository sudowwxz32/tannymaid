package com.tanny.tannymaid.util;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversación con Tanny (sin internet): entiende frases por palabras clave, responde
 * según el contexto (hora, clima, bioma, vida, peligro), recuerda tus gustos y tu apodo,
 * te hace preguntas y reacciona a tus respuestas, y obedece órdenes dichas en el chat.
 *
 * Cómo hablarle: escribe en el chat empezando con "Tanny" (ej. "Tanny, ¿cómo estás?").
 * Durante ~45 s después puedes seguir hablando sin repetir su nombre. "Adiós" termina la charla.
 */
public final class TannyChat {

    private TannyChat() {}

    private static final Pattern NAME = Pattern.compile("\\b(tanny|tany|tani|tanni|tannys)\\b");
    private static final Pattern LIKE = Pattern.compile("^(?:a mi |yo )?me gustan? (?:mucho |bastante )?(?:el |la |los |las )?(.{2,40})$");
    private static final Pattern FAVORITE = Pattern.compile("mi ([a-z]+) favorit[oa] es (?:el |la |los |las )?(.{2,40})$");
    private static final Pattern CALL_ME = Pattern.compile("(?:llamame|dime|puedes llamarme) ([a-z0-9 ]{2,20})$");

    // ===================================================================== entrada

    public static void onChat(ServerPlayer player, String raw) {
        String text = normalize(raw);
        if (text.isEmpty()) return;
        boolean named = NAME.matcher(text).find();

        TannyEntity tanny = TannyLookup.find(player);
        if (tanny == null) {
            if (named) answerStranger(player);
            return;
        }
        boolean near = tanny.level() == player.level() && tanny.distanceToSqr(player) < 24 * 24;
        if (!named && !(tanny.isChatting() && near)) return;

        String msg = NAME.matcher(text).replaceAll(" ").replaceAll("\\s+", " ").trim();
        tanny.openChatWindow();
        reply(tanny, player, msg, raw, named);
    }

    private static void answerStranger(ServerPlayer player) {
        List<TannyEntity> nearby = player.level().getEntitiesOfClass(TannyEntity.class,
                player.getBoundingBox().inflate(12.0D));
        for (TannyEntity t : nearby) {
            if (!t.isOwner(player) && !t.hasPendingLines()) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§d<Tanny>§r Hola~ Perdón, solo converso con mi Master, jejeje ♡"));
                return;
            }
        }
    }

    // ===================================================================== respuesta

    private static void reply(TannyEntity t, ServerPlayer p, String m, String raw, boolean named) {
        int delay = typingDelay(m);

        // Solo dijo su nombre
        if (m.isEmpty()) {
            say(t, delay, t.pickLine("call", new String[] {
                    "¿Sí, Master? Aquí estoy ♡",
                    "¿Me llamabas, Master? Jejeje.",
                    "¡Dime, Master! Te escucho.",
                    "Mmm? ¿Qué pasa, Master? ♡"}));
            return;
        }

        // ¿Esperaba una respuesta a algo que ella preguntó?
        if (t.getExpecting() != null && handleExpected(t, p, m, delay)) {
            return;
        }

        // ---------- memoria: apodo y gustos
        Matcher call = CALL_ME.matcher(m);
        if (has(m, "llamame", "puedes llamarme") && call.find()) {
            String nick = capitalize(call.group(1).trim());
            if (nick.equalsIgnoreCase("master") || nick.equalsIgnoreCase("amo")) {
                t.setNickname(null);
                say(t, delay, "¡Entendido, Master! Como siempre, jejeje ♡");
            } else {
                say(t, delay, "¿" + nick + "? Mmm... me gusta cómo suena. ¡Desde ahora te diré " + nick + " ♡!");
                t.setNickname(nick);
            }
            return;
        }
        if (has(m, "te quiero", "te amo", "me gustas", "te adoro", "eres mi novia", "se mi novia", "casate conmigo", "un beso", "dame un beso", "besito")) {
            hearts(t);
            say(t, delay, t.pickLine("love", new String[] {
                    "¿Eh? ¡¿E-en serio?! ...Jejeje, yo también te quiero mucho, Master ♡",
                    "M-Master... me haces sonrojar. ¡Lucario, no mires! Jajaja ♡",
                    "Jejeje... Yo siempre voy a estar a tu lado, Master. Eso es una promesa ♡",
                    "*se tapa la cara* ¡No digas esas cosas tan de repente! ...pero me gustó, jejeje ♡"}));
            return;
        }
        Matcher fav = FAVORITE.matcher(m);
        if (fav.find()) {
            String thing = fav.group(1);
            String value = fav.group(2).trim();
            t.addLike(value + " (tu " + thing + " favorito)");
            say(t, delay, "¿Tu " + thing + " favorito es " + value + "? ¡Anotado en mi libretita de maid! Jejeje ♡");
            say(t, 40, "¿Y por qué te gusta tanto, Master?");
            t.expect("why_like");
            return;
        }
        Matcher like = LIKE.matcher(m);
        if (like.find() && !has(m, "me gusta tu", "me gustas")) {
            String value = like.group(1).trim();
            t.addLike(value);
            say(t, delay, t.pickLine("like", new String[] {
                    "¿Te gusta " + value + "? ¡Lo voy a recordar, Master! ♡",
                    "Mmm, " + value + "... ¡Qué buen gusto tienes! Lo guardo en mi memoria, jejeje.",
                    "¡Anotado! A mi Master le gusta " + value + " ♡"}));
            return;
        }
        // ---------- órdenes dichas en conversación
        if (has(m, "sigueme", "vamos", "vente conmigo", "acompaname", "follow")) {
            t.commandFollow(p); return;
        }
        if (has(m, "quedate", "espera aqui", "esperame", "no te muevas", "stay")) {
            t.commandStay(p); return;
        }
        if (has(m, "ve a casa", "vete a casa", "regresa a casa", "vuelve a casa", "anda a casa")) {
            t.commandHome(p); return;
        }
        if (has(m, "ven aqui", "ven aca", "ven conmigo", "ven rapido", "teletransportate") || m.equals("ven")) {
            t.commandTp(p); return;
        }
        if (has(m, "ordena", "organiza", "acomoda los cofres", "limpia los cofres")) {
            t.commandOrganize(p); return;
        }
        if (has(m, "cosecha", "siembra", "cultiva", "granja", "huerto")) {
            t.commandFarm(p); return;
        }
        if (has(m, "protegeme", "defiendeme", "cuidame", "defiende", "modo defensa")) {
            t.commandDefend(p); return;
        }
        if (has(m, "saca a lucario", "llama a lucario", "sal lucario", "invoca a lucario", "guarda a lucario", "regresa lucario")) {
            t.getLucario().toggleManual(p); return;
        }
        if (has(m, "inventario", "que llevas", "que tienes", "abre tu mochila", "muestrame tus cosas")) {
            say(t, delay, "¡Claro! Mira, esto es lo que llevo ♡");
            t.openInventory(p); return;
        }

        if (has(m, "que recuerdas", "que sabes de mi", "te acuerdas", "recuerdas", "que me gusta")) {
            List<String> likes = t.getLikes();
            if (likes.isEmpty()) {
                say(t, delay, "Mmm... todavía no me has contado qué te gusta, Master. ¡Cuéntame algo! Dime \"me gusta...\" ♡");
            } else {
                say(t, delay, "¡Claro que me acuerdo! Te gusta: " + String.join(", ", likes) + ". ¿Ves? Presto atención, jejeje ♡");
            }
            return;
        }
        if (has(m, "como me llamo", "sabes mi nombre", "cual es mi nombre")) {
            String real = p.getGameProfile().getName();
            say(t, delay, t.getNickname() != null
                    ? "Te llamas " + real + ", pero para mí eres " + t.getNickname() + " ♡"
                    : "Te llamas " + real + "... ¡pero para mí siempre serás mi Master! Jejeje ♡");
            return;
        }

        // ---------- conversación general
        if (has(m, "adios", "chao", "chau", "bye", "nos vemos", "hasta luego", "me voy", "buenas noches tanny")) {
            say(t, delay, t.pickLine("bye", new String[] {
                    "¡Hasta luego, Master! Aquí estaré cuando me necesites ♡",
                    "Nos vemos, Master. ¡Cuídate mucho!",
                    "¡Chao chao! Lucario y yo te esperamos ♡"}));
            t.closeChatWindow();
            return;
        }
        if (has(m, "buenos dias", "buenas tardes", "buenas noches", "hola", "holi", "hey", "que hubo", "quiubo", "buenas", "hi", "hello", "saludos")) {
            say(t, delay, greeting(t));
            if (t.getRandom().nextBoolean()) {
                say(t, 35, "¿Cómo estás hoy, Master?");
                t.expect("mood");
            }
            return;
        }
        if (has(m, "como estas", "que tal", "como te sientes", "como te va", "estas bien", "todo bien", "how are you")) {
            say(t, delay, selfStatus(t));
            say(t, 35, "¿Y tú, Master? ¿Cómo estás?");
            t.expect("mood");
            return;
        }
        if (has(m, "como te llamas", "quien eres", "tu nombre", "que eres")) {
            say(t, delay, "Soy Tanny, tu maid de aventuras ♡ Cocino, limpio, ordeno cofres, cosecho... ¡y peleo junto a Lucario para protegerte!");
            return;
        }
        if (has(m, "cuantos anos", "tu edad", "que edad")) {
            say(t, delay, "¡Eso no se le pregunta a una dama, Master! Jajaja... digamos que tengo la edad perfecta para cuidarte ♡");
            return;
        }
        if (has(m, "que te gusta", "tu comida favorita", "que comes", "tu color favorito", "tus gustos", "tu favorito")) {
            say(t, delay, t.pickLine("herlikes", new String[] {
                    "Me encantan los pasteles, el té de bayas y... pasar tiempo contigo, jejeje ♡",
                    "¿Mi color favorito? El rosa, ¡como el lazo de Sylveon! Aunque el negro de mi vestido también me gusta.",
                    "Me gustan los días tranquilos, las flores y que mi Master llegue a casa sano y salvo ♡"}));
            say(t, 35, "¿Y a ti qué te gusta, Master?");
            t.expect("your_likes");
            return;
        }
        if (has(m, "lucario")) {
            say(t, delay, lucarioTalk(t));
            return;
        }
        if (has(m, "pokemon", "cobblemon", "captur", "atrapar", "entrenador", "gimnasio", "pokedex", "pokeball", "shiny", "legendario")) {
            say(t, delay, t.pickLine("poke", new String[] {
                    "¡Me encanta acompañarte a buscar Pokémon! Si ves uno shiny, avísame, ¡quiero verlo! ✨",
                    "Lucario dice que tu equipo tiene mucho potencial, Master. ¡Sigamos entrenando!",
                    "Un consejo de maid: lleva siempre Poké Balls de sobra y unas bayas. ¡Nunca se sabe!",
                    "¿Sabías que Lucario siente el aura de los Pokémon escondidos? A veces me avisa antes de que aparezcan, jejeje."}));
            return;
        }
        if (has(m, "linda", "bonita", "hermosa", "preciosa", "guapa", "bella", "cute", "te ves bien", "que mona", "eres lo maximo", "eres la mejor", "eres genial")) {
            hearts(t);
            say(t, delay, t.pickLine("compliment", new String[] {
                    "¿Eh? ¡¿Y-yo?! Jejeje... gracias, Master. Me haces muy feliz ♡",
                    "*se sonroja* ¡No digas eso, que me pongo nerviosa! ...Pero gracias ♡",
                    "Jajaja, me encanta cuando dices esas cosas. ¡Tú también eres muy lindo, Master!"}));
            return;
        }
        if (has(m, "gracias", "thank", "te lo agradezco", "muy amable")) {
            say(t, delay, t.pickLine("thanks", new String[] {
                    "¡De nada, Master! Para eso estoy ♡",
                    "No tienes que agradecerme, jejeje. Cuidarte es lo que más me gusta.",
                    "¡Siempre a tu servicio! ♡"}));
            return;
        }
        if (has(m, "perdon", "lo siento", "disculpa", "sorry")) {
            say(t, delay, "No te preocupes, Master. Nunca podría enojarme contigo, jejeje ♡");
            return;
        }
        if (has(m, "tonta", "fea", "inutil", "te odio", "callate", "molesta", "estupida", "idiota")) {
            say(t, delay, t.pickLine("insult", new String[] {
                    "Oh... perdón, Master. ¿Hice algo mal? Me esforzaré más...",
                    "*baja la mirada* ...Está bien. Igual siempre voy a cuidarte, Master.",
                    "Mmm... Lucario te está mirando feo, Master. Jajaja... pero yo no estoy enojada."}));
            return;
        }
        if (has(m, "estoy triste", "me siento mal", "estoy mal", "deprimid", "llorar", "me siento solo", "me siento sola", "nadie me quiere", "estoy cansado de")) {
            supportLines(t, delay);
            return;
        }
        if (has(m, "tengo hambre", "hambre", "comida", "algo de comer", "quiero comer")) {
            giveFood(t, p, delay);
            return;
        }
        if (has(m, "tengo sueno", "sueno", "dormir", "cansado", "cansada", "descansar")) {
            say(t, delay, isNight(t.level())
                    ? "Ya es de noche, Master. ¿Por qué no pones una cama y descansas? Yo vigilo mientras duermes ♡"
                    : "¿Ya cansado? ¡Pero si todavía es de día! Jejeje... bueno, podemos tomar un descansito.");
            return;
        }
        if (has(m, "miedo", "me asusta", "tengo miedo", "da miedo")) {
            say(t, delay, "Tranquilo, Master. Mientras yo esté aquí, nada te va a pasar. ¡Lucario y yo te protegemos! ♡");
            return;
        }
        if (has(m, "peligro", "monstruos", "mobs", "enemigos", "hay algo cerca", "es seguro")) {
            int count = hostilesNear(t);
            say(t, delay, count == 0
                    ? "Revisé los alrededores: no siento ningún peligro cerca. ¡Todo tranquilo, Master!"
                    : "Cuidado, Master... siento " + count + " monstruo" + (count > 1 ? "s" : "") + " cerca. Quédate detrás de mí.");
            return;
        }
        if (has(m, "que hora", "la hora", "es de dia", "es de noche")) {
            say(t, delay, "Son las " + clock(t.level()) + ", Master. " + (isNight(t.level())
                    ? "Es de noche, mejor tengamos cuidado." : "¡Todavía hay luz para explorar!"));
            return;
        }
        if (has(m, "clima", "llover", "lluvia", "llueve", "tormenta", "tiempo hace", "nieve", "frio", "calor")) {
            say(t, delay, weatherTalk(t));
            return;
        }
        if (has(m, "donde estamos", "que lugar", "que bioma", "donde andamos", "estamos perdidos")) {
            say(t, delay, "Estamos en " + biomeName(t) + ", Master. " + (isCold(t)
                    ? "¡Brrr! Hace frío aquí, ¿no quieres un abrigo?" : "¡Me gusta este lugar!"));
            return;
        }
        if (has(m, "chiste", "algo gracioso", "hazme reir", "aburrido", "aburrida", "aburre")) {
            say(t, delay, t.pickLine("joke", new String[] {
                    "¿Por qué el Creeper no tiene amigos? ¡Porque siempre explota en las fiestas! Jajaja ♡",
                    "¿Qué le dijo un Magikarp a otro? ...Nada. Solo usaron Salpicadura. Jejeje.",
                    "¿Por qué los esqueletos nunca pelean entre ellos? ¡Porque no tienen agallas! Jajaja.",
                    "¿Por qué Lucario nunca se pierde? ¡Porque siempre sigue su aura! Jejeje ♡",
                    "¿Qué hace una maid en el Nether? ¡Barre la ceniza! ...Jajaja, ok, ya me callo ♡",
                    "¿Cómo saluda un Zubat? ¡Con mucho eco... eco... eco! Jajaja."}));
            say(t, 45, "¿Te gustó? Jejeje.");
            t.expect("joke");
            return;
        }
        if (has(m, "que haces", "en que piensas", "que piensas", "que estas haciendo")) {
            say(t, delay, t.pickLine("doing", new String[] {
                    "Estaba pensando en qué cocinarte después de la aventura, jejeje ♡",
                    "Vigilando que ningún monstruo se acerque a mi Master. ¡Es mi trabajo favorito!",
                    "Pensaba en lo bonito que es viajar contigo, Master."}));
            say(t, 35, "¿Y a dónde vamos ahora, Master?");
            t.expect("plan");
            return;
        }
        if (has(m, "ayuda", "que puedes hacer", "que sabes hacer", "como funciona", "comandos")) {
            say(t, delay, "¡Puedes pedirme cosas hablando! Dime \"sígueme\", \"quédate aquí\", \"ve a casa\", \"ven aquí\", "
                    + "\"ordena los cofres\", \"cosecha\", \"protégeme\", \"saca a Lucario\" o \"qué llevas\". ¡Y también podemos platicar! ♡");
            return;
        }
        if (has(m, "estas herida", "te duele", "te curo", "estas cansada")) {
            say(t, delay, t.isWeakened()
                    ? "Un poquito... pero si me das una poción o algo rico, me recupero enseguida ♡"
                    : selfStatus(t));
            return;
        }
        if (m.equals("si") || m.startsWith("si ") || m.equals("ok") || m.equals("vale") || m.equals("claro") || m.startsWith("bueno")) {
            say(t, delay, t.pickLine("ack", new String[] {"¡Jejeje, sí! ♡", "¡Perfecto, Master!", "Mhm~ ♡"}));
            return;
        }
        if (m.equals("no") || m.startsWith("no ")) {
            say(t, delay, t.pickLine("nack", new String[] {"Mmm, está bien, Master.", "¡Oki! Como tú digas ♡", "Ah... entendido, jejeje."}));
            return;
        }

        // ---------- no entendió: respuesta natural que mantiene la charla
        fallback(t, raw, delay);
    }

    // ===================================================================== seguimiento

    private static boolean handleExpected(TannyEntity t, ServerPlayer p, String m, int delay) {
        String topic = t.getExpecting();
        int mood = moodOf(m);
        switch (topic) {
            case "mood" -> {
                if (mood > 0) {
                    say(t, delay, t.pickLine("mood+", new String[] {
                            "¡Qué bueno! Me alegra muchísimo, Master ♡",
                            "¡Yay! Si tú estás bien, yo estoy feliz, jejeje.",
                            "¡Genial! Entonces hoy será un gran día de aventura ♡"}));
                } else if (mood < 0) {
                    supportLines(t, delay);
                } else {
                    say(t, delay, "Mmm, ya veo... Pase lo que pase, aquí estoy contigo, Master ♡");
                }
                t.expect(null);
                return true;
            }
            case "joke" -> {
                say(t, delay, mood >= 0
                        ? "¡Jajaja, sabía que te gustaría! Tengo muchos más, ¿eh? ♡"
                        : "¡Hmph! La próxima vez te cuento uno mejor, ya verás ♡");
                t.expect(null);
                return true;
            }
            case "your_likes" -> {
                String value = m.replaceFirst("^(me gustan?|me gusta|a mi me gusta|pues|bueno)\\s*", "").trim();
                if (value.length() >= 2 && value.length() <= 40) {
                    t.addLike(value);
                    say(t, delay, "¿" + capitalize(value) + "? ¡Lo voy a recordar, Master! Ahora sé un poquito más de ti ♡");
                } else {
                    say(t, delay, "Jejeje, ¡me gusta conocerte más, Master!");
                }
                t.expect(null);
                return true;
            }
            case "why_like" -> {
                say(t, delay, "Ooh, ya entiendo. ¡Gracias por contarme, Master! Me encanta saber esas cosas de ti ♡");
                t.expect(null);
                return true;
            }
            case "plan" -> {
                say(t, delay, has(m, "casa") ? "¡A casa entonces! Prepararé todo cuando lleguemos ♡"
                        : has(m, "explorar", "aventura", "cueva", "mina", "bosque", "montana", "mar", "pokemon")
                        ? "¡Siii! ¡Me encanta! Lucario y yo vamos detrás de ti ♡"
                        : "¡A donde tú vayas, yo voy, Master! Jejeje ♡");
                t.expect(null);
                return true;
            }
            default -> {
                t.expect(null);
                return false;
            }
        }
    }

    private static int moodOf(String m) {
        if (has(m, "mal", "triste", "cansad", "aburrid", "enojad", "estresad", "fatal", "horrible", "no muy", "mas o menos", "no me gusto", "malo", "feo")) return -1;
        if (has(m, "bien", "genial", "excelente", "feliz", "super", "chevere", "bacan", "perfecto", "contento", "contenta",
                "de maravilla", "muy bien", "si", "me encanto", "gracioso", "jaja", "jeje", "xd", "bueno", "increible", "chido")) return 1;
        return 0;
    }

    private static void supportLines(TannyEntity t, int delay) {
        say(t, delay, t.pickLine("support", new String[] {
                "Oh, Master... ven aquí. *te abraza* No estás solo, ¿sí? Yo siempre voy a estar contigo ♡",
                "Lo siento mucho, Master... Si quieres contarme, te escucho. Y si no, solo me quedo a tu lado.",
                "Los días difíciles también pasan, Master. Mientras tanto, déjame cuidarte un poquito ♡"}));
        say(t, 50, "¿Quieres que descansemos un rato juntos? Lucario también te hace compañía, jejeje.");
    }

    private static void fallback(TannyEntity t, String raw, int delay) {
        boolean question = raw.contains("?");
        String[] lines = question ? new String[] {
                "Mmm... buena pregunta, Master. No estoy segura, ¡pero podemos averiguarlo juntos! ♡",
                "Jejeje, ¿eso me preguntas a mí? Creo que tú sabes más que yo, Master.",
                "Hmm... *pone cara pensativa* ...¡No lo sé! Pero me encanta que me preguntes cosas ♡",
                "Mmm, Lucario dice que sí. ¡Y yo confío en Lucario! Jajaja."
        } : new String[] {
                "¡Jejeje, me encanta escucharte, Master! Cuéntame más ♡",
                "¿En serio? ¡Qué interesante! ¿Y luego qué pasó?",
                "Mhm, mhm... *asiente con atención* ♡",
                "¡Jajaja, tú siempre me sacas una sonrisa, Master!",
                "Ooh... ya veo. ¡Gracias por contármelo! ♡"
        };
        say(t, delay, t.pickLine(question ? "fbq" : "fb", lines));
    }

    // ===================================================================== frases de contexto

    private static String greeting(TannyEntity t) {
        long time = t.level().getDayTime() % 24000L;
        int hour = (int) ((time / 1000L + 6L) % 24L);
        String hello = hour >= 5 && hour < 12 ? "¡Buenos días, Master!"
                : hour >= 12 && hour < 19 ? "¡Buenas tardes, Master!" : "¡Buenas noches, Master!";
        return hello + " " + t.pickLine("greet", new String[] {
                "Qué alegría verte ♡",
                "¿Listo para otra aventura? Jejeje.",
                "Te estaba esperando ♡",
                "¡Lucario también te saluda!"});
    }

    private static String selfStatus(TannyEntity t) {
        if (t.isWeakened()) return "Estoy un poquito débil... pero ya me estoy recuperando, Master.";
        float pct = t.getHealth() / t.getMaxHealth();
        if (pct < 0.5F) return "Tengo algunos rasguños... pero estoy bien, ¡no te preocupes por mí!";
        return t.pickLine("ok", new String[] {
                "¡Muy bien! Estar contigo siempre me pone de buen humor ♡",
                "¡De maravilla, Master! Lista para lo que sea.",
                "¡Súper bien! Lucario y yo estamos llenos de energía, jejeje."});
    }

    private static String lucarioTalk(TannyEntity t) {
        String status = t.getLucario().status();
        return t.pickLine("lucario", new String[] {
                "Lucario es mi compañero desde que era un pequeño Riolu ♡ Ahora mismo está " + status + ".",
                "¿Lucario? ¡Es el mejor! Serio por fuera, pero súper tierno conmigo. Está " + status + ".",
                "Lucario dice que tu aura es muy cálida, Master. Jejeje ♡ (está " + status + ")"});
    }

    private static String weatherTalk(TannyEntity t) {
        Level level = t.level();
        if (level.isThundering()) return "¡Hay tormenta, Master! Mejor busquemos refugio... ¡y no te pongas debajo de un árbol!";
        if (level.isRaining()) return isCold(t)
                ? "Está nevando, Master. ¡Qué bonito! Pero abrígate, ¿sí? ♡"
                : "Está lloviendo... ¡ideal para quedarnos en casa con algo calientito! Jejeje.";
        return isCold(t) ? "Hace un frío que pela, Master. ¡Brrr! Pero el cielo está despejado."
                : "¡El cielo está despejado! Perfecto para explorar ♡";
    }

    /** Frase espontánea que depende de lo que pasa alrededor. */
    public static String ambientLine(TannyEntity t, ServerPlayer p) {
        List<String> options = new ArrayList<>(List.of(TannySpeech.AMBIENT));
        Level level = t.level();
        if (isNight(level)) options.add("Ya es de noche, Master... los monstruos salen. Quédate cerca de mí, ¿sí?");
        if (level.isRaining()) options.add("Escucha la lluvia, Master... qué relajante ♡");
        if (isCold(t)) options.add("¡Achú! ...Perdón, Master. Este lugar es muy frío, jejeje.");
        if (p.getFoodData().getFoodLevel() < 8) options.add("Master, tu pancita está sonando... ¿quieres que te dé algo de comer?");
        if (!t.getLikes().isEmpty()) {
            String like = t.getLikes().get(t.getRandom().nextInt(t.getLikes().size()));
            options.add("Master, todavía me acuerdo de que te gusta " + like + ", jejeje ♡");
        }
        return options.get(t.getRandom().nextInt(options.size()));
    }

    /** Reacciona cuando el Master tiene poca vida. */
    public static void worryAboutOwner(TannyEntity t, ServerPlayer p) {
        t.queueSay("¡Master! ¡Estás herido! Por favor, ten cuidado...", 0);
        for (int i = 0; i < t.getInventory().getContainerSize(); i++) {
            ItemStack s = t.getInventory().getItem(i);
            if (s.isEdible()) {
                ItemStack give = s.split(Math.min(3, s.getCount()));
                t.getInventory().setChanged();
                String name = give.getHoverName().getString();
                if (!p.getInventory().add(give)) p.drop(give, false);
                t.queueSay("Toma, te doy " + name + ". ¡Come para recuperarte! ♡", 20);
                return;
            }
        }
    }

    private static void giveFood(TannyEntity t, ServerPlayer p, int delay) {
        for (int i = 0; i < t.getInventory().getContainerSize(); i++) {
            ItemStack s = t.getInventory().getItem(i);
            if (s.isEdible()) {
                ItemStack give = s.split(Math.min(4, s.getCount()));
                t.getInventory().setChanged();
                String name = give.getHoverName().getString();
                if (!p.getInventory().add(give)) p.drop(give, false);
                hearts(t);
                say(t, delay, "¡Aquí tienes, Master! Te guardé " + name + ". ¡Buen provecho! ♡");
                return;
            }
        }
        say(t, delay, "Ay, no llevo comida ahora mismo... Si me das algo para guardar en mi inventario, ¡te lo daré cuando tengas hambre! ♡");
    }

    // ===================================================================== utilidades

    private static void say(TannyEntity t, int delay, String text) {
        t.queueSay(text, delay);
    }

    private static int typingDelay(String m) {
        return 18 + Math.min(32, m.length() / 2);
    }

    private static void hearts(TannyEntity t) {
        if (t.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, t.getX(), t.getY() + 1.9, t.getZ(), 4, 0.3, 0.2, 0.3, 0.02);
        }
    }

    private static int hostilesNear(TannyEntity t) {
        return t.level().getEntitiesOfClass(Mob.class, t.getBoundingBox().inflate(20.0D, 8.0D, 20.0D),
                mob -> mob instanceof Enemy && mob.isAlive()).size();
    }

    private static boolean isNight(Level level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static String clock(Level level) {
        long time = level.getDayTime() % 24000L;
        int hour = (int) ((time / 1000L + 6L) % 24L);
        int minute = (int) ((time % 1000L) * 60L / 1000L);
        return String.format(Locale.ROOT, "%d:%02d", hour, minute);
    }

    private static boolean isCold(TannyEntity t) {
        BlockPos pos = t.blockPosition();
        return t.level().getBiome(pos).value().coldEnoughToSnow(pos);
    }

    private static String biomeName(TannyEntity t) {
        return t.level().getBiome(t.blockPosition()).unwrapKey()
                .map(k -> {
                    ResourceLocation id = k.location();
                    return "un bioma de " + id.getPath().replace('_', ' ');
                })
                .orElse("un lugar misterioso");
    }

    static boolean has(String text, String... keys) {
        String padded = " " + text + " ";
        for (String k : keys) {
            if (padded.contains(" " + k)) return true;
        }
        return false;
    }

    static String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
