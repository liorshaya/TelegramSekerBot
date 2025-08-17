import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private List<QuestionStats> questions;
    private Color[] colors = {
            new Color(100, 149, 237),
            new Color(60, 179, 113),
            new Color(255, 165, 0),
            new Color(220, 20, 60)
    };

    public StatisticsPanel(List<QuestionStats> questions) {
        this.questions = questions;
        setLayout(new GridLayout(1, questions.size()));
        setPreferredSize(new Dimension(1200, 250));
        setBackground(Color.WHITE);

        for (QuestionStats q : questions) {
            add(createQuestionPanel(q));
        }
    }

    private JPanel createQuestionPanel(QuestionStats q) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(q.questionText));
        panel.setBackground(Color.WHITE);

        JPanel barsPanel = new JPanel();
        barsPanel.setLayout(new GridLayout(1, q.optionVotes.size()));
        barsPanel.setBackground(Color.WHITE);

        int maxVotes = q.optionVotes.values().stream().mapToInt(i -> i).max().orElse(1);
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : q.optionVotes.entrySet()) {
            String label = entry.getKey();
            int votes = entry.getValue();
            Color color = colors[colorIndex++ % colors.length];

            JPanel fullBar = new JPanel();
            fullBar.setLayout(new BorderLayout());
            fullBar.setBackground(Color.WHITE);

            JLabel voteLabel = new JLabel(String.valueOf(votes), SwingConstants.CENTER);
            voteLabel.setForeground(Color.BLACK);
            fullBar.add(voteLabel, BorderLayout.NORTH);

            JPanel coloredBar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int totalHeight = getHeight();
                    int barHeight = (int) ((votes / (double) maxVotes) * totalHeight);
                    g.setColor(color);
                    g.fillRect(10, totalHeight - barHeight, getWidth() - 20, barHeight);
                }
            };
            coloredBar.setPreferredSize(new Dimension(100, 120));
            coloredBar.setBackground(Color.WHITE);
            fullBar.add(coloredBar, BorderLayout.CENTER);

            JLabel optionLabel = new JLabel(label, SwingConstants.CENTER);
            optionLabel.setForeground(Color.DARK_GRAY);
            fullBar.add(optionLabel, BorderLayout.SOUTH);

            barsPanel.add(fullBar);
        }

        panel.add(barsPanel, BorderLayout.CENTER);
        return panel;
    }

    public void refreshStatistics(List<QuestionStats> newStats) {
        this.removeAll();
        this.questions = newStats;
        setLayout(new GridLayout(1, newStats.size()));

        for (QuestionStats q : newStats) {
            add(createQuestionPanel(q));
        }

        revalidate();
        repaint();
    }

}
