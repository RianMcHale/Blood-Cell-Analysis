package com.example.bloodcellanalysis;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.scene.control.Label;

import java.io.File;
import java.util.List;

public class MainController {

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView triColorImageView;

    @FXML
    private CheckBox labelCellsCheckbox;


    @FXML
    private Label cellCountsLabel;

    @FXML
    private Pane overlayPane;

    @FXML
    private Slider thresholdSlider;

    private BloodCellAnalyser analyzer;

    @FXML
    private void onLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Blood Sample Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());

            analyzer = new BloodCellAnalyser(image);

            originalImageView.setImage(image);
            originalImageView.setPreserveRatio(true);
            originalImageView.setFitWidth(image.getWidth());
            originalImageView.setFitHeight(image.getHeight());

            overlayPane.setPrefWidth(image.getWidth());
            overlayPane.setPrefHeight(image.getHeight());

            originalImageView.setX(0);
            originalImageView.setY(0);

            triColorImageView.setFitWidth(image.getWidth());
            triColorImageView.setFitHeight(image.getHeight());
            triColorImageView.setImage(null);
            overlayPane.getChildren().clear();
        }
    }



    @FXML
    private void onRevertToOriginal() {
        if (triColorImageView != null) {
            triColorImageView.setVisible(false);
        }
    }

    @FXML
    private void onConvertToTriColor() {
        if (analyzer == null) return;
        analyzer.setThreshold(thresholdSlider.getValue());
        analyzer.convertToTriColor();
        triColorImageView.setImage(analyzer.getTriColorImage());
        triColorImageView.setVisible(true);
    }




    @FXML
    private void onAnalyzeCells() {
        if (analyzer == null) return;
        analyzer.analyzeCells();

        int redSingles = analyzer.countSingleRedCells();
        int redClusters = analyzer.countRedClusters();
        int whiteCells = analyzer.countWhiteCells();

        cellCountsLabel.setText(
                "Single Red Cells detected: " + redSingles +
                        "\nRed Clusters detected: " + redClusters +
                        "\nWhite Cells detected: " + whiteCells
        );
    }



    @FXML
    private void onShowRectangles() {
        if (analyzer == null) return;

        overlayPane.getChildren().clear();
        List<Rectangle> rectangles = analyzer.getRectangles();

        double imgWidth = analyzer.getOriginalImage().getWidth();
        double imgHeight = analyzer.getOriginalImage().getHeight();
        double viewWidth = originalImageView.getBoundsInLocal().getWidth();
        double viewHeight = originalImageView.getBoundsInLocal().getHeight();
        double scaleX = viewWidth / imgWidth;
        double scaleY = viewHeight / imgHeight;

        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle rect = rectangles.get(i);

            Rectangle scaledRect = new Rectangle(
                    rect.getX() * scaleX,
                    rect.getY() * scaleY,
                    Math.max(rect.getWidth() * scaleX, 2),
                    Math.max(rect.getHeight() * scaleY, 2)
            );
            scaledRect.setFill(null);
            scaledRect.setStroke(rect.getStroke());
            scaledRect.setStrokeWidth(1.5);

            if (labelCellsCheckbox.isSelected()) {
                javafx.scene.text.Text label = new javafx.scene.text.Text(
                        (rect.getX() + rect.getWidth() / 2) * scaleX,
                        (rect.getY() + rect.getHeight() / 2) * scaleY,
                        String.valueOf(i + 1)
                );
                label.setFill(Color.BLACK);
                overlayPane.getChildren().add(label);
            }

            overlayPane.getChildren().add(scaledRect);
        }
    }


}
