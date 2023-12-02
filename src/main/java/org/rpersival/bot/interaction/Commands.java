package org.rpersival.bot.interaction;

import discord4j.discordjson.json.ApplicationCommandRequest;
import org.rpersival.Util;

import java.util.List;

public class Commands {

    public static final String welcome = "welcome";

    @Command
    public static final ApplicationCommandRequest welcomeCommand = ApplicationCommandRequest.builder()
            .name(welcome)
            .description("hello")
            .build();

    public static List<ApplicationCommandRequest> getAllCommands() {
        return Util.getTargetFields(Commands.class, ApplicationCommandRequest.class);
    }
}
