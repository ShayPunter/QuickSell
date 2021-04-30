package me.mrCookieSlime.QuickSell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import co.aikar.commands.PaperCommandManager;
import io.github.thebusybiscuit.cscorelib2.config.Config;
import me.mrCookieSlime.QuickSell.commands.*;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent;
import me.mrCookieSlime.QuickSell.listeners.CitizensListener;
import me.mrCookieSlime.QuickSell.listeners.SellListener;
import me.mrCookieSlime.QuickSell.utils.Localization;
import me.mrCookieSlime.QuickSell.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.listeners.XPBoosterListener;
import net.milkbowl.vault.economy.Economy;

public class QuickSell extends JavaPlugin {
	
	private static QuickSell instance;
	
	public static Config cfg;
	public static Economy economy = null;
	public static Localization local;
	public static Map<UUID, Shop> shop;
	public static List<SellEvent> events;
	
	public ShopEditor editor;
	private boolean citizens = false, backpacks = false, mcmmo = false, prisongems = false;
	public Config npcs;
	
	@Override
	public void onEnable() {
		instance = this;
		PluginUtils utils = new PluginUtils(this);
		new Metrics(this, 11203);

		// Setup Messages & Configs
		utils.setupConfig();
		cfg = utils.getConfig();
		npcs = new Config("plugins/QuickSell/citizens_npcs.yml");
		utils.setupLocalization();
		local = utils.getLocalization();
		setupMessages();
		local.save();

		if (!new File(getDataFolder() + File.separator + "data-storage/boosters/").exists())
			new File(getDataFolder() + File.separator + "data-storage/boosters/").mkdirs();

		// Move any existing root data-storage over to the plugin folder
		if (new File("data-storage/QuickSell/boosters/").exists()) {
			File dir = new File("data-storage/QuickSell/boosters/");
			try {
				Files.move(dir.toPath(), Paths.get(getDataFolder() + File.separator + "data-storage/boosters/"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Initiate Variables
		PaperCommandManager commandManager = new PaperCommandManager(this);
		commandManager.registerDependency(QuickSell.class, "QuickSell", this);
		shop = new HashMap<UUID, Shop>();
		events = new ArrayList<SellEvent>();
		editor = new ShopEditor(this);
		citizens = getServer().getPluginManager().isPluginEnabled("Citizens");
		mcmmo = getServer().getPluginManager().isPluginEnabled("mcMMO");
		prisongems = Bukkit.getPluginManager().isPluginEnabled("PrisonGems");
		backpacks = Bukkit.getPluginManager().isPluginEnabled("PrisonUtils");

		// Looks and setups transaction logging
		if (cfg.getBoolean("shop.enable-logging")) {
			registerSellEvent(new SellEvent() {

				@Override
				public void onSell(Player p, Type type, int itemsSold, double money) {
					SellProfile profile = SellProfile.getProfile(p);
					profile.storeTransaction(type, itemsSold, money);
				}
			});
		}

		// Reload config files and setup economy
		reload();
		setupEconomy();

		// Listeners
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new XPBoosterListener(), this);
		pluginManager.registerEvents(new CitizensListener(), this);
		pluginManager.registerEvents(new SellListener(), this);

		// Commands
		commandManager.registerCommand(new BoosterCommand());
		commandManager.registerCommand(new PBoosterCommand());
		commandManager.registerCommand(new BoostersCommand());
		commandManager.registerCommand(new PricesCommand());
		commandManager.registerCommand(new QuickSellCommand());
		commandManager.registerCommand(new SellAllCommand());
		commandManager.registerCommand(new SellCommand());

		for (int i = 0; i < 1000; i++) {
			if (new File(getDataFolder() + File.separator + "data-storage/boosters/" + i + ".booster").exists()) {
				try {
					if (new Config(new File(getDataFolder() + File.separator + "data-storage/boosters/" + i + ".booster")).getBoolean("private")) new PrivateBooster(i);
					else new Booster(i);
				} catch (ParseException e) {
				}
			}
		}

		getServer().getScheduler().runTaskTimer(this, Booster::update, 0L, cfg.getInt("boosters.refresh-every") * 20L);
	}
	
	@Override
	public void onDisable() {
		cfg = null;
		shop = null;
		economy = null;
		local = null;
		events = null;
		
		for (SellProfile profile: SellProfile.profiles.values()) {
			profile.save();
		}
		
		SellProfile.profiles = null;
		Booster.active = null;
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	    if (economyProvider != null) {
	      economy = (Economy)economyProvider.getProvider();
	    }

	    return economy != null;
	}

	public boolean isCitizensInstalled() {
		return citizens;
	}

	public boolean isPrisonUtilsInstalled() {
		return backpacks;
	}

	public boolean isMCMMOInstalled() {
		return mcmmo;
	}

	public void reload() {
		cfg.reload();
		Shop.reset();
		
		for (String shop: cfg.getStringList("list")) {
			if (!shop.equalsIgnoreCase("")) {
				cfg.setDefaultValue("shops." + shop + ".name", "&9" + shop);
				cfg.setDefaultValue("shops." + shop + ".amount", 1);
				cfg.setDefaultValue("shops." + shop + ".itemtype", "CHEST");
				cfg.setDefaultValue("shops." + shop + ".lore", new ArrayList<String>());
				cfg.setDefaultValue("shops." + shop + ".permission", "QuickSell.shop." + shop);
				cfg.setDefaultValue("shops." + shop + ".inheritance", new ArrayList<String>());
				
				if (cfg.getBoolean("options.pregenerate-all-item-prices")) {
					for (Material m: Material.values()) {
						if (m != Material.AIR) cfg.setDefaultValue("shops." + shop + ".price." + m.toString(), 0.0);
					}
				}
				else cfg.setDefaultValue("shops." + shop + ".price.COBBLESTONE", 0.0);
				new Shop(shop);
			}
			else new Shop();
		}
		cfg.save();
	}
	
	public static void registerSellEvent(SellEvent event) {
		events.add(event);
	}

	public static List<SellEvent> getSellEvents() {
		return events;
	}

	public static QuickSell getInstance() {
		return instance;
	}

	public boolean isPrisonGemsInstalled() {
		return prisongems;
	}

	/**
	 * Logs something in the console
	 * @param level Level
	 * @param message Message
	 */
	public void log(Level level, String message) {
		if (level == Level.SEVERE)
			System.err.println("[QuickSell] " + message);

		if (level == Level.INFO)
			System.out.println("[QuickSell] " + message);
	}

	/**
	 * Sets up the plugins messages
	 */
	private void setupMessages() {
		local.setDefaultMessage("messages.sell", "&a&l+ ${MONEY} &7[ &eSold &o{ITEMS} &eItems&7 ]");
		local.setDefaultMessage("messages.no-access", "&4You do not have access to this Shop");
		local.setDefaultMessage("messages.total", "&2TOTAL: &6+ ${MONEY}");
		local.setDefaultMessage("messages.get-nothing", "&4Sorry, but you will get nothing for these Items :(");
		local.setDefaultMessage("messages.dropped", "&cYou have been given back some of your Items because you could not sell them...");
		local.setDefaultMessage("messages.no-permission", "&cYou do not have the required Permission to do this!");
		local.setDefaultMessage("commands.booster.permission", "&cYou do not have permission to activate a Booster!");
		local.setDefaultMessage("commands.permission", "&cYou do not have permission for this!");
		local.setDefaultMessage("commands.usage", "&4Usage: &c%usage%");
		local.setDefaultMessage("commands.reload.done", "&7All Shops have been reloaded!");
		local.setDefaultMessage("messages.unknown-shop", "&cUnknown Shop!");
		local.setDefaultMessage("messages.no-items", "&cSorry, but you have no Items that can be sold!");
		local.setDefaultMessage("commands.price-set", "&7%item% is now worth &a$%price% &7in the Shop %shop%");
		local.setDefaultMessage("commands.shop-created", "&7You successfully created a new Shop called &b%shop%");
		local.setDefaultMessage("commands.shop-deleted", "&cYou successfully deleted the Shop called &4%shop%");
		local.setDefaultMessage("menu.accept", "&a> Click to sell");
		local.setDefaultMessage("menu.estimate", "&e> Click to estimate");
		local.setDefaultMessage("menu.cancel", "&c> Click to cancel");
		local.setDefaultMessage("menu.title", "&6&l$ Sell your Items $");
		local.setDefaultMessage("messages.estimate", "&eYou will get &6${MONEY} &efor these Items");
		local.setDefaultMessage("commands.sellall.usage", "&4Usage: &c/sellall <Shop>");
		local.setDefaultMessage("commands.disabled", "&cThis command has been disabled");
		local.setDefaultMessage("booster.reset", "&cReset %player%'s multiplier to 1.0x");
		local.setDefaultMessage("boosters.reset", "&cReset all Boosters to 1.0x");
		local.setDefaultMessage("commands.prices.usage", "&4Usage: &c/prices <Shop>");
		local.setDefaultMessage("commands.prices.permission", "&cYou do not have permission for this!");
		local.setDefaultMessages("editor.create-shop", "&a&l! &7Please type in a Name for your Shop in Chat!", "&7&oColor Codes are supported!");
		local.setDefaultMessages("editor.rename-shop", "&a&l! &7Please type in a Name for your Shop in Chat!", "&7&oColor Codes are supported!");
		local.setDefaultMessages("editor.renamed-shop", "&a&l! &7Successfully renamed Shop!");
		local.setDefaultMessage("editor.no-inheritance", "&a&l! &cThis shop has no inheritance");
		local.setDefaultMessages("editor.set-permission-shop", "&a&l! &7Please type in a Permission for your Shop!", "&7&oType \"none\" to specify no Permission");
		local.setDefaultMessage("editor.permission-set-shop", "&a&l! &7Successfully specified a Permission for your Shop!");

		local.setDefaultMessage("messages.booster-use.MONETARY", "&a&l+ ${MONEY} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");
		local.setDefaultMessage("messages.booster-use.EXP", "");
		local.setDefaultMessage("messages.booster-use.MCMMO", "");
		local.setDefaultMessage("messages.booster-use.PRISONGEMS", "&7+ &a{GEMS} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");

		local.setDefaultMessage("messages.pbooster-use.MONETARY", "&a&l+ ${MONEY} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");
		local.setDefaultMessage("messages.pbooster-use.EXP", "");
		local.setDefaultMessage("messages.pbooster-use.MCMMO", "");
		local.setDefaultMessage("messages.pbooster-use.PRISONGEMS", "&7+ &a{GEMS} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");

		local.setDefaultMessage("booster.extended.MONETARY", "&6%player% &ehas extended the %multiplier%x Booster (Money) for %time% more Minute/s");
		local.setDefaultMessage("booster.extended.EXP", "&6%player% &ehas extended the %multiplier%x Booster (Experience) for %time% more Minute/s");
		local.setDefaultMessage("booster.extended.MCMMO", "&6%player% &ehas extended the %multiplier%x Booster (mcMMO) for %time% more Minute/s");
		local.setDefaultMessage("booster.extended.PRISONGEMS", "&6%player% &ehas extended the %multiplier%x Booster (Gems) for %time% more Minute/s");

		local.setDefaultMessage("pbooster.extended.MONETARY", "&eYour %multiplier%x Booster (Money) has been extended for %time% more Minute/s");
		local.setDefaultMessage("pbooster.extended.EXP", "&eYour %multiplier%x Booster (Experience) has been extended for %time% more Minute/s");
		local.setDefaultMessage("pbooster.extended.MCMMO", "&eYour %multiplier%x Booster (mcMMO) has been extended for %time% more Minute/s");
		local.setDefaultMessage("pbooster.extended.PRISONGEMS", "&eYour %multiplier%x Booster (Gems) has been extended for %time% more Minute/s");

		local.setDefaultMessage("booster.activate.MONETARY", "&6&l%player% &ehas activated a %multiplier%x Booster (Money) for %time% Minute/s");
		local.setDefaultMessage("booster.activate.EXP", "&6&l%player% &ehas activated a %multiplier%x Booster (Experience) for %time% Minute/s");
		local.setDefaultMessage("booster.activate.MCMMO", "&6&l%player% &ehas activated a %multiplier%x Booster (mcMMO) for %time% Minute/s");
		local.setDefaultMessage("booster.activate.PRISONGEMS", "&6&l%player% &ehas activated a %multiplier%x Booster (Gems) for %time% Minute/s");

		local.setDefaultMessage("booster.deactivate.MONETARY", "&4%player%'s &c%multiplier%x Booster (Money) wore off!");
		local.setDefaultMessage("booster.deactivate.EXP", "&4%player%'s &c%multiplier%x Booster (Experience) wore off!");
		local.setDefaultMessage("booster.deactivate.MCMMO", "&4%player%'s &c%multiplier%x Booster (mcMMO) wore off!");
		local.setDefaultMessage("booster.deactivate.PRISONGEMS", "&4%player%'s &c%multiplier%x Booster (Gems) wore off!");

		local.setDefaultMessage("pbooster.activate.MONETARY", "&eYou have been given a %multiplier%x Booster (Money) for %time% Minute/s");
		local.setDefaultMessage("pbooster.activate.EXP", "&eYou have been given a %multiplier%x Booster (Experience) for %time% Minute/s");
		local.setDefaultMessage("pbooster.activate.MCMMO", "&eYou have been given a %multiplier%x Booster (mcMMO) for %time% Minute/s");
		local.setDefaultMessage("pbooster.activate.PRISONGEMS", "&eYou have been given a %multiplier%x Booster (Gems) for %time% Minute/s");

		local.setDefaultMessage("pbooster.deactivate.MONETARY", "&4Your &c%multiplier%x Booster (Money) wore off!");
		local.setDefaultMessage("pbooster.deactivate.EXP", "&4Your &c%multiplier%x Booster (Experience) wore off!");
		local.setDefaultMessage("pbooster.deactivate.MCMMO", "&4Your &c%multiplier%x Booster (mcMMO) wore off!");
		local.setDefaultMessage("pbooster.deactivate.PRISONGEMS", "&4Your &c%multiplier%x Booster (Gems) wore off!");
	}

}
