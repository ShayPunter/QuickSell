package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.interfaces.SellEvent;
import me.mrCookieSlime.QuickSell.Shop;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sellall")
public class SellAllCommand extends BaseCommand {

    @Default
    @Syntax("[Shop Name]")
    public static void onDefault(CommandSender sender, @Optional String shopName) {
        if (!QuickSell.cfg.getBoolean("options.enable-commands")) {
            QuickSell.local.sendMessage(sender, "commands.disabled", false);
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
            return;
        }

        if (shopName == null) {
            if (Shop.getHighestShop((Player) sender) == null) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }
            Shop.getHighestShop((Player) sender).sellall((Player) sender, "", SellEvent.Type.SELLALL);
            return;
        }

        Shop shop = Shop.getShop(shopName);
        if (shop != null) {
            if (!shop.hasUnlocked((Player) sender)) {
                QuickSell.local.sendMessage(sender, "messages.no-access", false);
                return;
            }

            shop.sellall((Player) sender, "", SellEvent.Type.SELLALL);
        } else {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
        }
    }

}
