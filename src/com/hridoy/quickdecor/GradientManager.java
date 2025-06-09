package com.hridoy.quickdecor;


import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import java.util.Random;


public class GradientManager {
    private Random mRandom = new Random();
    private Context mContext;
    private Point mSize;
    private int mTileMode;
    private int[] mColorArray;

    public GradientManager(Point size, int tileMode, int[] colorArray) {
        this.mSize = size;
        this.mTileMode = tileMode;
        this.mColorArray = colorArray;
    }

    // Custom method to generate a random LinearGradient
    protected LinearGradient getRandomLinearGradient() {
        /*
            public LinearGradient (float x0, float y0, float x1, float y1, int[] colors, float[]
                positions, Shader.TileMode tile)

                Create a shader that draws a linear gradient along a line.

                Parameters
                x0 : The x-coordinate for the start of the gradient line
                y0 : The y-coordinate for the start of the gradient line
                x1 : The x-coordinate for the end of the gradient line
                y1 : The y-coordinate for the end of the gradient line
                colors : The colors to be distributed along the gradient line
                positions : May be null. The relative positions [0..1] of each corresponding color
                    in the colors array. If this is null, the the colors are distributed evenly
                    along the gradient line.
                tile : The Shader tiling mode
        */

        LinearGradient gradient = new LinearGradient(
                0,
                0,
                mSize.x,
                mSize.y,
                //getRandomColorArray(), // Colors to draw the gradient
                mColorArray,
                null, // No position defined
                getRandomShaderTileMode() // Shader tiling mode
        );
        // Return the LinearGradient
        return gradient;
    }

    // Custom method to generate a random RadialGradient
    protected RadialGradient getRandomRadialGradient() {
        /*
            public RadialGradient (float centerX, float centerY, float radius, int[] colors,
                float[] stops, Shader.TileMode tileMode)

                Create a shader that draws a radial gradient given the center and radius.

                Parameters
                    centerX : The x-coordinate of the center of the radius
                    centerY : The y-coordinate of the center of the radius
                    radius : Must be positive. The radius of the circle for this gradient.
                    colors : The colors to be distributed between the center and edge of the circle
                    stops : May be null. Valid values are between 0.0f and 1.0f. The relative
                        position of each corresponding color in the colors array. If null, colors
                        are distributed evenly between the center and edge of the circle.
                    tileMode : The Shader tiling mode
        */
        RadialGradient gradient = new RadialGradient(
                mRandom.nextInt(mSize.x),
                mRandom.nextInt(mSize.y),
                mRandom.nextInt(mSize.x),
                //getRandomColorArray(),
                mColorArray,
                null, // Stops position is undefined
                getRandomShaderTileMode() // Shader tiling mode

        );
        // Return the RadialGradient
        return gradient;
    }

    // Custom method to generate a random SweepGradient
    protected SweepGradient getRandomSweepGradient() {
        /*
            public SweepGradient (float cx, float cy, int[] colors, float[] positions)
                A subclass of Shader that draws a sweep gradient around a center point.

                Parameters
                cx : The x-coordinate of the center
                cy : The y-coordinate of the center
                colors : The colors to be distributed between around the center. There must be at
                    least 2 colors in the array.
                positions : May be NULL. The relative position of each corresponding color in the
                    colors array, beginning with 0 and ending with 1.0. If the values are not
                    monotonic, the drawing may produce unexpected results. If positions is NULL,
                    then the colors are automatically spaced evenly.
        */
        SweepGradient gradient = new SweepGradient(
                mRandom.nextInt(mSize.x),
                mRandom.nextInt(mSize.y),
                //getRandomColorArray(), // Colors to draw gradient
                mColorArray,
                null // Position is undefined
        );
        // Return the SweepGradient
        return gradient;
    }

    // Custom method to generate random Shader TileMode
    protected Shader.TileMode getRandomShaderTileMode() {
        /*
            Shader
                Shader is the based class for objects that return horizontal spans of colors during
                drawing. A subclass of Shader is installed in a Paint calling paint.setShader(shader).
                After that any object (other than a bitmap) that is drawn with that paint will get
                its color(s) from the shader.
        */
        Shader.TileMode mode = null;
        //int indicator = mRandom.nextInt(3);
        int indicator = mTileMode;
        if (indicator == 0) {
            /*
                Shader.TileMode : CLAMP
                    replicate the edge color if the shader draws outside of its original bounds
            */
            mode = Shader.TileMode.CLAMP;
        } else if (indicator == 1) {
            /*
                Shader.TileMode : MIRROR
                    repeat the shader's image horizontally and vertically, alternating mirror images
                    so that adjacent images always seam
            */
            mode = Shader.TileMode.MIRROR;
        } else if (indicator == 2){
            /*
                Shader.TileMode : REPEAT
                    repeat the shader's image horizontally and vertically
            */
            mode = Shader.TileMode.REPEAT;
        }
        // Return the random Shader TileMode
        return mode;
    }

    // Custom method to generate random color array
    protected int[] getRandomColorArray() {
        int length = mRandom.nextInt(16 - 3) + 3;
        int[] colors = new int[length];
        for (int i = 0; i < length; i++) {
            colors[i] = getRandomHSVColor();
        }
        // Return the color array
        return colors;
    }

    // Custom method to generate random HSV color
    protected int getRandomHSVColor() {
        /*
            Hue is the variation of color
            Hue range 0 to 360

            Saturation is the depth of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% solid color

            Value/Black is the lightness of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% bright less of a color that means black
        */

        // Generate a random hue value between 0 to 360
        int hue = mRandom.nextInt(361);

        // We make the color depth full
        float saturation = 1.0f;

        // We make a full bright color
        float value = 1.0f;

        // We avoid color transparency
        int alpha = 255;

        // Finally, generate the color
        int color = Color.HSVToColor(alpha, new float[]{hue, saturation, value});

        // Return the color
        return color;
    }
}
