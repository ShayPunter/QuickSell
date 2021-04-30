package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.thebusybiscuit.cscorelib2.inventory.ChestMenu;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ShopEditor implements Listener {
	
	Map<UUID, Input> input;
	QuickSell quicksell;
	
	final int shop_size = 36;
	
	public ShopEditor(QuickSell quicksell) {
		this.quicksell = quicksell;
		this.input = new HashMap<UUID, Input>();
		quicksell.getServer().getPluginManager().registerEvents(this, quicksell);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (input.containsKey(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
			
			Input input = this.input.get(e.getPlayer().getUniqueId());
			
			switch (input.getType()) {
				case NEW_SHOP: {
					List<String> list = new ArrayList<String>();
					Shop.list().forEach(shop -> list.add(shop.getID()));

					for (int i = list.size(); i <= (Integer) input.getValue(); i++) {
						list.add("");
					}

					list.set((Integer) input.getValue(), ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage())));

					QuickSell.cfg.setValue("list", list);
					QuickSell.cfg.setValue("shops." + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage())) + ".name", e.getMessage());
					QuickSell.cfg.save();

					QuickSell.local.sendMessage(e.getPlayer(), "commands.shop-created", false, new Variable("%shop%", e.getMessage()));

					openEditor(e.getPlayer());

					this.input.remove(e.getPlayer().getUniqueId());
					break;
				}
				case RENAME: {
					Shop shop = (Shop) input.getValue();

					QuickSell.cfg.setValue("shops." + shop.getID() + ".name", e.getMessage());
					QuickSell.cfg.save();
					quicksell.reload();

					openShopEditor(e.getPlayer(), Shop.getShop(shop.getID()));
					QuickSell.local.sendMessage(e.getPlayer(), "editor.renamed-shop", false);

					this.input.remove(e.getPlayer().getUniqueId());
					break;
				}
				case SET_PERMISSION: {
					Shop shop = (Shop) input.getValue();

					QuickSell.cfg.setValue("shops." + shop.getID() + ".permission", e.getMessage().equals("none") ? "": e.getMessage());
					QuickSell.cfg.save();
					quicksell.reload();

					openShopEditor(e.getPlayer(), Shop.getShop(shop.getID()));
					QuickSell.local.sendMessage(e.getPlayer(), "editor.permission-set-shop", false);

					this.input.remove(e.getPlayer().getUniqueId());
					break;
				}
				default: break;
			}
		}
	}

	/**
	 * Opens the shop editor for a player
	 * @param p Player
	 */
	public void openEditor(Player p) {
		quicksell.reload();
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&6QuickSell - Shop Editor");
		
		menu.addMenuOpeningHandler(p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		for (int i = 0; i < 54; i++) {
			final Shop shop = Shop.list().size() > i ? Shop.list().get(i): null;

			if (shop == null) {
				menu.addItem(i, new CustomItem(Material.GOLD_NUGGET, "&cNew Shop", "", "&rLeft Click: &7Create a new Shop"));
				menu.addMenuClickHandler(i, (player, i1, itemStack, itemStack1, clickAction) -> {
					input.put(p.getUniqueId(), new Input(InputType.NEW_SHOP, i1));
					QuickSell.local.sendMessage(p, "editor.create-shop", false);
					p.closeInventory();
					return false;
				});
				continue;
			}

			menu.addItem(i, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), shop.getName(), "", "&rLeft Click: &7Edit Shop", "&rRight Click: &7Edit Shop Contents", "&rShift + Right Click: &4Delete Shop"));
			menu.addMenuClickHandler(i, (player, i12, itemStack, itemStack1, clickAction) -> {
				if (clickAction.isRightClick()) {
					if (clickAction.isShiftClick())  {
						List<String> list = new ArrayList<String>();

						for (Shop shop1 : Shop.list()) {
							list.add(shop1.getID());
						}

						list.remove(shop.getID());
						QuickSell.cfg.setValue("list", list);
						QuickSell.cfg.save();
						quicksell.reload();
						openEditor(p);
					} else {
						openShopContentEditor(p, shop, 1);
					}
				} else {
					openShopEditor(p, shop);
				}
				return false;
			});
		}

		Bukkit.getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

	/**
	 * Opens a GUI to edit a shop with
	 * @param p Player
	 * @param shop Shop
	 */
	public void openShopEditor(Player p, final Shop shop) {
		quicksell.reload();
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&6QuickSell - Shop Editor");

		menu.addMenuOpeningHandler(p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		menu.addItem(0, new CustomItem(Material.NAME_TAG, shop.getName(), "", "&rClick: &7Change Name"));
		menu.addMenuClickHandler(0, (player, i, itemStack, itemStack1, clickAction) -> {
			input.put(p.getUniqueId(), new Input(InputType.RENAME, shop));
			QuickSell.local.sendMessage(p, "editor.rename-shop", false);
			p.closeInventory();
			return false;
		});
		
		menu.addItem(1, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), "&rDisplay Item", "", "&rClick: &7Change Item to the Item held in your Hand"));
		menu.addMenuClickHandler(1, (player, i, itemStack, itemStack1, clickAction) -> {
			if (p.getInventory().getItemInMainHand() != null
					&& p.getInventory().getItemInMainHand().getType() != null
					&& p.getInventory().getItemInMainHand().getType() != Material.AIR) {
				QuickSell.cfg.setValue("shops." + shop.getID() + ".itemtype", p.getItemInHand().getType().toString());
				QuickSell.cfg.save();
				quicksell.reload();
			}

			openShopEditor(p, Shop.getShop(shop.getID()));
			return false;
		});
		
		menu.addItem(2, new CustomItem(Material.DIAMOND, "&7Shop Permission: &r" + (shop.getPermission().equals("") ? "None": shop.getPermission()), "", "&rClick: &7Change Permission Node"));
		menu.addMenuClickHandler(2, (player, i, itemStack, itemStack1, clickAction) -> {
			input.put(p.getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
			QuickSell.local.sendMessage(p, "editor.set-permission-shop", false);
			p.closeInventory();
			return false;
		});
		
		menu.addItem(3, new CustomItem(Material.COMMAND_BLOCK, "&bInheritance Manager", "", "&rClick: &7Open Inheritance Manager"));
		menu.addMenuClickHandler(3, (player, i, itemStack, itemStack1, clickAction) -> {
			openShopInheritanceEditor(p, shop);
			return false;
		});

		QuickSell.getInstance().getServer().getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

	/**
	 * Opens a shop content editor GUI
	 * @param p Player
	 * @param shop Shop
	 * @param page Integer
	 */
	public void openShopContentEditor(Player p, final Shop shop, final int page) {
		quicksell.reload();
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&6QuickSell - Shop Editor");

		menu.addMenuOpeningHandler(p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		int index = 9;
		final int pages = shop.getPrices().getInfo().size() / shop_size + 1;
		
		for (int i = 0; i < 4; i++) {
			menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
			menu.addMenuClickHandler(i, (plugin, player, slot, item, action) -> false);
		}
		
		menu.addItem(4, new CustomItem(Material.GOLD_INGOT, "&7\u21E6 Back"));
		menu.addMenuClickHandler(4, (plugin, player, slot, item, action) -> {
			openEditor(p);
			return false;
		});
		
		for (int i = 5; i < 9; i++) {
			menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
			menu.addMenuClickHandler(i, (plugin, player, slot, item, action) -> false);
		}
		
		for (int i = 45; i < 54; i++) {
			menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
			menu.addMenuClickHandler(i, (plugin, player, slot, item, action) -> false);
		}
		
		menu.addItem(46, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&r\u21E6 Previous Page", "", "&7(" + page + " / " + pages + ")"));
		menu.addMenuClickHandler(46, (player, i, itemStack, itemStack1, clickAction) -> {
			int next = page - 1;
			if (next < 1) next = pages;
			if (next != page) openShopContentEditor(p, shop, next);
			return false;
		});
		
		menu.addItem(52, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&rNext Page \u21E8", "", "&7(" + page + " / " + pages + ")"));
		menu.addMenuClickHandler(52, (player, i, itemStack, itemStack1, clickAction) -> {
			int next = page + 1;
			if (next > pages) next = 1;
			if (next != page) openShopContentEditor(p, shop, next);
			return false;
		});
		
		int shop_index = shop_size * (page - 1);
		
		for (int i = 0; i < shop_size; i++) {
			int target = shop_index + i;
			if (target >= shop.getPrices().getItems().size()) {
				menu.addItem(index, new CustomItem(Material.COMMAND_BLOCK, "&cAdd Item", "", "&rLeft Click: &7Add an Item to this Shop"));
				menu.addMenuClickHandler(index, (player, i1, itemStack, itemStack1, clickAction) -> {
					openItemEditor(p, shop);
					return false;
				});
				break;
			}

			final String string = shop.getPrices().getItems().get(target);
			final ItemStack item = shop.getPrices().getItem(string);

			menu.addItem(index, new CustomItem(item.getType(), item.getItemMeta().getDisplayName(), "&7Price (1): &6$" + DoubleHandler.getFancyDouble(shop.getPrices().getPrice(string)), "", "&rLeft Click: &7Edit Price", "&rShift + Right Click: &7Remove this Item from this Shop"));
			menu.addMenuClickHandler(index, (plugin, player, slot, stack, action) -> {
				if (action.isShiftClick() && action.isRightClick()) {
					QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + string, 0.0D);
					QuickSell.cfg.save();
					quicksell.reload();
					openShopContentEditor(p, Shop.getShop(shop.getID()), 1);
				}

				if (!action.isRightClick()) {
					openPriceEditor(p, Shop.getShop(shop.getID()), item, string, shop.getPrices().getPrice(string));
				}

				return false;
			});

			index++;
			
		}

		Bukkit.getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

	/**
	 * Opens an GUI to edit items from a shop
	 * @param p Player
	 * @param shop Shop
	 */
	public void openItemEditor(Player p, final Shop shop) {
		final ItemStack item = p.getInventory().getItemInMainHand();
		
		if (item == null || item.getType() == null || item.getType() == Material.AIR) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lYou need to be holding the Item you want to add in your Hand!"));
			return;
		}
		
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&6QuickSell - Shop Editor");
		
		menu.addMenuOpeningHandler((player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		menu.addItem(4, item);
		menu.addMenuClickHandler(4, (plugin, player, slot, stack, action) -> false);
		
		menu.addItem(10, new CustomItem(Material.LIME_WOOL, "&2Material Only &7(e.g. STONE)", "&rAdds the Item above to the Shop", "&rThis Option is going to ignore", "&rany Item Names and such"));
		menu.addMenuClickHandler(10, (plugin, player, slot, stack, action) -> {
			QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + item.getType().toString(), 1.0D);
			QuickSell.cfg.save();
			quicksell.reload();
			openShopContentEditor(p, Shop.getShop(shop.getID()), 1);
			
			QuickSell.local.sendMessage(p, "commands.price-set", false, new Variable("%item%", item.getType().toString()), new Variable("%shop%", shop.getName()), new Variable("%price%", "1.0"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
			return false;
		});
		
		menu.addItem(11, new CustomItem(Material.LIME_WOOL, "&2Material Only and exclude Metadata &7(e.g. STONE-nodata)", "&rAdds the Item above to the Shop", "&rThis Option is going to only take Items", "&rwhich are NOT renamed and do NOT have Lore"));
		menu.addMenuClickHandler(11, (plugin, player, slot, stack, action) -> {
			QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + item.getType().toString() + "-nodata", 1.0D);
			QuickSell.cfg.save();
			quicksell.reload();
			openShopContentEditor(p, Shop.getShop(shop.getID()), 1);
			
			QuickSell.local.sendMessage(p, "commands.price-set", false, new Variable("%item%", item.getType().toString() + ":" + item.getData().getData()), new Variable("%shop%", shop.getName()), new Variable("%price%", "1.0"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
			return false;
		});
		
		menu.addItem(12, new CustomItem(Material.CYAN_WOOL, "&2Material + Display Name &7(e.g. STONE named &5Cool Stone &7)", "&rAdds the Item above to the Shop", "&rThis Option is going to respect Display Names"));
		menu.addMenuClickHandler(14, (player, slot, newitem, stack, action) -> {
			if (!item.getItemMeta().hasDisplayName()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can only choose this Option if the selected Item had a Display Name!"));
				return false;
			}
			
			QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + item.getType().toString() + "-" + item.getItemMeta().getDisplayName().replaceAll("&", "&"), 1.0D);
			QuickSell.cfg.save();
			quicksell.reload();
			openShopContentEditor(player, Shop.getShop(shop.getID()), 1);
			
			QuickSell.local.sendMessage(player, "commands.price-set", false, new Variable("%item%", item.getType().toString() + " named " + item.getItemMeta().getDisplayName()), new Variable("%shop%", shop.getName()), new Variable("%price%", "1.0"));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
			return false;
		});
		
		menu.addItem(16, new CustomItem(Material.RED_WOOL, "&cCancel"));
		menu.addMenuClickHandler(16, (player, slot, clickItem, stack, action) -> {
			openShopContentEditor(player, Shop.getShop(shop.getID()), 1);
			return false;
		});

		Bukkit.getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

	/**
	 * Opens the price editor for a shop
	 * @param p Player
	 * @param shop Shop
	 * @param item ItemStack
	 * @param string String
	 * @param worth Double
	 */
	public void openPriceEditor(Player p, final Shop shop, final ItemStack item, final String string, final double worth) {
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(),"&6QuickSell - Shop Editor");
		
		menu.addMenuOpeningHandler((player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		menu.addItem(4, new CustomItem(item, item.getItemMeta().getDisplayName(), "", "&8Price: &6$" + DoubleHandler.getFancyDouble(worth)));
		menu.addMenuClickHandler(4, (plugin, player, slot, stack, action) -> false);
		
		menu.addItem(9, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+0.1", "&7Shift + Left Click: &r+1", "&7Right Click: &r-0.1", "&7Shift + Right Click: &r-1"));
		menu.addMenuClickHandler(9, (player, slot, itemstack, stack, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 1: 0.1);
			else
				price = price + (action.isShiftClick() ? 1: 0.1);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(10, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+10", "&7Shift + Left Click: &r+100", "&7Right Click: &r-10", "&7Shift + Right Click: &r-100"));
		menu.addMenuClickHandler(10, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 100: 10);
			else
				price = price + (action.isShiftClick() ? 100: 10);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(11, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+1K", "&7Shift + Left Click: &r+10K", "&7Right Click: &r-1K", "&7Shift + Right Click: &r-10K"));
		menu.addMenuClickHandler(11, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 10000: 1000);
			else
				price = price + (action.isShiftClick() ? 10000: 1000);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(12, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+100K", "&7Shift + Left Click: &r+1M", "&7Right Click: &r-100K", "&7Shift + Right Click: &r-1M"));
		menu.addMenuClickHandler(12, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 1000000: 100000);
			else
				price = price + (action.isShiftClick() ? 1000000: 100000);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(13, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+10M", "&7Shift + Left Click: &r+100M", "&7Right Click: &r-10M", "&7Shift + Right Click: &r-100M"));
		menu.addMenuClickHandler(13, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 100000000: 10000000);
			else
				price = price + (action.isShiftClick() ? 100000000: 10000000);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(14, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+1B", "&7Shift + Left Click: &r+10B", "&7Right Click: &r-1B", "&7Shift + Right Click: &r-10B"));
		menu.addMenuClickHandler(14, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 10000000000D: 1000000000);
			else
				price = price + (action.isShiftClick() ? 10000000000D: 1000000000);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(15, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+100B", "&7Shift + Left Click: &r+1T", "&7Right Click: &r-100B", "&7Shift + Right Click: &r-1T"));
		menu.addMenuClickHandler(15, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 1000000000000D: 100000000000D);
			else
				price = price + (action.isShiftClick() ? 1000000000000D: 100000000000D);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(16, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+10T", "&7Shift + Left Click: &r+100T", "&7Right Click: &r-10T", "&7Shift + Right Click: &r-100T"));
		menu.addMenuClickHandler(16, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 100000000000000D: 10000000000000D);
			else
				price = price + (action.isShiftClick() ? 100000000000000D: 10000000000000D);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(17, new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth), "", "&7Left Click: &r+1Q", "&7Shift + Left Click: &r+10Q", "&7Right Click: &r-1Q", "&7Shift + Right Click: &r-10Q"));
		menu.addMenuClickHandler(17, (player, i, itemStack, itemStack1, action) -> {
			double price = worth;
			if (action.isRightClick())
				price = price - (action.isShiftClick() ? 10000000000000000D: 1000000000000000D);
			else
				price = price + (action.isShiftClick() ? 10000000000000000D: 1000000000000000D);

			if (price <= 0)
				price = 0.1;

			openPriceEditor(p, shop, item, string, price);
			return false;
		});
		
		menu.addItem(20, new CustomItem(Material.LIME_WOOL, "&2Save"));
		menu.addMenuClickHandler(20, (player, i, itemStack, itemStack1, action) -> {
			QuickSell.cfg.setValue("shops." + shop.getID() + ".price." + string, worth);
			QuickSell.cfg.save();
			quicksell.reload();

			QuickSell.local.sendMessage(p, "commands.price-set", false, new Variable("%item%", string), new Variable("%shop%", shop.getName()), new Variable("%price%", DoubleHandler.getFancyDouble(worth)));
			openShopContentEditor(p, Shop.getShop(shop.getID()), 1);
			return false;
		});

		menu.addItem(24, new CustomItem(Material.RED_WOOL, "&4Cancel"));
		menu.addMenuClickHandler(24, (player, i, itemStack, itemStack1, action) -> {
			openShopContentEditor(p, Shop.getShop(shop.getID()), 1);
			return false;
		});

		Bukkit.getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

	/**
	 * Opens the shop inheritance menu
	 * @param p Player
	 * @param s Shop
	 */
	public void openShopInheritanceEditor(final Player p, final Shop s) {
		quicksell.reload();
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&6QuickSell - Shop Editor");
		
		menu.addMenuOpeningHandler((player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
		
		for (int i = 0; i < 54; i++) {
			if (Shop.list().size() <= i)
				break;

			final Shop shop = Shop.list().get(i);
			
			if (!shop.getID().equalsIgnoreCase(s.getID())) {
				final boolean inherit = QuickSell.cfg.getStringList("shops." + s.getID() + ".inheritance").contains(shop.getID());
				
				menu.addItem(i, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), shop.getName(), "", "&7Inherit: " + (inherit ? "&2&l\u2714": "&4&l\u2718"), "", "&7&oClick to toggle"));
				menu.addMenuClickHandler(i, (player, slot, newitem, item, action) -> {
					List<String> shops = QuickSell.cfg.getStringList("shops." + s.getID() + ".inheritance");

					if (inherit)
						shops.remove(shop.getID());
					else
						shops.add(shop.getID());

					QuickSell.cfg.setValue("shops." + s.getID() + ".inheritance", shops);
					QuickSell.cfg.save();
					openShopInheritanceEditor(p, Shop.getShop(s.getID()));
					return false;
				});
			}
		}

		if (menu.getSize() < 9) {
			QuickSell.local.sendMessage(p, "editor.no-inheritance");
			return;
		}

		menu.setSize(54);
		Bukkit.getScheduler().runTask(QuickSell.getInstance(), (runnable) -> menu.open(p));
	}

}
