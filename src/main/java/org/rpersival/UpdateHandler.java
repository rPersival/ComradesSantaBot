package org.rpersival;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class UpdateHandler {
    public static Mono<?> handleMessageCreateEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        if (message.getContent().equalsIgnoreCase("ping")) {
            return message.getChannel()
                    .flatMap(channel -> channel.createMessage("Pong!"));
        }
        return Mono.empty();
    }

    public static Mono<?> handleReadyEvent(ReadyEvent event) {
        return event.getClient().getChannelById(Snowflake.of(1179184584089403403L))
                .ofType(MessageChannel.class)
                .flatMap(y -> y.createMessage("Hello, World!"));
    }

    public static Mono<?> handleApplicationCommand(ChatInputInteractionEvent event) {
        return switch (event.getCommandName()) {
            case Commands.welcome -> event.reply("Hello!");
            default -> throw new IllegalStateException("Unexpected value: " + event.getCommandName());
        };
    }
}
