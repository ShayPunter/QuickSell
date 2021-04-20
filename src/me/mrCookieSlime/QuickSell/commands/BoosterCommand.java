package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@CommandAlias("booster")
@CommandPermission("quicksell.booster")
public class BoosterCommand extends BaseCommand {

    @Dependency("QuickSell")
    public static QuickSell plugin;

    @Default
    public static void onDefault(CommandSender sender, String type, String player, Double multi, int duration) {
        BoosterType boosterType = type.equalsIgnoreCase("all") ? null : BoosterType.valueOf(type.toUpperCase());

        if (boosterType != null) {
            Booster booster = new Booster(boosterType, player, multi, duration);
            booster.activate();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have activated a " + multi + " " + boosterType + " booster for " + duration + " minutes!"));
            return;
        }

        for (BoosterType bt: BoosterType.values()) {
            Booster booster = new Booster(bt, player, multi, duration);
            booster.activate();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have activated a " + multi + " " + bt + " booster for " + duration + " minutes!"));
        }
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
