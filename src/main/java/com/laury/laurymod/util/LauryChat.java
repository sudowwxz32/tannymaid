package com.laury.laurymod.util;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.entity.LauryResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversación con Laury (sin internet). Escribe en el chat empezando con "Laury"
 * (ej. "Laury, ¿cómo estás?"). Durante ~45 s puedes seguir hablando sin repetir su nombre.
 * Personalidad: enérgica, entusiasta, astuta, cariñosa, valiente, analítica, amigable, risueña, fuerte y dura.
 */
public final class LauryChat {

    private LauryChat() {}

    private static final Pattern NAME = Pattern.compile("\\b(laury|lauri|lori|laurie|laura)\\b");
    private static final Pattern LIKE = Pattern.compile("^(?:a mi |yo )?me gustan? (?:mucho |bastante )?(?:el |la |los |las )?(.{2,40})$");
    private static final Pattern FAVORITE = Pattern.compile("mi ([a-z]+) favorit[oa] es (?:el |la |los |las )?(.{2,40})$");
    private static final Pattern CALL_ME = Pattern.compile("(?:llamame|dime|puedes llamarme) ([a-z0-9 ]{2,20})$");

    public static void onChat(ServerPlayer player, String raw) {
        String text = normalize(raw);
        if (text.isEmpty()) return;
        boolean named = NAME.matcher(text).find();
        LauryEntity laury = LauryLookup.find(player);
        if (laury == null) return;
        boolean near = laury.level() == player.level() && laury.distanceToSqr(player) < 32 * 32;
        boolean riding = player.getVehicle() == laury;
        if (!named && !((laury.isChatting() || riding) && near)) return;
        String msg = NAME.matcher(text).replaceAll(" ").replaceAll("\\s+", " ").trim();
        laury.openChatWindow();
        reply(laury, player, msg, raw);
    }

    private static void reply(LauryEntity l, ServerPlayer p, String m, String raw) {
        int delay = 15 + Math.min(30, m.length() / 2);

        if (m.isEmpty()) {
            say(l, delay, l.pickLine("call", new String[] {"¿Sí, {p}? ¡Aquí estoy!", "¡Dime, dime! Soy toda oídos... y cuernos, jaja.", "¿Me llamabas? ¿Peleamos? ¿Volamos? ¡Lo que sea!"}));
            return;
        }
        if (l.getExpecting() != null && expected(l, m, delay)) return;

        // ---------- memoria
        Matcher call = CALL_ME.matcher(m);
        if (has(m, "llamame", "puedes llamarme") && call.find()) {
            String nick = cap(call.group(1).trim());
            l.setNickname(nick);
            say(l, delay, "¿" + nick + "? ¡Me gusta! Suena a alguien valiente. ¡Así te diré desde ahora, " + nick + "!");
            return;
        }
        if (has(m, "te quiero", "te amo", "me gustas", "te adoro", "eres mi novia", "se mi novia", "un beso", "dame un beso")) {
            hearts(l);
            say(l, delay, l.pickLine("love", new String[] {
                    "¿¡Eh!? ...J-jaja, ¡no digas esas cosas tan de golpe! ...Yo también te quiero, {p}. Pero no se lo digas a nadie.",
                    "*se le encienden las mejillas* ¡Tonto! ...Bueno, sí. Eres mi persona favorita. Ya, lo dije, jajaja.",
                    "Un dragón no se encariña con cualquiera... pero contigo hice una excepción. ¡Jaja!"}));
            return;
        }
        Matcher fav = FAVORITE.matcher(m);
        if (fav.find()) {
            String thing = fav.group(1), value = fav.group(2).trim();
            l.addLike(value + " (tu " + thing + " favorito)");
            say(l, delay, "¿Tu " + thing + " favorito es " + value + "? Interesante... lo anoto en mi memoria de dragón. ¡Nunca olvido nada!");
            say(l, 40, "¿Y por qué te gusta? Quiero analizarlo, jaja.");
            l.expect("why");
            return;
        }
        Matcher like = LIKE.matcher(m);
        if (like.find() && !has(m, "me gusta tu", "me gustas")) {
            String v = like.group(1).trim();
            l.addLike(v);
            say(l, delay, l.pickLine("like", new String[] {"¿" + cap(v) + "? ¡Buen gusto, {p}! Lo recordaré.", "Anotado: a {p} le gusta " + v + ". ¡Jaja, ya te voy conociendo!"}));
            return;
        }
        if (has(m, "que recuerdas", "que sabes de mi", "te acuerdas", "que me gusta")) {
            say(l, delay, l.getLikes().isEmpty() ? "Todavía no me has contado nada de ti, {p}. ¡Dime \"me gusta...\" y lo recordaré!"
                    : "¡Claro que me acuerdo! Te gusta: " + String.join(", ", l.getLikes()) + ". ¿Ves? Memoria de dragón, jajaja.");
            return;
        }

        // ---------- órdenes por chat
        boolean toHuman = has(m, "transformate en humana", "vuelve a humana", "vuelve a ser humana", "forma humana", "modo humana",
                "vuelve a la normalidad", "forma normal", "conviertete en humana", "humana otra vez");
        if (!toHuman && has(m, "transformate", "conviertete", "modo dragon", "forma de dragon", "se un dragon", "hazte dragon")) {
            l.transform(true, p); return;
        }
        if (toHuman) { l.transform(false, p); return; }
        if (has(m, "sigueme", "vamos", "vente", "acompaname")) { l.commandFollow(p); return; }
        if (has(m, "quedate", "espera aqui", "esperame", "no te muevas")) { l.commandStay(p); return; }
        if (has(m, "ven aqui", "ven aca", "ven rapido", "teletransportate") || m.equals("ven")) { l.commandTp(p); return; }
        if (has(m, "recolecta", "consigue", "traeme", "trae ", "junta", "busca", "saca ", "pica ", "corta", "caza")) {
            LauryResource res = null;
            for (String w : m.split(" ")) { res = LauryResource.byName(w); if (res != null) break; }
            if (res != null) { l.commandGather(p, res, firstNumber(m, 64)); return; }
        }
        if (has(m, "craftea", "fabrica", "fabricame", "hazme", "construye", "crea ", "creame", "forja")) {
            String obj = craftTarget(m);
            if (obj != null) { LauryCrafting.craft(l, p, obj, firstNumber(m, 1)); return; }
        }
        if (has(m, "dame carne", "quiero carne", "carne asada", "carne cocinada", "cocina carne")) { l.commandMeat(p); return; }
        if (has(m, "para ", "deten", "deja de", "ya basta", "suficiente") || m.equals("para")) {
            if (l.isGathering()) { l.commandStop(p); return; }
        }
        if (has(m, "inventario", "que llevas", "que tienes", "tu mochila")) {
            say(l, delay, "¡Mira, esto es lo que cargo!");
            l.openInventory(p);
            return;
        }

        // ---------- charla
        if (has(m, "adios", "chao", "chau", "bye", "nos vemos", "hasta luego", "me voy")) {
            say(l, delay, l.pickLine("bye", new String[] {"¡Nos vemos, {p}! No te metas en líos sin mí, jaja.", "¡Chao! Aquí estaré, afilando mis garras.", "¡Hasta luego! Si algo te ataca, solo grita mi nombre."}));
            l.closeChatWindow();
            return;
        }
        if (has(m, "buenos dias", "buenas tardes", "buenas noches", "hola", "holi", "hey", "que hubo", "buenas", "saludos", "hello")) {
            say(l, delay, greeting(l));
            if (l.getRandom().nextBoolean()) { say(l, 35, "¿Cómo estás hoy, {p}?"); l.expect("mood"); }
            return;
        }
        if (has(m, "como estas", "que tal", "como te sientes", "como te va", "estas bien", "todo bien")) {
            say(l, delay, self(l));
            say(l, 35, "¿Y tú qué tal, {p}?");
            l.expect("mood");
            return;
        }
        if (has(m, "quien eres", "como te llamas", "tu nombre", "que eres")) {
            say(l, delay, "Soy Laury, mujer dragón, 25 años, fuerte como una montaña y el doble de lista. ¡Y tu compañera, jaja!");
            return;
        }
        if (has(m, "cuantos anos", "tu edad", "que edad")) {
            say(l, delay, "25 años humanos... en años de dragón soy una bebé, jajaja. ¡Pero una bebé que tumba Wardens!");
            return;
        }
        if (has(m, "eres fuerte", "que tan fuerte", "cuanta vida", "puedes morir", "eres inmortal")) {
            say(l, delay, "Tengo la vida de un Warden: 500 puntos. Y no, no muero. Si me tumban, me levanto. ¡Siempre! Jajaja.");
            return;
        }
        if (has(m, "volar", "vuela", "montar", "subirme", "llevame volando")) {
            say(l, delay, isDragonTip(l));
            return;
        }
        if (has(m, "que te gusta", "tu comida favorita", "tu color favorito", "tus gustos", "tu favorito")) {
            say(l, delay, l.pickLine("herlikes", new String[] {"¡Volar al atardecer, la carne asada y una buena pelea! Jajaja.",
                    "Mi color favorito es el verde, como mi sudadera. Y el azul, obvio... ¡mira mis cuernos!",
                    "Me gusta resolver acertijos y analizar enemigos. Y los abrazos... pero eso es secreto."}));
            say(l, 35, "¿Y a ti qué te gusta, {p}?");
            l.expect("likes");
            return;
        }
        if (has(m, "linda", "bonita", "hermosa", "preciosa", "guapa", "bella", "cute", "eres la mejor", "eres genial", "increible")) {
            hearts(l);
            say(l, delay, l.pickLine("comp", new String[] {"¡Jajaja, ya lo sé! ...Pero gracias, {p}, me alegraste el día.",
                    "*mueve la cola* ¡N-no es que me guste que me lo digas! ...Bueno, sí me gusta, jaja.",
                    "¡Tú tampoco estás nada mal, {p}! Para ser humano, claro, jajaja."}));
            return;
        }
        if (has(m, "gracias", "te lo agradezco")) {
            say(l, delay, l.pickLine("thx", new String[] {"¡De nada! Para eso están las compañeras dragón.", "¡Cuando quieras, {p}!", "Jaja, me debes una carne asada."}));
            return;
        }
        if (has(m, "perdon", "lo siento", "disculpa")) { say(l, delay, "Tranqui, {p}. Ya está olvidado. Los dragones no guardamos rencor... casi nunca, jaja."); return; }
        if (has(m, "tonta", "fea", "inutil", "te odio", "callate", "estupida", "idiota", "debil")) {
            say(l, delay, l.pickLine("ins", new String[] {"¿Ah sí? Repítelo cuando esté en forma de dragón, jajaja. ...No, en serio, ¿estás bien?",
                    "Tsk. Me han dicho cosas peores dragones más grandes. Igual te voy a seguir cuidando.",
                    "Hmph. Soy dura, eso no me afecta. Pero si algo te molesta, dímelo de frente."}));
            return;
        }
        if (has(m, "estoy triste", "me siento mal", "estoy mal", "deprimid", "llorar", "me siento solo", "me siento sola")) {
            support(l, delay);
            return;
        }
        if (has(m, "tengo hambre", "hambre", "algo de comer")) { l.commandMeat(p); return; }
        if (has(m, "miedo", "me asusta", "tengo miedo")) { say(l, delay, "¿Miedo? ¡Tienes a un dragón de tu lado! Nada te va a pasar mientras yo respire fuego."); return; }
        if (has(m, "peligro", "monstruos", "enemigos", "hay algo cerca", "es seguro")) {
            int n = hostiles(l);
            say(l, delay, n == 0 ? "Analicé los alrededores: cero amenazas. ¡Zona segura!"
                    : "Detecto " + n + " monstruo" + (n > 1 ? "s" : "") + " cerca. ¡Quédate detrás de mí, esto va a estar divertido!");
            return;
        }
        if (has(m, "que hora", "la hora", "es de dia", "es de noche")) {
            say(l, delay, "Son las " + clock(l.level()) + ". " + (night(l.level()) ? "De noche salen los monstruos... ¡perfecto para entrenar!" : "¡Todavía hay sol para aventuras!"));
            return;
        }
        if (has(m, "clima", "lluvia", "llueve", "tormenta", "nieve", "frio", "calor")) { say(l, delay, weather(l)); return; }
        if (has(m, "chiste", "algo gracioso", "hazme reir", "aburrido", "aburrida")) {
            say(l, delay, l.pickLine("joke", new String[] {
                    "¿Qué le dijo un dragón a un caballero en armadura? ...¡Comida enlatada! Jajajaja.",
                    "¿Por qué los dragones duermen de día? ¡Porque les gusta pelear con los caballeros! Jaja, ¿entiendes? Caba-llero-noche... ok no.",
                    "¿Sabes por qué el Creeper no tiene amigos? Porque cada vez que se emociona... ¡BOOM! Jajaja.",
                    "Un esqueleto entra a un bar y pide una cerveza... y un trapeador. Jajaja."}));
            say(l, 45, "¿Y? ¿Te reíste o te lanzo fuego?");
            l.expect("joke");
            return;
        }
        if (has(m, "que haces", "en que piensas", "que piensas")) {
            say(l, delay, l.pickLine("doing", new String[] {"Analizando cuál sería la mejor ruta para explorar. ¡Y pensando en carne asada!",
                    "Vigilando. Siempre vigilando. Los dragones nunca bajamos la guardia, jaja.", "Pensaba en lo divertido que es viajar contigo."}));
            say(l, 35, "¿A dónde vamos ahora, {p}?");
            l.expect("plan");
            return;
        }
        if (has(m, "ayuda", "que puedes hacer", "que sabes hacer", "comandos")) {
            say(l, delay, "Puedes pedirme: \"sígueme\", \"quédate\", \"ven aquí\", \"transfórmate en dragón\", \"vuelve a humana\", "
                    + "\"recolecta 30 de hierro\", \"caza carne\", \"craftea un pico de hierro\", \"dame carne\"... ¡o solo platicar, jaja!");
            return;
        }
        if (m.equals("si") || m.startsWith("si ") || m.equals("ok") || m.equals("vale") || m.equals("claro")) { say(l, delay, l.pickLine("ack", new String[] {"¡Eso!", "¡Jaja, perfecto!", "¡Así me gusta!"})); return; }
        if (m.equals("no") || m.startsWith("no ")) { say(l, delay, l.pickLine("nack", new String[] {"Ok, ok, como digas.", "Hmm, entendido.", "¡Tú te lo pierdes, jaja!"})); return; }

        boolean q = raw.contains("?");
        say(l, delay, l.pickLine(q ? "fbq" : "fb", q ? new String[] {
                "Hmm... déjame analizarlo... ¡No tengo idea! Pero suena interesante, jaja.",
                "Buena pregunta. Mi instinto de dragón dice que sí... aunque mi instinto también me dice que coma ovejas.",
                "No lo sé, {p}. ¡Pero lo averiguamos juntos!"
        } : new String[] {"¡Jaja, en serio? Cuéntame más!", "Ajá, ajá... *asiente con la cola*", "¡Me encanta cuando me cuentas cosas, {p}!", "Interesante... muy interesante."}));
    }

    private static boolean expected(LauryEntity l, String m, int delay) {
        String t = l.getExpecting();
        int mood = mood(m);
        switch (t) {
            case "mood" -> {
                if (mood > 0) say(l, delay, l.pickLine("m+", new String[] {"¡Así me gusta! ¡Energía al máximo!", "¡Genial! Entonces hoy conquistamos el mundo, jaja."}));
                else if (mood < 0) support(l, delay);
                else say(l, delay, "Hmm, ok. Si algo te preocupa, aquí está tu dragona favorita.");
            }
            case "joke" -> say(l, delay, mood >= 0 ? "¡Jajaja, sabía que te gustaría! Soy graciosa Y fuerte." : "¡Hmph! La próxima vez te reirás, ya verás.");
            case "likes" -> {
                String v = m.replaceFirst("^(me gustan?|a mi me gusta|pues|bueno)\\s*", "").trim();
                if (v.length() >= 2 && v.length() <= 40) { l.addLike(v); say(l, delay, "¿" + cap(v) + "? ¡Anotado! Ahora te conozco un poco más, {p}."); }
                else say(l, delay, "¡Jaja, me gusta conocerte!");
            }
            case "why" -> say(l, delay, "Ya veo... tiene lógica. ¡Gracias por contármelo!");
            case "plan" -> say(l, delay, has(m, "casa") ? "¡A casa! Prepararé carne asada cuando lleguemos."
                    : has(m, "explorar", "aventura", "cueva", "mina", "bosque", "montana", "pelear", "volar")
                    ? "¡SÍ! ¡Esa es la actitud! ¡Vamos!" : "¡A donde tú digas, yo voy!");
            default -> { l.expect(null); return false; }
        }
        l.expect(null);
        return true;
    }

    private static void support(LauryEntity l, int delay) {
        say(l, delay, l.pickLine("sup", new String[] {
                "Oye... ven aquí. *te envuelve con sus alas* No estás solo, {p}. Tienes a un dragón de tu lado.",
                "Los días malos también pasan. Y mientras tanto, yo te cuido. Eso no se negocia.",
                "Hey, eres más fuerte de lo que crees. Lo he visto. Pero hoy puedes descansar, yo vigilo."}));
        say(l, 50, "¿Quieres dar una vuelta volando? A mí siempre me ayuda a despejarme.");
    }

    private static int mood(String m) {
        if (has(m, "mal", "triste", "cansad", "aburrid", "enojad", "estresad", "fatal", "horrible", "mas o menos", "malo")) return -1;
        if (has(m, "bien", "genial", "excelente", "feliz", "super", "chevere", "bacan", "perfecto", "contento", "contenta", "si", "jaja", "jeje", "xd", "bueno", "increible")) return 1;
        return 0;
    }

    private static String greeting(LauryEntity l) {
        long t = l.level().getDayTime() % 24000L;
        int h = (int) ((t / 1000L + 6L) % 24L);
        String hi = h >= 5 && h < 12 ? "¡Buenos días, {p}!" : h < 19 ? "¡Buenas tardes, {p}!" : "¡Buenas noches, {p}!";
        return hi + " " + l.pickLine("greet", new String[] {"¡Lista para la acción!", "¿Qué aventura toca hoy?", "¡Jaja, te estaba esperando!"});
    }

    private static String self(LauryEntity l) {
        if (l.isWeakened()) return "Un poco golpeada... pero ya me estoy levantando. ¡Nada me detiene!";
        if (l.getHealth() < l.getMaxHealth() * 0.5F) return "Tengo algunos rasguños, pero estoy entera. ¡Soy dura!";
        return l.pickLine("ok", new String[] {"¡De maravilla! Con energía para pelear contra diez Creepers.", "¡Súper bien! ¿Ves esta sonrisa? ¡Jaja!", "¡Mejor que nunca!"});
    }

    private static String isDragonTip(LauryEntity l) {
        return l.isDragon() ? "¡Súbete! Clic derecho sobre mí. Avanza hacia donde mires: arriba para subir, abajo para bajar. Shift para bajarte."
                : "¡Claro! Dime \"transfórmate en dragón\" o usa /laury dragon, y luego súbete con clic derecho. ¡El cielo nos espera!";
    }

    private static String weather(LauryEntity l) {
        Level lv = l.level();
        if (lv.isThundering()) return "¡Tormenta eléctrica! Los dragones azules adoramos los rayos... pero tú mejor busca refugio, jaja.";
        if (lv.isRaining()) return cold(l) ? "Está nevando. ¡Mi aliento de fuego nos calentará!" : "Llueve. Mis alas se mojan, pero no me importa, jaja.";
        return "Cielo despejado. ¡Perfecto para volar!";
    }

    public static String ambientLine(LauryEntity l, ServerPlayer p) {
        List<String> o = new ArrayList<>(List.of(LaurySpeech.AMBIENT));
        if (night(l.level())) o.add("Ya es de noche... quédate cerca, {p}. Los monstruos no saben con quién se meten.");
        if (l.level().isRaining()) o.add("Me encanta el olor a lluvia.");
        if (p.getFoodData().getFoodLevel() < 8) o.add("Escucho tu estómago desde aquí, jaja. ¿Te doy carne? Solo di \"Laury, dame carne\".");
        if (!l.getLikes().isEmpty()) o.add("Oye {p}, aún recuerdo que te gusta " + l.getLikes().get(l.getRandom().nextInt(l.getLikes().size())) + ", jaja.");
        return o.get(l.getRandom().nextInt(o.size()));
    }

    public static void worry(LauryEntity l, ServerPlayer p) {
        l.queueSay("¡{p}! ¡Estás muy herido! ¡Retrocede, yo me encargo!", 0);
        for (int i = 0; i < l.getInventory().getContainerSize(); i++) {
            ItemStack s = l.getInventory().getItem(i);
            if (s.isEdible()) {
                ItemStack give = s.split(Math.min(3, s.getCount()));
                l.getInventory().setChanged();
                String name = give.getHoverName().getString();
                if (!p.getInventory().add(give)) p.drop(give, false);
                l.queueSay("Toma " + name + ". ¡Come, rápido!", 20);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- utilidades
    private static void say(LauryEntity l, int delay, String text) { l.queueSay(text, delay); }

    private static void hearts(LauryEntity l) {
        if (l.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HEART, l.getX(), l.getY() + l.getBbHeight() + 0.2, l.getZ(), 4, 0.3, 0.2, 0.3, 0.02);
    }

    private static int hostiles(LauryEntity l) {
        return l.level().getEntitiesOfClass(Mob.class, l.getBoundingBox().inflate(24.0D, 8.0D, 24.0D), m -> m instanceof Enemy && m.isAlive()).size();
    }

    private static boolean night(Level lv) { long t = lv.getDayTime() % 24000L; return t >= 13000L && t < 23000L; }

    private static String clock(Level lv) {
        long t = lv.getDayTime() % 24000L;
        return String.format(Locale.ROOT, "%d:%02d", (int) ((t / 1000L + 6L) % 24L), (int) ((t % 1000L) * 60L / 1000L));
    }

    private static boolean cold(LauryEntity l) {
        BlockPos pos = l.blockPosition();
        return l.level().getBiome(pos).value().coldEnoughToSnow(pos);
    }

    private static int firstNumber(String m, int def) {
        for (String w : m.split(" ")) if (w.matches("\\d{1,3}")) return Math.max(1, Math.min(64, Integer.parseInt(w)));
        return def;
    }

    @Nullable
    private static String craftTarget(String m) {
        if (has(m, "mazo", "martillo")) return "mazo";
        if (has(m, "guadana")) return "guadana";
        if (has(m, "espada") && has(m, "fuego")) return "espada_fuego";
        if (has(m, "espada") && has(m, "fuerza", "salto")) return "espada_fuerza";
        String[] tools = {"pico", "hacha", "pala", "espada", "azada"};
        String[] mats = {"madera", "piedra", "hierro", "oro", "diamante"};
        for (String t : tools) if (has(m, t)) { for (String mt : mats) if (has(m, mt)) return t + "_" + mt; return t + "_madera"; }
        String[] armor = {"casco", "peto", "pantalones", "botas"};
        String[] amats = {"cuero", "hierro", "oro", "diamante"};
        for (String a : armor) if (has(m, a)) { for (String mt : amats) if (has(m, mt)) return a + "_" + mt; return a + "_hierro"; }
        if (has(m, "palo")) return "palos";
        if (has(m, "tablon")) return "tablones";
        if (has(m, "escudo")) return "escudo";
        if (has(m, "arco")) return "arco";
        if (has(m, "flecha")) return "flechas";
        if (has(m, "antorcha")) return "antorchas";
        if (has(m, "mesa")) return "mesa";
        if (has(m, "cofre")) return "cofre";
        if (has(m, "horno")) return "horno";
        return null;
    }

    static boolean has(String text, String... keys) {
        String padded = " " + text + " ";
        for (String k : keys) if (padded.contains(" " + k)) return true;
        return false;
    }

    static String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String cap(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
}
