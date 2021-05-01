package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.thebusybiscuit.cscorelib2.inventory.InvUtils;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuickSell.interfaces.SellEvent.Type;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;

public class Shop {
	
	private static final List<Shop> shops = new ArrayList<Shop>();
	private static final Map<String, Shop> map = new HashMap<String, Shop>();
	
	String shop, permission;
    PriceInfo prices;
    ItemStack unlocked, locked;
    String name;
	
	public Shop(String id) {
		this.shop = id;
		this.prices = new PriceInfo(this);
		
		name = QuickSell.cfg.getString("shops." + shop + ".name");
		permission = QuickSell.cfg.getString("shops." + shop + ".permission");
		
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7<&a&l Click to open &7>"));
		for (String line: QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		unlocked = new CustomItem(Material.getMaterial(QuickSell.cfg.getString("shops." + shop + ".itemtype")), name, lore.toArray(new String[lore.size()]));
		
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', QuickSell.local.getMessage("messages.no-access")));
		for (String line: QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		locked = new CustomItem(Material.getMaterial(QuickSell.cfg.getString("options.locked-item")), name, lore.toArray(new String[lore.size()]));
		
		shops.add(this);
		map.put(this.shop.toLowerCase(), this);
	}
	
	public Shop() {
		shops.add(null);
	}

	/**
	 * Check if the player has unlocked a shop
	 * @param p Player
	 * @return Boolean
	 */
	public boolean hasUnlocked(Player p) {
		return permission.equalsIgnoreCase("") ? true: p.hasPermission(permission);
	}

	/**
	 * Resets the shops
	 */
	// todo: setup shop manager
	public static void reset() {
		shops.clear();
		map.clear();
	}

	/**
	 * Get a list of shops
	 * @return List<Shop>
	 */
	// todo: setup shop manager
	public static List<Shop> list() {
		return shops;
	}

	/**
	 * Gets the highest shop a player has access to
	 * @param p Player
	 * @return Shop
	 */
	// todo: setup shop manager
	public static Shop getHighestShop(Player p) {
		for (int i = shops.size() - 1; i >= 0; i--) {
			if (shops.get(i) != null && shops.get(i).hasUnlocked(p)) return shops.get(i);
		}
		return null;
	}

	/**
	 * Gets an ID of a shop
	 * @return String
	 */
	public String getID() {
		return shop;
	}

	/**
	 * Gets the shop permission
	 * @return String
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * Get a shop
	 * @param id String
	 * @return Shop
	 */
	public static Shop getShop(String id) {
		return map.get(id.toLowerCase());
	}

	/**
	 * Get the shops prices
	 * @return PriceInfo
	 */
	public PriceInfo getPrices() {
		return prices;
	}

	/**
	 * Sell all items
	 * @param p Player
	 * @param item String
	 */
	public void sellall(Player p, String item) {
		sellall(p, item, Type.UNKNOWN);
	}

	/**
	 * Sell all items
	 * @param p Player
	 * @param item String
	 * @param type Type
	 */
	// Todo: test if function works with removed backpack support. Potential depreciated method.
	public void sellall(Player p, String item, Type type) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (int slot = 0; slot < p.getInventory().getSize(); slot++) {
			ItemStack is = p.getInventory().getItem(slot);
			if (getPrices().getPrice(is) > 0.0) {
				items.add(is);
				p.getInventory().setItem(slot, new ItemStack(Material.AIR));
				p.updateInventory();
			}
		}
		sell(p, false, type, items.toArray(new ItemStack[items.size()]));
	}

	/**
	 * Sell an item
	 * @param p Player
	 * @param silent Boolean
	 * @param soldItems ItemStack...
	 */
	public void sell(Player p, boolean silent, ItemStack... soldItems) {
		sell(p, silent, Type.UNKNOWN, soldItems);
	}

	/**
	 * Sell an item
	 * @param p Player
	 * @param silent Boolean
	 * @param soldItems ItemStack...
	 */
	public void sell(Player p, boolean silent, Type type, ItemStack... soldItems) {
		if (soldItems.length == 0) {
			if (!silent)
				QuickSell.local.sendMessage(p, "messages.no-items", false);
			return;
		}

		double money = 0.0;
		int sold = 0;
		int total = 0;

		for (ItemStack item: soldItems) {
			if (item != null) {
				total = total + item.getAmount();
				if (getPrices().getPrice(item) > 0.0) {
					sold = sold + item.getAmount();
					money = money + getPrices().getPrice(item);
				} else if (InvUtils.fits(p.getInventory(), item)) {
					p.getInventory().addItem(item);
				} else {
					p.getWorld().dropItemNaturally(p.getLocation(), item);
				}
			}
		}

		money = DoubleHandler.fixDouble(money, 2);

		if (money > 0.0) {
			double totalmoney = handoutReward(p, money, sold, silent);

			if (!silent) {
				if (QuickSell.cfg.getBoolean("sound.enabled"))
					p.playSound(p.getLocation(), Sound.valueOf(QuickSell.cfg.getString("sound.sound")), 1F, 1F);

				QuickSell.cfg.getStringList("commands-on-sell").forEach(command -> {
					if (command.contains("{PLAYER}"))
						command = command.replace("{PLAYER}", p.getName());

					if (command.contains("{MONEY}"))
						command = command.replace("{MONEY}", String.valueOf(DoubleHandler.fixDouble(totalmoney, 2)));

					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				});
			}

			int finalTotal = total;
			QuickSell.getSellEvents().forEach(event -> event.onSell(p, type, finalTotal, totalmoney));
		}

		if (!silent && total <= 0)  {
			QuickSell.local.sendMessage(p, "messages.get-nothing", false);
		}

		if (!silent && sold < total && total > 0) {
			QuickSell.local.sendMessage(p, "messages.dropped", false);
		}

		p.updateInventory();
	}

	/**
	 * Gives the player the sold amount with the booster calculations
	 * @param p Player
	 * @param totalmoney Double
	 * @param items Integer
	 * @param silent Boolean
	 * @return Double
	 */
	public double handoutReward(Player p, double totalmoney, int items, boolean silent) {
		double money = totalmoney;
		if (!silent)
			QuickSell.local.sendMessage(p, "messages.sell", false, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money)), new Variable("{ITEMS}", String.valueOf(items)));

		for (Booster booster: Booster.getBoosters(p.getName())) {
			if (booster.getType().equals(BoosterType.MONETARY)) {
				if (!silent) booster.sendMessage(p, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money * (booster.getMultiplier() - 1))));
				money = money + money * (booster.getMultiplier() - 1);
			}
		}

		if (!silent && !Booster.getBoosters(p.getName()).isEmpty())
			QuickSell.local.sendMessage(p, "messages.total", false, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money)));

		money = DoubleHandler.fixDouble(money, 2);
		QuickSell.economy.depositPlayer(p, money);
		return money;
	}

	/**
	 * Gets a shop item depending on if the user has it locked or not
	 * @param status ShopStatus
	 * @return ItemStack
	 */
	public ItemStack getItem(ShopStatus status) {
		switch(status) {
		case LOCKED: return locked;
		case UNLOCKED: return unlocked;
		default: return null;
		}
	}

	/**
	 * Gets the name of the shop
	 * @return String
	 */
	public String getName() {
		return ChatColor.translateAlternateColorCodes('&', name);
	}

	/**
	 * Opens a GUI with the shop prices to the player
	 * @param p Player
	 */
	public void showPrices(Player p) {
		ShopMenu.openPrices(p, this, 1);
	}
	
}
