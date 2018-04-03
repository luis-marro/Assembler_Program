package Assembler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by luisf on 25/03/2018.
 */
public class Translation extends JFrame {
    private JButton btLoadFile;
    private JLabel lblShowPath;
    private JPanel mainPanel;
    private JButton btAssemble;
    private JTextArea textAreaResult;
    private JFileChooser chooser;
    private Assemble assembler;

    public Translation(String title) {
        setSize(500, 500);
        setTitle(title);
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        assembler = new Assemble();
        // make the result text area scrollable
        textAreaResult.setEditable(false);
        textAreaResult.setWrapStyleWord(true);
        textAreaResult.setLineWrap(true);

        btLoadFile.addActionListener(new ActionListener() {
            // Load file button is clicked,
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser = new JFileChooser();
                int result = chooser.showOpenDialog(mainPanel);
                // user does select a file
                if(result == JFileChooser.APPROVE_OPTION){
                    // select the file and read it
                    File selectedFile = chooser.getSelectedFile();
                    lblShowPath.setText(selectedFile.getPath());
                }else{
                    JOptionPane.showMessageDialog(null, "No ha seleccionado un archivo", "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        btAssemble.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(lblShowPath.getText().contains(".asm")) {
                    textAreaResult.setText("");
                    assembler.translate(lblShowPath.getText());
                    // print the binary code.
                    for(int i = 0; i < assembler.getBinaryCode().size(); i++){
                        textAreaResult.append(assembler.getBinaryCode().get(i) + "\n");
                    }
                    lblShowPath.setText("");
                    assembler = new Assemble(); 
                }
                else
                    JOptionPane.showMessageDialog(null, "No ha seleccionado un archivo válido", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }




}
