package fields.recognition;

import com.swing.tablelayout.swing.Table;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fields.recognition.ClassificationEngine.readAllBytesOrExit;
import static fields.recognition.ClassificationEngine.readAllLinesOrExit;

public class Recognizer extends JFrame implements ActionListener {

    private Table table;
    private JButton predict;
    private JButton incep;
    private JButton img;
    private JFileChooser incepch;
    private JFileChooser imgch;
    private JLabel viewer;
    private JTextField result;
    private JTextField imgpth;
    private JTextField modelpth;
    private FileNameExtensionFilter imgfilter = new FileNameExtensionFilter(
            "JPG & JPEG Images", "jpg", "jpeg");
    private String modelpath;
    private String imagepath;
    private boolean modelselected = false;
    private byte[] graphDef;
    private List<String> labels;

    public Recognizer() {
        setTitle("Object Recognition - Szostakowski");
        setSize(500, 500);
        table = new Table();

        predict = new JButton("Predict");
        predict.setEnabled(false);
        incep = new JButton("Choose Inception");
        img = new JButton("Choose Image");
        incep.addActionListener(this);
        img.addActionListener(this);
        predict.addActionListener(this);

        incepch = new JFileChooser();
        imgch = new JFileChooser();
        imgch.setFileFilter(imgfilter);
        imgch.setFileSelectionMode(JFileChooser.FILES_ONLY);
        incepch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        result = new JTextField();
        modelpth = new JTextField();
        imgpth = new JTextField();
        modelpth.setEditable(false);
        imgpth.setEditable(false);
        viewer = new JLabel();
        getContentPane().add(table);
        table.addCell(modelpth).width(250);
        table.addCell(incep);
        table.row();
        table.addCell(imgpth).width(250);
        table.addCell(img);

        table.row();
        table.addCell(viewer).size(200, 200).colspan(2);
        table.row();
        table.addCell(predict).colspan(2);
        table.row();
        table.addCell(result).width(300).colspan(2);

        setLocationRelativeTo(null);

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == incep) {
            int returnVal = incepch.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = incepch.getSelectedFile();
                modelpath = file.getAbsolutePath();
                modelpth.setText(modelpath);
                System.out.println("Opening: " + file.getAbsolutePath());
                modelselected = true;
                graphDef = readAllBytesOrExit(Paths.get(modelpath, "output_graph.pb"));
                labels = readAllLinesOrExit(Paths.get(modelpath, "output_labels.txt"));
            } else {
                System.out.println("Process was cancelled by user.");
            }

        } else if (e.getSource() == img) {
            int returnVal = imgch.showOpenDialog(Recognizer.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = imgch.getSelectedFile();
                    imagepath = file.getAbsolutePath();
                    imgpth.setText(imagepath);
                    System.out.println("Image Path: " + imagepath);
                    Image img = ImageIO.read(file);

                    viewer.setIcon(new ImageIcon(img.getScaledInstance(200, 200, 200)));
                    if (modelselected) {
                        predict.setEnabled(true);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Recognizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Process was cancelled by user.");
            }
        } else if (e.getSource() == predict) {
            byte[] imageBytes = readAllBytesOrExit(Paths.get(imagepath));
            ClassificationEngine classificationEngine = new ClassificationEngine();
            ClassificationResults results = classificationEngine.classifyImage(imageBytes);
            result.setText(String.format("BEST MATCH: %s (%.2f%% likely)", results.getName(), results.getProbability()));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Recognizer().setVisible(true));
    }
}
