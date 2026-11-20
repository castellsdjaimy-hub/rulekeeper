package fr.rulekeeper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * /regles                      -> affiche les règles (tout le monde)
 * /regles activer <regle>      -> admin
 * /regles desactiver <regle>   -> admin
 * /regles ajouter <texte>      -> admin (règle libre, affichée seulement)
 * /regles retirer <numero>     -> admin
 * /regles exempter <joueur>    -> admin
 * /regles retablir <joueur>    -> admin
 * /regles ops <true|false>     -> admin (les ops sont-ils exemptés ?)
 */
public final class RuleCommand {

    private static final SuggestionProvider<CommandSourceStack> RULE_IDS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(Rule.values()).map(r -> r.id), builder);

    private RuleCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("regles")
                .executes(c -> list(c.getSource()))

                .then(Commands.literal("menu")
                        .executes(c -> {
                            RulesMenu.open(c.getSource().getPlayerOrException());
                            return 1;
                        }))

                .then(Commands.literal("activer").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("regle", StringArgumentType.word()).suggests(RULE_IDS)
                                .executes(c -> toggle(c.getSource(), StringArgumentType.getString(c, "regle"), true))))

                .then(Commands.literal("desactiver").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("regle", StringArgumentType.word()).suggests(RULE_IDS)
                                .executes(c -> toggle(c.getSource(), StringArgumentType.getString(c, "regle"), false))))

                .then(Commands.literal("ajouter").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("texte", StringArgumentType.greedyString())
                                .executes(c -> addCustom(c.getSource(), StringArgumentType.getString(c, "texte")))))

                .then(Commands.literal("retirer").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("numero", IntegerArgumentType.integer(1))
                                .executes(c -> removeCustom(c.getSource(), IntegerArgumentType.getInteger(c, "numero")))))

                .then(Commands.literal("exempter").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .executes(c -> exempt(c.getSource(), EntityArgument.getPlayer(c, "joueur"), true))))

                .then(Commands.literal("retablir").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .executes(c -> exempt(c.getSource(), EntityArgument.getPlayer(c, "joueur"), false))))

                .then(Commands.literal("ops").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("exemptes", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean v = BoolArgumentType.getBool(c, "exemptes");
                                    RuleManager.setExemptOps(v);
                                    reply(c.getSource(), "§aLes opérateurs " + (v ? "sont" : "ne sont plus") + " exemptés des règles.");
                                    return 1;
                                }))));
    }

    private static void reply(CommandSourceStack src, String text) {
        src.sendSuccess(() -> Component.literal(text), false);
    }

    private static int list(CommandSourceStack src) {
        StringBuilder sb = new StringBuilder("§6§l=== Règles du serveur ===");
        int n = 0;
        for (Rule r : Rule.values()) {
            if (RuleManager.isEnabled(r)) {
                sb.append("\n§e- ").append(r.text);
                n++;
            }
        }
        List<String> custom = RuleManager.customRules();
        for (int i = 0; i < custom.size(); i++) {
            sb.append("\n§e").append(i + 1).append(". ").append(custom.get(i));
            n++;
        }
        if (n == 0) sb.append("\n§7Aucune règle pour le moment.");
        reply(src, sb.toString());
        return n;
    }

    private static int toggle(CommandSourceStack src, String id, boolean on) {
        var rule = Rule.byId(id);
        if (rule.isEmpty()) {
            src.sendFailure(Component.literal("§cRègle inconnue : " + id + ". Règles disponibles : "
                    + String.join(", ", Arrays.stream(Rule.values()).map(r -> r.id).toList())));
            return 0;
        }
        setAndAnnounce(src.getServer(), rule.get(), on);
        return 1;
    }

    /** Active ou désactive une règle et prévient tout le serveur (utilisé aussi par le menu). */
    public static void setAndAnnounce(net.minecraft.server.MinecraftServer server, Rule r, boolean on) {
        RuleManager.setEnabled(r, on);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal(on ? "§6[Règles] §eNouvelle règle : §f" + r.text
                                     : "§6[Règles] §7Règle retirée : " + r.text), false);
    }

    private static int addCustom(CommandSourceStack src, String text) {
        RuleManager.addCustom(text);
        src.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§6[Règles] §eNouvelle règle : §f" + text), false);
        return 1;
    }

    private static int removeCustom(CommandSourceStack src, int number) {
        if (!RuleManager.removeCustom(number - 1)) {
            src.sendFailure(Component.literal("§cAucune règle libre numéro " + number));
            return 0;
        }
        reply(src, "§aRègle " + number + " retirée.");
        return 1;
    }

    private static int exempt(CommandSourceStack src, ServerPlayer p, boolean exempt) {
        RuleManager.setExempt(p.getUUID(), exempt);
        reply(src, "§a" + p.getName().getString() + (exempt ? " est exempté des règles." : " doit de nouveau respecter les règles."));
        return 1;
    }
}
