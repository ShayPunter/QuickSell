package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sell")
public class SellCommand extends BaseCommand {

    @Dependency("QuickSell")
    public static QuickSell plugin;

    @Default
    public static void onDefault(CommandSender sender, String shopName) {
        if (!QuickSell.cfg.getBoolean("options.enable-commands")) {
            QuickSell.local.sendMessage(sender, "commands.disabled", false);
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
            return;
        }

        if (Shop.list().size() == 1) {
            ShopMenu.open((Player) sender, Shop.list().get(0));
            return;
        }

        Shop shop = Shop.getShop(shopName);
        if (shop != null) {
            if (!shop.hasUnlocked((Player) sender)) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }

            ShopMenu.openMenu((Player) sender);
            return;
        }

        if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) == null) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }
            ShopMenu.open((Player) sender, Shop.getHighestShop((Player) sender));
        }
    }

}
