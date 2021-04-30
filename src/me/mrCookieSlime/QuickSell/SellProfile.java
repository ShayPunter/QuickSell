package me.mrCookieSlime.QuickSell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent.Type;

import org.bukkit.entity.Player;

public class SellProfile {
	
	public static Map<UUID, SellProfile> profiles = new HashMap<UUID, SellProfile>();
	
	UUID uuid;
	Config cfg;
	List<String> transactions;
	
	public SellProfile(Player p) {
		uuid = p.getUniqueId();
		transactions = new ArrayList<String>();
		cfg = new Config(new File(QuickSell.getInstance().getDataFolder() + File.separator + "data-storage/transactions/" + p.getUniqueId() + ".log"));
		profiles.put(uuid, this);
		
		if (QuickSell.cfg.getBoolean("shop.enable-logging")) {
			cfg.getKeys().forEach(transaction -> transactions.add(cfg.getString(transaction)));
		}
	}

	/**
	 * Get a sell profile of a player
	 * @param p Player
	 * @return SellProfile
	 */
	public static SellProfile getProfile(Player p) {
		return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId()): new SellProfile(p);
	}

	/**
	 * Save and unload sell profiles
	 */
	public void unregister() {
		save();
		profiles.remove(uuid);
	}

	/**
	 * Save sell profiles
	 */
	public void save() {
		cfg.save();
	}

	/**
	 * Stores a transaction
	 * @param type Type
	 * @param soldItems Integer
	 * @param money Double
	 */
	public void storeTransaction(Type type, int soldItems, double money) {
		long timestamp = System.currentTimeMillis();
		String string = timestamp + " __ " + type.toString() + " __ " + soldItems + " __ " + money;
		cfg.setValue(String.valueOf(timestamp), string);
		transactions.add(string);
	}

	/**
	 * Gets the transactions
	 * @return List<String>
	 */
	public List<String> getTransactions() {
		return transactions;
	}

	/**
	 * Gets the most recent transactions
	 * @param amount Integer. Amount of transactions to retrive
	 * @return Transaction
	 */
	public Transaction getRecentTransactions(int amount) {
		int items = 0;
		double money = 0;
		for (int i = (transactions.size() - amount); i < transactions.size(); i++) {
			items = items + Transaction.getItemsSold(transactions.get(i));
			money = money + Transaction.getMoney(transactions.get(i));
		}
		return new Transaction(System.currentTimeMillis(), Type.UNKNOWN, items, money);
	}

}
