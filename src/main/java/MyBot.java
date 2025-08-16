import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
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

    private PollValidator validator = new PollValidator(userManager);

    private VotesCsvManager votesCsvManager = new VotesCsvManager();

    private PollMapCsvManager pollMapCsvManager = new PollMapCsvManager();


    public String getBotToken(){
        return Constant.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return Constant.BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasPollAnswer()) {
            handlePollAnswer(update.getPollAnswer());
            pollsCsvManager.updatePollStatuses();
            return;
        }

        if (!update.hasMessage()) return;

        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();

        if (!userManager.isUserExists(chatId)) {
            userManager.addUser(chatId);
            sendNewMemberJoin(firstName, lastName);
        }




        SendPoll poll = new SendPoll();
        poll.setChatId(chatId.toString());

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

    private void handlePollAnswer(PollAnswer pollAnswer) {
        String telegramPollId = pollAnswer.getPollId();
        int selectedIndex = pollAnswer.getOptionIds().isEmpty() ? -1 : pollAnswer.getOptionIds().get(0);
        if (selectedIndex == -1) return;

        // שלב 1: מצא את question_id המתאים ל־telegramPollId
        String questionId = pollMapCsvManager.getQuestionIdByPollId(telegramPollId);
        if (questionId == null) {
            System.err.println("❌ לא נמצאה התאמה בין telegram_poll_id לשאלה");
            return;
        }

        // שלב 2: מצא את ה־PollId הפנימי (מתוך questions.csv)
        int questionPollId = pollManager.getPollIdFromQuestionId(questionId);
        if (questionPollId == -1) {
            System.err.println("❌ לא נמצא PollId פנימי עבור questionId " + questionId);
            return;
        }

        // שלב 3: בדוק מהו הפול הפעיל הנוכחי
        int activePollId = pollsCsvManager.getActivePollId();
        if (questionPollId != activePollId) {
            System.err.println("⚠️ התקבלה הצבעה לפול שאינו פעיל: " + questionPollId + " (פעיל כעת: " + activePollId + ")");
            return;
        }

        // שלב 4: אשר את ההצבעה
        votesCsvManager.recordVote(questionId, selectedIndex);
        System.out.println("✅ הצבעה עודכנה: שאלה " + questionId + ", אופציה " + selectedIndex);
    }




}
