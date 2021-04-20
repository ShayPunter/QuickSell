package me.mrCookieSlime.QuickSell.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.thebusybiscuit.cscorelib2.inventory.ChestMenu;
import io.github.thebusybiscuit.cscorelib2.inventory.ClickAction;
import io.github.thebusybiscuit.cscorelib2.inventory.MenuClickHandler;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class BoosterMenu {

	public static void showBoosterOverview(Player p) {
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&3Booster Overview");
		
		menu.addItem(1, new CustomItem(Material.GOLD_INGOT, "&bBoosters (Money)", "&7Current Multiplier: &b" + Booster.getMultiplier(p.getName(), BoosterType.MONETARY), "", "&7\u21E8 Click for Details"));
		menu.addMenuClickHandler(1, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player player, int i, ItemStack itemStack, ItemStack itemStack1, ClickAction clickAction) {
				showBoosterDetails(p, BoosterType.MONETARY);
				return false;
			}
		});
		
		menu.addItem(7, new CustomItem(Material.EXPERIENCE_BOTTLE, "&bBoosters (Experience)", "&7Current Multiplier: &b" + Booster.getMultiplier(p.getName(), BoosterType.EXP), "", "&7\u21E8 Click for Details"));
		menu.addMenuClickHandler(7, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player player, int i, ItemStack itemStack, ItemStack itemStack1, ClickAction clickAction) {
				showBoosterDetails(p, BoosterType.EXP);
				return false;
			}
		});
		
		menu.open(p);
	}

	public static void showBoosterDetails(Player p, BoosterType type) {
		ChestMenu menu = new ChestMenu(QuickSell.getInstance(), "&3" + StringUtils.format(type.toString()) + " Boosters");
		
		menu.addItem(1, new CustomItem(Material.GOLD_INGOT, "&bBoosters (Money)", "&7Current Multiplier: &b" + Booster.getMultiplier(p.getName(), BoosterType.MONETARY), "", "&7\u21E8 Click for Details"));
		menu.addMenuClickHandler(1, new MenuClickHandler() {

			@Override
			public boolean onClick(Player player, int i, ItemStack itemStack, ItemStack itemStack1, ClickAction clickAction) {
				showBoosterDetails(p, BoosterType.MONETARY);
				return false;
			}

		});
		
		menu.addItem(7, new CustomItem(Material.EXPERIENCE_BOTTLE, "&bBoosters (Experience)", "&7Current Multiplier: &b" + Booster.getMultiplier(p.getName(), BoosterType.EXP), "", "&7\u21E8 Click for Details"));
		menu.addMenuClickHandler(7, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player player, int i, ItemStack itemStack, ItemStack itemStack1, ClickAction clickAction) {
				showBoosterDetails(p, BoosterType.EXP);
				return false;
			}
		});
		
		int index = 9;
		
		for (Booster booster: Booster.getBoosters(p.getName(), type)) {
			menu.addItem(index, getBoosterItem(booster));
			menu.addMenuClickHandler(index, (plugin, player, slot, stack, action) -> false);
			
			index++;
		}
		
		menu.open(p);
	}

	public static ItemStack getBoosterItem(Booster booster) {
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add("&7Multiplier: &e" + booster.getMultiplier() + "x");
		lore.add("&7Time Left: &e" + (booster.isInfinite() ? "Infinite": booster.formatTime() + "m"));
		lore.add("&7Global: " + (booster.isPrivate() ? "&4&l\u2718": "&2&l\u2714"));
		lore.add("");
		lore.add("&7Contributors:");
		for (Map.Entry<String, Integer> entry: booster.getContributors().entrySet()) {
			lore.add(" &8\u21E8 " + entry.getKey() + ": &a+" + entry.getValue() + "m");
		}
		return new CustomItem(Material.EXPERIENCE_BOTTLE, "&3" + booster.getMultiplier() + "x &b" + booster.getUniqueName(), lore.toArray(new String[lore.size()]));
	}

	public static String getTellRawMessage(Booster booster) {
		StringBuilder builder = new StringBuilder("&3" + booster.getMultiplier() + "x &b" + booster.getUniqueName() + "\n \n");
		builder.append("&7Multiplier: &e" + booster.getMultiplier() + "x\n");
		builder.append("&7Time Left: &e" + (booster.isInfinite() ? "Infinite": booster.formatTime() + "m") + "\n");
		builder.append("&7Global: " + (booster.isPrivate() ? "&4&l\u2718": "&2&l\u2714") + "\n\n&7Contributors:\n");
		for (Map.Entry<String, Integer> entry: booster.getContributors().entrySet()) {
			builder.append(" &8\u21E8 " + entry.getKey() + ": &a+" + entry.getValue() + "m\n");
		}
		
		return ChatColor.translateAlternateColorCodes('&', builder.toString());
	}

}
