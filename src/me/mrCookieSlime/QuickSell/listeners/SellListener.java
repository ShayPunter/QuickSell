package me.mrCookieSlime.QuickSell.listeners;

import java.util.ArrayList;
import java.util.List;

import io.github.thebusybiscuit.cscorelib2.inventory.InvUtils;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.SellProfile;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import me.mrCookieSlime.QuickSell.SellEvent.Type;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SellListener implements Listener {
	
	@EventHandler
	public void onSignCreate(SignChangeEvent e) {
		// SELL SIGN
		String prefix = ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix"));
		if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {

			if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
				e.setLine(0, prefix);
				return;
			}

			e.setCancelled(true);
			QuickSell.local.sendMessage(e.getPlayer(), "messages.no-permission", false);
		}

		// SELLALL SIGN
		prefix = ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix"));
		if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {
			if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
				e.setLine(0, prefix);
				return;
			}

			e.setCancelled(true);
			QuickSell.local.sendMessage(e.getPlayer(), "messages.no-permission", false);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getState() instanceof Sign) {

				// IF SIGN IS [SELL] OPEN SELL MENU
				Sign sign = (Sign) e.getClickedBlock().getState();
				if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
					Shop shop = Shop.getShop(sign.getLine(1));
					if (shop != null) {
						ShopMenu.open(e.getPlayer(), shop);
						return;
					}

					ShopMenu.openMenu(e.getPlayer());
					e.setCancelled(true);
					return;
				}

				// IF SIGN IS [SELLALL] THEN SELL ALL INVENTORY
				if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
					Shop shop = Shop.getShop(sign.getLine(1));
					if (shop != null) {
						if (shop.hasUnlocked(e.getPlayer())) {
							String item = sign.getLine(2);
							item = item.toUpperCase();

							if (item.contains(" ")) {
								item = item.replace(" ", "_");
							}

							shop.sellall(e.getPlayer(), item, Type.SELLALL);
							return;
						}
						QuickSell.local.sendMessage(e.getPlayer(), "messages.no-access", false);
						return;
					}

					if (Shop.getHighestShop(e.getPlayer()) != null) {
						String item = sign.getLine(2);
						item = item.toUpperCase();

						if (item.contains(" ")) {
							item = item.replace(" ", "_");
						}

						Shop.getHighestShop(e.getPlayer()).sellall(e.getPlayer(), item, Type.SELLALL);
					} else {
						QuickSell.local.sendMessage(e.getPlayer(), "messages.unknown-shop", false);
						return;
					}

					e.setCancelled(true);
				}
			}
			return;
		}

		// SHOW PRICES
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) e.getClickedBlock().getState();

				// IF SIGN IS A [SELL] SIGN
				if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
					Shop shop = Shop.getShop(sign.getLine(1));
					if (shop != null) {
						shop.showPrices(e.getPlayer());
						return;
					}

					if (Shop.getHighestShop(e.getPlayer()) != null) {
						Shop.getHighestShop(e.getPlayer()).showPrices(e.getPlayer());
						return;
					}
					return;
				}

				// IF SIGN IS A [SELLALL] SIGN
				if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
					Shop shop = Shop.getShop(sign.getLine(1));
					if (shop != null) {
						if (shop.hasUnlocked(e.getPlayer())) {
							shop.showPrices(e.getPlayer());
							return;
						}
						QuickSell.local.sendMessage(e.getPlayer(), "messages.no-access", false);
						return;
					}

					if (Shop.getHighestShop(e.getPlayer()) != null) {
						Shop.getHighestShop(e.getPlayer()).showPrices(e.getPlayer());
					}
				}
			}
		}
	}

	/**
	 * Sells the item to the shop upon inventory close
	 * @param e InventoryCloseEvent
	 */
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();

		if (QuickSell.shop.containsKey(p.getUniqueId())) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			int size = e.getInventory().getSize();
			if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {
				size = size - 9;
			}

			for (int i = 0; i < size; i++) {
				items.add(e.getInventory().getContents()[i]);
			}

			Shop shop = QuickSell.shop.get(p.getUniqueId());
			QuickSell.shop.remove(p.getUniqueId());
			shop.sell(p, false, Type.SELL, items.toArray(new ItemStack[items.size()]));
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (QuickSell.cfg.getBoolean("options.enable-menu-line") && e.getRawSlot() < e.getInventory().getSize()) {
			Player p = (Player) e.getWhoClicked();
			if (QuickSell.shop.containsKey(p.getUniqueId())) {
				Shop shop = QuickSell.shop.get(p.getUniqueId());

				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 9
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 7
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 6
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 4
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 3
						|| e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 1) {
					e.setCancelled(true);
				}

				// Close GUI
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 8) {
					e.setCancelled(true);
					p.closeInventory();
				}

				// Estimate sold price
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 5) {
					e.setCancelled(true);
					double money = 0.0;

					for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
						ItemStack item = e.getInventory().getContents()[i];
						if (item != null) {
							money = money + shop.getPrices().getPrice(item);
						}
					}
					
					money = DoubleHandler.fixDouble(money, 2);
					if (money > 0.0) {
						for (Booster booster: Booster.getBoosters(p.getName(), BoosterType.MONETARY)) {
							money = money + money * (booster.getMultiplier() - 1);
						}
					}

					QuickSell.local.sendMessage(p, "messages.estimate", false, new Variable("{MONEY}", String.valueOf(DoubleHandler.fixDouble(money, 2))));
				}

				// Sell Items
				if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 2) {
					e.setCancelled(true);
					QuickSell.shop.remove(p.getUniqueId());

					for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
						ItemStack item = e.getInventory().getContents()[i];

						if (item != null) {
							if (item.getType() != Material.AIR) {
								if (InvUtils.fits(p.getInventory(), item)) p.getInventory().addItem(item);
								else p.getWorld().dropItemNaturally(p.getLocation(), item);
							}
						}

					}
					p.closeInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		SellProfile.getProfile(e.getPlayer()).unregister();
	}
}
