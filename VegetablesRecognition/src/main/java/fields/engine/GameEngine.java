package fields.engine;

import fields.recognition.ClassificationEngine;
import fields.recognition.ClassificationResults;
import fields.ui.CustomJLabel;
import fields.utils.LabelUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static fields.recognition.ClassificationEngine.readAllBytesOrExit;

public class GameEngine implements Runnable {

    private Map<Integer, List<CustomJLabel>> labels;
    private ClassificationEngine classificationEngine;
    private JComboBox<String> vegetableChooser;
    private Boolean gameIsRunning;
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    public GameEngine(Map<Integer, List<CustomJLabel>> labels, JComboBox<String> vegetableChooser, Boolean gameIsRunning) {
        this.labels = labels;
        this.classificationEngine = new ClassificationEngine();
        this.vegetableChooser = vegetableChooser;
        this.gameIsRunning = gameIsRunning;
    }

    @Override
    public void run() {
        int x = 0;
        int y = 0;
        Object selectedItem = vegetableChooser.getSelectedItem();
        while (gameIsRunning) {
            Random random = new Random();
            int destination = random.nextInt(4);
            try {
                if (!selectedItem.equals(vegetableChooser.getSelectedItem())) {
                    changeAllVisitedToFalse();
                    selectedItem = vegetableChooser.getSelectedItem();
                }
                if (destination == LEFT)
                    x = x + 1;
                else if (destination == RIGHT)
                    x = x - 1;
                else if (destination == UP)
                    y = y + 1;
                else if (destination == DOWN)
                    y = y - 1;

                CustomJLabel jLabel = labels.get(x).get(y);
                Icon icon = jLabel.getIcon();
                LabelUtils.setCombinedIcon(jLabel);
                if (!jLabel.isVisited() && !jLabel.isRecognized()) {
                    jLabel.setVisited(true);
                    jLabel.revalidate();
//                Thread.sleep(1000);
                    String absolutePath = jLabel.getImageToClassification().getAbsolutePath();
                    byte[] imageBytes = readAllBytesOrExit(Paths.get(absolutePath));
                    ClassificationResults results = classificationEngine.classifyImage(imageBytes);
                    if ((vegetableChooser.getSelectedItem()).equals(results.getName())) {
                        jLabel.setRecognized(true);
                        jLabel.setText(results.getName());
                        jLabel.setBackground(Color.GREEN);
                        jLabel.setIcon(null);
                    } else {
                        jLabel.setIcon(icon);
                    }
                    jLabel.revalidate();
                } else {
                     Thread.sleep(400);
                    System.out.println("Found recognized image!");
                    jLabel.setIcon(icon);
                }
            } catch (IndexOutOfBoundsException | NullPointerException | IOException e) {
                if (destination == LEFT)
                    x = x - 1;
                else if (destination == RIGHT)
                    x = x + 1;
                else if (destination == UP)
                    y = y - 1;
                else if (destination == DOWN)
                    y = y + 1;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeAllVisitedToFalse() {
        labels.keySet().forEach(integer -> labels.get(integer).forEach(customJLabel -> customJLabel.setVisited(false)));
    }

    public void setGameIsRunning(boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }
}
