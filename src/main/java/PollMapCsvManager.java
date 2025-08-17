import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class PollMapCsvManager {
    private static final String FILE_PATH = "src/data/poll_map.csv";
    private static final Object LOCK = new Object();

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public PollMapCsvManager() {
        ensureFile();
        loadCache();
    }

    private void ensureFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (CSVWriter w = new CSVWriter(new FileWriter(file))) {
                w.writeNext(new String[]{"telegram_poll_id","question_id"}, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCache() {
        try (CSVReader r = new CSVReader(new FileReader(FILE_PATH))) {
            String[] row;
            r.readNext();
            while ((row = r.readNext()) != null) {
                if (row.length >= 2) {
                    cache.put(clean(row[0]), clean(row[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveMapping(String pollId, String questionId) {
        String p = clean(pollId), q = clean(questionId);
        if (p.isEmpty() || q.isEmpty()) return;

        cache.put(p, q);

        synchronized (LOCK) {
            try (FileWriter fw = new FileWriter(FILE_PATH, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 CSVWriter w = new CSVWriter(bw)) {
                w.writeNext(new String[]{p, q}, false);
                w.flush(); bw.flush(); fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFromCacheOrWait(String pollId, long timeoutMs, long stepMs) {
        String key = clean(pollId);
        long end = System.currentTimeMillis() + timeoutMs;

        do {
            String qid = cache.get(key);
            if (qid != null && !qid.isEmpty()) return qid;

            String fromFile = getQuestionIdByPollId(key);
            if (fromFile != null && !fromFile.isEmpty()) {
                cache.put(key, fromFile);
                return fromFile;
            }

            try { Thread.sleep(stepMs); } catch (InterruptedException ignored) {}
        } while (System.currentTimeMillis() < end);

        return null;
    }

    public String getQuestionIdByPollId(String pollId) {
        String target = clean(pollId);
        synchronized (LOCK) {
            try (CSVReader r = new CSVReader(new FileReader(FILE_PATH))) {
                String[] row;
                r.readNext();
                while ((row = r.readNext()) != null) {
                    if (row.length >= 2 && clean(row[0]).equals(target)) {
                        return clean(row[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("\"","").trim();
    }
}