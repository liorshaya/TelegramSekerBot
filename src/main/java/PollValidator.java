import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PollValidator {

    private final UserManager userManager;
    private final VotesCsvManager votesCsvManager;

    public PollValidator(UserManager userManager) {
        this.userManager = userManager;
        this.votesCsvManager = new VotesCsvManager();
    }

    public boolean allUsersAnsweredAllQuestions(int pollId) {
        int totalUsers = userManager.getNumberOfUsers();
        System.out.println(pollId);

        List<String> questionIds = getQuestionsByPollId(pollId);
        if (questionIds.isEmpty()) {
            System.err.println("⚠️ לא נמצאו שאלות עבור PollId " + pollId);
            return false;
        }

        for (String questionId : questionIds) {
            int numberOfVotes = votesCsvManager.getNumberOfVotesForQuestion(questionId);
            if (numberOfVotes < totalUsers) {
                System.out.println("🔎 שאלה " + questionId + " קיבלה " + numberOfVotes + " קולות מתוך " + totalUsers);
                return false;
            }
        }

        return true;
    }

    private List<String> getQuestionsByPollId(int pollId) {
        List<String> questionIds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/data/questions.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String questionId = parts[0].replace("\"", "").trim();
                    String pollIdStr = parts[6].replace("\"", "").trim();
                    if (Integer.parseInt(pollIdStr) == pollId) {
                        questionIds.add(questionId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionIds;
    }
}

