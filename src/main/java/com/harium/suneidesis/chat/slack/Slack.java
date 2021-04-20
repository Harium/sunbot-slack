package com.harium.suneidesis.chat.slack;

import com.harium.suneidesis.chat.Parser;
import com.harium.suneidesis.chat.box.BoxHandler;
import com.harium.suneidesis.chat.input.InputContext;
import com.harium.suneidesis.chat.output.Output;
import com.harium.suneidesis.chat.output.OutputContext;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.socket_mode.SocketModeClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Slack implements BoxHandler {

    private String botToken;
    private String appToken;

    private App app;

    private List<Parser> parsers = new ArrayList<>();

    public Slack(String botToken, String appToken) {
        this.botToken = botToken;
        this.appToken = appToken;
    }

    public void connect() throws Exception {
        AppConfig appConfig = AppConfig.builder().singleTeamBotToken(botToken).build();
        app = new App(appConfig);

        // Read all message events
        app.event(MessageEvent.class, initHandler());
        new SocketModeApp(appToken, SocketModeClient.Backend.JavaWebSocket, app).startAsync();
    }

    @Override
    public void addParser(Parser parser) {
        parsers.add(parser);
    }

    @Override
    public void sendMessage(String channel, String message) {
        throw new RuntimeException("Not implemented yet.");
    }

    private BoltEventHandler<MessageEvent> initHandler() {
        return new BoltEventHandler<MessageEvent>() {
            @Override
            public Response apply(EventsApiPayload<MessageEvent> eventsApiPayload, EventContext eventContext) {
                InputContext inputContext = buildContext(eventsApiPayload.getEvent(), eventContext);

                for (Parser parser : parsers) {
                    if (parser.parse(inputContext, new SlackOutput(eventContext))) {
                        break;
                    }
                }

                return eventContext.ack();
            }

            private InputContext buildContext(MessageEvent event, EventContext eventContext) {
                InputContext context = new InputContext();
                context.setSentence(event.getText());
                context.getProperties().put(InputContext.USER_ID, event.getUser());
                context.getProperties().put(InputContext.USER_USERNAME, event.getUser());
                context.getProperties().put(InputContext.CHANNEL_ID, eventContext.getChannelId());
                context.getProperties().put(InputContext.CHANNEL_NAME, event.getChannel());

                return context;
            }
        };
    }

    private class SlackOutput implements Output {
        private final EventContext eventContext;

        public SlackOutput(EventContext eventContext) {
            this.eventContext = eventContext;
        }

        @Override
        public void print(String sentence, OutputContext context) {
            try {
                eventContext.say(sentence);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SlackApiException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void produceFile(String path, String description) {
            throw new RuntimeException("Not implemented yet.");
        }
    }
}
