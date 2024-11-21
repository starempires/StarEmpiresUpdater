package com.starempires.util;

public class StarEmpiresUtils {

    public static String plural(final int number, final String noun) {
        return plural(number, noun, "s");
    }

    public static String plural(final int number, final String noun, final String suffix) {
        String rv = number + " " + noun;
        if (number != 1) {
            rv += suffix;
        }
        return rv;
    }
}