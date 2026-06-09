package com.hridoy.quickdecor;

import android.annotation.TargetApi;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import java.util.Arrays;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CustomBackgroundDrawable extends Drawable {
    public static final int GRADIENT_TYPE_LINEAR = 0;
    public static final int GRADIENT_TYPE_RADIAL = 1;
    public static final int GRADIENT_TYPE_SWEEP = 2;

    public static final int STROKE_TYPE_SOLID = 0;
    public static final int STROKE_TYPE_DASHED = 1;
    public static final int STROKE_TYPE_DOTTED = 2;
    public static final int STROKE_TYPE_DASH_DOT = 3;
    public static final int STROKE_TYPE_CUSTOM = 4;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Path fillPath = new Path();
    private final Path strokePath = new Path();

    private RectF boundsRect = new RectF();
    private RectF strokeBoundsRect = new RectF();

    private int[] gradientColors;
    private GradientDrawable.Orientation orientation;
    private int gradientType;
    private int shape;

    private float[] cornerSizes = new float[4];
    private boolean[] isCutCorner = new boolean[4];

    private int[] strokeGradientColors;
    private GradientDrawable.Orientation strokeOrientation;
    private int strokeGradientType;
    private float strokeWidth;
    private int strokeType;
    private DashPathEffect dashPathEffect;

    public static class Builder {
        private int[] gradientColors;
        private GradientDrawable.Orientation orientation = GradientDrawable.Orientation.LEFT_RIGHT;
        private int gradientType = GRADIENT_TYPE_LINEAR;
        private int shape = GradientDrawable.RECTANGLE;
        private float[] cornerSizes = new float[4];
        private boolean[] isCutCorner = new boolean[4];

        private int[] strokeGradientColors;
        private GradientDrawable.Orientation strokeOrientation = GradientDrawable.Orientation.LEFT_RIGHT;
        private int strokeGradientType = GRADIENT_TYPE_LINEAR;
        private float strokeWidth = 0f;
        private int strokeType = STROKE_TYPE_SOLID;
        private float dashLength = 10f;
        private float gapLength = 5f;

        public Builder setColors(int... colors) {
            if (colors == null || colors.length < 2) {
                throw new IllegalArgumentException("Gradient requires at least 2 colors");
            }
            this.gradientColors = colors;
            return this;
        }

        public Builder setOrientation(GradientDrawable.Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setGradientType(int type) {
            this.gradientType = type;
            return this;
        }

        public Builder setShape(int shape) {
            this.shape = shape;
            return this;
        }

        public Builder setCornerSizes(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.cornerSizes = new float[]{topLeft, topRight, bottomRight, bottomLeft};
            return this;
        }

        public Builder setCornerTypes(boolean topLeft, boolean topRight, boolean bottomRight, boolean bottomLeft) {
            this.isCutCorner = new boolean[]{topLeft, topRight, bottomRight, bottomLeft};
            return this;
        }

        public Builder setStroke(@NonNull int[] colors, float width) {
            if (colors.length < 2) {
                throw new IllegalArgumentException("Stroke gradient requires at least 2 colors");
            }
            this.strokeGradientColors = colors;
            this.strokeWidth = width;
            return this;
        }

        public Builder setStrokeType(int type, float dash, float gap) {
            this.strokeType = type;
            this.dashLength = dash;
            this.gapLength = gap;
            return this;
        }

        public Builder setStrokeGradientOrientation(GradientDrawable.Orientation orientation) {
            this.strokeOrientation = orientation;
            return this;
        }

        public Builder setStrokeGradientType(int type) {
            this.strokeGradientType = type;
            return this;
        }

        public CustomBackgroundDrawable build() {
            return new CustomBackgroundDrawable(this);
        }
    }

    private CustomBackgroundDrawable(Builder builder) {
        this.gradientColors = builder.gradientColors;
        this.orientation = builder.orientation;
        this.gradientType = builder.gradientType;
        this.shape = builder.shape;
        this.cornerSizes = builder.cornerSizes;
        this.isCutCorner = builder.isCutCorner;
        this.strokeGradientColors = builder.strokeGradientColors;
        this.strokeOrientation = builder.strokeOrientation;
        this.strokeGradientType = builder.strokeGradientType;
        this.strokeWidth = builder.strokeWidth;
        this.strokeType = builder.strokeType;
        updateStrokeEffect(builder.dashLength, builder.gapLength);

        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);
    }

    private void updateStrokeEffect(float dash, float gap) {
        switch (strokeType) {
            case STROKE_TYPE_DASHED:
                dashPathEffect = new DashPathEffect(new float[]{dash, gap}, 0);
                break;
            case STROKE_TYPE_DOTTED:
                dashPathEffect = new DashPathEffect(new float[]{2f, gap}, 0);
                break;
            case STROKE_TYPE_DASH_DOT:
                dashPathEffect = new DashPathEffect(new float[]{dash, gap, 2f, gap}, 0);
                break;
            default:
                dashPathEffect = null;
        }
        strokePaint.setPathEffect(dashPathEffect);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        boundsRect.set(bounds);
        float halfStroke = strokeWidth / 2f;
        strokeBoundsRect.set(bounds.left + halfStroke, bounds.top + halfStroke, bounds.right - halfStroke, bounds.bottom - halfStroke);

        Shader fillShader = createShader(bounds, gradientColors, orientation, gradientType);
        fillPaint.setShader(fillShader);

        Shader strokeShader = createShader(bounds, strokeGradientColors, strokeOrientation, strokeGradientType);
        strokePaint.setShader(strokeShader);

        if (shape == GradientDrawable.OVAL) {
            fillPath.reset();
            strokePath.reset();
            fillPath.addOval(boundsRect, Path.Direction.CW);
            strokePath.addOval(strokeBoundsRect, Path.Direction.CW);
        } else {
            buildCustomPath(fillPath, boundsRect, 0);
            buildCustomPath(strokePath, strokeBoundsRect, strokeWidth / 2f);
        }
    }

    private void buildCustomPath(Path path, RectF rect, float inset) {
        path.reset();
        float left = rect.left + inset;
        float top = rect.top + inset;
        float right = rect.right - inset;
        float bottom = rect.bottom - inset;

        float maxW = (right - left) / 2f;
        float maxH = (bottom - top) / 2f;

        float[] cs = new float[4];
        for (int i = 0; i < 4; i++) {
            // clamp only for round corners
            if (isCutCorner[i]) {
                cs[i] = Math.max(0, cornerSizes[i]);
            } else {
                cs[i] = Math.max(0, Math.min(cornerSizes[i], Math.min(maxW, maxH)));
            }
        }

        // Start top-left
        if (isCutCorner[0] && cs[0] > 0) {
            path.moveTo(left, top + cs[0]);
            path.lineTo(left + cs[0], top);
        } else if (cs[0] > 0) {
            path.moveTo(left, top + cs[0]);
            path.arcTo(new RectF(left, top, left + 2 * cs[0], top + 2 * cs[0]), 180, 90);
        } else {
            path.moveTo(left, top);
        }

        // Top edge → top-right corner
        if (isCutCorner[1] && cs[1] > 0) {
            path.lineTo(right - cs[1], top);
            path.lineTo(right, top + cs[1]);
        } else if (cs[1] > 0) {
            path.lineTo(right - cs[1], top);
            path.arcTo(new RectF(right - 2 * cs[1], top, right, top + 2 * cs[1]), -90, 90);
        } else {
            path.lineTo(right, top);
        }

        // Right edge → bottom-right corner
        if (isCutCorner[2] && cs[2] > 0) {
            path.lineTo(right, bottom - cs[2]);
            path.lineTo(right - cs[2], bottom);
        } else if (cs[2] > 0) {
            path.lineTo(right, bottom - cs[2]);
            path.arcTo(new RectF(right - 2 * cs[2], bottom - 2 * cs[2], right, bottom), 0, 90);
        } else {
            path.lineTo(right, bottom);
        }

        // Bottom edge → bottom-left corner
        if (isCutCorner[3] && cs[3] > 0) {
            path.lineTo(left + cs[3], bottom);
            path.lineTo(left, bottom - cs[3]);
        } else if (cs[3] > 0) {
            path.lineTo(left + cs[3], bottom);
            path.arcTo(new RectF(left, bottom - 2 * cs[3], left + 2 * cs[3], bottom), 90, 90);
        } else {
            path.lineTo(left, bottom);
        }

        path.close();
    }

    private Shader createShader(Rect bounds, int[] colors, GradientDrawable.Orientation orientation, int type) {
        float x0 = bounds.left, y0 = bounds.top, x1 = bounds.right, y1 = bounds.bottom;
        switch (orientation) {
            case LEFT_RIGHT:
                x0 = bounds.left; x1 = bounds.right; y0 = y1 = bounds.centerY(); break;
            case RIGHT_LEFT:
                x0 = bounds.right; x1 = bounds.left; y0 = y1 = bounds.centerY(); break;
            case TOP_BOTTOM:
                y0 = bounds.top; y1 = bounds.bottom; x0 = x1 = bounds.centerX(); break;
            case BOTTOM_TOP:
                y0 = bounds.bottom; y1 = bounds.top; x0 = x1 = bounds.centerX(); break;
            case TL_BR:
                x0 = bounds.left; y0 = bounds.top; x1 = bounds.right; y1 = bounds.bottom; break;
            case TR_BL:
                x0 = bounds.right; y0 = bounds.top; x1 = bounds.left; y1 = bounds.bottom; break;
            case BL_TR:
                x0 = bounds.left; y0 = bounds.bottom; x1 = bounds.right; y1 = bounds.top; break;
            case BR_TL:
                x0 = bounds.right; y0 = bounds.bottom; x1 = bounds.left; y1 = bounds.top; break;
        }

        switch (type) {
            case GRADIENT_TYPE_RADIAL:
                return new RadialGradient(bounds.centerX(), bounds.centerY(), Math.max(bounds.width(), bounds.height()) / 2f, colors, null, Shader.TileMode.CLAMP);
            case GRADIENT_TYPE_SWEEP:
                return new SweepGradient(bounds.centerX(), bounds.centerY(), colors, null);
            default:
                return new LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(fillPath, fillPaint);
        if (strokeWidth > 0) {
            canvas.drawPath(strokePath, strokePaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
