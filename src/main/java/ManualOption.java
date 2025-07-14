import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManualOption extends JPanel {
    private final List<JTextField> questionFields = new ArrayList<>();
    private final List<List<JTextField>> optionFields = new ArrayList<>();
    private final JCheckBox scheduleCheckbox;
    private final JSpinner timeSpinner;

    public ManualOption(int x, int y, int width, int height) {
        setLayout(null);
        setBounds(x, y, width, height);
        setBackground(Color.ORANGE);

        createQuestionBlock(1, 10, 10);
        createQuestionBlock(2, 10, 180);
        createQuestionBlock(3, 300, 10);

        JButton generateButton = new JButton("Send Poll");
        generateButton.setBounds(460, 300, 120, 40);
        this.add(generateButton);

        scheduleCheckbox = new JCheckBox("Schedule time?");
        scheduleCheckbox.setBounds(310, 250, 150, 40);
        this.add(scheduleCheckbox);

        timeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 60, 1));
        timeSpinner.setBounds(470, 250, 80, 40);
        timeSpinner.setEnabled(false);
        this.add(timeSpinner);

        JLabel minLabel = new JLabel("Minutes");
        minLabel.setBounds(553, 250, 50, 40);
        minLabel.setVisible(false);
        this.add(minLabel);

        scheduleCheckbox.addActionListener(e -> {
            timeSpinner.setEnabled(scheduleCheckbox.isSelected());
            minLabel.setVisible(scheduleCheckbox.isSelected());
        });

        generateButton.addActionListener(e -> {
            String csvData = buildPollTextFromInput();
            if (csvData != null) {
                int minutes = scheduleCheckbox.isSelected() ? (int) timeSpinner.getValue() : 0;
                new PollManager().addPollWithQuestions(csvData, minutes);
                JOptionPane.showMessageDialog(this, "✅ Poll sent successfully!");
            }
        });
    }

    private void createQuestionBlock(int number, int x, int y) {
        JLabel label = new JLabel("Question " + number + ":");
        label.setBounds(x, y, 70, 30);
        add(label);

        JTextField question = new JTextField();
        question.setBounds(x + 70, y, 200, 25);
        add(question);
        questionFields.add(question);

        List<JTextField> options = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            JLabel optionLabel = new JLabel("Option " + (i + 1) + ":");
            optionLabel.setBounds(x, y + 32 * (i + 1), 70, 30);
            add(optionLabel);

            JTextField optionField = new JTextField();
            optionField.setBounds(x + 70, y + 32 * (i + 1), 200, 25);
            add(optionField);
            options.add(optionField);
        }

        optionFields.add(options);
    }

    private String buildPollTextFromInput() {
        StringBuilder result = new StringBuilder();
        boolean hasAtLeastOneValidQuestion = false;

        for (int i = 0; i < questionFields.size(); i++) {
            String question = questionFields.get(i).getText().trim();
            List<JTextField> options = optionFields.get(i);
            String[] answers = new String[4]; // נשמור תמיד 4 אופציות

            int filledCount = 0;
            for (int j = 0; j < 4; j++) {
                String text = options.get(j).getText().trim();
                if (!text.isEmpty()) {
                    // בדיקה: אסור למלא את תשובה 3 בלי למלא 1 ו־2
                    if (j > 0 && answers[j - 1] == null) {
                        JOptionPane.showMessageDialog(this,
                                "❌ Please fill options in order for Question " + (i + 1) +
                                        ". You cannot skip to Option " + (j + 1) + " without filling previous ones.");
                        return null;
                    }
                    answers[j] = text;
                    filledCount++;
                } else {
                    answers[j] = ""; // חובה לשמור פסיק אפילו אם ריק
                }
            }

            if (!question.isEmpty()) {
                if (filledCount < 2) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Question " + (i + 1) + " must have at least 2 options.");
                    return null;
                }
                hasAtLeastOneValidQuestion = true;
                result.append(question);
                for (String ans : answers) {
                    result.append(",").append(ans);
                }
                result.append("\n");
            } else if (filledCount > 0) {
                JOptionPane.showMessageDialog(this,
                        "❌ You entered options for Question " + (i + 1) + " but no question!");
                return null;
            }
        }

        if (!hasAtLeastOneValidQuestion) {
            JOptionPane.showMessageDialog(this, "❌ You must enter at least one valid question.");
            return null;
        }

        return result.toString();
    }
}
