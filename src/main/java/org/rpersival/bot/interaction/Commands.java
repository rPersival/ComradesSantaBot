package org.rpersival.bot.interaction;

import discord4j.discordjson.json.ApplicationCommandRequest;
import org.rpersival.Util;

import java.util.List;
import java.util.Map;

public class Commands {

    public static final String welcome = "welcome";
    public static final String sendSantaMessage = "santa";
    public static final String registerMessage = "register";
    public static final List<ApplicationCommandRequest> GLOBAL;
    public static final List<ApplicationCommandRequest> GUILD;

    // TODO: remove
    @Command(scope = Command.Scope.GUILD)
    public static final ApplicationCommandRequest welcomeCommand = ApplicationCommandRequest.builder()
            .name(welcome)
            .description("hello")
            .build();

    @Command(scope = Command.Scope.GUILD)
    public static final ApplicationCommandRequest santaMessageCommand = ApplicationCommandRequest.builder()
            .name(sendSantaMessage)
            .description("Sends welcome message for Secret Santa (admins only)")
            .build();

    @Command(scope = Command.Scope.GLOBAL)
    public static final ApplicationCommandRequest registerCommand = ApplicationCommandRequest.builder()
            .name(registerMessage)
            .description("Register")
            .build();

    // TODO: implement commands
//    @Command(scope = Command.Scope.GLOBAL)
    public static final ApplicationCommandRequest wishlistCommand = ApplicationCommandRequest.builder()
            .name("")
            .description("")
            .build();

    //    @Command(scope = Command.Scope.GLOBAL)
    public static final ApplicationCommandRequest linkCommand = ApplicationCommandRequest.builder()
            .name("")
            .description("")
            .build();

    //    @Command(scope = Command.Scope.GLOBAL)
    public static final ApplicationCommandRequest messageCommand = ApplicationCommandRequest.builder()
            .name("")
            .description("")
            .build();

    //    @Command(scope = Command.Scope.GLOBAL)
    public static final ApplicationCommandRequest start = ApplicationCommandRequest.builder()
            .name("")
            .description("")
            .build();

    static {
        Map.Entry<List<ApplicationCommandRequest>, List<ApplicationCommandRequest>> lists = getCommands();
        GLOBAL = lists.getKey();
        GUILD = lists.getValue();
    }

    private static Map.Entry<List<ApplicationCommandRequest>, List<ApplicationCommandRequest>> getCommands() {
        return Util.getPartitions(Commands.class, ApplicationCommandRequest.class, Command.class,
                (x) -> x.getAnnotation(Command.class).scope() == Command.Scope.GLOBAL);
    }
}
