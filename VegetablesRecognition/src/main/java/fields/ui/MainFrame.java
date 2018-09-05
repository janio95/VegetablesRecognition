package fields.ui;

import fields.engine.GameEngine;
import fields.utils.LabelUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static fields.recognition.ClassificationEngine.readAllLinesOrExit;

public class MainFrame {

    private static final int HEIGHT = 150;
    private static final int WIDTH = 400;
    private static final int FIELD_SIZE = 50;
    private static final int SPINNER_HEIGHT = 20;
    private static final int SPINNER_WIDTH = 40;
    private static final int SPINNER_VALUE = 7;
    private static final int SPINNER_MAX = 18;
    private static final int SPINNER_MIN = 7;
    private static final int SPINNER_STEP = 2;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int SPINNER_LABEL_HEIGHT = SPINNER_HEIGHT;
    private static final int SPINNER_LABEL_WIDTH = SPINNER_WIDTH + 20;
    private static final String PATH_MODEL = "model/";
    private static final String PATH_IMAGES = "images/";

    private JFrame window;
    private JPanel panel;
    private JButton generateFieldsButton;
    private JButton startButton;
    private JSpinner spinnerX;
    private JSpinner spinnerY;
    private JComboBox<String> vegetableChooser;
    private JLabel labelX;
    private JLabel labelY;
    private Map<Integer, List<CustomJLabel>> labels;

    private List<File> images;
    private List<String> classificationLabels;
    private GameEngine gameEngine;

    public MainFrame() {
        createMainFrame();
        createPanel();
        createGenerateFieldsButton();
        createStartButton();
        createSpinners();
        createSpinnerLabels();
        loadClassificationLabels();
        createVegetableChooser();
        panel.updateUI();
        images = new ArrayList<>();
        loadPhotos();
    }

    private void loadClassificationLabels() {
        File graph = new File(PATH_MODEL);
        String modelPath = graph.getAbsolutePath();
        this.classificationLabels = readAllLinesOrExit(Paths.get(modelPath, "output_labels.txt"));
    }

    private void createLabels(int x, int y) throws IOException {
        labels = new HashMap<>();
        updateComponents(x, y);
        Random random = new Random();
        for (int i = 0; i < x; i++) {
            List<CustomJLabel> list = new ArrayList<>();
            for (int j = 0; j < y; j++) {
                CustomJLabel label = LabelUtils.createFieldLabel(FIELD_SIZE, i, j, images.get(random.nextInt(images.size())));
                list.add(label);
                panel.add(label);
            }
            labels.put(i, list);
        }
    }

    private void loadPhotos() {
        final File directory = new File(PATH_IMAGES);
        FilenameFilter IMAGE_FILTER = (dir, name) -> name.endsWith(".jpg") && !name.equals("traktor.jpg");
        if (directory.isDirectory()) { // make sure it's a directory
            images.addAll(Arrays.asList(directory.listFiles(IMAGE_FILTER)));
        }
    }

    private void updateComponents(int x, int y) {
        panel.removeAll();
        window.setSize((x * (FIELD_SIZE + 1)) + 10, (y * (FIELD_SIZE + 1) + 130));
        panel.add(generateFieldsButton);
        panel.add(spinnerY);
        panel.add(spinnerX);
        panel.add(labelX);
        panel.add(labelY);
        panel.add(startButton);
        panel.add(vegetableChooser);
        generateFieldsButton.setLocation(window.getSize().width / 2 - generateFieldsButton.getSize().width / 2, window.getSize().height - generateFieldsButton.getSize().height * 2);
        startButton.setLocation(window.getSize().width / 2 - generateFieldsButton.getSize().width / 2 + generateFieldsButton.getSize().width, window.getSize().height - generateFieldsButton.getSize().height * 2);
        spinnerX.setLocation(generateFieldsButton.getLocation().x - spinnerX.getSize().width, generateFieldsButton.getLocation().y);
        spinnerY.setLocation(generateFieldsButton.getLocation().x - spinnerY.getSize().width, generateFieldsButton.getLocation().y + spinnerX.getSize().height);
        labelX.setLocation(spinnerX.getLocation().x - labelX.getSize().width, spinnerX.getLocation().y);
        labelY.setLocation(spinnerY.getLocation().x - labelY.getSize().width, spinnerY.getLocation().y);
        vegetableChooser.setLocation(generateFieldsButton.getLocation().x, generateFieldsButton.getLocation().y - vegetableChooser.getHeight());
        panel.updateUI();
    }

    private void createMainFrame() {
        window = new JFrame("Game of fields");
        window.setVisible(true);
        window.setSize(WIDTH, HEIGHT);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createPanel() {
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        window.add(panel);
    }

    private void createGenerateFieldsButton() {
        generateFieldsButton = new JButton("Generate fields");
        generateFieldsButton.setBounds(WIDTH / 2 - 35, HEIGHT - 90, BUTTON_WIDTH, BUTTON_HEIGHT);
        generateFieldsButton.addActionListener(e -> {
            try {
                createLabels((Integer) spinnerX.getValue(), (Integer) spinnerY.getValue());
                if (gameEngine != null)
                    gameEngine.setGameIsRunning(false);
                startButton.setEnabled(true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        panel.add(generateFieldsButton);
    }

    private void createVegetableChooser() {
        vegetableChooser = new JComboBox<>(classificationLabels.toArray(new String[0]));
        vegetableChooser.setBounds(WIDTH / 2 - 35, HEIGHT - 80 - generateFieldsButton.getHeight(), BUTTON_WIDTH, BUTTON_HEIGHT - 10);
        panel.add(vegetableChooser);
    }

    private void createStartButton() {
        startButton = new JButton("Start");
        startButton.setBounds(WIDTH / 2 - 35 + generateFieldsButton.getSize().width, HEIGHT - 90, BUTTON_WIDTH - 20, BUTTON_HEIGHT);
        startButton.addActionListener(e -> {
            gameEngine = new GameEngine(labels, vegetableChooser, true);
            Thread gameThread = new Thread(gameEngine);
            gameThread.start();
            startButton.setEnabled(false);
        });
        startButton.setEnabled(false);
        panel.add(startButton);
    }

    private void createSpinners() {
        SpinnerModel spinnerModelX = new SpinnerNumberModel(SPINNER_VALUE, SPINNER_MIN, SPINNER_MAX, SPINNER_STEP);
        SpinnerModel spinnerModelY = new SpinnerNumberModel(SPINNER_VALUE, SPINNER_MIN, SPINNER_MAX, SPINNER_STEP);
        spinnerX = new JSpinner(spinnerModelX);
        spinnerY = new JSpinner(spinnerModelY);
        spinnerX.setSize(SPINNER_WIDTH, SPINNER_HEIGHT);
        spinnerY.setSize(SPINNER_WIDTH, SPINNER_HEIGHT);
        spinnerX.setLocation(generateFieldsButton.getLocation().x - spinnerX.getSize().width, generateFieldsButton.getLocation().y);
        spinnerY.setLocation(generateFieldsButton.getLocation().x - spinnerY.getSize().width, generateFieldsButton.getLocation().y + spinnerX.getSize().height);
        panel.add(spinnerX);
        panel.add(spinnerY);
    }

    private void createSpinnerLabels() {
        labelX = new JLabel("rows:");
        labelX.setSize(SPINNER_LABEL_WIDTH, SPINNER_LABEL_HEIGHT);
        labelX.setLocation(spinnerX.getLocation().x - labelX.getSize().width, spinnerX.getLocation().y);
        panel.add(labelX);

        labelY = new JLabel("columns:");
        labelY.setSize(SPINNER_LABEL_WIDTH, SPINNER_LABEL_HEIGHT);
        labelY.setLocation(spinnerY.getLocation().x - labelY.getSize().width, spinnerY.getLocation().y);
        panel.add(labelY);
    }
}
