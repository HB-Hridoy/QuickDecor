package com.hridoy.quickdecor;

import com.google.appinventor.components.runtime.util.YailList;

public class GradientBackgroundTemplate {
    private final YailList colorsList;
    private final int orientation;
    private final int shape;
    private final YailList cornersRadius;
    private final int strokeWidth;
    private final int strokeColor;

    public GradientBackgroundTemplate(YailList colorsList, int orientation, int shape, YailList cornersRadius, int strokeWidth, int strokeColor) {
        this.colorsList = colorsList;
        this.orientation = orientation;
        this.shape = shape;
        this.cornersRadius = cornersRadius;
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
    }

    // Getters for each property
    public YailList getColorsList() { return colorsList; }
    public int getOrientation() { return orientation; }
    public int getShape() { return shape; }
    public YailList getCornersRadius() { return cornersRadius; }
    public int getStrokeWidth() { return strokeWidth; }
    public int getStrokeColor() { return strokeColor; }
}

