import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StatisticsLoader {

    public static List<QuestionStats> loadLatestPollStatistics() {
        try {
            String lastPollId = getLastPollId();
            Map<String, QuestionStats> stats = new LinkedHashMap<>();

            List<String[]> questionRows = loadCSV("src/data/questions.csv");
            for (String[] row : questionRows) {
                if (row.length >= 7 && row[6].replace("\"", "").equals(lastPollId)) {
                    String qId = row[0].replace("\"", "");
                    String qText = row[1].replace("\"", "");

                    QuestionStats qs = new QuestionStats(qText);
                    for (int i = 2; i <= 5; i++) {
                        if (i < row.length) {
                            String opt = row[i].replace("\"", "");
                            qs.addOption(opt, 0);
                        }
                    }

                    stats.put(qId, qs);
                }
            }

            List<String[]> voteRows = loadCSV("src/data/poll_votes.csv");
            for (String[] row : voteRows) {
                if (row.length >= 2 && stats.containsKey(row[0])) {
                    QuestionStats qs = stats.get(row[0]);

                    System.out.println("---- שאלות שנמצאו ----");
                    for (String key : stats.keySet()) {
                        System.out.println("שאלה: " + key);
                    }

                    System.out.println("---- הצבעות שנמצאו ----");
                    for (String[] row1 : voteRows) {
                        System.out.println("הצבעה: " + Arrays.toString(row1));
                    }

                    for (int i = 0; i < qs.options.size(); i++) {
                        if (i + 1 < row.length && !row[i + 1].isEmpty()) {
                            int count = Integer.parseInt(row[i + 1]);
                            String opt = qs.options.get(i);
                            qs.optionVotes.put(opt, count);
                        }
                    }
                }
            }

            return new ArrayList<>(stats.values());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static String getLastPollId() throws IOException, CsvValidationException {
        List<String[]> rows = loadCSV("src/data/polls.csv");
        return rows.get(rows.size() - 1)[0].replace("\"", "");
    }

    private static List<String[]> loadCSV(String path) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> list = new ArrayList<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                list.add(row);
            }
            return list;
        }
    }
}

