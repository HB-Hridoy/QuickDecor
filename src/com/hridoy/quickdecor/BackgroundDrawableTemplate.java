package com.hridoy.quickdecor;

public class BackgroundDrawableTemplate {
    private final Object colorsList;
    private final int gradientType;
    private final int orientation;
    private final int shape;
    private final String cornerSizes;
    private final String cutCorners;
    private final Object stroke;

    public BackgroundDrawableTemplate(
            Object colorsList,
            int gradientType,
            int orientation,
            int shape,
            String cornerSizes,
            String cutCorners,
            Object stroke
    ) {
        this.colorsList = colorsList;
        this.gradientType = gradientType;
        this.orientation = orientation;
        this.shape = shape;
        this.cornerSizes = cornerSizes;
        this.cutCorners = cutCorners;
        this.stroke = stroke;
    }

    // Getters
    public Object getColorsList() { return colorsList; }
    public int getGradientType() { return gradientType; }
    public int getOrientation() { return orientation; }
    public int getShape() { return shape; }
    public String getCornerSizes() { return cornerSizes; }
    public String getCutCorners() { return cutCorners; }
    public Object getStroke() { return stroke; }
}