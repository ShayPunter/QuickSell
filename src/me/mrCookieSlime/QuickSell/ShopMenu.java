package me.mrCookieSlime.QuickSell;

import io.github.thebusybiscuit.cscorelib2.inventory.ChestMenu;
import io.github.thebusybiscuit.cscorelib2.inventory.ClickAction;
import io.github.thebusybiscuit.cscorelib2.inventory.MenuClickHandler;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.handlers.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ShopMenu {

	/**
	 * Opens a shop menu for a player
	 * @param p Player
	 * @param shop Shop
	 */
	public static void open(Player p, Shop shop) {
		if (shop.hasUnlocked(p)) {
			Inventory inv = Bukkit.createInventory(null, 9 * QuickSell.cfg.getInt("options.sell-gui-rows"), ChatColor.translateAlternateColorCodes('&', QuickSell.local.getMessage("menu.title")));
			if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 9, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 8, new CustomItem(Material.LIME_STAINED_GLASS_PANE, QuickSell.local.getMessage("menu.accept")));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 7, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
				
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 6, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 5, new CustomItem(Material.YELLOW_STAINED_GLASS_PANE, QuickSell.local.getMessage("menu.estimate")));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 4, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
				
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 3, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 2, new CustomItem(Material.RED_STAINED_GLASS_PANE, QuickSell.local.getMessage("menu.cancel")));
				inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 1, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
			}
			p.openInventory(inv);
			QuickSell.shop.put(p.getUniqueId(), shop);
			return;
		}

		QuickSell.local.sendMessage(p, "messages.no-access", false);
	}

	/**
	 * Opens a shop menu based on the hierarchy
	 * @param p Player
	 */
	public static void openMenu(Player p) {
		if (QuickSell.cfg.getBoolean("shop.enable-hierarchy")) {
			if (Shop.getHighestShop(p) != null) {
				open(p, Shop.getHighestShop(p));
				return;
			}
			QuickSell.local.sendMessage(p, "messages.no-access", false);
			return;
		}

		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), QuickSell.local.getMessage("menu.title"));

		for (int i = 0; i < Shop.list().size(); i++) {
			if (Shop.list().get(i) != null) {
				final Shop shop = Shop.list().get(i);
				menu.addItem(i, shop.getItem(shop.hasUnlocked(p) ? ShopStatus.UNLOCKED: ShopStatus.LOCKED));
				menu.addMenuClickHandler(i, (player, i1, itemStack, itemStack1, clickAction) -> {
					ShopMenu.open(p, shop);
					return false;
				});
			}
		}
		menu.open(p);

	}
	
	final static int shop_size = 45;

	/**
	 * Opens a GUI displaying shop items and prices to a player
	 * @param p Player
	 * @param shop Shop
	 * @param page Integer
	 */
	public static void openPrices(Player p, final Shop shop, final int page) {
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(),"Shop Prices");
		
		menu.addMenuOpeningHandler(p1 -> p1.playSound(p1.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F));
		
		int index = 0;
		final int pages = shop.getPrices().getInfo().size() / shop_size + 1;
		
		for (int i = 45; i < 54; i++) {
			menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
			menu.addMenuClickHandler(i, (player, i1, itemStack, itemStack1, clickAction) -> false);
		}
		
		menu.addItem(46, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&r\u21E6 Previous Page", "", "&7(" + page + " / " + pages + ")"));
		menu.addMenuClickHandler(46, (player, i, itemStack, itemStack1, clickAction) -> {
			int next = page - 1;
			if (next < 1) next = pages;
			if (next != page) openPrices(p, shop, next);
			return false;
		});
		
		menu.addItem(52, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&rNext Page \u21E8", "", "&7(" + page + " / " + pages + ")"));
		menu.addMenuClickHandler(52, (player, i, itemStack, itemStack1, clickAction) -> {
			int next = page + 1;
			if (next > pages) next = 1;
			if (next != page) openPrices(p, shop, next);
			return false;
		});
		
		int shop_index = shop_size * (page - 1);
		
		for (int i = 0; i < shop_size; i++) {
			int target = shop_index + i;
			if (target >= shop.getPrices().getItems().size()) break;
			else {
				final String string = shop.getPrices().getItems().get(target);
				final ItemStack item = shop.getPrices().getItem(string);
				menu.addItem(index, item);
				menu.addMenuClickHandler(index, (player, i12, itemStack, itemStack1, clickAction) -> false);
				index++;
			}
			
		}
		
		menu.open(p);
	}

}
