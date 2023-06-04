package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private JSONObject[] jsonData;
    private JRadioButton[] radioButtons;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    Main(String[] args) {
        // Invoked on the event dispatching thread.
        // Do any initialization here.
        loadDataFromJSON();
    }

    public void show() {
        JFrame frame = new JFrame("My Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create radio buttons panel
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioButtons = new JRadioButton[jsonData.length];
        ButtonGroup buttonGroup = new ButtonGroup();

        for (int i = 0; i < jsonData.length; i++) {
            radioButtons[i] = new JRadioButton(jsonData[i].getString("shortName"));
            radioButtons[i].addActionListener(new RadioButtonListener());
            radioPanel.add(radioButtons[i]);
            buttonGroup.add(radioButtons[i]);
        }

        // Create card panel with card layout
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // Add panels dynamically based on the selected radio button
        for (int i = 0; i < jsonData.length; i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel nameLabel = new JLabel(jsonData[i].getString("name"));
            JLabel categoryLabel = new JLabel(jsonData[i].getString("category"));
            JLabel versionLabel = new JLabel(jsonData[i].getString("version"));

            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Create a JLabel for the barcode image
            JLabel barcodeLabel = new JLabel();
            barcodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            BufferedImage barcodeImage = (BufferedImage) jsonData[i].get("barcodeImage");
            if (barcodeImage != null) {
                ImageIcon barcodeIcon = new ImageIcon(barcodeImage);
                barcodeLabel.setIcon(barcodeIcon);
            }

            panel.add(nameLabel);
            panel.add(categoryLabel);
            panel.add(versionLabel);
            panel.add(barcodeLabel);

            cardPanel.add(panel, jsonData[i].getString("shortName"));
        }

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // Set the menu bar to the frame
        frame.setJMenuBar(menuBar);

        // Set the layout of the main frame
        frame.setLayout(new BorderLayout());
        frame.add(radioPanel, BorderLayout.WEST);
        frame.add(cardPanel, BorderLayout.CENTER);

        // Pack and display the frame
        frame.pack();
        frame.setVisible(true);
    }

    private void loadDataFromJSON() {
        try {
            // Read the JSON data from file1
            JSONTokener tokener = new JSONTokener(new FileReader("C:\\Users\\Admin\\IdeaProjects\\hope\\src\\main\\java\\org\\example\\file.json"));
            JSONArray jsonArray = new JSONArray(tokener);

            // Extract the data from the JSON array
            jsonData = new JSONObject[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonData[i] = jsonArray.getJSONObject(i);

                // Generate barcode image for each JSON object based on the "version" field
                String version = jsonData[i].getString("version");
                BufferedImage barcodeImage = generateBarcodeImage(version);
                jsonData[i].put("barcodeImage", barcodeImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage generateBarcodeImage(String version) {
        int width = 200;
        int height = 50;

        try {
            // Set encoding parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.MARGIN, 2);

            // Generate the barcode bit matrix
            BitMatrix matrix = new MultiFormatWriter().encode(version, BarcodeFormat.CODE_128, width, height, hints);

            // Create a buffered image from the bit matrix
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    int grayValue = matrix.get(x, y) ? 0 : 255;
                    image.setRGB(x, y, new Color(grayValue, grayValue, grayValue).getRGB());
                }
            }

            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private class RadioButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < radioButtons.length; i++) {
                if (radioButtons[i].isSelected()) {
                    cardLayout.show(cardPanel, jsonData[i].getString("shortName"));
                    break;
                }
            }
        }
    }

    public static void main(final String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> new Main(args).show());
    }
}