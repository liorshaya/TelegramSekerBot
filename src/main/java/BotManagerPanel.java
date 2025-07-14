import javax.swing.*;
import java.awt.*;

public class BotManagerPanel extends JPanel {
    private JFrame parentFrame;
    private JPanel currentOptionPanel = null;

    public BotManagerPanel(JFrame parentFrame ,int x, int y, int width, int height){
        PollsCsvManager pollsCsvManager = new PollsCsvManager();

        this.parentFrame = parentFrame;
        this.setBounds(x, y, width, height);
        this.setLayout(null);
        this.setBackground(Color.CYAN);

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
            replaceOptionPanel(new AiOption(0, 0, 300, 200));
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
            replaceOptionPanel(new ManualOption(0, 0, 300, 200));
        });

        this.add(aiPoll);
        this.add(manualPoll);
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
