package fr.rulekeeper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Stocke les règles actives et les exemptions dans config/rulekeeper.json. */
public final class RuleManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("rulekeeper.json");

    public static class Data {
        public Set<String> enabled = new LinkedHashSet<>();
        public List<String> custom = new ArrayList<>();
        public Set<String> exempt = new HashSet<>();
        public boolean exemptOps = true;
    }

    private static Data data = new Data();

    private RuleManager() {}

    public static synchronized void load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    Data d = GSON.fromJson(r, Data.class);
                    if (d != null) data = d;
                }
            }
            if (data.enabled == null) data.enabled = new LinkedHashSet<>();
            if (data.custom == null) data.custom = new ArrayList<>();
            if (data.exempt == null) data.exempt = new HashSet<>();
        } catch (Exception e) {
            RuleKeeper.LOGGER.error("Impossible de lire {}", FILE, e);
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, w);
            }
        } catch (Exception e) {
            RuleKeeper.LOGGER.error("Impossible d'écrire {}", FILE, e);
        }
    }

    public static synchronized boolean isEnabled(Rule r) {
        return data.enabled.contains(r.id);
    }

    public static synchronized void setEnabled(Rule r, boolean on) {
        if (on) data.enabled.add(r.id); else data.enabled.remove(r.id);
        save();
    }

    public static synchronized List<String> customRules() {
        return new ArrayList<>(data.custom);
    }

    public static synchronized void addCustom(String text) {
        data.custom.add(text);
        save();
    }

    public static synchronized boolean removeCustom(int index) {
        if (index < 0 || index >= data.custom.size()) return false;
        data.custom.remove(index);
        save();
        return true;
    }

    public static synchronized void setExempt(java.util.UUID id, boolean exempt) {
        if (exempt) data.exempt.add(id.toString()); else data.exempt.remove(id.toString());
        save();
    }

    public static synchronized boolean isExemptOps() {
        return data.exemptOps;
    }

    public static synchronized void setExemptOps(boolean value) {
        data.exemptOps = value;
        save();
    }

    /** Vrai si la règle est active ET que ce joueur doit la respecter. */
    public static synchronized boolean applies(Player p, Rule r) {
        if (!isEnabled(r)) return false;
        if (data.exempt.contains(p.getUUID().toString())) return false;
        if (data.exemptOps && p.hasPermissions(2)) return false;
        return true;
    }
}
