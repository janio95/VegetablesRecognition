package fields.ui;

import javax.swing.*;
import java.io.File;

public class CustomJLabel extends JLabel {
    private File imageToClassification;
    private boolean visited;
    private boolean recognized;

    public File getImageToClassification() {
        return imageToClassification;
    }

    public void setImageToClassification(File imageToClassification) {
        this.imageToClassification = imageToClassification;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public void setRecognized(boolean recognized) {
        this.recognized = recognized;
    }
}
