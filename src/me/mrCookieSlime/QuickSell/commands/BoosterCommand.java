package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("booster")
public class BoosterCommand extends BaseCommand {

    @Dependency("QuickSell")
    public static QuickSell plugin;

    @Default
    public static void onDefault(CommandSender sender, String type, String player, Double multi, int duration) {
        BoosterType boosterType = type.equalsIgnoreCase("all") ? null: BoosterType.valueOf(type.toUpperCase());

    }

    @CatchUnknown
    @HelpCommand
    public static void help(CommandSender sender) {
        sendHelpMessage(sender);
    }


    private static void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /boosters <all/monetary/prisongems/exp/mcmmo/casino> <Player> <Multiplier> <Duration in Minutes> stopboosters"));
    }

}
