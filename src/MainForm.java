import sun.net.util.URLUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MainForm {
    //Main panel
    private JPanel jMainPanel;
    //Input field
    private JTextField txtInput;
    //Button
    private JButton startButton;

    private MainForm() {
        //Set button text
        startButton.setText("Start");
        //Set button tooltip
        startButton.setToolTipText("Start offline browsing");
        //Add click event for button
        startButton.addActionListener(e -> {
                    //Initialize object
                    OfflineBrowser ob = new OfflineBrowser(txtInput.getText());
                    //Start processing
                    //ob.Start();
                    ob.StartBrowsing();
                }
        );
        //Add enter shortkey for text field
        txtInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == txtInput) {
                    startButton.doClick();
                }
            }
        });
    }

    public static void main(String[] args) {
        //Initialize new frame and set title
        JFrame frame = new JFrame("Offline Browser");
        //Set content panel
        frame.setContentPane(new MainForm().jMainPanel);
        //Disallow changing form size
        frame.setResizable(false);
        //Pack
        frame.pack();
        //Set form to display in center of the screen
        frame.setLocationRelativeTo(null);
        //Set default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set form to be visible
        frame.setVisible(true);
    }
}
