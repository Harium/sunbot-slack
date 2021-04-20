package examples;

import com.harium.suneidesis.chat.Parser;
import com.harium.suneidesis.chat.box.EchoBox;
import com.harium.suneidesis.chat.slack.Slack;

import java.io.IOException;

public class BasicExample {

    public static void main(String[] args) throws Exception {
        Parser bot = new EchoBox();

        String botToken = "BOT_TOKEN";
        String appToken = "APP_TOKEN";

        Slack slack = new Slack(botToken, appToken);
        slack.connect();
        slack.addParser(bot);
    }

}
