package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.utils.StringUtils;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PriceInfo {
	
	Shop shop;
	Map<String, Double> prices;
	Map<String, ItemStack> info;
	List<String> order;
	int amount;
	
	private static final Map<String, PriceInfo> map = new HashMap<String, PriceInfo>();
	
	public PriceInfo(Shop shop) {
		this.shop = shop;
		this.prices = new HashMap<String, Double>();
		this.order = new ArrayList<String>();
		this.amount = QuickSell.cfg.getInt("shops." + shop.getID() + ".amount");

		QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop.getID() + ".price").getKeys(false).forEach(key -> {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) > 0.0)
				prices.put(key, QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) / amount);
		});

		QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop.getID() + ".price").getKeys(false).forEach(key -> {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) > 0.0)
				prices.put(key, QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) / amount);
		});

		QuickSell.cfg.getStringList("shops." + shop.getID() + ".inheritance").forEach(this::loadParent);
		
		info = new HashMap<String, ItemStack>();
		for (String item: prices.keySet()) {
			if (info.size() >= 54)
				break;

			if (Material.getMaterial(item) != null) {
				info.put(item, new CustomItem(Material.getMaterial(item), "&r" + StringUtils.formatItemName(new ItemStack(Material.getMaterial(item)), false), "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
				order.add(item);
				return;
			}

			if (item.split("-").length > 1) {
				if (Material.getMaterial(item.split("-")[0]) != null) {
					if (!item.split("-")[1].equals("nodata")) {
						info.put(item, new CustomItem(new CustomItem(Material.getMaterial(item.split("-")[0]), item.split("-")[1], "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)), getAmount()));
					} else {
						info.put(item, new CustomItem(Material.getMaterial(item.split("-")[0]), "&r" + StringUtils.formatItemName(new ItemStack(Material.getMaterial(item.split("-")[0])), false), "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
					}
					order.add(item);
					return;
				}
			}
			System.err.println("[QuickSell] Could not recognize Item String: \"" + item + "\"");
		}
		
		map.put(shop.getID(), this);
	}
	
	private void loadParent(String parent) {
		QuickSell.cfg.getKeys("shops." + parent + ".price").forEach(key -> {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0)
				prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
		});

		QuickSell.cfg.getStringList("shops." + parent + ".inheritance").forEach(this::loadParent);
	}

	public PriceInfo(String shop) {
		this.prices = new HashMap<String, Double>();

		QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop + ".price").getKeys(false).forEach(key -> {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + shop + ".price." + key) > 0.0)
				prices.put(key, QuickSell.cfg.getDouble("shops." + shop + ".price." + key) / amount);
		});

		QuickSell.cfg.getStringList("shops." + shop + ".inheritance").forEach(parent -> getInfo(parent).getPrices().keySet().forEach(key -> {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0)
				prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
		}));
	}

	/**
	 * Gets the prices of a shop
	 * @return Map<String, Double>
	 */
	public Map<String, Double> getPrices() {
		return prices;
	}

	/**
	 * Gets the price of an item
	 * @param item ItemStack
	 * @return Double
	 */
	public double getPrice(ItemStack item) {
		if (item == null)
			return 0.0;

		String string = toString(item);
		if (prices.containsKey(string))
			return DoubleHandler.fixDouble(prices.get(string) * item.getAmount());

		return 0.0D;
	}

	/**
	 * Gets the price of an item
	 * @param string String
	 * @return Double
	 */
	public double getPrice(String string) {
		return prices.get(string);
	}

	/**
	 * Converts an ItemStack into a string
	 * @param item ItemStack
	 * @return String
	 */
	public String toString(ItemStack item) {
		if (item == null) return "null";
		String name = item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName().replace("&", "&"): "": "";

		if (!name.equalsIgnoreCase("") && prices.containsKey(item.getType().toString() + "-" + name))
			return item.getType().toString() + "-" + name;

		if (item.isSimilar(new ItemStack(item.getType(), item.getAmount())) && prices.containsKey(item.getType().toString() + "-nodata"))
			return item.getType().toString() + "-nodata";

		if (prices.containsKey(item.getType().toString()))
			return item.getType().toString();

		return "null";
	}

	/**
	 * Gets the price information from a shop
	 * @param shop String
	 * @return PriceInfo
	 */
	public static PriceInfo getInfo(String shop) {
		return map.containsKey(shop) ? map.get(shop): new PriceInfo(shop);
	}

	/**
	 * Gets the amount
	 * @return Integer
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Gets all the shop items
	 * @return Collection<ItemStack>
	 */
	// todo: rename method into a more suitable one
	public Collection<ItemStack> getInfo() {
		return info.values();
	}

	/**
	 * Gets the shop items
	 * @return List<String>
	 */
	public List<String> getItems() {
		return this.order;
	}

	/**
	 * Gets a shop item from string
	 * @param string String
	 * @return ItemStack
	 */
	public ItemStack getItem(String string) {
		return this.info.get(string);
	}
}
