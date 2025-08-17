import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestionStats {
    public String questionText;
    public List<String> options;
    public Map<String, Integer> optionVotes;

    public QuestionStats(String questionText) {
        this.questionText = questionText;
        this.options = new ArrayList<>();
        this.optionVotes = new LinkedHashMap<>();
    }

    public void addOption(String option, int votes) {
        options.add(option);
        optionVotes.put(option, votes);
    }
}

