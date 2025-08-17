import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.List;

public class VotesCsvManager {
    private static final String FILE_PATH = "src/data/poll_votes.csv";
    private static final String QUESTIONS_PATH = "src/data/questions.csv";
    private static final Object LOCK = new Object();

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
        synchronized (LOCK) {
            File inputFile = new File(FILE_PATH);
            File tempFile  = new File(FILE_PATH + ".tmp");

            try (
                    FileInputStream fis = new FileInputStream(inputFile);
                    InputStreamReader isr = new InputStreamReader(fis);
                    CSVReader reader = new CSVReader(isr);

                    FileOutputStream fos = new FileOutputStream(tempFile, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    CSVWriter writer = new CSVWriter(osw)
            ) {
                String[] row;
                String targetId = clean(questionId);

                while ((row = reader.readNext()) != null) {
                    if (row.length > 0 && clean(row[0]).equals(targetId)) {
                        int col = selectedOptionIndex + 1;

                        if (col >= row.length) {
                            String[] extended = new String[col + 1];
                            System.arraycopy(row, 0, extended, 0, row.length);
                            for (int i = row.length; i < extended.length; i++) extended[i] = "";
                            row = extended;
                        }

                        int current = 0;
                        if (row[col] != null && !row[col].trim().isEmpty()) {
                            try { current = Integer.parseInt(clean(row[col])); } catch (NumberFormatException ignored) {}
                        }
                        row[col] = String.valueOf(current + 1);
                    }

                    writer.writeNext(row, false);
                }


                writer.flush();
                osw.flush();
                fos.getFD().sync();

            } catch (IOException | com.opencsv.exceptions.CsvValidationException e) {
                e.printStackTrace();
                try { tempFile.delete(); } catch (Exception ignored) {}
                return;
            }

            try {
                java.nio.file.Files.move(
                        tempFile.toPath(),
                        inputFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                System.err.println("⚠️ לא הצלחנו לעדכן את קובץ ההצבעות.");
                e.printStackTrace();
            }
        }
    }

    public int getNumberOfVotesForQuestion(String questionId) {
        synchronized (LOCK) {
            int count = 0;
            try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
                String[] row;
                String target = clean(questionId);
                while ((row = reader.readNext()) != null) {
                    if (row.length >= 2 && clean(row[0]).equals(target)) {
                        for (int i = 1; i < row.length && i <= 4; i++) {
                            String v = clean(row[i]);
                            if (!v.isEmpty()) {
                                try { count += Integer.parseInt(v); } catch (NumberFormatException ignored) {}
                            }
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return count;
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("\"","").trim();
    }


}


