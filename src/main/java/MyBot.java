import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class MyBot extends TelegramLongPollingBot {
    public static final String BOT_TOKEN = "8015288522:AAEPcTr6voCYh_00iWqCe1mFPyXvlmn0STM";
    public static final String BOT_USERNAME = "LNSeker_BOT";

    private static final String DATA_DIR = "src/data/";
    private static final String FILE_PATH = DATA_DIR + "users.csv";

    private Map<Long, UserState> userStates = new HashMap<>();

    private UserManager userManager = new UserManager();


    public String getBotToken(){
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }


    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        System.out.println("HERE1");
        if (!userManager.isUserExists(chatId)){
            System.out.println("HERE2");
            userManager.addUser(chatId);

            sendNewMemberJoin(update.getMessage().getFrom().getFirstName(),update.getMessage().getFrom().getLastName());
        }
    }

    private void sendNewMemberJoin(String firstName,String lastName){
        String text = "חבר חדש בקהילה! " + "\n" +
                "ברוכים הבאים ל " + firstName + " " + lastName + "!" + "\n" +
                "עכשיו גדלנו ל- " + userManager.getNumberOfUsers() + " משתמשים!";

        for (Long chatId : userManager.getAllUsers()){
            sendAllMessage(chatId, text);
        }

    }


    private void sendAllMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
