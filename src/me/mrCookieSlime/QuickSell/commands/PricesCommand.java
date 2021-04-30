package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("price|prices")
@CommandPermission("quicksell.prices")
public class PricesCommand extends BaseCommand {

    @Default
    @Syntax("<Shop Name>")
    public static void onDefault(CommandSender sender, String shopName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
            return;
        }

        Shop shop = Shop.getShop(shopName);
        if (shop != null) {
            if (!shop.hasUnlocked((Player) sender)) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }

            shop.showPrices((Player) sender);
            return;
        } else {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
        }

        if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) == null) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }
            Shop.getHighestShop((Player) sender).showPrices((Player) sender);
            return;
        }
        QuickSell.local.sendMessage(sender, "commands.prices.usage", false);
    }

}
