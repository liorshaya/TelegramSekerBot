import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class MyBot extends TelegramLongPollingBot {


    private static final String DATA_DIR = "src/data/";
    private static final String FILE_PATH = DATA_DIR + "users.csv";

    private Map<Long, UserState> userStates = new HashMap<>();

    private UserManager userManager = new UserManager();

    private PollManager pollManager = new PollManager();

    private PollsCsvManager pollsCsvManager = new PollsCsvManager();


    public String getBotToken(){
        return Constant.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return Constant.BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(pollManager.getPollById(5));
        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        if (!userManager.isUserExists(chatId)){
            userManager.addUser(chatId);
            sendNewMemberJoin(firstName,lastName);
        }
    }

    private void sendNewMemberJoin(String firstName,String lastName){
        String text = "חבר חדש בקהילה! " + "\n" +
                "ברוכים הבאים ל " + firstName + " " + lastName + "!" + "\n" +
                "עכשיו גדלנו ל- " + userManager.getNumberOfUsers() + " משתמשים!";

        broadcastMessage(text);
    }

    public void broadcastMessage(String text) {
        for (Long chatId : userManager.getAllUsers()) {
            sendPersonalMessage(chatId, text);
        }
    }

    private void sendPersonalMessage(Long chatId, String text) {
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
