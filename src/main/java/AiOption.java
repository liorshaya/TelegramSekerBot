import javax.swing.*;
import java.awt.*;

public class AiOption extends JPanel {
    public AiOption(int x, int y, int width, int height){
        this.setBounds(x, y, width, height);
        this.setLayout(null);
        this.setBackground(new Color(0x6565DD));

        JLabel header = new JLabel("Enter subject:");
        header.setBounds(60 , 10 , 150 , 40);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        this.add(header);

        JTextField subject = new JTextField();
        subject.setBounds(50 , 50 , 200 , 40);
        this.add(subject);

        JCheckBox scheduleCheckbox = new JCheckBox("Schedule time?");
        scheduleCheckbox.setBounds(50, 250, 150, 40);
        this.add(scheduleCheckbox);


        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 60, 1));
        timeSpinner.setBounds(210, 250, 80, 40);
        timeSpinner.setEnabled(false);
        this.add(timeSpinner);

        JLabel minLable = new JLabel("Minuets");
        minLable.setBounds(300, 250, 50, 40);
        minLable.setVisible(false);
        this.add(minLable);

        scheduleCheckbox.addActionListener(e -> {
            timeSpinner.setEnabled(scheduleCheckbox.isSelected());
            minLable.setVisible(scheduleCheckbox.isSelected());
        });

        JButton generateButton = new JButton("Send Poll");
        generateButton.setBounds(360, 250, 120, 40);
        this.add(generateButton);
        
    }
}
