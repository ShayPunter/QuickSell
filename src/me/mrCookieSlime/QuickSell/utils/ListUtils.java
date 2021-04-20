package me.mrCookieSlime.QuickSell.utils;

import java.util.List;

public final class ListUtils {

    private ListUtils() {}

    /**
     * Forms the StringList into a normal sentence with Spaces
     *
     * @param  list The List you want to convert
     * @return      Converted String
     */
    public static String toString(List<String> list) {
        return toString(list.toArray(new String[list.size()]));
    }

    /**
     * Forms the String Array into a normal sentence with Spaces
     *
     * @param  list The List you want to convert
     * @return      Converted String
     */
    public static String toString(String... list) {
        return toString(0, list);
    }

    /**
     * Forms the String Array into a normal sentence with Spaces
     * and excludes the first X entries
     *
     * @param  list The List you want to convert
     * @param  excluded The Start Index for the String Array
     * @return      Converted String
     */
    public static String toString(int excluded, String... list) {
        StringBuilder builder = new StringBuilder();

        for (int i = excluded; i < list.length; i++) {
            if (i > excluded) builder.append(" ");
            builder.append(list[i]);
        }

        return builder.toString();
    }

}
