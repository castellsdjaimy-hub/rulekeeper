package fr.rulekeeper;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;

/** Les règles intégrées que l'admin peut activer ou désactiver. */
public enum Rule {
    PAS_JETER("pas_jeter", "Je ne dois pas jeter d'objet", Items.DROPPER),
    PAS_CASSER("pas_casser", "Je ne dois pas casser de bloc", Items.IRON_PICKAXE),
    PAS_POSER("pas_poser", "Je ne dois pas poser de bloc", Items.GRASS_BLOCK),
    PAS_RAMASSER("pas_ramasser", "Je ne dois pas ramasser d'objet au sol", Items.HOPPER),
    PAS_PVP("pas_pvp", "Je ne dois pas attaquer les autres joueurs", Items.IRON_SWORD),
    PAS_CHAT("pas_chat", "Je ne dois pas écrire dans le chat", Items.WRITABLE_BOOK);

    public final String id;
    public final String text;
    public final Item icon;

    Rule(String id, String text, Item icon) {
        this.id = id;
        this.text = text;
        this.icon = icon;
    }

    public static Optional<Rule> byId(String id) {
        for (Rule r : values()) {
            if (r.id.equalsIgnoreCase(id)) return Optional.of(r);
        }
        return Optional.empty();
    }
}
