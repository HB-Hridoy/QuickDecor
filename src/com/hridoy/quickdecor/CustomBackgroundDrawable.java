
package com.hridoy.quickdecor;

import android.annotation.TargetApi;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CustomBackgroundDrawable extends Drawable {
    private static final String TAG = "CustomBackgroundDrawable";

    // Constants
    private static final int MIN_COLORS = 2;
    private static final int MAX_COLORS = 10;
    private static final float MIN_CORNER_SIZE = 0f;
    private static final float MAX_CORNER_SIZE = 1000f;
    private static final float MIN_STROKE_WIDTH = 0f;
    private static final float MAX_STROKE_WIDTH = 100f;
    private static final float DEFAULT_DASH_LENGTH = 10f;
    private static final float DEFAULT_GAP_LENGTH = 5f;
    private static final float DEFAULT_DOT_LENGTH = 2f;

    // Paint objects
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Path objects with reuse for performance
    private final Path fillPath = new Path();
    private final Path strokePath = new Path();
    private final Path shadowPath = new Path();

    // Bounds and rectangles
    private final RectF boundsRect = new RectF();
    private final RectF strokeBoundsRect = new RectF();
    private final RectF shadowBoundsRect = new RectF();
    private final RectF tempRect = new RectF();

    // Core properties
    private final float[] cornerSizes = new float[4]; // top-left, top-right, bottom-right, bottom-left
    private final boolean[] isCutCorner = new boolean[4]; // same order
    private int[] gradientColors;
    private float[] gradientPositions;
    private GradientDrawable.Orientation orientation;

    // Stroke properties
    private int strokeColor = Color.TRANSPARENT;
    private float strokeWidth = 0f;
    private DashPathEffect dashPathEffect = null;
    private Paint.Cap strokeCap = Paint.Cap.ROUND;
    private Paint.Join strokeJoin = Paint.Join.ROUND;
    private float strokeMiterLimit = 4f;

    // Shadow properties
    private boolean shadowEnabled = false;
    private int shadowColor = Color.BLACK;
    private float shadowRadius = 0f;
    private float shadowDx = 0f;
    private float shadowDy = 0f;

    // Stroke types
    public static final int STROKE_TYPE_SOLID = 0;
    public static final int STROKE_TYPE_DASHED = 1;
    public static final int STROKE_TYPE_DOTTED = 2;
    public static final int STROKE_TYPE_DASH_DOT = 3;
    public static final int STROKE_TYPE_CUSTOM = 4;

    private int strokeType = STROKE_TYPE_SOLID;
    private float dashLength = DEFAULT_DASH_LENGTH;
    private float gapLength = DEFAULT_GAP_LENGTH;
    private float dotLength = DEFAULT_DOT_LENGTH;
    private float[] customDashPattern = null;

    // Performance optimization flags
    private boolean needsPathUpdate = true;
    private boolean needsShaderUpdate = true;
    private Rect lastBounds = new Rect();
    private int currentAlpha = 255;

    // Builder pattern for easier construction
    public static class Builder {
        private int[] gradientColors;
        private float[] gradientPositions;
        private GradientDrawable.Orientation orientation = GradientDrawable.Orientation.LEFT_RIGHT;
        private float[] cornerSizes = new float[4];
        private boolean[] isCutCorner = new boolean[4];

        private int strokeColor = Color.TRANSPARENT;
        private float strokeWidth = 0f;
        private int strokeType = STROKE_TYPE_SOLID;
        private float dashLength = DEFAULT_DASH_LENGTH;
        private float gapLength = DEFAULT_GAP_LENGTH;
        private float dotLength = DEFAULT_DOT_LENGTH;
        private float[] customDashPattern = null;
        private Paint.Cap strokeCap = Paint.Cap.ROUND;
        private Paint.Join strokeJoin = Paint.Join.ROUND;
        private float strokeMiterLimit = 4f;

        private boolean shadowEnabled = false;
        private int shadowColor = Color.BLACK;
        private float shadowRadius = 0f;
        private float shadowDx = 0f;
        private float shadowDy = 0f;

        public Builder() {

        }

        public Builder setColors(int... colors) {
            if (colors == null || colors.length < MIN_COLORS) {
                throw new IllegalArgumentException("At least " + MIN_COLORS + " colors required");
            }
            if (colors.length > MAX_COLORS) {
                throw new IllegalArgumentException("Maximum " + MAX_COLORS + " colors allowed");
            }
            this.gradientColors = colors.clone();
            return this;
        }

        public Builder setGradientPositions(float... positions) {
            if (positions != null && positions.length != gradientColors.length) {
                throw new IllegalArgumentException("Positions array must match colors array length");
            }
            this.gradientPositions = positions != null ? positions.clone() : null;
            return this;
        }

        public Builder setOrientation(@NonNull GradientDrawable.Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setCornerSizes(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.cornerSizes[0] = validateCornerSize(topLeft);
            this.cornerSizes[1] = validateCornerSize(topRight);
            this.cornerSizes[2] = validateCornerSize(bottomRight);
            this.cornerSizes[3] = validateCornerSize(bottomLeft);
            return this;
        }

        public Builder setCornerTypes(boolean topLeftCut, boolean topRightCut, boolean bottomRightCut, boolean bottomLeftCut) {
            this.isCutCorner[0] = topLeftCut;
            this.isCutCorner[1] = topRightCut;
            this.isCutCorner[2] = bottomRightCut;
            this.isCutCorner[3] = bottomLeftCut;
            return this;
        }

        public Builder setStroke(@ColorInt int color, float width) {
            this.strokeColor = color;
            this.strokeWidth = Math.max(MIN_STROKE_WIDTH, Math.min(MAX_STROKE_WIDTH, width));
            return this;
        }

        public Builder setStrokeType(int strokeType) {
            this.strokeType = strokeType;
            return this;
        }

        public Builder setStrokeType(int strokeType, float dashLength, float gapLength) {
            this.strokeType = strokeType;
            this.dashLength = Math.max(1f, dashLength);
            this.gapLength = Math.max(1f, gapLength);
            return this;
        }

        public Builder setStrokeType(int strokeType, float dashLength, float gapLength, float dotLength) {
            this.strokeType = strokeType;
            this.dashLength = Math.max(1f, dashLength);
            this.gapLength = Math.max(1f, gapLength);
            this.dotLength = Math.max(1f, dotLength);
            return this;
        }

        public Builder setCustomStrokePattern(@NonNull float[] pattern) {
            if (pattern != null && pattern.length > 0 && pattern.length % 2 == 0) {
                this.customDashPattern = pattern.clone();
                this.strokeType = STROKE_TYPE_CUSTOM;
            }
            return this;
        }

        public Builder setStrokeCap(@NonNull Paint.Cap cap) {
            this.strokeCap = cap != null ? cap : Paint.Cap.ROUND;
            return this;
        }

        public Builder setStrokeJoin(@NonNull Paint.Join join) {
            this.strokeJoin = join != null ? join : Paint.Join.ROUND;
            return this;
        }

        public Builder setStrokeMiterLimit(float miterLimit) {
            this.strokeMiterLimit = Math.max(1f, miterLimit);
            return this;
        }

        public Builder setShadow(@ColorInt int color, float radius, float dx, float dy) {
            this.shadowEnabled = radius > 0;
            this.shadowColor = color;
            this.shadowRadius = Math.max(0f, radius);
            this.shadowDx = dx;
            this.shadowDy = dy;
            return this;
        }

        public Builder clearShadow() {
            this.shadowEnabled = false;
            this.shadowRadius = 0f;
            return this;
        }

        public CustomBackgroundDrawable build() {
            CustomBackgroundDrawable drawable = new CustomBackgroundDrawable(this);

            // Apply stroke settings
            if (strokeWidth > 0) {
                drawable.setStroke(strokeColor, strokeWidth);
                drawable.setStrokeType(strokeType, dashLength, gapLength, dotLength);
                if (customDashPattern != null) {
                    drawable.setCustomStrokePattern(customDashPattern);
                }
                drawable.setStrokeCap(strokeCap);
                drawable.setStrokeJoin(strokeJoin);
                drawable.setStrokeMiterLimit(strokeMiterLimit);
            }

            // Apply shadow settings
            if (shadowEnabled) {
                drawable.setShadow(shadowColor, shadowRadius, shadowDx, shadowDy);
            }

            return drawable;
        }

        private float validateCornerSize(float size) {
            return Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, size));
        }
    }

    // Private constructor for builder
    private CustomBackgroundDrawable(Builder builder) {
        this.gradientColors = builder.gradientColors.clone();
        this.gradientPositions = builder.gradientPositions != null ? builder.gradientPositions.clone() : null;
        this.orientation = builder.orientation;
        System.arraycopy(builder.cornerSizes, 0, this.cornerSizes, 0, 4);
        System.arraycopy(builder.isCutCorner, 0, this.isCutCorner, 0, 4);

        initializePaints();
        validateConfiguration();
    }

    // Legacy constructor for backward compatibility
    public CustomBackgroundDrawable(@NonNull int[] gradientColors,
                                    @NonNull GradientDrawable.Orientation orientation,
                                    @NonNull float[] cornerSizes,
                                    @NonNull boolean[] isCutCorner) {
        if (gradientColors == null || gradientColors.length < MIN_COLORS) {
            throw new IllegalArgumentException("Gradient must have at least " + MIN_COLORS + " colors.");
        }
        if (gradientColors.length > MAX_COLORS) {
            throw new IllegalArgumentException("Maximum " + MAX_COLORS + " colors allowed.");
        }
        if (cornerSizes == null || cornerSizes.length != 4) {
            throw new IllegalArgumentException("Corner sizes must be an array of 4 elements.");
        }
        if (isCutCorner == null || isCutCorner.length != 4) {
            throw new IllegalArgumentException("Cut corner flags must be an array of 4 elements.");
        }

        this.gradientColors = gradientColors.clone();
        this.orientation = orientation;

        // Validate and copy corner sizes
        for (int i = 0; i < 4; i++) {
            this.cornerSizes[i] = Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, cornerSizes[i]));
        }
        System.arraycopy(isCutCorner, 0, this.isCutCorner, 0, 4);

        initializePaints();
        validateConfiguration();
    }

    private void initializePaints() {
        // Initialize fill paint
        fillPaint.setStyle(Paint.Style.FILL);

        // Initialize stroke paint
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(strokeCap);
        strokePaint.setStrokeJoin(strokeJoin);
        strokePaint.setStrokeMiter(strokeMiterLimit);

        // Initialize shadow paint
        shadowPaint.setStyle(Paint.Style.FILL);
    }

    private void validateConfiguration() {
        try {
            // Validate gradient positions if provided
            if (gradientPositions != null) {
                if (gradientPositions.length != gradientColors.length) {
                    Log.w(TAG, "Gradient positions length doesn't match colors length, ignoring positions");
                    gradientPositions = null;
                } else {
                    // Ensure positions are in ascending order and within [0,1]
                    for (int i = 0; i < gradientPositions.length; i++) {
                        gradientPositions[i] = Math.max(0f, Math.min(1f, gradientPositions[i]));
                        if (i > 0 && gradientPositions[i] < gradientPositions[i-1]) {
                            Log.w(TAG, "Gradient positions not in ascending order, auto-correcting");
                            gradientPositions[i] = gradientPositions[i-1];
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating configuration", e);
        }
    }

    // Enhanced stroke methods
    public void setStroke(@ColorInt int color, @FloatRange(from = 0f, to = MAX_STROKE_WIDTH) float width) {
        if (width < MIN_STROKE_WIDTH || width > MAX_STROKE_WIDTH) {
            Log.w(TAG, "Stroke width " + width + " is out of valid range [" + MIN_STROKE_WIDTH + ", " + MAX_STROKE_WIDTH + "]");
            width = Math.max(MIN_STROKE_WIDTH, Math.min(MAX_STROKE_WIDTH, width));
        }

        this.strokeColor = color;
        this.strokeWidth = width;
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(width);
        needsPathUpdate = true;
        invalidateSelf();
    }

    public void setStrokeType(int strokeType) {
        if (strokeType < STROKE_TYPE_SOLID || strokeType > STROKE_TYPE_CUSTOM) {
            Log.w(TAG, "Invalid stroke type: " + strokeType + ", using SOLID");
            strokeType = STROKE_TYPE_SOLID;
        }
        this.strokeType = strokeType;
        updateStrokeEffect();
        invalidateSelf();
    }

    public void setStrokeType(int strokeType, float dashLength, float gapLength) {
        setStrokeType(strokeType);
        this.dashLength = Math.max(1f, dashLength);
        this.gapLength = Math.max(1f, gapLength);
        updateStrokeEffect();
        invalidateSelf();
    }

    public void setStrokeType(int strokeType, float dashLength, float gapLength, float dotLength) {
        setStrokeType(strokeType, dashLength, gapLength);
        this.dotLength = Math.max(1f, dotLength);
        updateStrokeEffect();
        invalidateSelf();
    }

    public void setCustomStrokePattern(@NonNull float[] pattern) {
        if (pattern == null || pattern.length == 0 || pattern.length % 2 != 0) {
            Log.w(TAG, "Invalid custom stroke pattern, using solid stroke");
            setStrokeType(STROKE_TYPE_SOLID);
            return;
        }

        // Validate pattern values
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] <= 0) {
                Log.w(TAG, "Invalid pattern value at index " + i + ", using default");
                pattern[i] = i % 2 == 0 ? DEFAULT_DASH_LENGTH : DEFAULT_GAP_LENGTH;
            }
        }

        this.customDashPattern = pattern.clone();
        this.strokeType = STROKE_TYPE_CUSTOM;
        updateStrokeEffect();
        invalidateSelf();
    }

    public void setStrokeCap(@NonNull Paint.Cap cap) {
        this.strokeCap = cap != null ? cap : Paint.Cap.ROUND;
        strokePaint.setStrokeCap(this.strokeCap);
        invalidateSelf();
    }

    public void setStrokeJoin(@NonNull Paint.Join join) {
        this.strokeJoin = join != null ? join : Paint.Join.ROUND;
        strokePaint.setStrokeJoin(this.strokeJoin);
        invalidateSelf();
    }

    public void setStrokeMiterLimit(@FloatRange(from = 1f) float miterLimit) {
        this.strokeMiterLimit = Math.max(1f, miterLimit);
        strokePaint.setStrokeMiter(this.strokeMiterLimit);
        invalidateSelf();
    }

    // Shadow methods
    public void setShadow(@ColorInt int color, float radius, float dx, float dy) {
        this.shadowEnabled = radius > 0;
        this.shadowColor = color;
        this.shadowRadius = Math.max(0f, radius);
        this.shadowDx = dx;
        this.shadowDy = dy;

        if (shadowEnabled) {
            shadowPaint.setColor(shadowColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
            }
        }

        needsPathUpdate = true;
        invalidateSelf();
    }

    public void clearShadow() {
        setShadow(Color.TRANSPARENT, 0f, 0f, 0f);
    }

    // Gradient methods
    public void updateGradientColors(@ColorInt int... colors) {
        if (colors == null || colors.length < MIN_COLORS) {
            Log.w(TAG, "Invalid gradient colors, keeping current");
            return;
        }
        if (colors.length > MAX_COLORS) {
            Log.w(TAG, "Too many gradient colors, truncating to " + MAX_COLORS);
            int[] truncated = new int[MAX_COLORS];
            System.arraycopy(colors, 0, truncated, 0, MAX_COLORS);
            colors = truncated;
        }

        this.gradientColors = colors.clone();
        this.gradientPositions = null; // Reset positions when colors change
        needsShaderUpdate = true;
        invalidateSelf();
    }

    public void setGradientPositions(@NonNull float... positions) {
        if (positions == null || positions.length != gradientColors.length) {
            Log.w(TAG, "Gradient positions length doesn't match colors length");
            return;
        }

        this.gradientPositions = positions.clone();
        validateConfiguration();
        needsShaderUpdate = true;
        invalidateSelf();
    }

    // Corner methods
    public void updateCornerSizes(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.cornerSizes[0] = Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, topLeft));
        this.cornerSizes[1] = Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, topRight));
        this.cornerSizes[2] = Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, bottomRight));
        this.cornerSizes[3] = Math.max(MIN_CORNER_SIZE, Math.min(MAX_CORNER_SIZE, bottomLeft));
        needsPathUpdate = true;
        invalidateSelf();
    }

    public void updateCornerTypes(boolean topLeftCut, boolean topRightCut, boolean bottomRightCut, boolean bottomLeftCut) {
        this.isCutCorner[0] = topLeftCut;
        this.isCutCorner[1] = topRightCut;
        this.isCutCorner[2] = bottomRightCut;
        this.isCutCorner[3] = bottomLeftCut;
        needsPathUpdate = true;
        invalidateSelf();
    }

    private void updateStrokeEffect() {
        try {
            switch (strokeType) {
                case STROKE_TYPE_SOLID:
                    dashPathEffect = null;
                    break;
                case STROKE_TYPE_DASHED:
                    dashPathEffect = new DashPathEffect(new float[]{dashLength, gapLength}, 0);
                    break;
                case STROKE_TYPE_DOTTED:
                    dashPathEffect = new DashPathEffect(new float[]{dotLength, gapLength}, 0);
                    break;
                case STROKE_TYPE_DASH_DOT:
                    dashPathEffect = new DashPathEffect(new float[]{dashLength, gapLength, dotLength, gapLength}, 0);
                    break;
                case STROKE_TYPE_CUSTOM:
                    if (customDashPattern != null) {
                        dashPathEffect = new DashPathEffect(customDashPattern, 0);
                    } else {
                        dashPathEffect = null;
                    }
                    break;
                default:
                    dashPathEffect = null;
                    break;
            }
            strokePaint.setPathEffect(dashPathEffect);
        } catch (Exception e) {
            Log.e(TAG, "Error updating stroke effect", e);
            dashPathEffect = null;
            strokePaint.setPathEffect(null);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        // Performance optimization: only update if bounds actually changed
        if (bounds.equals(lastBounds)) {
            return;
        }
        lastBounds.set(bounds);

        try {
            boundsRect.set(bounds);

            // Calculate stroke bounds
            float halfStroke = strokeWidth / 2f;
            strokeBoundsRect.set(
                    bounds.left + halfStroke,
                    bounds.top + halfStroke,
                    bounds.right - halfStroke,
                    bounds.bottom - halfStroke
            );

            // Calculate shadow bounds
            if (shadowEnabled) {
                shadowBoundsRect.set(
                        bounds.left + shadowDx - shadowRadius,
                        bounds.top + shadowDy - shadowRadius,
                        bounds.right + shadowDx + shadowRadius,
                        bounds.bottom + shadowDy + shadowRadius
                );
            }

            // Update shader if needed
            if (needsShaderUpdate || needsPathUpdate) {
                updateShader(bounds);
                needsShaderUpdate = false;
            }

            // Update paths
            if (needsPathUpdate) {
                buildAllPaths();
                needsPathUpdate = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onBoundsChange", e);
        }
    }

    private void updateShader(Rect bounds) {
        try {
            Shader shader = new LinearGradient(
                    getX0(bounds), getY0(bounds),
                    getX1(bounds), getY1(bounds),
                    gradientColors, gradientPositions, Shader.TileMode.CLAMP
            );
            fillPaint.setShader(shader);
        } catch (Exception e) {
            Log.e(TAG, "Error creating shader", e);
            // Fallback to solid color
            fillPaint.setShader(null);
            fillPaint.setColor(gradientColors[0]);
        }
    }

    private void buildAllPaths() {
        try {
            buildFillPath();
            if (strokeWidth > 0) {
                buildStrokePath();
            }
            if (shadowEnabled) {
                buildShadowPath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error building paths", e);
        }
    }

    private void buildFillPath() {
        fillPath.reset();
        float w = boundsRect.width();
        float h = boundsRect.height();

        if (w <= 0 || h <= 0) {
            return;
        }

        // Adjust corner sizes based on available space
        float[] adjustedCorners = adjustCornerSizes(w, h, cornerSizes);

        buildPath(fillPath, boundsRect, adjustedCorners, 0f);
    }

    private void buildStrokePath() {
        strokePath.reset();
        float w = strokeBoundsRect.width();
        float h = strokeBoundsRect.height();

        if (w <= 0 || h <= 0) {
            return;
        }

        // Adjust corner sizes for stroke
        float[] adjustedCorners = adjustCornerSizes(w, h, cornerSizes);
        for (int i = 0; i < 4; i++) {
            adjustedCorners[i] = Math.max(0, adjustedCorners[i] - strokeWidth / 2f);
        }

        buildPath(strokePath, strokeBoundsRect, adjustedCorners, 0f);
    }

    private void buildShadowPath() {
        shadowPath.reset();
        if (!shadowEnabled) return;

        // Shadow path is same as fill path but offset
        float w = boundsRect.width();
        float h = boundsRect.height();

        if (w <= 0 || h <= 0) {
            return;
        }

        tempRect.set(boundsRect);
        tempRect.offset(shadowDx, shadowDy);

        float[] adjustedCorners = adjustCornerSizes(w, h, cornerSizes);
        buildPath(shadowPath, tempRect, adjustedCorners, shadowRadius);
    }

    private float[] adjustCornerSizes(float width, float height, float[] originalSizes) {
        float[] adjusted = new float[4];
        float maxCornerSize = Math.min(width, height) / 2f;

        for (int i = 0; i < 4; i++) {
            adjusted[i] = Math.min(originalSizes[i], maxCornerSize);
        }

        return adjusted;
    }

    private void buildPath(Path path, RectF bounds, float[] corners, float inset) {
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset;
        float w = right - left;
        float h = bottom - top;

        if (w <= 0 || h <= 0) {
            return;
        }

        // Start from top-left
        if (isCutCorner[0] && corners[0] > 0) {
            // Cut corner
            path.moveTo(left, top + corners[0]);
            path.lineTo(left + corners[0], top);
        } else if (corners[0] > 0) {
            // Rounded corner
            path.moveTo(left, top + corners[0]);
            path.quadTo(left, top, left + corners[0], top);
        } else {
            // No corner
            path.moveTo(left, top);
        }

        // Top edge to top-right
        if (isCutCorner[1] && corners[1] > 0) {
            // Cut corner
            path.lineTo(right - corners[1], top);
            path.lineTo(right, top + corners[1]);
        } else if (corners[1] > 0) {
            // Rounded corner
            path.lineTo(right - corners[1], top);
            path.quadTo(right, top, right, top + corners[1]);
        } else {
            // No corner
            path.lineTo(right, top);
        }

        // Right edge to bottom-right
        if (isCutCorner[2] && corners[2] > 0) {
            // Cut corner
            path.lineTo(right, bottom - corners[2]);
            path.lineTo(right - corners[2], bottom);
        } else if (corners[2] > 0) {
            // Rounded corner
            path.lineTo(right, bottom - corners[2]);
            path.quadTo(right, bottom, right - corners[2], bottom);
        } else {
            // No corner
            path.lineTo(right, bottom);
        }

        // Bottom edge to bottom-left
        if (isCutCorner[3] && corners[3] > 0) {
            // Cut corner
            path.lineTo(left + corners[3], bottom);
            path.lineTo(left, bottom - corners[3]);
        } else if (corners[3] > 0) {
            // Rounded corner
            path.lineTo(left + corners[3], bottom);
            path.quadTo(left, bottom, left, bottom - corners[3]);
        } else {
            // No corner
            path.lineTo(left, bottom);
        }

        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (getBounds().isEmpty()) {
            return;
        }

        try {
            // Draw shadow first
            if (shadowEnabled && !shadowPath.isEmpty()) {
                int shadowAlpha = (int) (Color.alpha(shadowColor) * (currentAlpha / 255f));
                shadowPaint.setAlpha(shadowAlpha);
                canvas.drawPath(shadowPath, shadowPaint);
            }

            // Draw fill
            if (!fillPath.isEmpty()) {
                canvas.drawPath(fillPath, fillPaint);
            }

            // Draw stroke
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT && !strokePath.isEmpty()) {
                canvas.drawPath(strokePath, strokePaint);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in draw", e);
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        if (currentAlpha != alpha) {
            currentAlpha = alpha;
            fillPaint.setAlpha(alpha);
            strokePaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (fillPaint.getColorFilter() != colorFilter) {
            fillPaint.setColorFilter(colorFilter);
            strokePaint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getAlpha() {
        return currentAlpha;
    }

    // Getters for current state
    public int[] getGradientColors() {
        return gradientColors.clone();
    }

    public float[] getGradientPositions() {
        return gradientPositions != null ? gradientPositions.clone() : null;
    }

    public GradientDrawable.Orientation getOrientation() {
        return orientation;
    }

    public float[] getCornerSizes() {
        return cornerSizes.clone();
    }

    public boolean[] getCornerTypes() {
        return isCutCorner.clone();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public int getStrokeType() {
        return strokeType;
    }

    public boolean isShadowEnabled() {
        return shadowEnabled;
    }

    private float getX0(Rect bounds) {
        switch (orientation) {
            case LEFT_RIGHT: return bounds.left;
            case RIGHT_LEFT: return bounds.right;
            case TOP_BOTTOM:
            case BOTTOM_TOP: return (bounds.left + bounds.right) / 2f;
            case TL_BR: return bounds.left;
            case TR_BL: return bounds.right;
            case BL_TR: return bounds.left;
            case BR_TL: return bounds.right;
            default: return bounds.left;
        }
    }

    private float getX1(Rect bounds) {
        switch (orientation) {
            case LEFT_RIGHT: return bounds.right;
            case RIGHT_LEFT: return bounds.left;
            case TOP_BOTTOM:
            case BOTTOM_TOP: return (bounds.left + bounds.right) / 2f;
            case TL_BR: return bounds.right;
            case TR_BL: return bounds.left;
            case BL_TR: return bounds.right;
            case BR_TL: return bounds.left;
            default: return bounds.right;
        }
    }

    private float getY0(Rect bounds) {
        switch (orientation) {
            case TOP_BOTTOM: return bounds.top;
            case BOTTOM_TOP: return bounds.bottom;
            case LEFT_RIGHT:
            case RIGHT_LEFT: return (bounds.top + bounds.bottom) / 2f;
            case TL_BR: return bounds.top;
            case TR_BL: return bounds.top;
            case BL_TR: return bounds.bottom;
            case BR_TL: return bounds.bottom;
            default: return bounds.top;
        }
    }

    private float getY1(Rect bounds) {
        switch (orientation) {
            case TOP_BOTTOM: return bounds.bottom;
            case BOTTOM_TOP: return bounds.top;
            case LEFT_RIGHT:
            case RIGHT_LEFT: return (bounds.top + bounds.bottom) / 2f;
            case TL_BR: return bounds.bottom;
            case TR_BL: return bounds.bottom;
            case BL_TR: return bounds.top;
            case BR_TL: return bounds.top;
            default: return bounds.bottom;
        }
    }

}