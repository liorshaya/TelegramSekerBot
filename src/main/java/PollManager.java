import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class PollManager {
    private static final String DATA_DIR = "src/data/";
    private static final String FILE_PATH = DATA_DIR + "questions.csv";

    private final PollMapCsvManager pollMapManager = new PollMapCsvManager();
    private final PollsCsvManager pollsCsvManager = new PollsCsvManager();


    public PollManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(FILE_PATH);
        boolean fileExists = file.exists();

        if (!fileExists) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(new String[]{"ID", "Q", "A1", "A2", "A3", "A4", "PollId"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void addPollWithQuestions(String questionsText, int minutes, TelegramLongPollingBot bot, UserManager userManager) {
        LocalDateTime pollTime = LocalDateTime.now().plusMinutes(minutes);

        PollsCsvManager pollsManager = new PollsCsvManager();
        int pollId = pollsManager.createPoll(pollTime);

        PollScheduler scheduler = new PollScheduler();
        scheduler.schedulePollActivation(pollTime, pollId, this, bot, userManager);

        int nextQuestionId = getNextId();
        List<String> createdQuestionIds = new ArrayList<>();

        Scanner scanner = new Scanner(questionsText);
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",", -1);  // שמירה גם על שדות ריקים
                    if (parts.length < 5) continue;         // שאלה + לפחות 2 תשובות

                    String[] fullRow = new String[8];       // בדיוק 7 שדות
                    fullRow[0] = String.valueOf(nextQuestionId); // questionId
                    fullRow[1] = parts[0];                       // השאלה

                    // הכנסת עד 4 תשובות
                    for (int i = 0; i < 4; i++) {
                        fullRow[2 + i] = (i + 1 < parts.length) ? parts[i + 1] : "";
                    }

                    fullRow[6] = String.valueOf(pollId);
                    fullRow[7] = "";

                    writer.writeNext(fullRow);
                    createdQuestionIds.add(fullRow[0]);
                    nextQuestionId++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        VotesCsvManager votesCsvManager = new VotesCsvManager();
        votesCsvManager.initializeVotesCsv(createdQuestionIds);
    }


    private int getNextId() {
        int maxId = 0;
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 0) {
                    try {
                        int id = Integer.parseInt(row[0].replaceAll("\"", "").trim());
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    public void sendActivePollToAllUsers(TelegramLongPollingBot bot, UserManager userManager) {
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // 1) לאתר את הסקר הפעיל
        int activePollId = pollsCsvManager.getActivePollId();
        if (activePollId == -1) {
            System.out.println("❌ No active poll found.");
            return;
        }

        // 2) לקרוא את השאלות והאפשרויות של הסקר הפעיל, מיושרות לפי אינדקס
        List<String> questionIds   = new ArrayList<>();
        List<String> questionTexts = new ArrayList<>();
        List<List<String>> questionOptions = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader("src/data/questions.csv"))) {
            reader.readNext(); // דילוג על כותרות
            String[] row;
            while ((row = reader.readNext()) != null) {
                // מבנה שורה צפוי: [0]=ID, [1]=Q, [2]=A1, [3]=A2, [4]=A3, [5]=A4, [6]=PollId
                if (row.length >= 7 && row[6] != null && row[6].trim().equals(String.valueOf(activePollId))) {
                    String qId   = row[0] == null ? "" : row[0].trim().replace("\"", "");
                    String qText = row[1] == null ? "" : row[1].trim();

                    List<String> opts = new ArrayList<>(4);
                    for (int i = 2; i <= 5; i++) {
                        if (i < row.length && row[i] != null && !row[i].trim().isEmpty()) {
                            opts.add(row[i].trim());
                        }
                    }

                    // חובה לפחות 2 אפשרויות כדי שסקר טלגרם יישלח
                    if (!qText.isEmpty() && opts.size() >= 2) {
                        questionIds.add(qId);
                        questionTexts.add(qText);
                        questionOptions.add(opts);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed reading questions.csv");
            e.printStackTrace();
            return;
        }

        if (questionIds.isEmpty()) {
            System.out.println("⚠️ לא נמצאו שאלות עבור PollId " + activePollId);
            return;
        }

        // לוג לבדיקה
        System.out.println("🔎 PollId " + activePollId + " — loaded " + questionIds.size() + " questions");
        for (int i = 0; i < questionIds.size(); i++) {
            System.out.println("• QID=" + questionIds.get(i) + " | " + questionTexts.get(i) + " | options=" + questionOptions.get(i));
        }

        // 3) שליחה לכל המשתמשים + שמירת מיפוי telegramPollId → questionId לכל שליחה
        for (Long userId : userManager.getAllUsers()) {
            System.out.println("➡ sending to user " + userId);
            try {
                // הודעת פתיח
                SendMessage intro = new SendMessage();
                intro.setChatId(userId.toString());
                intro.setText("📊 סקר חדש מוכן! נשמח לשמוע את דעתך:");
                bot.execute(intro);

                // כל שאלה כסקר נפרד, באותו סדר
                for (int i = 0; i < questionIds.size(); i++) {
                    String qId   = questionIds.get(i);
                    String qText = questionTexts.get(i);
                    List<String> opts = questionOptions.get(i);

                    SendPoll poll = new SendPoll();
                    poll.setChatId(userId.toString());
                    poll.setQuestion(qText);
                    poll.setOptions(opts);
                    poll.setIsAnonymous(false);
                    poll.setAllowMultipleAnswers(false);

                    System.out.println("📤 שולח סקר למשתמש " + userId + " | QID=" + qId + " | " + qText + " | " + opts);

                    Message msg = bot.execute(poll);

                    // שמירת המיפוי עבור ההצבעות שיגיעו מהמשתמש הזה
                    if (msg != null && msg.getPoll() != null) {
                        String telegramPollId = msg.getPoll().getId();
                        pollMapManager.saveMapping(telegramPollId, qId);
                    } else {
                        System.err.println("⚠️ bot.execute(poll) לא החזיר Poll למשתמש " + userId);
                    }

                    // השהייה קטנה כדי להימנע מ-429
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                }
            } catch (Exception ex) {
                System.err.println("❌ שגיאה בשליחת סקר ל-" + userId);
                ex.printStackTrace();
            }
        }
    }




    public String getPollById(int targetId) {
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 2) {
                    try {
                        int id = Integer.parseInt(row[0].replaceAll("\"", "").trim());
                        if (id == targetId) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("שאלה: ").append(row[1].replaceAll("\"", "").trim()).append("\n");
                            for (int i = 2; i < row.length-1; i++) {
                                sb.append("תשובה ").append(i - 1).append(": ").append(row[i].replaceAll("\"", "").trim()).append("\n");
                            }
                            return sb.toString();
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return "❌ לא נמצאה שאלה עם ID " + targetId;
    }


    public int getPollIdFromQuestionId(String questionId) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/data/questions.csv"))) {
            reader.readLine(); // דלג על כותרת
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[0].replace("\"", "").trim().equals(questionId)) {
                    String pollIdStr = parts[6].replace("\"", "").trim(); // עמודה 6 זה PollId
                    return Integer.parseInt(pollIdStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


}
