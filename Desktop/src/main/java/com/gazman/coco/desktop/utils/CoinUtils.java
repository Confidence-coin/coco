package com.gazman.coco.desktop.utils;

import java.text.DecimalFormat;

/**
 * Created by Ilya Gazman on 7/16/2018.
 */
public class CoinUtils {
    private static DecimalFormat commaFormatter = new DecimalFormat("#,###");
    private static DecimalFormat fullNumberFormatter = new DecimalFormat("0.00000000");

    public static String toCoinsString(double amount) {
        String format = fullNumberFormatter.format(amount);
        String[] split = format.split("\\.");

        int goldCoins = Integer.parseInt(split[0]);
        int silverCoins = Integer.parseInt(split[1].substring(0, 4));
        int bronzeCoins = Integer.parseInt(split[1].substring(4));

        if (goldCoins == 0 && silverCoins == 0 && bronzeCoins == 0) {
            return "0";
        }

        String coins = "";
        if (goldCoins > 0) {
            coins += commaFormatter.format(goldCoins) + " Gold";
        }
        if (silverCoins > 0) {
            if (goldCoins > 0) {
                if (bronzeCoins > 0) {
                    coins += ", ";
                } else {
                    coins += " and ";
                }
            }
            coins += commaFormatter.format(silverCoins) + " Silver";
        }
        if (bronzeCoins > 0) {
            if (goldCoins > 0 || silverCoins > 0) {
                coins += " and ";
            }
            coins += commaFormatter.format(bronzeCoins) + " Bronze";
        }

        return coins;
    }
}
