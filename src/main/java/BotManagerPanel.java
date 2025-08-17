import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import javax.swing.*;
import java.awt.*;

public class BotManagerPanel extends JPanel {

    private int cornerRadius = 24;
    private Color fillColor   = new Color(0, 255, 255, 140);
    private Color borderColor = new Color(0, 180, 180, 200);

    private final JFrame parentFrame;
    private final StatisticsPanel statisticsPanel;
    private JPanel currentOptionPanel = null;
    private final TelegramLongPollingBot bot;

    public BotManagerPanel(JFrame parentFrame, StatisticsPanel statisticsPanel, int x, int y, int width, int height, TelegramLongPollingBot bot) {
        this.parentFrame = parentFrame;
        this.statisticsPanel = statisticsPanel;
        this.bot = bot;

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        this.setOpaque(false);

        //this.setBackground(fillColor);

        PollsCsvManager pollsCsvManager = new PollsCsvManager();

        JLabel header = new JLabel("Create poll with:");
        header.setBounds(35 , 40 , 200 , 50 );
        header.setFont(new Font("Arial", Font.BOLD, 25));
        this.add(header);

        JButton aiPoll = new JButton("AI");
        aiPoll.setBounds(85 , 130 , 100 , 50);

        JButton manualPoll = new JButton("Manually");
        manualPoll.setBounds(85 , 205 , 100 , 50);

        aiPoll.addActionListener(e -> {
            if (pollsCsvManager.hasOpenPolls()){
                JOptionPane.showMessageDialog(
                        null,
                        "There is already an open poll.",
                        "Open Poll Detected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            replaceOptionPanel(new AiOption(0, 0, 300, 200, bot, statisticsPanel));
        });

        manualPoll.addActionListener(e -> {
            if (pollsCsvManager.hasOpenPolls()){
                JOptionPane.showMessageDialog(
                        null,
                        "There is already an open poll.",
                        "Open Poll Detected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            replaceOptionPanel(new ManualOption(0, 0, 300, 200, bot, statisticsPanel));
        });

        this.add(aiPoll);
        this.add(manualPoll);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        g2.dispose();
    }

    private void replaceOptionPanel(JPanel newPanel) {
        Container contentPane = parentFrame.getContentPane();

        if (currentOptionPanel != null) {
            contentPane.remove(currentOptionPanel);
        }

        if (newPanel instanceof ManualOption) {
            newPanel.setBounds(350, 300, 600, 350);
        } else {
            newPanel.setBounds(420, 350, 500, 300);
        }

        contentPane.add(newPanel);
        currentOptionPanel = newPanel;
        contentPane.revalidate();
        contentPane.repaint();
    }
}