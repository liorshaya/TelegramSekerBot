import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame(MyBot myBot) {
        setSize(Constant.WINDOW_WIDTH, Constant.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setTitle("Telegram Bot Manager");

        BotManagerPanel botManagerPanel = new BotManagerPanel(this,70, 350, 300, 300);
        this.add(botManagerPanel);

        setVisible(true);
    }

}
