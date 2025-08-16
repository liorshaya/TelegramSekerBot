import java.io.*;
import java.util.*;

public class PollMapCsvManager {
    private static final String FILE_PATH = "src/data/poll_map.csv";

    public PollMapCsvManager() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("telegram_poll_id,question_id");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMapping(String pollId, String questionId) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.printf("%s,%s%n", pollId, questionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getQuestionIdByPollId(String pollId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            reader.readLine(); // דלג על הכותרת
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(pollId)) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
