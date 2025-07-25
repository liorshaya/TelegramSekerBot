import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UserManager {
    private static final String DATA_DIR = "src/data/";
    private static final String FILE_PATH = DATA_DIR + "users.csv";

    private Set<Long> users = new HashSet<>();

    public UserManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(FILE_PATH);
        boolean fileExists = file.exists();

        if (!fileExists) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(new String[]{"UserID"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadUsers();
    }

    private void loadUsers() {
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    long id = Long.parseLong(nextLine[0]);
                    users.add(id);
                } catch (NumberFormatException e) {
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserExists(Long chatId) {
        return users.contains(chatId);
    }

    public void addUser(Long chatId) {
        if (!users.contains(chatId)) {
            users.add(chatId);
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
                writer.writeNext(new String[]{String.valueOf(chatId)});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getNumberOfUsers(){
        return users.size();
    }

    public Set<Long> getAllUsers(){
        return users;
    }

}
