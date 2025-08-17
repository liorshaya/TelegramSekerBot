import javax.swing.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final StatisticsPanel statsPanel;

    public MainFrame(MyBot myBot) {
        setSize(Constant.WINDOW_WIDTH, Constant.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setTitle("Telegram Bot Manager");

        List<QuestionStats> stats = StatisticsLoader.loadLatestPollStatistics();
        statsPanel = new StatisticsPanel(stats); // שימוש בשדה
        statsPanel.setBounds(20, 20, 1200, 280);
        this.add(statsPanel);
        myBot.setStatisticsPanel(statsPanel);

        BotManagerPanel botManagerPanel = new BotManagerPanel(this, statsPanel, 20, 350, 300, 300, myBot);
        this.add(botManagerPanel);

        setVisible(true);
    }
}


