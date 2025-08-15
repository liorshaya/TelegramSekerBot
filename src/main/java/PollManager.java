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

        // אתחול קובץ ההצבעות (ללא שימוש ב־telegramPollId)
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
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        PollsCsvManager pollCsvManager = new PollsCsvManager();
        int activePollId = pollCsvManager.getActivePollId();

        if (activePollId == -1) {
            System.out.println("❌ No active poll found.");
            return;
        }

        Map<String, List<String>> pollQuestions = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader("src/data/questions.csv"))) {
            reader.readNext();
            String[] row;

            while ((row = reader.readNext()) != null) {
                if (row.length >= 7 && row[6].trim().equals(String.valueOf(activePollId))) {
                    System.out.println("row: " + row);
                    String question = row[1].trim();
                    List<String> options = new ArrayList<>();
                    for (int i = 2; i <= 5; i++) {
                        if (row[i] != null && !row[i].trim().isEmpty()) {
                            options.add(row[i].trim());
                        }
                    }
                    if (options.size() >= 2) {
                        pollQuestions.put(question, options);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("🔎 תוכן pollQuestions:");
        for (Map.Entry<String, List<String>> entry : pollQuestions.entrySet()) {
            System.out.println("שאלה: " + entry.getKey());
            System.out.println("אפשרויות: " + entry.getValue());
        }

        for (Long userId : userManager.getAllUsers()) {
            System.out.println("➡ " + userId);
            try {
                SendMessage intro = new SendMessage();
                intro.setChatId(userId.toString());
                intro.setText("📊 סקר חדש מוכן! נשמח לשמוע את דעתך:");
                bot.execute(intro);

                for (Map.Entry<String, List<String>> entry : pollQuestions.entrySet()) {
                    SendPoll poll = new SendPoll();
                    poll.setChatId(userId.toString());
                    poll.setQuestion(entry.getKey());
                    poll.setOptions(entry.getValue());
                    poll.setIsAnonymous(false);
                    poll.setAllowMultipleAnswers(false);

                    System.out.println("📤 שולח סקר ל-" + userId);
                    System.out.println("שאלה: " + entry.getKey());
                    System.out.println("אפשרויות: " + entry.getValue());

                    bot.execute(poll);


                    Thread.sleep(1000);
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

}
