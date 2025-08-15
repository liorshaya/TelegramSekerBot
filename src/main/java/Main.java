import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            MyBot myBot = new MyBot();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(myBot);


            SwingUtilities.invokeLater(() -> {
                new MainFrame(myBot);
            });


        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
