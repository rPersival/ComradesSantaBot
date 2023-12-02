package org.rpersival.bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.service.ApplicationService;
import org.rpersival.bot.interaction.Commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DiscordBot {
    public static final long COMRADES_ID = 638292530802196491L;

    private final Long applicationId;
    private final GatewayDiscordClient client;
    private static DiscordBot instance;

    private DiscordBot(String token) {
        client = DiscordClient.create(token)
                .gateway().setEntityRetrievalStrategy(EntityRetrievalStrategy.REST)
                .setEnabledIntents(IntentSet.of(
                        Intent.GUILD_MESSAGES,
                        Intent.GUILD_MEMBERS,
                        Intent.GUILDS
                ))
                .login()
                .block();

        if (client == null) {
            throw new RuntimeException();
        }

        applicationId = client.getRestClient().getApplicationId().block();

        initializeCommands();
        subscribeToEvents();

        client.onDisconnect().block();
    }

    public static void start(String token) {
        if (instance == null) {
            instance = new DiscordBot(token);
        }
    }

    public void subscribeToEvents() {
        client.on(ReadyEvent.class, UpdateHandler::handleReadyEvent).subscribe();
        client.on(MessageCreateEvent.class, UpdateHandler::handleMessageCreateEvent).subscribe();
        client.on(ChatInputInteractionEvent.class, UpdateHandler::handleApplicationCommand).subscribe();
    }

    public void initializeCommands() {
        ApplicationService service = client.getRestClient().getApplicationService();

        Map<String, ApplicationCommandData> commandDataMap =
                service.getGuildApplicationCommands(applicationId, COMRADES_ID)
                .collectMap(ApplicationCommandData::name)
                .block();

        if (commandDataMap == null) {
            commandDataMap = Collections.emptyMap();
        }

        List<ApplicationCommandRequest> commands = Commands.getAllCommands();
        for (ApplicationCommandRequest command : commands) {
            if (!commandDataMap.containsKey(command.name())) {
                service.createGuildApplicationCommand(applicationId, COMRADES_ID, command).subscribe();
            }
        }
    }
}
