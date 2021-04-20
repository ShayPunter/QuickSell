package me.mrCookieSlime.QuickSell.handlers;

import io.github.thebusybiscuit.cscorelib2.inventory.ClickAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class MenuHandler {

    @FunctionalInterface
    public interface MenuClickHandler {
        public boolean onClick(Player p, int slot, ItemStack item, ClickAction action);
    }

    public interface AdvancedMenuClickHandler extends MenuClickHandler {
        public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action);
    }

    @FunctionalInterface
    public interface MenuOpeningHandler {
        public void onOpen(Player p);
    }

    @FunctionalInterface
    public interface MenuCloseHandler {
        public void onClose(Player p);
    }

}
