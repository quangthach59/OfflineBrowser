import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainForm {
    //Main panel
    private JPanel jMainPanel;
    //Input field
    private JTextField txtInput;
    //Button
    private JButton startButton;
    private JCheckBox cbReplaceCSS;

    private MainForm(){
        //Set button text
        startButton.setText("Start");
        //Set button tooltip
        startButton.setToolTipText("Start offline browsing");
        //Add click event for button
        startButton.addActionListener(e -> {
                    //Initialize object
                    OffBrowser ob = new OffBrowser(txtInput.getText());
                    //Determine if CSS will be replaced
                    ob.ReplaceCSS(cbReplaceCSS.isSelected());
                    //Start processing
                    ob.StartBrowsing();
                    try {
                        File outputFolder = new File(OffBrowser.DEFAULT_FOLDER_OUTPUT);
                        boolean bool = outputFolder.mkdirs();
                        Desktop.getDesktop().browse(outputFolder.toURI());
                    } catch(Exception ex)
                    {
                        System.out.println(ex.getMessage());
                    }
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
        JOptionPane.showMessageDialog(null, "Output will be saved to: " + OffBrowser.DEFAULT_FOLDER_OUTPUT);
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
