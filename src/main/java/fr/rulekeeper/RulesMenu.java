package fr.rulekeeper;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Menu coffre (3 lignes) côté serveur : les clients n'ont pas besoin du mod.
 * Ligne 1 : l'icône de chaque règle. Ligne 2 : un vitrage vert (active) ou rouge (inactive).
 * Cliquer sur l'icône ou sur le vitrage active / désactive la règle (admins seulement).
 */
public class RulesMenu extends ChestMenu {
    private static final int SIZE = 27;
    private static final int FIRST_ICON = 2;    // slots 2..7
    private static final int FIRST_PANE = 11;   // slots 11..16
    private static final int OPS_SLOT = 22;

    private final SimpleContainer box;

    private RulesMenu(int id, Inventory inv, SimpleContainer box) {
        super(MenuType.GENERIC_9x3, id, inv, box, 3);
        this.box = box;
        refresh();
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new RulesMenu(id, inv, new SimpleContainer(SIZE)),
                Component.literal("§6Règles du serveur")));
    }

    private void refresh() {
        ItemStack filler = named(new ItemStack(Items.GRAY_STAINED_GLASS_PANE), " ");
        for (int i = 0; i < SIZE; i++) box.setItem(i, filler.copy());

        Rule[] rules = Rule.values();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];
            boolean on = RuleManager.isEnabled(r);
            String hint = "§8Clique pour " + (on ? "désactiver" : "activer");

            ItemStack icon = named(new ItemStack(r.icon), (on ? "§a" : "§7") + r.text);
            lore(icon, on ? "§aActive" : "§cInactive", hint);
            box.setItem(FIRST_ICON + i, icon);

            ItemStack pane = named(new ItemStack(on ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE),
                    on ? "§aActive" : "§cInactive");
            lore(pane, hint);
            box.setItem(FIRST_PANE + i, pane);
        }

        boolean ops = RuleManager.isExemptOps();
        ItemStack opsItem = named(new ItemStack(Items.COMMAND_BLOCK), "§eOps exemptés : " + (ops ? "§aoui" : "§cnon"));
        lore(opsItem, "§8Les opérateurs peuvent-ils ignorer les règles ?", "§8Clique pour changer");
        box.setItem(OPS_SLOT, opsItem);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SIZE && player instanceof ServerPlayer sp) {
            handleClick(slotId, sp);
        }
        // Rien ne doit jamais être déplacé : on ignore le clic et on resynchronise le client.
        sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void handleClick(int slot, ServerPlayer p) {
        int count = Rule.values().length;
        int idx = -1;
        if (slot >= FIRST_ICON && slot < FIRST_ICON + count) idx = slot - FIRST_ICON;
        else if (slot >= FIRST_PANE && slot < FIRST_PANE + count) idx = slot - FIRST_PANE;
        if (idx < 0 && slot != OPS_SLOT) return;

        if (!p.hasPermissions(2)) {
            p.displayClientMessage(Component.literal("§cSeuls les admins peuvent modifier les règles."), true);
            return;
        }

        if (slot == OPS_SLOT) {
            RuleManager.setExemptOps(!RuleManager.isExemptOps());
        } else {
            Rule r = Rule.values()[idx];
            RuleCommand.setAndAnnounce(p.server, r, !RuleManager.isEnabled(r));
        }
        p.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6f, 1.2f);
        refresh();
    }

    private static ItemStack named(ItemStack stack, String name) {
        stack.setHoverName(Component.literal(name).withStyle(s -> s.withItalic(false)));
        return stack;
    }

    private static void lore(ItemStack stack, String... lines) {
        ListTag list = new ListTag();
        for (String line : lines) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal(line).withStyle(s -> s.withItalic(false)))));
        }
        stack.getOrCreateTagElement("display").put("Lore", list);
    }
}
