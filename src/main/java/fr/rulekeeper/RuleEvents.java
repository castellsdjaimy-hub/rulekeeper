package fr.rulekeeper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Applique les règles en annulant les actions interdites. */
public class RuleEvents {

    private static void warn(Player p, Rule r) {
        p.displayClientMessage(Component.literal("§c✖ Règle : " + r.text), true);
    }

    @SubscribeEvent
    public void onToss(ItemTossEvent e) {
        Player p = e.getPlayer();
        if (p.level().isClientSide || !RuleManager.applies(p, Rule.PAS_JETER)) return;
        e.setCanceled(true);
        // Forge a déjà retiré l'objet de l'inventaire : on le rend au joueur.
        ItemStack stack = e.getEntity().getItem();
        p.getInventory().add(stack);
        warn(p, Rule.PAS_JETER);
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent e) {
        Player p = e.getPlayer();
        if (p == null || !RuleManager.applies(p, Rule.PAS_CASSER)) return;
        e.setCanceled(true);
        warn(p, Rule.PAS_CASSER);
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!RuleManager.applies(p, Rule.PAS_POSER)) return;
        e.setCanceled(true);
        warn(p, Rule.PAS_POSER);
    }

    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent e) {
        Player p = e.getEntity();
        if (!RuleManager.applies(p, Rule.PAS_RAMASSER)) return;
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        Player p = e.getEntity();
        if (!(e.getTarget() instanceof Player)) return;
        if (!RuleManager.applies(p, Rule.PAS_PVP)) return;
        e.setCanceled(true);
        warn(p, Rule.PAS_PVP);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent e) {
        Player p = e.getPlayer();
        if (!RuleManager.applies(p, Rule.PAS_CHAT)) return;
        e.setCanceled(true);
        warn(p, Rule.PAS_CHAT);
    }
}
