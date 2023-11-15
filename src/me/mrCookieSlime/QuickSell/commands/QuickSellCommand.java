package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.utils.StringUtils;
import me.mrCookieSlime.QuickSell.utils.Variable;
import me.mrCookieSlime.QuickSell.utils.maths.DoubleHandler;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

@CommandAlias("quicksell|qs")
@CommandPermission("quicksell.manage")
public class QuickSellCommand extends BaseCommand {

    @Dependency("QuickSell")
    public static QuickSell plugin;

    @Default
    @CatchUnknown
    @HelpCommand
    public static void onDefault(CommandSender sender) {
        sendHelpMessager(sender);
    }

    @Subcommand("reload")
    public static void onReload(CommandSender sender) {
        plugin.reload();
        QuickSell.local.sendMessage(sender, "commands.reload.done", false);
    }

    @Subcommand("editor")
    public static void onEditor(CommandSender sender) {
        if (sender instanceof Player) plugin.editor.openEditor((Player) sender);
    }

    @Subcommand("edit")
    @Syntax("<Shop Name> <Item> <Price>")
    public static void onEdit(CommandSender sender, String shop, String item, Double price) {
        if (Shop.getShop(shop) == null) {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
            return;
        }

        if (item == null || price == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell edit <ShopName> <Item> <Price>"));
        }

        QuickSell.cfg.setValue("shops." + shop + ".price." + item.toUpperCase(), price);
        QuickSell.cfg.save();
        plugin.reload();
        QuickSell.local.sendMessage(sender, "commands.price-set", false
                                    , new Variable("%item%", item.toUpperCase())
                                    , new Variable("%price%", DoubleHandler.getFancyDouble(price))
                                    , new Variable("%shop%", shop));
    }

    @Subcommand("create")
    @Syntax("<Shop Name>")
    public static void onCreate(CommandSender sender, String shopName) {
        if (shopName == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell create <ShopName>"));
        }

        List<String> shops = QuickSell.cfg.getStringList("list");
        shops.add(shopName);
        QuickSell.cfg.setValue("list", shops);
        QuickSell.cfg.save();

        plugin.reload();

        QuickSell.local.sendMessage(sender, "commands.shop-created", false, new Variable("%shop%", shopName));
    }

    @Subcommand("delete")
    @Syntax("<Shop Name>")
    public static void onDelete(CommandSender sender, String shopName) {
        if (shopName == null) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell delete <ShopName>"));
        }

        List<String> shops = QuickSell.cfg.getStringList("list");
        shops.remove(shopName);
        QuickSell.cfg.setValue("list", shops);
        QuickSell.cfg.save();

        plugin.reload();

        QuickSell.local.sendMessage(sender, "commands.shop-deleted", false, new Variable("%shop%", shopName));
    }

    @Subcommand("linknpc")
    @Syntax("<Shop Name> <sell/sellall>")
    public static void onNPCLink(CommandSender sender, String shopName, String type) {
        if (!plugin.isCitizensInstalled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Citizens is not installed on this server!"));
            return;
        }

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);

        if (npc == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must select an NPC before linking it!"));
            return;
        }

        if (Shop.getShop(shopName) == null) {
            QuickSell.local.sendMessage(sender, "messages.unknown-shop", false);
            return;
        }

        if (!type.equalsIgnoreCase("sell") && !type.equalsIgnoreCase("sellall")) {
            QuickSell.local.sendMessage(sender, "commands.usage", false, new Variable("%usage%", "/quicksell linknpc <ShopName> <sell/sellall>"));
            return;
        }

        plugin.npcs.setValue(String.valueOf(npc.getId()), shopName + " ; " + type.toUpperCase());
        plugin.npcs.save();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', npc.getName() + " &7is now a Remote &r" + StringUtils.format(type) + "&7Shop for the Shop &r" + shopName + "&7"));
    }

    @Subcommand("unlinknpc")
    public static void unLinkNPC(CommandSender sender) {
        if (!plugin.isCitizensInstalled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Citizens is not installed on this server!"));
            return;
        }

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);

        if (npc == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must select an NPC before linking it!"));
            return;
        }

        if (!plugin.npcs.contains(String.valueOf(npc.getId()))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', npc.getName() + " &cis not linked to any Shop!"));
            return;
        }

        plugin.npcs.setValue(String.valueOf(npc.getId()), null);
        plugin.npcs.save();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', npc.getName() + " &cis no longer linked to any Shop!"));
    }

    @Subcommand("stopboosters")
    @Syntax("[Player]")
    public static void stopBoosters(CommandSender sender, @Optional String player) {
        Iterator<Booster> boosters = Booster.iterate();

        if (player == null) {
            while(boosters.hasNext()) {
                Booster booster = boosters.next();
                boosters.remove();
                booster.deactivate();
            }
            QuickSell.local.sendMessage(sender, "boosters.reset", false);
            return;
        }

        while (boosters.hasNext()) {
            Booster booster = boosters.next();
            if (booster.getAppliedPlayers().contains(player)) {
                boosters.remove();
                booster.deactivate();
            }
        }
        QuickSell.local.sendMessage(sender, "booster.reset", false, new Variable("%player%", player));
    }

    private static void sendHelpMessager(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lQuickSell v" + plugin.getDescription().getVersion() + " by &6mrCookieSlime"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell: &bDisplays this Help Menu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell reload: &bReloads all of QuickSell's Files and Systems"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell editor: &bOpens up the Ingame Shop Editor"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell stopboosters [Player]: &bStops certain Boosters"));
        if (plugin.isCitizensInstalled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell linknpc <Shop> <sell/sellall>: &bLinks a Citizens NPC to a Shop"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7\u21E8 /quicksell unlinknpc: &bUnlinks your selected NPC from a Shop"));
        }
    }

}
