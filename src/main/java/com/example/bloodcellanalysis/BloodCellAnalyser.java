package com.example.bloodcellanalysis;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class BloodCellAnalyser {
    private Image originalImage;
    private WritableImage triColorImage;
    private List<Cell> detectedCells = new ArrayList<>();

    private static final int MIN_CELL_SIZE = 20;

    public BloodCellAnalyser(Image image) {
        this.originalImage = image;
    }

    public Image getOriginalImage() {
        return originalImage;
    }

    public Image getTriColorImage() {
        return triColorImage;
    }

    private double threshold = 0.3;

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<Cell> getDetectedCells() {
        return detectedCells;
    }

    public void convertToTriColor() { // converts to tricolor (red, purple, white)
        int width = (int) originalImage.getWidth(); // get width of image
        int height = (int) originalImage.getHeight(); // get height
        triColorImage = new WritableImage(width, height); // create empty writable image with the same size
        PixelReader reader = originalImage.getPixelReader(); // gets the pixel reader

        for (int y = 0; y < height; y++) { // loop through each row
            for (int x = 0; x < width; x++) { // loop through each column
                Color color = reader.getColor(x, y); // read color of scanned pixel
                double hue = color.getHue(); // get color of pixel
                double saturation = color.getSaturation(); // get saturation
                double brightness = color.getBrightness(); // gets brightness

                if ((hue >= 330 || hue <= 30) && saturation > 0.15 && brightness < 0.95) {
                    triColorImage.getPixelWriter().setColor(x, y, Color.RED); // if pixel is red mark as red
                } else if (hue >= 200 && hue <= 280 && saturation > 0.1 && brightness < 0.85) {
                    triColorImage.getPixelWriter().setColor(x, y, Color.PURPLE); // if pixel is purple (white blood cell) - mark as purple
                } else {
                    triColorImage.getPixelWriter().setColor(x, y, Color.WHITE); // otherwise mark as white (background)
                }
            }
        }
    }

    private WritableImage triColorPreview;

    public void generateTriColorPreview(double threshold) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();
        triColorPreview = new WritableImage(width, height);
        PixelReader reader = originalImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double hue = color.getHue();
                double saturation = color.getSaturation();
                double brightness = color.getBrightness();

                if ((hue >= 330 || hue <= 30) && saturation > threshold && brightness < 0.98) {
                    triColorPreview.getPixelWriter().setColor(x, y, Color.RED);
                } else if (hue >= 200 && hue <= 280 && saturation > threshold && brightness < 0.9) {
                    triColorPreview.getPixelWriter().setColor(x, y, Color.PURPLE);
                } else {
                    triColorPreview.getPixelWriter().setColor(x, y, Color.WHITE);
                }
            }
        }
    }


    public void analyzeCells() {
        int width = (int) triColorImage.getWidth();
        int height = (int) triColorImage.getHeight();
        PixelReader reader = triColorImage.getPixelReader();
        int[] pixelId = new int[width * height];
        Arrays.fill(pixelId, -1);
        UnionFind uf = new UnionFind(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                if (color.equals(Color.RED) || color.equals(Color.PURPLE)) {
                    int id = y * width + x;
                    pixelId[id] = id;

                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                                Color neighborColor = reader.getColor(nx, ny);
                                if (sameColor(neighborColor, color)) {
                                    uf.union(id, ny * width + nx);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<Integer, Cell> cellGroups = new HashMap<>();
        Map<Integer, Integer> cellSizes = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int id = y * width + x;
                if (pixelId[id] != -1) {
                    int root = uf.find(id);
                    Color color = reader.getColor(x, y);
                    CellType type = color.equals(Color.RED) ? CellType.RED : CellType.WHITE;

                    cellGroups.putIfAbsent(root, new Cell(type));
                    cellSizes.put(root, cellSizes.getOrDefault(root, 0) + 1);

                    Cell cell = cellGroups.get(root);
                    cell.minX = Math.min(cell.minX, x);
                    cell.minY = Math.min(cell.minY, y);
                    cell.maxX = Math.max(cell.maxX, x);
                    cell.maxY = Math.max(cell.maxY, y);
                }
            }
        }

        List<Integer> singleRedCellAreas = new ArrayList<>();
        List<Integer> singleRedCellPixelCounts = new ArrayList<>();

        for (Map.Entry<Integer, Cell> entry : cellGroups.entrySet()) {
            Cell cell = entry.getValue();
            int size = cellSizes.get(entry.getKey());

            int cellWidth = cell.maxX - cell.minX;
            int cellHeight = cell.maxY - cell.minY;
            int area = cellWidth * cellHeight;

            if (cell.type == CellType.RED) {
                if (area >= 100 && area <= 800 && size <= 1000) {
                    singleRedCellAreas.add(area);
                    singleRedCellPixelCounts.add(size);
                }
            }
        }


        double avgArea = singleRedCellAreas.stream().mapToInt(a -> a).average().orElse(400);
        double avgPixels = singleRedCellPixelCounts.stream().mapToInt(a -> a).average().orElse(400);

        detectedCells.clear();
        for (Map.Entry<Integer, Cell> entry : cellGroups.entrySet()) {
            Cell cell = entry.getValue();
            int size = cellSizes.get(entry.getKey());

            int cellWidth = cell.maxX - cell.minX;
            int cellHeight = cell.maxY - cell.minY;
            int area = cellWidth * cellHeight;

            if (area >= 80 &&
                    cellWidth >= 5 && cellHeight >= 5 &&
                    Math.min(cellWidth, cellHeight) >= 0.4 * Math.max(cellWidth, cellHeight)) {

                if (cell.type == CellType.RED) {
                    if (size > 2.0 * avgPixels || area > 2.0 * avgArea) {
                        cell.type = CellType.CLUSTER;
                    }

                }

                detectedCells.add(cell);
            }
        }
    }


    public int countSingleRedCells() {
        int count = 0;
        for (Cell cell : detectedCells) {
            if (cell.type == CellType.RED) count++;
        }
        return count;
    }

    public int countRedClusters() {
        int count = 0;
        for (Cell cell : detectedCells) {
            if (cell.type == CellType.CLUSTER) count++;
        }
        return count;
    }

    public int countWhiteCells() {
        int count = 0;
        for (Cell cell : detectedCells) {
            if (cell.type == CellType.WHITE) count++;
        }
        return count;
    }



    private boolean sameColor(Color c1, Color c2) {
        if ((isRed(c1) && isRed(c2)) || (isWhite(c1) && isWhite(c2))) {
            return true;
        }
        return colorDistance(c1, c2) < 0.25;
    }

    private boolean isRed(Color color) {
        double hue = color.getHue();
        double saturation = color.getSaturation();
        double brightness = color.getBrightness();
        return (hue >= 330 || hue <= 30) && saturation > 0.1 && brightness < 0.98;
    }

    private boolean isWhite(Color color) {
        double hue = color.getHue();
        double saturation = color.getSaturation();
        double brightness = color.getBrightness();
        return !(isRed(color)) && saturation < 0.2 && brightness > 0.9;
    }


    private double colorDistance(Color c1, Color c2) {
        double dr = c1.getRed() - c2.getRed();
        double dg = c1.getGreen() - c2.getGreen();
        double db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public List<Rectangle> getRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        for (Cell cell : detectedCells) {
            rectangles.add(cell.toRectangle());
        }
        return rectangles;
    }
}

