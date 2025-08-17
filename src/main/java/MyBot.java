import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class MyBot extends TelegramLongPollingBot {


    private static final String DATA_DIR = "src/data/";
    private static final String FILE_PATH = DATA_DIR + "users.csv";


    private final Map<Long, UserState> userStates = new HashMap<>();

    private final UserManager userManager = new UserManager();

    private final PollManager pollManager = new PollManager();

    private final PollsCsvManager pollsCsvManager = new PollsCsvManager();

    private final PollValidator validator = new PollValidator(userManager);

    private final VotesCsvManager votesCsvManager = new VotesCsvManager();

    private final PollMapCsvManager pollMapCsvManager = new PollMapCsvManager();


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
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handlePollAnswer(PollAnswer pollAnswer) {
        String telegramPollId = pollAnswer.getPollId();
        if (pollAnswer.getOptionIds() == null || pollAnswer.getOptionIds().isEmpty()) return;

        int selectedIndex = pollAnswer.getOptionIds().get(0); // single-select

        // ✅ קודם מה-cache (עם המתנה קצרה למקרה שהמשתמש לחץ “מהר מדי”)
        String questionId = pollMapCsvManager.getFromCacheOrWait(telegramPollId, 1200, 100);
        if (questionId == null) {
            System.err.println("⚠️ לא נמצא מיפוי ל-pollId=" + telegramPollId + " — דילוג");
            return;
        }

        // אימות רך שזו באמת שאלה של הסקר הפעיל
        int qPollId = pollManager.getPollIdFromQuestionId(questionId);
        int active  = pollsCsvManager.getActivePollId();
        if (qPollId != -1 && active != -1 && qPollId != active) {
            System.out.println("ℹ️ הצבעה לשאלה " + questionId + " שאינה בסקר הפעיל (qPollId=" + qPollId + ", active=" + active + "). ממשיך.");
        }

        votesCsvManager.recordVote(questionId, selectedIndex);

        int total = votesCsvManager.getNumberOfVotesForQuestion(questionId);
        System.out.println("✅ הצבעה עודכנה: שאלה " + questionId + ", אופציה " + selectedIndex + " | סה\"כ קולות: " + total);

        pollsCsvManager.updatePollStatuses();
    }




}
