package me.mrCookieSlime.QuickSell.listeners;

import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent.Type;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {

	@EventHandler
	public void onNPCInteract(NPCRightClickEvent e) {
		NPC npc = e.getNPC();
		if (QuickSell.getInstance().npcs.contains(String.valueOf(npc.getId()))) {
			String action = QuickSell.getInstance().npcs.getString(String.valueOf(npc.getId()));
			Shop shop = Shop.getShop(action.split(" ; ")[0]);
			if (shop == null) {
				QuickSell.local.sendMessage(e.getClicker(), "messages.unknown-shop", false);
				return;
			}

			if (action.split(" ; ")[1].equalsIgnoreCase("SELL")) {
				ShopMenu.open(e.getClicker(), shop);
				return;
			}

			if (shop.hasUnlocked(e.getClicker())) {
				shop.sellall(e.getClicker(), "", Type.CITIZENS);
				return;
			}

			QuickSell.local.sendMessage(e.getClicker(), "messages.no-access", false);
		}
	}
	
	@EventHandler
	public void onDamage(NPCDamageByEntityEvent e) {
		NPC npc = e.getNPC();
		Entity damager = e.getDamager();
		if (damager instanceof Player && QuickSell.getInstance().npcs.contains(String.valueOf(npc.getId()))) {
			Player p = (Player) damager;
			String action = QuickSell.getInstance().npcs.getString(String.valueOf(npc.getId()));
			Shop shop = Shop.getShop(action.split(" ; ")[0]);

			if (shop == null) {
				QuickSell.local.sendMessage(p, "messages.unknown-shop", false);
				return;
			}

			shop.showPrices(p);
		}

	}
}
