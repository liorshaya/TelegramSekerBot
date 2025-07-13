import javax.swing.*;
import java.awt.*;

public class ManualOption extends JPanel {
    public ManualOption(int x, int y, int width, int height) {
        setLayout(null);
        setBounds(x, y, width, height);
        setBackground(Color.ORANGE);

//Question1
        JLabel label = new JLabel("Question 1:");
        label.setBounds(10, 10, 70, 30);
        add(label);

         JTextField question1 = new JTextField();
         question1.setBounds(80 , 10 , 200 , 25);
         this.add(question1);

        JLabel labelOptionQ1O1 = new JLabel("Option 1:");
        labelOptionQ1O1.setBounds(10, 45, 70, 30);
        add(labelOptionQ1O1);

        JTextField textOptionQ1O1 = new JTextField();
        textOptionQ1O1.setBounds(80 , 45 , 200 , 25);
        this.add(textOptionQ1O1);

        JLabel labelOptionQ1O2 = new JLabel("Option 2:");
        labelOptionQ1O2.setBounds(10, 75, 70, 30);
        add(labelOptionQ1O2);

        JTextField textOptionQ1O2 = new JTextField();
        textOptionQ1O2.setBounds(80 , 75 , 200 , 25);
        this.add(textOptionQ1O2);

        JLabel labelOptionQ1O3 = new JLabel("Option 3:");
        labelOptionQ1O3.setBounds(10, 105, 70, 30);
        add(labelOptionQ1O3);

        JTextField textOptionQ1O3 = new JTextField();
        textOptionQ1O3.setBounds(80 , 105 , 200 , 25);
        this.add(textOptionQ1O3);

        JLabel labelOptionQ1O4 = new JLabel("Option 4:");
        labelOptionQ1O4.setBounds(10, 135, 70, 30);
        add(labelOptionQ1O4);

        JTextField textOptionQ1O4 = new JTextField();
        textOptionQ1O4.setBounds(80 , 135 , 200 , 25);
        this.add(textOptionQ1O4);
//Question1

//Question2
        JLabel label2 = new JLabel("Question 2:");
        label2.setBounds(10, 180, 70, 30);
        add(label2);

        JTextField question2 = new JTextField();
        question2.setBounds(80 , 180 , 200 , 25);
        this.add(question2);

        JLabel labelOptionQ2O1 = new JLabel("Option 1:");
        labelOptionQ2O1.setBounds(10, 215, 70, 30);
        add(labelOptionQ2O1);

        JTextField textOptionQ2O1 = new JTextField();
        textOptionQ2O1.setBounds(80 , 215 , 200 , 25);
        this.add(textOptionQ2O1);

        JLabel labelOptionQ2O2 = new JLabel("Option 2:");
        labelOptionQ2O2.setBounds(10, 245, 70, 30);
        add(labelOptionQ2O2);

        JTextField textOptionQ2O2 = new JTextField();
        textOptionQ2O2.setBounds(80 , 245 , 200 , 25);
        this.add(textOptionQ2O2);

        JLabel labelOptionQ2O3 = new JLabel("Option 3:");
        labelOptionQ2O3.setBounds(10, 275, 70, 30);
        add(labelOptionQ2O3);

        JTextField textOptionQ2O3 = new JTextField();
        textOptionQ2O3.setBounds(80 , 275 , 200 , 25);
        this.add(textOptionQ2O3);

        JLabel labelOptionQ2O4 = new JLabel("Option 4:");
        labelOptionQ2O4.setBounds(10, 305, 70, 30);
        add(labelOptionQ2O4);

        JTextField textOptionQ2O4 = new JTextField();
        textOptionQ2O4.setBounds(80 , 305 , 200 , 25);
        this.add(textOptionQ2O4);
//Question2

//Question3
        JLabel label3 = new JLabel("Question 3:");
        label3.setBounds(300, 10, 70, 30);
        add(label3);

        JTextField question3 = new JTextField();
        question3.setBounds(370 , 10 , 200 , 25);
        this.add(question3);

        JLabel labelOptionQ3O1 = new JLabel("Option 1:");
        labelOptionQ3O1.setBounds(300, 45, 70, 30);
        add(labelOptionQ3O1);

        JTextField textOptionQ3O1 = new JTextField();
        textOptionQ3O1.setBounds(370 , 45 , 200 , 25);
        this.add(textOptionQ3O1);

        JLabel labelOptionQ3O2 = new JLabel("Option 2:");
        labelOptionQ3O2.setBounds(300, 75, 70, 30);
        add(labelOptionQ3O2);

        JTextField textOptionQ3O2 = new JTextField();
        textOptionQ3O2.setBounds(370 , 75 , 200 , 25);
        this.add(textOptionQ3O2);

        JLabel labelOptionQ3O3 = new JLabel("Option 3:");
        labelOptionQ3O3.setBounds(300, 105, 70, 30);
        add(labelOptionQ3O3);

        JTextField textOptionQ3O3 = new JTextField();
        textOptionQ3O3.setBounds(370 , 105 , 200 , 25);
        this.add(textOptionQ3O3);

        JLabel labelOptionQ3O4 = new JLabel("Option 4:");
        labelOptionQ3O4.setBounds(300, 135, 70, 30);
        add(labelOptionQ3O4);

        JTextField textOptionQ3O4 = new JTextField();
        textOptionQ3O4.setBounds(370 , 135 , 200 , 25);
        this.add(textOptionQ3O4);
//Question3


        JButton generateButton = new JButton("Send Poll");
        generateButton.setBounds(460, 300, 120, 40);
        this.add(generateButton);


        JCheckBox scheduleCheckbox = new JCheckBox("Schedule time?");
        scheduleCheckbox.setBounds(300, 250, 150, 40);
        this.add(scheduleCheckbox);


        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 60, 1));
        timeSpinner.setBounds(460, 250, 80, 40);
        timeSpinner.setEnabled(false);
        this.add(timeSpinner);

        JLabel minLable = new JLabel("Minuets");
        minLable.setBounds(550, 250, 50, 40);
        minLable.setVisible(false);
        this.add(minLable);

        scheduleCheckbox.addActionListener(e -> {
            timeSpinner.setEnabled(scheduleCheckbox.isSelected());
            minLable.setVisible(scheduleCheckbox.isSelected());
        });
    }
}

