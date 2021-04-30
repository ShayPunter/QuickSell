package me.mrCookieSlime.QuickSell;

import me.mrCookieSlime.QuickSell.interfaces.SellEvent.Type;

public class Transaction {
	
	long timestamp;
	int items;
	double money;
	Type type;

	/**
	 * Creates a new Transaction
	 * @param value String
	 */
	public Transaction(String value) {
		timestamp = Long.parseLong(value.split(" __ ")[0]);
		type = Type.valueOf(value.split(" __ ")[1]);
		items = Integer.parseInt(value.split(" __ ")[2]);
		money = Double.parseDouble(value.split(" __ ")[3]);
	}

	/**
	 * Creates a new Transaction
	 * @param timestamp Long
	 * @param type Type
	 * @param soldItems Integer
	 * @param money Double
	 */
	public Transaction(long timestamp, Type type, int soldItems, double money) {
		this.timestamp = timestamp;
		this.type = type;
		this.items = soldItems;
		this.money = money;
	}

	/**
	 * Get the number of items stold
	 * @return Integer
	 */
	public int getItemsSold() {
		return items;
	}

	/**
	 * Get the amount of money those items sold for
	 * @return Double
	 */
	public double getMoney() {
		return money;
	}

	/**
	 * Get items sold
	 * @param string String
	 * @return Integer
	 */
	public static int getItemsSold(String string) {
		return Integer.parseInt(string.split(" __ ")[2]);
	}

	/**
	 * Gets the amount of money those items sold for
	 * @param string String
	 * @return Double
	 */
	public static double getMoney(String string) {
		return Double.valueOf(string.split(" __ ")[3]);
	}

}
