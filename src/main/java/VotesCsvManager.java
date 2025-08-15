import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.List;

public class VotesCsvManager {
    private static final String FILE_PATH = "src/data/poll_votes.csv";
    private static final String QUESTIONS_PATH = "src/data/questions.csv";

    public void initializeVotesCsv(List<String> questionIds) {
        File file = new File(FILE_PATH);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false));
             CSVReader reader = new CSVReader(new FileReader(QUESTIONS_PATH))) {

            reader.readNext();
            String[] row;

            while ((row = reader.readNext()) != null) {
                String questionId = row[0].replace("\"", "").trim();
                if (!questionIds.contains(questionId)) continue;

                int optionCount = 0;
                for (int i = 2; i <= 5; i++) {
                    if (i < row.length && !row[i].trim().isEmpty()) {
                        optionCount++;
                    }
                }

                StringBuilder sb = new StringBuilder(questionId);
                for (int i = 0; i < optionCount; i++) {
                    sb.append(",0");
                }
                for (int i = optionCount; i < 4; i++) {
                    sb.append(",");
                }

                writer.println(sb.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public void recordVote(String questionId, int selectedOptionIndex) {
        File inputFile = new File(FILE_PATH);
        File tempFile = new File("src/data/temp_votes.csv");

        try (CSVReader reader = new CSVReader(new FileReader(inputFile));
             CSVWriter writer = new CSVWriter(new FileWriter(tempFile))) {

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 0 && row[0].equals(questionId)) {
                    // ודא שהאופציה קיימת בשאלה זו
                    if (selectedOptionIndex + 1 < row.length && !row[selectedOptionIndex + 1].isEmpty()) {
                        int currentCount = Integer.parseInt(row[selectedOptionIndex + 1]);
                        row[selectedOptionIndex + 1] = String.valueOf(currentCount + 1);
                    }
                }
                writer.writeNext(row);
            }

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return;
        }

        // החלף את הקובץ הישן בחדש
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.err.println("⚠️ לא הצלחנו לעדכן את קובץ ההצבעות.");
        }
    }
}

