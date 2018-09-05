package fields.utils;

import fields.ui.CustomJLabel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LabelUtils {

    private static final String PATH = "traktor/traktor.jpg";

    public static CustomJLabel createFieldLabel(int size, int locationX, int locationY, File image) throws IOException {
        CustomJLabel label = new CustomJLabel();
        label.setSize(size, size);
        label.setLocation((locationX * size + 1) + 5, (locationY * size + 1) + 5); // Sets the location
        label.setOpaque(true);
        label.setImageToClassification(image);
        Image img = ImageIO.read(image);
        Image scaledInstance = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledInstance);
        label.setIcon(icon);
        return label;
    }

    public static void setCombinedIcon(CustomJLabel customJLabel) throws IOException {
        final BufferedImage imgFG = ImageIO.read(new File(PATH));
        ImageIcon imageIcon;
        if (customJLabel.getIcon() != null) {
            final BufferedImage imgBG = ImageIO.read(customJLabel.getImageToClassification());
            Image imgBG2 = imgBG.getScaledInstance(customJLabel.getWidth(), customJLabel.getHeight(), Image.SCALE_SMOOTH);
            final BufferedImage combinedImage = new BufferedImage(
                    customJLabel.getWidth(),
                    customJLabel.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = combinedImage.createGraphics();
            g.drawImage(imgBG2, 0, 0, null);
            g.drawImage(imgFG, 0, 5, null);
            g.dispose();
            imageIcon = new ImageIcon(combinedImage);
        } else {
            customJLabel.setHorizontalTextPosition(JLabel.CENTER);
            imageIcon = new ImageIcon(imgFG);
        }
        customJLabel.setIcon(imageIcon);
    }
}
