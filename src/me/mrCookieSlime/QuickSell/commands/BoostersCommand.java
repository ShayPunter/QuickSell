package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.mrCookieSlime.QuickSell.boosters.BoosterMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("boosters")
public class BoostersCommand extends BaseCommand {

    @Default
    public static void onDefault(CommandSender sender, String shopName) {
        if (sender instanceof Player)
            BoosterMenu.showBoosterOverview((Player) sender);
    }

}
