import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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

    public void addPollWithQuestions(String questionsText, int minutes) {
        LocalDateTime pollTime = LocalDateTime.now().plusMinutes(minutes);

        PollsCsvManager pollsManager = new PollsCsvManager();
        int pollId = pollsManager.createPoll(pollTime);

        int nextQuestionId = getNextId();

        Scanner scanner = new Scanner(questionsText);
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    String[] fullRow = new String[parts.length + 2];

                    fullRow[0] = String.valueOf(nextQuestionId++);
                    System.arraycopy(parts, 0, fullRow, 1, parts.length);
                    fullRow[parts.length + 1] = String.valueOf(pollId);

                    writer.writeNext(fullRow);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        // מתעלם
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return maxId + 1;
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
                        // ממשיך הלאה
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return "❌ לא נמצאה שאלה עם ID " + targetId;
    }

}
