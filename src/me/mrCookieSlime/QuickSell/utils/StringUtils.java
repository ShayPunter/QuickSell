package me.mrCookieSlime.QuickSell.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StringUtils {

    public static String formatItemName(ItemStack item, boolean includePlural) {
        String name;

        // First check if the item has a custom display name
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            } else {
                // Use the localized name if available (1.13+)
                if (meta.hasLocalizedName()) {
                    name = meta.getLocalizedName();
                } else {
                    // Fall back to formatted material name
                    name = format(item.getType().toString());
                }
            }
        } else {
            // No meta, use formatted material name
            name = format(item.getType().toString());
        }

        if (includePlural) name = item.getAmount() + " " + name + "/s";
        return name;
    }

    public static String format(String string) {
        string = string.toLowerCase();
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String s: string.split("_")) {
            if (i == 0) builder.append(Character.toUpperCase(s.charAt(0)) + s.substring(1));
            else builder.append(" " + Character.toUpperCase(s.charAt(0)) + s.substring(1));
            i++;
        }
        return builder.toString();
    }

    public static boolean contains(String string, String... contain) {
        for (String s: contain) {
            if (string.contains(s)) return true;
        }
        return false;
    }

    public static boolean equals(String string, String... equal) {
        for (String s: equal) {
            if (string.equals(s)) return true;
        }
        return false;
    }

    public static boolean endsWith(String string, String... end) {
        for (String s: end) {
            if (string.endsWith(s)) return true;
        }
        return false;
    }

}
