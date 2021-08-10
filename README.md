# sunbot-slack
Plugin to turn your Suneidesis Chatbot into a Slack Bot

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.harium.suneidesis.sunbot/slack/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.harium.suneidesis.sunbot/slack/)

## How to setup your Slack App
- Enable Socket Mode
- Add Event Subscriptions: message.channels and message.im
- Add Oauth Permissions: chat:write and im:read

Get both Bot and App tokens 

## How to use it

```
    Parser bot = new EchoBox(); // Use your own parser

    String botToken = "xoxb-123456-MY_TOKEN";
    String appToken = "xapp-654321-MY_TOKEN";

    Slack slack = new Slack(botToken, appToken);
    slack.init();
    slack.addParser(bot);
```
