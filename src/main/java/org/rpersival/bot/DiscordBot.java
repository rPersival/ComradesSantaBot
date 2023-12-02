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
import org.rpersival.bot.interaction.Command;
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

    private void initializeCommands() {
        ApplicationService service = client.getRestClient().getApplicationService();
        initializeCommands(service, Command.Scope.GLOBAL);
        initializeCommands(service, Command.Scope.GUILD);
    }

    private void initializeCommands(ApplicationService service, Command.Scope scope) {
        Map<String, ApplicationCommandData> commandDataMap = getCommandDataMap(service, scope);
        List<ApplicationCommandRequest> commands = scope == Command.Scope.GLOBAL ? Commands.GLOBAL : Commands.GUILD;

        for (ApplicationCommandRequest command : commands) {
            if (!commandDataMap.containsKey(command.name())) {
                if (scope == Command.Scope.GUILD) {
                    service.createGuildApplicationCommand(applicationId, COMRADES_ID, command).subscribe();
                } else {
                    service.createGlobalApplicationCommand(applicationId, command).subscribe();
                }
            }
        }
    }

    private Map<String, ApplicationCommandData> getCommandDataMap(ApplicationService service, Command.Scope scope) {
        return (scope == Command.Scope.GLOBAL ?
                service.getGlobalApplicationCommands(applicationId) :
                service.getGuildApplicationCommands(applicationId, COMRADES_ID))
                .collectMap(ApplicationCommandData::name)
                .blockOptional()
                .orElse(Collections.emptyMap());
    }

    // TODO: remove before release
    public void deleteLocalCommands() {
        ApplicationService service = client.getRestClient().getApplicationService();

        Map<String, ApplicationCommandData> commandDataMap =
                service.getGuildApplicationCommands(applicationId, COMRADES_ID)
                        .collectMap(ApplicationCommandData::name)
                        .block();

        if (commandDataMap == null) {
            return;
        }

        for (ApplicationCommandData data : commandDataMap.values()) {
            service.deleteGuildApplicationCommand(applicationId, COMRADES_ID, data.id().asLong()).subscribe();
        }
    }

    public void deleteGlobalCommands() {
        ApplicationService service = client.getRestClient().getApplicationService();

        Map<String, ApplicationCommandData> commandDataMap =
                service.getGlobalApplicationCommands(applicationId)
                        .collectMap(ApplicationCommandData::name)
                        .block();

        if (commandDataMap == null) {
            return;
        }

        for (ApplicationCommandData data : commandDataMap.values()) {
            service.deleteGlobalApplicationCommand(applicationId, data.id().asLong()).subscribe();
        }
    }

    private void deleteCommands() {
        deleteLocalCommands();
        deleteGlobalCommands();
    }
}
