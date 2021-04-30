package me.mrCookieSlime.QuickSell.boosters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import me.mrCookieSlime.QuickSell.boosters.menu.BoosterMenu;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.QuickSell;

import me.mrCookieSlime.QuickSell.utils.chat.TellRawMessage;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Booster {
	
	public static List<Booster> active = new ArrayList<Booster>();
	
	BoosterType type;
	int id;
	int minutes;
	public String owner;
	double multiplier;
	Date timeout;
	Config cfg;
	boolean silent, infinite;
	Map<String, Integer> contributors = new HashMap<String, Integer>();

	/**
	 * Creates a new Booster
	 * @param multiplier Double
	 * @param silent Boolean
	 * @param infinite Boolean
	 */
	public Booster(double multiplier, boolean silent, boolean infinite) {
		this(BoosterType.MONETARY, multiplier, silent, infinite);
	}

	/**
	 * Creates a new Booster
	 * @param type BoosterType
	 * @param multiplier Double
	 * @param silent Boolean
	 * @param infinite Boolean
	 */
	public Booster(BoosterType type, double multiplier, boolean silent, boolean infinite) {
		this.type = type;
		this.multiplier = multiplier;
		this.silent = silent;
		this.infinite = infinite;
		if (infinite) {
			this.minutes = Integer.MAX_VALUE;
			this.timeout = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
		}
		this.owner = "INTERNAL";
		
		active.add(this);
	}

	/**
	 * Creates a new Booster
	 * @param owner String
	 * @param multiplier Double
	 * @param minutes Integer
	 */
	public Booster(String owner, double multiplier, int minutes) {
		this(BoosterType.MONETARY, owner, multiplier, minutes);
	}

	/**
	 * Creates a new Booster
	 * @param type BoosterType
	 * @param owner String
	 * @param multiplier Double
	 * @param minutes Integer
	 */
	public Booster(BoosterType type, String owner, double multiplier, int minutes) {
		this.type = type;
		this.minutes = minutes;
		this.multiplier = multiplier;
		this.owner = owner;
		this.timeout = new Date(System.currentTimeMillis() + minutes * 60 * 1000);
		this.silent = false;
		this.infinite = false;
		
		contributors.put(owner, minutes);
	}

	/**
	 * Creates a new Booster
	 * @param id Integer
	 * @throws ParseException if the booster ID cannot be found, this exception will occur.
	 */
	public Booster(int id) throws ParseException {
		active.add(this);
		this.id = id;
		this.cfg = new Config(new File(QuickSell.getInstance().getDataFolder() + File.separator + "data-storage/boosters/" + id + ".booster"));
		if (cfg.contains("type")) this.type = BoosterType.valueOf(cfg.getString("type"));
		else {
			cfg.setValue("type", BoosterType.MONETARY.toString());
			cfg.save();
			this.type = BoosterType.MONETARY;
		}
		
		this.minutes = cfg.getInt("minutes");
		this.multiplier = (Double) cfg.getValue("multiplier");
		this.owner = cfg.getString("owner");
		this.timeout = new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString("timeout"));
		this.silent= false;
		this.infinite = false;
		
		if (cfg.contains("contributors." + owner)) {
			for (String key: cfg.getKeys("contributors")) {
				contributors.put(key, cfg.getInt("contributors." + key));
			}
		}
		else {
			contributors.put(owner, minutes);
			writeContributors();
		}
	}

	/**
	 * Writes the boosters contributors to the config
	 */
	private void writeContributors() {
		for (Map.Entry<String, Integer> entry: contributors.entrySet()) {
			cfg.setValue("contributors." + entry.getKey(), entry.getValue());
		}
		
		cfg.save();
	}

	/**
	 * Activates a booster
	 */
	public void activate() {
		if (QuickSell.cfg.getBoolean("boosters.extension-mode")) {
			for (Booster booster: active) {
				if (!booster.getType().equals(this.type) && Double.compare(booster.getMultiplier(), getMultiplier()) != 0)
					return;

				// Extend the booster
				booster.extend(this);
				// If it isn't silenced, send a notification that the booster has been extended.
				if (!silent) {
					if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) {
						QuickSell.local.sendMessage(Bukkit.getPlayer(getOwner()), "pbooster.extended." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
						return;
					}

					QuickSell.local.getMessages("booster.extended." + type.toString()).forEach(message ->
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier())))));
				}
				return;
			}
		}

		// Writes the value to the booster config file
		if (!infinite) {
			for (int i = 0; i < 100000; i++) {
				if (!new File(QuickSell.getInstance().getDataFolder() + File.separator + "data-storage/boosters/" + i + ".booster").exists()) {
					this.id = i;
					break;
				}
			}
			this.cfg = new Config(new File(QuickSell.getInstance().getDataFolder() + File.separator + "data-storage/boosters/" + id + ".booster"));
			cfg.setValue("type", type.toString());
			cfg.setValue("owner", getOwner());
			cfg.setValue("multiplier", multiplier);
			cfg.setValue("minutes", minutes);
			cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
			cfg.setValue("private", this instanceof PrivateBooster ? true: false);
			
			writeContributors();
		}
		
		active.add(this);

		// Notifies the user/server if the booster is active and hasn't been silenced
		if (!silent) {
			if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) {
				QuickSell.local.sendMessage(Bukkit.getPlayer(getOwner()), "pbooster.activate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
				return;
			}

			QuickSell.local.getMessages("booster.activate." + type.toString()).forEach(message ->
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier())))));
		}
	}

	/**
	 * Extends an existing boosters time
	 * @param booster Booster
	 */
	// todo: move into a booster manager class.
	public void extend(Booster booster) {
		addTime(booster.getDuration());
		
		int minutes = contributors.containsKey(booster.getOwner()) ? contributors.get(booster.getOwner()): 0;
		minutes = minutes + booster.getDuration();
		contributors.put(booster.getOwner(), minutes);
		
		writeContributors();
	}

	/**
	 * Deactivates an active booster
	 */
	public void deactivate() {
		if (!silent) {
			if (this instanceof PrivateBooster) {
				if (Bukkit.getPlayer(getOwner()) != null) {
					QuickSell.local.sendMessage(Bukkit.getPlayer(getOwner()), "pbooster.deactivate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
					return;
				}
			}

			QuickSell.local.getMessages("booster.deactivate." + type.toString()).forEach(message ->
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier())))));
		}

		// If the booster isn't infinite, remove the booster data file.
		if (!infinite)
			new File(QuickSell.getInstance().getDataFolder() + File.separator + "data-storage/boosters/" + getID() + ".booster").delete();

		active.remove(this);
	}

	/**
	 * Iterate over the boosters
	 * @return Iterator<Booster>
	 */
	// todo: move into a booster manager class.
	public static Iterator<Booster> iterate() {
		return active.iterator();
	}

	/**
	 * Gets the owner of the booster
	 * @return String
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Gets the multiplier amount
	 * @return Double
	 */
	public Double getMultiplier() {
		return multiplier;
	}

	/**
	 * Gets the duration of the multiplier
	 * @return Integer
	 */
	public int getDuration() {
		return minutes;
	}

	/**
	 * Get the boosters expiration date
	 * @return Date
	 */
	public Date getDeadLine() {
		return timeout;
	}

	/**
	 * Get the boosters ID
	 * @return int
	 */
	public int getID() {
		return id;
	}

	/**
	 * Format the remaining time on the booster
	 * @return Long
	 */
	// todo: rename to a more appropriate method/move to boostermanager class.
	public long formatTime() {
		return ((getDeadLine().getTime() - new Date().getTime()) / (1000 * 60));
	}

	/**
	 * Add time to the booster
	 * @param minutes Integer
	 */
	// todo: move into a booster manager class.
	public void addTime(int minutes) {
		timeout = new Date(timeout.getTime() + minutes * 60 * 1000);
		cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
		cfg.save();
	}

	/**
	 * Checks if any of the boosters have expired, if it has, it will remove them.
	 */
	// todo: move into a booster manager class.
	public static void update() {
		Iterator<Booster> boosters = Booster.iterate();

		boosters.forEachRemaining(booster -> {
			if (new Date().after(booster.getDeadLine())) {
				boosters.remove();
				booster.deactivate();
			}
		});
	}

	/**
	 * Gets a list of boosters a player currently owns
	 * @param player String
	 * @return List<Booster>
	 */
	// todo: move into a booster manager class.
	public static List<Booster> getBoosters(String player) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();

		active.forEach(booster -> {
			if (booster.getAppliedPlayers().contains(player))
				boosters.add(booster);
		});

		return boosters;
	}

	/**
	 * Gets a list of boosters a player currently owns
	 * @param player String
	 * @param type BoosterType
	 * @return List<Booster>
	 */
	// todo: move into a booster manager class.
	public static List<Booster> getBoosters(String player, BoosterType type) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();

		active.forEach(booster -> {
			if (booster.getAppliedPlayers().contains(player) && booster.getType().equals(type))
				boosters.add(booster);
		});

		return boosters;
	}

	/**
	 * Gets a list of players the booster is applying to
	 * @return List<String>
	 */
	public List<String> getAppliedPlayers() {
		List<String> players = new ArrayList<String>();
		for (Player p: Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}

	/**
	 * Gets the booster use message
	 * @return String
	 */
	public String getMessage() {
		return "messages.booster-use." + type.toString();
	}

	/**
	 * Gets the booster type
	 * @return BoosterType
	 */
	public BoosterType getType() {
		return this.type;
	}

	/**
	 * Gets if the booster is silent
	 * @return Boolean
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * Get the boosters readable name
	 * @return String
	 */
	public String getUniqueName() {
		switch(type) {
		case EXP:
			return "Booster (Experience)";
		case MONETARY:
			return "Booster (Money)";
		default:
			return "Booster";
		}
	}

	public static double getMultiplier(String name, BoosterType type) {
		double multiplier = 1.0;
		for (Booster booster: getBoosters(name, type)) {
			multiplier = multiplier * booster.getMultiplier();
		}
		return DoubleHandler.fixDouble(multiplier, 2);
	}

	/**
	 * Get if the booster is a private booster
	 * @return Boolean
	 */
	public boolean isPrivate() {
		return this instanceof PrivateBooster;
	}

	/**
	 * Get if the booster is permanent/infinite
	 * @return Boolean
	 */
	public boolean isInfinite() {
		return this.infinite;
	}

	/**
	 * Get a list of players who contributed to the booster
	 * @return Map<String, Integer>
	 */
	public Map<String, Integer> getContributors() {
		return this.contributors;
	}

	/**
	 * Sends a message to a player
	 * @param p Player
	 * @param variables Variable
	 */
	// todo: move into a booster manager class.
	public void sendMessage(Player p, Variable... variables) {
		List<String> messages = QuickSell.local.getMessages(getMessage());
		if (messages.isEmpty()) return;
		try {
			String message = ChatColor.translateAlternateColorCodes('&', messages.get(0).replace("%multiplier%", String.valueOf(this.multiplier)).replace("%minutes%", String.valueOf(this.formatTime())));
			for (Variable v: variables) {
				message = v.apply(message);
			}
			new TellRawMessage()
			.addText(message)
			.addClickEvent(TellRawMessage.ClickAction.RUN_COMMAND, "/boosters")
			.addHoverEvent(TellRawMessage.HoverAction.SHOW_TEXT, BoosterMenu.getTellRawMessage(this))
			.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
