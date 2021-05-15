package examples;

import com.harium.suneidesis.chat.Parser;
import com.harium.suneidesis.chat.box.EchoBox;
import com.harium.suneidesis.chat.slack.Slack;

public class BasicExample {

    public static void main(String[] args) throws Exception {
        Parser bot = new EchoBox();

        String botToken = "xoxb-123456-MY_TOKEN";
        String appToken = "xapp-654321-MY_TOKEN";

        Slack slack = new Slack(botToken, appToken);
        slack.init();
        slack.addParser(bot);
    }

}
