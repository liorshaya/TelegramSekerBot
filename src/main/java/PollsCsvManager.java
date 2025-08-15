import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PollsCsvManager {
    private static final String DATA_DIR = "src/data/";
    private static final String POLLS_PATH = DATA_DIR + "polls.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PollsCsvManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(POLLS_PATH);
        if (!file.exists()) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(POLLS_PATH))) {
                writer.writeNext(new String[]{"PollId", "DateTime", "Status"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updatePollStatuses();
    }

    public void updatePollStatuses() {
        List<String[]> updatedRows = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        try (CSVReader reader = new CSVReader(new FileReader(POLLS_PATH))) {
            String[] header = reader.readNext();
            if (header != null) {
                updatedRows.add(header);
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 3) {
                    String timeStr = row[1].trim().replace("\"", "");
                    LocalDateTime pollTime = LocalDateTime.parse(timeStr, formatter);

                    // קביעת סטטוס
                    String status;
                    if (now.isBefore(pollTime)) {
                        status = "AWAITING";
                    } else if (now.isAfter(pollTime.plusMinutes(5))) {
                        status = "DONE";
                    } else {
                        status = "ACTIVE";
                    }

                    row[2] = status;
                }

                updatedRows.add(row);
            }

            // כתיבה חזרה לקובץ
            try (CSVWriter writer = new CSVWriter(new FileWriter(POLLS_PATH))) {
                for (String[] updatedRow : updatedRows) {
                    writer.writeNext(updatedRow);
                }
            }

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public int createPoll(LocalDateTime dateTime) {
        int nextPollId = getNextPollId();
        String dateTimeStr = dateTime.format(FORMATTER);
        String status = getStatus(dateTimeStr);

        try (CSVWriter writer = new CSVWriter(new FileWriter(POLLS_PATH, true))) {
            writer.writeNext(new String[]{String.valueOf(nextPollId), dateTimeStr, status});
        } catch (IOException e) {
            e.printStackTrace();
        }

        updatePollStatuses();
        return nextPollId;
    }

    private int getNextPollId() {
        int maxId = 0;
        try (CSVReader reader = new CSVReader(new FileReader(POLLS_PATH))) {
            reader.readNext(); // skip header
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 0) {
                    try {
                        int id = Integer.parseInt(row[0].trim().replace("\"", ""));
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    private String getStatus(String dateTimeStr) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pollTime = LocalDateTime.parse(dateTimeStr, FORMATTER);

            if (now.isBefore(pollTime)) {
                return "AWAITING";
            } else if (!now.isAfter(pollTime.plusMinutes(5))) {
                return "ACTIVE";
            } else {
                return "DONE";
            }
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public boolean checkAndActivateScheduledPoll() {
        File inputFile = new File("src/data/polls.csv");
        File tempFile = new File("src/data/polls_temp.csv");

        boolean activated = false;
        long currentTimeMillis = System.currentTimeMillis();

        try (CSVReader reader = new CSVReader(new FileReader(inputFile));
             CSVWriter writer = new CSVWriter(new FileWriter(tempFile))) {

            String[] header = reader.readNext();
            writer.writeNext(header);

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 4) {
                    String status = row[2].trim().replace("\"", "");
                    long scheduledTime = Long.parseLong(row[3].trim());

                    if (status.equals("WAITING") && currentTimeMillis >= scheduledTime) {
                        row[2] = "ACTIVE";
                        activated = true;
                    }
                }
                writer.writeNext(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("⚠️ Failed to replace polls.csv with updated version.");
        }

        return activated;
    }

    public boolean hasOpenPolls() {
        try (CSVReader reader = new CSVReader(new FileReader(POLLS_PATH))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 3) {
                    String status = row[2].trim().replace("\"", "");
                    if (status.equals("AWAITING") || status.equals("ACTIVE")) {
                        return true;
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void activatePollById(int targetPollId) {
        File file = new File("src/data/polls.csv");
        List<String[]> rows = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] header = reader.readNext();
            rows.add(header);
            String[] row;

            while ((row = reader.readNext()) != null) {
                if (row.length >= 3 && row[0].replace("\"", "").trim().equals(String.valueOf(targetPollId))) {
                    row[2] = "ACTIVE";
                }
                rows.add(row);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeAll(rows);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getActivePollId() {
        try (CSVReader reader = new CSVReader(new FileReader("src/data/polls.csv"))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 3) {
                    String status = row[2].trim().replace("\"", "");
                    if (status.equalsIgnoreCase("ACTIVE")) {
                        return Integer.parseInt(row[0].replace("\"", "").trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean hasWaitingPoll() {
        try (CSVReader reader = new CSVReader(new FileReader("src/data/polls.csv"))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 3) {
                    String status = row[2].trim().replace("\"", "");
                    if (status.equalsIgnoreCase("WAITING")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
