import javax.swing.*;
import java.awt.*;

public class BotManagerPanel extends JPanel {
    private JFrame parentFrame;
    private JPanel currentOptionPanel = null; // נשמור את הפאנל הפעיל

    public BotManagerPanel(JFrame parentFrame ,int x, int y, int width, int height){
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
            replaceOptionPanel(new AiOption(0, 0, 300, 200));
        });

        manualPoll.addActionListener(e -> {
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

        // קביעת מיקום וגודל שונה לפי סוג הפאנל
        if (newPanel instanceof ManualOption) {
            newPanel.setBounds(350, 300, 600, 350); // גודל גדול יותר
        } else {
            newPanel.setBounds(420, 350, 500, 300); // גודל ברירת מחדל
        }

        contentPane.add(newPanel);
        currentOptionPanel = newPanel;
        contentPane.revalidate();
        contentPane.repaint();
    }
}
