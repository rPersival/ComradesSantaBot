package org.rpersival;

import org.rpersival.bot.DiscordBot;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException();
        }

        String token = args[0];
        DiscordBot.start(token);
    }
}