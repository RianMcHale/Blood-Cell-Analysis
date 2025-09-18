package com.example.bloodcellanalysis;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cell {
    public int minX = Integer.MAX_VALUE;
    public int minY = Integer.MAX_VALUE;
    public int maxX = Integer.MIN_VALUE;
    public int maxY = Integer.MIN_VALUE;

    public CellType type;
    public Color color;

    public Cell(CellType type) {
        this.type = type;
    }

    public Rectangle toRectangle() {
        Rectangle rect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        if (type == CellType.RED) {
            rect.setStroke(Color.GREEN);
        } else if (type == CellType.WHITE) {
            rect.setStroke(Color.PURPLE);
        } else if (type == CellType.CLUSTER) {
            rect.setStroke(Color.BLUE);
        }
        rect.setFill(null);
        rect.setStrokeWidth(2);
        return rect;
    }
}
