package com.hridoy.quickdecor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;
import com.hridoy.quickdecor.helpers.Orientation;
import com.hridoy.quickdecor.helpers.Shape;

import java.util.*;

// This annotation will not be present in the built extension
// When you use the optimize, proguard or the deannonate feature.
@DesignerComponent(version = 22, versionName = "1.0", description = "Developed by Hridoy by Fast.", iconName = "icon.png")
public class QuickDecor extends AndroidNonvisibleComponent {

  private final String TAG = "QuickDecor";

  private static final HashMap<String, GradientBackgroundTemplate> GRADIENT_BACKGROUND_TEMPLATES = new HashMap<>();
  private static final Map<Integer, GradientDrawable.Orientation> ORIENTATION_MAP = new HashMap<>();
  static {
    ORIENTATION_MAP.put(10, GradientDrawable.Orientation.LEFT_RIGHT);
    ORIENTATION_MAP.put(11, GradientDrawable.Orientation.RIGHT_LEFT);
    ORIENTATION_MAP.put(12, GradientDrawable.Orientation.TOP_BOTTOM);
    ORIENTATION_MAP.put(13, GradientDrawable.Orientation.BOTTOM_TOP);
    ORIENTATION_MAP.put(14, GradientDrawable.Orientation.BL_TR);
    ORIENTATION_MAP.put(15, GradientDrawable.Orientation.BR_TL);
    ORIENTATION_MAP.put(16, GradientDrawable.Orientation.TL_BR);
    ORIENTATION_MAP.put(17, GradientDrawable.Orientation.TR_BL);
  }

  private static final Map<Integer, Integer> STROKE_TYPE_MAP = new HashMap<>();
  static {
    STROKE_TYPE_MAP.put(0, CustomBackgroundDrawable.STROKE_TYPE_SOLID);
    STROKE_TYPE_MAP.put(1, CustomBackgroundDrawable.STROKE_TYPE_DASHED);
    STROKE_TYPE_MAP.put(2, CustomBackgroundDrawable.STROKE_TYPE_DOTTED);
    STROKE_TYPE_MAP.put(3, CustomBackgroundDrawable.STROKE_TYPE_DASH_DOT);
    STROKE_TYPE_MAP.put(4, CustomBackgroundDrawable.STROKE_TYPE_CUSTOM);
  }
  private boolean DEBUG_MODE = true;

  private Context context;

  public QuickDecor(ComponentContainer container) {
    super(container.$form());
      this.context = container.$context();
  }

  //----------------------------------------------------------------------
  // Events
  //----------------------------------------------------------------------

  @SimpleEvent(description = "ErrorOccurred")
  public void ErrorOccurred(String errorFrom, String error){
    EventDispatcher.dispatchEvent(this,"ErrorOccurred", errorFrom, error);
  }

  @SimpleEvent(description = "Triggered when a debug message is generated. This event is only fired if debugging is enabled.")
  public void Debug(String source, String message) {
    if (LogDebug()) {
      EventDispatcher.dispatchEvent(this, "Debug", source, message);
      Log.d(TAG, source + " : " + message);
    }
  }

  @DesignerProperty(
          editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False"
  )
  @SimpleProperty(description = "")
  public void LogDebug(boolean debug){
    DEBUG_MODE = debug;
  }
  @SimpleProperty(description = "")
  public boolean LogDebug(){
    return DEBUG_MODE;
  }

  //----------------------------------------------------------------------
  // Methods
  //----------------------------------------------------------------------

  @SimpleFunction(description = "Sets padding for the component. Use format: single value (e.g., '10') or four values (e.g., '10,20,30,40') for top, left, bottom, right.")
  public void SetPadding(Object component, String padding) {
    if (!(component instanceof AndroidViewComponent)) {
      ErrorOccurred("SetPadding", "Invalid component type. Expected an AndroidViewComponent.");
      Debug("SetPadding", "Invalid component type.");
      return;
    }

    View view = ((AndroidViewComponent) component).getView();
    List<Integer> paddingValues = parseCsvRow(padding);
    Debug("SetPadding", "Parsed padding values: " + paddingValues.toString());

    try {
      int top = paddingValues.get(0) * 3;
      int left = paddingValues.get(1) * 3;
      int bottom = paddingValues.get(2) * 3;
      int right = paddingValues.get(3) * 3;

      Debug("SetPadding", "Computed: top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right);
      view.setPadding(left, top, right, bottom);
    } catch (Exception e) {
      ErrorOccurred("SetPadding", "Invalid padding value. Ensure values are numbers.");
      Debug("SetPadding", "Exception: " + e.getMessage());
    }
  }

  @SimpleFunction(description = "Sets margin for the component. Use format: single value (e.g., '10') or four values (e.g., '10,20,30,40') for top, left, bottom, right.")
  public void SetMargin(Object component, String margin) {
    if (!(component instanceof AndroidViewComponent)) {
      ErrorOccurred("SetMargin", "Invalid component type. Expected an AndroidViewComponent.");
      Debug("SetMargin", "Invalid component type.");
      return;
    }

    View view = ((AndroidViewComponent) component).getView();
    ViewGroup.LayoutParams params = view.getLayoutParams();

    if (!(params instanceof LinearLayout.LayoutParams)) {
      ErrorOccurred("SetMargin", "This component does not support margin settings in its current layout.");
      Debug("SetMargin", "Unsupported layout params: " + params.getClass().getSimpleName());
      return;
    }

    List<Integer> marginValues = parseCsvRow(margin);
    Debug("SetMargin", "Parsed margin values: " + marginValues.toString());

    try {
      int top = marginValues.get(0) * 3;
      int left = marginValues.get(1) * 3;
      int bottom = marginValues.get(2) * 3;
      int right = marginValues.get(3) * 3;

      Debug("SetMargin", "Computed: top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right);

      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) params;
      layoutParams.setMargins(left, top, right, bottom);
      view.setLayoutParams(layoutParams);
    } catch (Exception e) {
      ErrorOccurred("SetMargin", "Failed to set margins. Ensure values are valid integers.");
      Debug("SetMargin", "Exception: " + e.getMessage());
    }
  }

  @SimpleFunction(description = "Creates or updates a gradient background template identified by a unique ID.\n" +
          "Parameters:\n" +
          "- id: Unique identifier for the gradient template.\n" +
          "- colorsList: List of colors for the gradient.\n" +
          "- orientation: Gradient orientation (see Orientation options).\n" +
          "- shape: Shape of the gradient background (see Shape options).\n" +
          "- cornerRadius: Corner radius for rounded corners (supports dimension units).\n" +
          "- strokeWidth: Width of the border stroke in pixels.\n" +
          "- strokeColor: Color integer for the stroke color.\n" +
          "If a template with the given ID exists, it will be replaced with the new one.")
  public void CreateGradientBackgroundTemplate(
          final String id,
          final YailList colorsList,
          final @Options(Orientation.class) int orientation,
          final @Options(Shape.class) int shape,
          final String cornerRadius,
          final int strokeWidth,
          final int strokeColor
  ) {
    Debug("CreateGradientBackgroundTemplate", "Creating/updating template with ID: " + id);

    GradientBackgroundTemplate template = new GradientBackgroundTemplate(colorsList, orientation, shape, cornerRadius, strokeWidth, strokeColor);

    if (GRADIENT_BACKGROUND_TEMPLATES.containsKey(id)) {
      GRADIENT_BACKGROUND_TEMPLATES.replace(id, template);
      Debug("CreateGradientBackgroundTemplate", "Replaced existing template with ID: " + id);
    } else {
      GRADIENT_BACKGROUND_TEMPLATES.put(id, template);
      Debug("CreateGradientBackgroundTemplate", "Added new template with ID: " + id);
    }
  }


  @SimpleFunction(description = "Applies a previously created gradient background template to the specified component.\n" +
          "Parameters:\n" +
          "- id: The unique identifier of the gradient template to apply.\n" +
          "- component: The component to which the gradient background will be applied.\n" +
          "Raises an error if the template ID does not exist.")
  public void ApplyGradientBackgroundTemplate(final String id, final AndroidViewComponent component) {
    Debug("ApplyGradientBackgroundTemplate", "Attempting to apply template with ID: " + id);

    GradientBackgroundTemplate template = GRADIENT_BACKGROUND_TEMPLATES.get(id);

    if (template != null) {
      Debug("ApplyGradientBackgroundTemplate", "Template found. Applying to component: " + component.getClass().getSimpleName());

      GradientBackground(
              component,
              template.getColorsList(),
              template.getOrientation(),
              template.getShape(),
              template.getCornersRadius(),
              template.getStrokeWidth(),
              template.getStrokeColor()
      );

      Debug("ApplyGradientBackgroundTemplate", "Gradient background applied successfully.");
    } else {
      ErrorOccurred("ApplyGradientBackgroundTemplate", "Template ID '" + id + "' does not exist.");
      Debug("ApplyGradientBackgroundTemplate", "Failed to find template with ID: " + id);
    }
  }


  @SimpleFunction(description = "Sets a gradient background to the specified view component.\n" +
          "Parameters:\n" +
          "- component: The view component to apply the gradient background.\n" +
          "- colorsList: List of colors used in the gradient.\n" +
          "- orientation: Gradient orientation (see Orientation options).\n" +
          "- shape: Shape of the gradient background (see Shape options).\n" +
          "- cornerRadius: Corner radius values as a CSV string (e.g., '10' or '10,20,30,40').\n" +
          "- strokeWidth: Width of the border stroke in pixels (multiplied by 5 internally).\n" +
          "- strokeColor: Color integer for the stroke color.")
  public void GradientBackground(
          AndroidViewComponent component,
          YailList colorsList,
          @Options(Orientation.class) int orientation,
          @Options(Shape.class) int shape,
          String cornerRadius,
          int strokeWidth,
          int strokeColor
  ) {
    Debug("GradientBackground", "Setting gradient background for component: " + component.getClass().getSimpleName());

    try {
      GradientDrawable layoutGradient = new GradientDrawable();

      LG_BgColor(colorsList, layoutGradient);
      Debug("GradientBackground", "Applied colors: " + colorsList.toString());

      LG_Orientation(orientation, layoutGradient);
      Orientation orientationEnum = Orientation.fromUnderlyingValue(orientation);
      String orientationText = orientationEnum != null ? orientationEnum.name() : "Unknown";
      Debug("GradientBackground", "Orientation set to: " + orientationText);

      LG_Shape(shape, layoutGradient);
      Shape shapeEnum = Shape.fromUnderlyingValue(shape);
      String shapeText = shapeEnum != null ? shapeEnum.name() : "Unknown";
      Debug("GradientBackground", "Shape set to: " + shapeText);

      List<Integer> cornerRadiusList = parseCsvRow(cornerRadius);
      LG_CornerRadius(cornerRadiusList, layoutGradient);
      Debug("GradientBackground", "Corner radius applied: " + cornerRadiusList.toString());

      int scaledStrokeWidth = strokeWidth * 5;
      layoutGradient.setStroke(scaledStrokeWidth, strokeColor);
      Debug("GradientBackground", "Stroke set with width: " + scaledStrokeWidth + " and color: " + strokeColor);

      // Clear previous background color to avoid overlay issues
      component.getView().setBackgroundColor(0xFFFFFF);
      component.getView().setBackground(layoutGradient);

      Debug("GradientBackground", "Gradient background successfully applied.");
    } catch (Exception e) {
      ErrorOccurred("GradientBackground", "Failed to set gradient background: " + e.getMessage());
      Debug("GradientBackground", "Exception: " + e.toString());
    }
  }


  @SimpleFunction(description = "Parses a color value. Supports:\n" +
          "- #RRGGBB (hex)\n" +
          "- #AARRGGBB (Android format)\n" +
          "- #RRGGBBAA (web/CSS/Figma format)\n" +
          "- #RRGGBB,alpha (0–100 alpha percent)\n" +
          "- RGB or RGBA (255,0,0 or 255,0,0,128)\n" +
          "- Color integer as string or number\n" +
          "Returns Android color int. Falls back to white if invalid.")
  public int FormatColor(Object color) {
    if (color == null) {
      Debug("FormatColor", "Input color is null");
      ErrorOccurred("FormatColor", "Color is null");
      return Color.WHITE;
    }

    if (color instanceof Number) {
      int intValue = ((Number) color).intValue();
      Debug("FormatColor", "Converted Number to int: " + intValue);
      return intValue;
    }

    if (color instanceof String) {
      String colorStr = ((String) color).trim();
      Debug("FormatColor", "Input is String: " + colorStr);

      // Handle #RRGGBB or #AARRGGBB or #RRGGBB,alpha
      if (colorStr.startsWith("#")) {
        try {
          if (colorStr.contains(",")) {
            String[] parts = colorStr.split(",");
            String hexPart = parts[0].trim();
            int alphaPercent = Integer.parseInt(parts[1].trim());

            Debug("FormatColor", "Hex with alpha percent detected: " + hexPart + ", alpha: " + alphaPercent);

            int baseColor = parseHexColor(hexPart);
            int alpha = Math.max(0, Math.min(100, alphaPercent));
            int alpha255 = (int) (alpha * 2.55);

            int resultColor = Color.argb(
                    alpha255,
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)
            );

            Debug("FormatColor", "Parsed color with alpha: " + Integer.toHexString(resultColor));
            return resultColor;
          } else {
            int resultColor = parseHexColor(colorStr);
            Debug("FormatColor", "Parsed hex color: " + Integer.toHexString(resultColor));
            return resultColor;
          }
        } catch (Exception e) {
          Debug("FormatColor", "Exception parsing hex or alpha: " + e.getMessage());
          ErrorOccurred("FormatColor", "Invalid hex or alpha format: " + colorStr);
          return Color.WHITE;
        }
      }

      // Handle RGB or RGBA, alpha in 0–255 only
      if (colorStr.contains(",")) {
        String[] parts = colorStr.split(",");
        try {
          int r = Integer.parseInt(parts[0].trim());
          int g = Integer.parseInt(parts[1].trim());
          int b = Integer.parseInt(parts[2].trim());

          int a = 255;
          if (parts.length == 4) {
            a = Integer.parseInt(parts[3].trim());
          }

          Debug("FormatColor", "RGB(A) values detected: R=" + r + ", G=" + g + ", B=" + b + ", A=" + a);

          int resultColor = Color.argb(
                  Math.max(0, Math.min(255, a)),
                  Math.max(0, Math.min(255, r)),
                  Math.max(0, Math.min(255, g)),
                  Math.max(0, Math.min(255, b))
          );

          Debug("FormatColor", "Parsed RGB(A) color: " + Integer.toHexString(resultColor));
          return resultColor;
        } catch (NumberFormatException e) {
          Debug("FormatColor", "Exception parsing RGB/RGBA values: " + e.getMessage());
          ErrorOccurred("FormatColor", "Invalid RGB/RGBA values: " + colorStr);
          return Color.WHITE;
        }
      }

      // Try parsing as integer string
      try {
        int parsedInt = Integer.parseInt(colorStr);
        Debug("FormatColor", "Parsed integer color string: " + parsedInt);
        return parsedInt;
      } catch (NumberFormatException e) {
        Debug("FormatColor", "Exception parsing integer color string: " + e.getMessage());
        ErrorOccurred("FormatColor", "Invalid integer color string: " + colorStr);
        return Color.WHITE;
      }
    }

    Debug("FormatColor", "Unsupported color input type: " + color.toString());
    ErrorOccurred("FormatColor", "Unsupported color input: " + color.toString());
    return Color.WHITE;
  }


  @SimpleFunction(description = "Parses a color input and applies an alpha transparency value.\n" +
          "Supports the following input formats:\n" +
          "- #RRGGBB (hexadecimal color)\n" +
          "- #AARRGGBB (Android ARGB format)\n" +
          "- #RRGGBBAA (web/CSS/Figma RGBA hex format)\n" +
          "- #RRGGBB,alpha (alpha as 0–100 percent)\n" +
          "- RGB or RGBA strings (e.g., \"255,0,0\" or \"255,0,0,128\")\n" +
          "- Color integer values (as string or numeric)\n" +
          "The alpha parameter accepts a percentage value between 0 and 100.\n" +
          "Returns an Android color integer with the specified opacity applied.\n" +
          "Defaults to white if the input is invalid.")
  public int ColorOpacity(Object color, int alphaPercent) {
    Debug("ColorOpacity", "Received color input: " + (color != null ? color.toString() : "null") + ", alphaPercent: " + alphaPercent);

    int baseColor = FormatColor(color);
    Debug("ColorOpacity", "Parsed base color: #" + Integer.toHexString(baseColor));

    alphaPercent = Math.max(0, Math.min(100, alphaPercent));
    int alpha = (int) (alphaPercent * 2.55);
    Debug("ColorOpacity", "Clamped alpha percent: " + alphaPercent + ", converted to 0-255 alpha: " + alpha);

    int red = Color.red(baseColor);
    int green = Color.green(baseColor);
    int blue = Color.blue(baseColor);

    int resultColor = Color.argb(alpha, red, green, blue);
    Debug("ColorOpacity", "Resulting color with alpha: #" + Integer.toHexString(resultColor));

    return resultColor;
  }




  //----------------------------------------------------------------------
  // Private Methods
  //----------------------------------------------------------------------

  private List<Integer> parseCsvRow(String input) {
    if (input == null || input.trim().isEmpty()) {
      ErrorOccurred("parseCsvRow", "Error: Input string is empty.");
      Debug("parseCsvRow", "Input string is empty.");
      return Arrays.asList(0, 0, 0, 0);
    }

    String[] parts = input.split(",");
    List<Integer> result = new ArrayList<>();

    if (parts.length > 4) {
      ErrorOccurred("parseCsvRow", "Error: More than 4 values provided. Only the first 4 will be used.");
      Debug("parseCsvRow", "More than 4 values provided; trimming to first 4.");
    }

    // Parse and sanitize input (up to 4 items)
    for (int i = 0; i < Math.min(4, parts.length); i++) {
      try {
        int val = Integer.parseInt(parts[i].trim());
        Debug("parseCsvRow", "Parsed integer value: " + val + " at index " + i);
        result.add(val);
      } catch (NumberFormatException e) {
        ErrorOccurred("parseCsvRow", "Error: '" + parts[i].trim() + "' is not a valid integer. Using 0.");
        Debug("parseCsvRow", "Invalid integer at index " + i + ": '" + parts[i].trim() + "', defaulting to 0");
        result.add(0);
      }
    }

    switch (result.size()) {
      case 1:
        int v = result.get(0);
        Debug("parseCsvRow", "Single value provided, applying to all sides: " + v);
        return Arrays.asList(v, v, v, v);
      case 2:
        Debug("parseCsvRow", "Two values provided, applying to top/bottom and left/right respectively: " + result);
        return Arrays.asList(result.get(0), result.get(1), result.get(0), result.get(1));
      case 3:
        Debug("parseCsvRow", "Three values provided, applying to top, left/right, bottom respectively: " + result);
        return Arrays.asList(result.get(0), result.get(1), result.get(2), 0);
      case 4:
        Debug("parseCsvRow", "Four values provided, applying individually: " + result);
        return result;
      default:
        ErrorOccurred("parseCsvRow", "Error: No valid integers found. Defaulting to [0, 0, 0, 0]");
        Debug("parseCsvRow", "No valid integers found, returning default zero padding.");
        return Arrays.asList(0, 0, 0, 0);
    }
  }

  private int parseHexColor(String hex) {
    Debug("parseHexColor", "Input hex string: " + hex);
    hex = hex.replace("#", "").toUpperCase(Locale.ROOT);
    Debug("parseHexColor", "Sanitized hex string: " + hex);

    if (hex.length() == 6) {
      Debug("parseHexColor", "Parsing as #RRGGBB format");
      // #RRGGBB
      int color = Color.parseColor("#" + hex);
      Debug("parseHexColor", "Parsed color int: " + color);
      return color;
    }

    if (hex.length() == 8) {
      try {
        int firstByte = Integer.parseInt(hex.substring(0, 2), 16); // possible AA (AARRGGBB)
        int lastByte  = Integer.parseInt(hex.substring(6, 8), 16); // possible AA (RRGGBBAA)

        Debug("parseHexColor", "First byte: " + firstByte + ", Last byte: " + lastByte);

        // Heuristic: if first byte <= 32 (low alpha), assume it's AARRGGBB
        if (firstByte <= 32 && lastByte > 32) {
          Debug("parseHexColor", "Assuming AARRGGBB format");
          int a = firstByte;
          int r = Integer.parseInt(hex.substring(2, 4), 16);
          int g = Integer.parseInt(hex.substring(4, 6), 16);
          int b = Integer.parseInt(hex.substring(6, 8), 16);
          return Color.argb(a, r, g, b);
        } else {
          Debug("parseHexColor", "Assuming RRGGBBAA format");
          int r = Integer.parseInt(hex.substring(0, 2), 16);
          int g = Integer.parseInt(hex.substring(2, 4), 16);
          int b = Integer.parseInt(hex.substring(4, 6), 16);
          int a = lastByte;
          return Color.argb(a, r, g, b);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid 8-digit hex: " + hex + ", error: " + e.getMessage());
      }
    }

    Debug("parseHexColor", "Invalid hex length, throwing exception");
    throw new IllegalArgumentException("Hex color must be 6 or 8 digits: " + hex);
  }


  private void LG_BgColor(YailList colorsList, GradientDrawable layoutGradient) {
    String[] arry = colorsList.toStringArray();
    int[] colors = new int[arry.length];

    for (int i = 0; i < arry.length; i++) {
      colors[i] = FormatColor(arry[i]);
    }

    layoutGradient.setColors(colors);
  }

  private void LG_Orientation(int orientation, GradientDrawable layoutGradient) {
    // grd orientation
    if (orientation == 10)
      layoutGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    if (orientation == 11)
      layoutGradient.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
    if (orientation == 12)
      layoutGradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
    if (orientation == 13)
      layoutGradient.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
    if (orientation == 14)
      layoutGradient.setOrientation(GradientDrawable.Orientation.BL_TR);
    if (orientation == 15)
      layoutGradient.setOrientation(GradientDrawable.Orientation.BR_TL);
    if (orientation == 16)
      layoutGradient.setOrientation(GradientDrawable.Orientation.TL_BR);
    if (orientation == 17)
      layoutGradient.setOrientation(GradientDrawable.Orientation.TR_BL);
  }

  private void LG_Shape(int shape, GradientDrawable layoutGradient){
    // this will set shape
    if (shape==0)
      layoutGradient.setShape(GradientDrawable.RECTANGLE);
    if (shape==1)
      layoutGradient.setShape(GradientDrawable.OVAL);
  }

  private void LG_CornerRadius(
          List<Integer> cornersRadius,
          GradientDrawable layoutGradient
  ) {

    if (cornersRadius == null || cornersRadius.size() != 4) {
      ErrorOccurred("CornerRadius", "Invalid corner radius format");
      return;
    }

    float topLeft = cornersRadius.get(0) * 5f;
    float topRight = cornersRadius.get(1) * 5f;
    float bottomRight = cornersRadius.get(2) * 5f;
    float bottomLeft = cornersRadius.get(3) * 5f;

    float[] radii = {
            topLeft, topLeft,         // top-left
            topRight, topRight,       // top-right
            bottomRight, bottomRight, // bottom-right
            bottomLeft, bottomLeft    // bottom-left
    };

    layoutGradient.setCornerRadii(radii);
  }

}
