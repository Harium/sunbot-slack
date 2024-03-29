package com.harium.suneidesis.chat.slack;

import com.harium.suneidesis.chat.Parser;
import com.harium.suneidesis.chat.box.BaseChatBox;
import com.harium.suneidesis.chat.input.InputContext;
import com.harium.suneidesis.chat.output.BaseOutput;
import com.harium.suneidesis.chat.output.Output;
import com.harium.suneidesis.chat.output.OutputContext;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.files.FilesUploadRequest;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.socket_mode.SocketModeClient;

import java.io.File;
import java.util.Collections;

public class Slack extends BaseChatBox {

    private static final String THREAD_ID = "thread_ts";

    private String botToken;
    private String appToken;

    private App app;

    public Slack(String botToken, String appToken) {
        this.botToken = botToken;
        this.appToken = appToken;
    }

    @Override
    public void init() {
        AppConfig appConfig = AppConfig.builder().singleTeamBotToken(botToken).build();
        app = new App(appConfig);

        // Read all message events
        app.event(MessageEvent.class, initHandler());
        try {
            new SocketModeApp(appToken, SocketModeClient.Backend.JavaWebSocket, app).startAsync();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void addParser(Parser parser) {
        parsers.add(parser);
    }

    /**
     * @param channel - channel name or channelID
     * @param message - the text to be sent in the channel
     */
    @Override
    public void sendMessage(String channel, String message) {
        AsyncMethodsClient methods = app.slack().methodsAsync(botToken);
        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(channel) // Use a channelID is preferable
                .text(message)
                .build();

        methods.chatPostMessage(request);
    }

    private BoltEventHandler<MessageEvent> initHandler() {
        return new BoltEventHandler<MessageEvent>() {
            @Override
            public Response apply(EventsApiPayload<MessageEvent> eventsApiPayload, EventContext eventContext) {

                app.executorService().execute(() -> {
                    InputContext inputContext = buildContext(eventsApiPayload.getEvent(), eventContext);

                    if (output == null) {
                        Output output = new SlackOutput(eventContext);
                        parse(inputContext, output);
                    } else {
                        parse(inputContext, output);
                    }
                });

                return eventContext.ack();
            }

            private InputContext buildContext(MessageEvent event, EventContext eventContext) {
                InputContext context = new InputContext();
                context.setSentence(event.getText());
                context.getProperties().put(InputContext.USER_ID, event.getUser());
                context.getProperties().put(InputContext.CHANNEL_ID, eventContext.getChannelId());
                context.getProperties().put(InputContext.CHANNEL_NAME, event.getChannel());
                context.getProperties().put(THREAD_ID, event.getEventTs());

                return context;
            }
        };
    }

    private class SlackOutput extends BaseOutput {
        private final EventContext eventContext;

        public SlackOutput(EventContext eventContext) {
            this.eventContext = eventContext;
        }

        @Override
        public void print(String sentence, OutputContext context) {
            String channel = eventContext.getChannelId();
            AsyncMethodsClient methods = app.slack().methodsAsync(botToken);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channel) // Use a channelID is preferable
                    .text(sentence)
                    .build();

            methods.chatPostMessage(request);
        }

        @Override
        public void produceFile(String path, String description) {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("File not found: " + path);
                return;
            }

            AsyncMethodsClient methods = app.slack().methodsAsync(botToken);
            String channel = eventContext.getChannelId();
            FilesUploadRequest request = FilesUploadRequest.builder()
                    .title(description)
                    .channels(Collections.singletonList(channel))
                    .file(file)
                    .build();

            methods.filesUpload(request);
        }

        @Override
        public void produceFile(byte[] data, String description) {
            AsyncMethodsClient methods = app.slack().methodsAsync(botToken);
            String channel = eventContext.getChannelId();
            FilesUploadRequest request = FilesUploadRequest.builder()
                    .title(description)
                    .channels(Collections.singletonList(channel))
                    .fileData(data)
                    .build();

            methods.filesUpload(request);
        }
    }

    public App getApp() {
        return app;
    }
}
