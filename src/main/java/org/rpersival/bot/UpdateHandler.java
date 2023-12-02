package org.rpersival.bot;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import org.rpersival.database.DatabaseManager;
import org.rpersival.bot.interaction.Buttons;
import org.rpersival.bot.interaction.Commands;
import org.rpersival.database.UserStatus;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.Optional;

public class UpdateHandler {
    private static final ActionRow acceptButtonRow =
            ActionRow.of(Buttons.acceptRulesButton, Buttons.doNotAcceptRulesButton);

    private static final String registerMessage = "Good";

    public static Mono<?> onMessageReceived(MessageCreateEvent event) {
        Message message = event.getMessage();
        Snowflake userId = message.getAuthor().orElseThrow().getId();

        if (message.getAuthor().flatMap(x -> Optional.of(x.isBot())).orElse(false)) {
            return Mono.empty();
        }

        try {
            if (DatabaseManager.exists(userId.asLong())
                    && DatabaseManager.getStatus(userId.asLong()) == UserStatus.AWAITING_DESCRIPTION_MESSAGE) {
                DatabaseManager.setDescription(userId.asLong(), message.getContent());
                DatabaseManager.setStatus(userId.asLong(), UserStatus.EMPTY);
            } else {
                return Mono.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: implement
            return message.getChannel().flatMap(x -> x.createMessage("Error"));
        }

        return message.getChannel().flatMap(x -> {
            try {
                return x.createMessage(String.valueOf(DatabaseManager.getDescription(userId.asLong())));
            } catch (SQLException e) {
                e.printStackTrace();
                return Mono.error(new RuntimeException(e));
            }
        });
    }

    public static Mono<?> onStart(ReadyEvent event) {
        return event.getClient().getChannelById(Snowflake.of(1179184584089403403L))
                .ofType(MessageChannel.class)
                .flatMap(y -> y.createMessage("Hello, World!"));
    }

    public static Mono<?> onChatInteraction(ChatInputInteractionEvent event) {
        switch (event.getCommandName()) {
            case Commands.welcome -> {
                return event.reply("Hello!");
            }

            case Commands.sendSantaMessage -> {
                return event.reply("ok").withComponents(ActionRow.of(Buttons.registerButton));
            }

            case Commands.registerMessage -> {
                return event.getInteraction().getChannel().flatMap(channel -> {
                    if (channel instanceof PrivateChannel) {
                        return event.reply(registerMessage).withComponents(acceptButtonRow);
                    } else {
                        return event.reply("Bad").withEphemeral(true);
                    }
                });
            }

            default -> throw new IllegalStateException("Unexpected value: " + event.getCommandName());
        }
    }

    public static Mono<?> onButtonClick(ButtonInteractionEvent event) {
        switch (event.getCustomId()) {
            case Buttons.registerId -> {
                return handleRegisterButton(event);
            }

            case Buttons.acceptId -> {
                long userId = event.getInteraction().getUser().getId().asLong();

                try {
                    DatabaseManager.addUser(userId);
                    DatabaseManager.setStatus(userId, UserStatus.AWAITING_DESCRIPTION_MESSAGE);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return event.reply("implement accept");
            }

            case Buttons.doNotAcceptId -> {
                // TODO: implement
                return event.reply("implement do not accept").and(event.getMessage().orElseThrow().delete());
            }

            default -> throw new IllegalStateException("Unexpected value: " + event.getCustomId());
        }
    }

    private static Mono<?> handleRegisterButton(ButtonInteractionEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();

        try {
            if (DatabaseManager.exists(userId)) {
                return event.reply("The user has already been registered.");
            }
        } catch (SQLException e) {
            // something probably went wrong; maybe there's a connection issue
            e.printStackTrace();
            return event.reply("Something went wrong." +
                    " Please contact the admin to let them know that there's something wrong with their database");
        }

        return event.getClient().getUserById(Snowflake.of(userId))
                .flatMap(User::getPrivateChannel)
                .flatMap(privateChannel -> privateChannel.createMessage(registerMessage)
                        .withComponents(acceptButtonRow)).and(
                        event.reply("Check your PM").withEphemeral(true)).doOnError(x -> {
                            // user should open their PM
                            x.printStackTrace();
                        });
    }
}
