package me.mrCookieSlime.QuickSell.utils;

import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.utils.encoding.Converter;
import me.mrCookieSlime.QuickSell.utils.encoding.EncodingType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class PlayerRunnable {

    public static Map<String, PlayerRunnable> map = new HashMap<>();

    private long timestamp;
    private String id;

    public abstract void run(Player p);

    public PlayerRunnable() {
        this(-1);
    }

    public PlayerRunnable(int expire_in_minutes) {
        this.timestamp = System.nanoTime();

        id = Converter.encode(EncodingType.BASE64, String.valueOf(timestamp));
        map.put(id, this);

        if (expire_in_minutes >= 0) {
            QuickSell.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(QuickSell.getInstance(), new Runnable() {

                @Override
                public void run() {
                    map.remove(id);
                }
            }, expire_in_minutes * 60L * 20L);
        }
    }

    public static void run(String id, Player p) {
        if (!map.containsKey(id)) {
            return;
        }

        map.get(id).run(p);
    }

    public String getID() {
        return id;
    }

}
