package com.hridoy.quickdecor;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.HashMap;

// This annotation will not be present in the built extension
// When you use the optimize, proguard or the deannonate feature.
@DesignerComponent(version = 6, versionName = "1.0", description = "Developed by Hridoy by Fast.", iconName = "icon.png")
public class QuickDecor extends AndroidNonvisibleComponent {

  private static final HashMap<String, GradientBackgroundTemplate> GRADIENT_BACKGROUND_TEMPLATES = new HashMap<>();

  public QuickDecor(ComponentContainer container) {
    super(container.$form());
  }

  //----------------------------------------------------------------------
  // Events
  //----------------------------------------------------------------------

  @SimpleEvent(description = "ErrorOccurred")
  public void ErrorOccurred(String errorFrom, String error){
    EventDispatcher.dispatchEvent(this,"ErrorOccurred", errorFrom, error);
  }

  //----------------------------------------------------------------------
  // Methods
  //----------------------------------------------------------------------

  @SimpleFunction(description = "Sets padding for the component. Use format: single value (e.g., '10') or four values (e.g., '10,20,30,40') for top, left, bottom, right.")
  public void SetPadding(Object component, String padding) {
    if (!(component instanceof AndroidViewComponent)) {
      throw new IllegalArgumentException("Invalid component type. Expected an AndroidViewComponent.");
    }

    View view = ((AndroidViewComponent) component).getView();
    String[] paddingValues = padding.split(",");

    try {
      if (paddingValues.length == 4) {
        // Four-sided padding (top, left, bottom, right)
        int top = Integer.parseInt(paddingValues[0].trim()) * 3;
        int left = Integer.parseInt(paddingValues[1].trim()) * 3;
        int bottom = Integer.parseInt(paddingValues[2].trim()) * 3;
        int right = Integer.parseInt(paddingValues[3].trim()) * 3;
        view.setPadding(left, top, right, bottom);
      } else if (paddingValues.length == 1) {
        // Uniform padding on all sides
        int pad = Integer.parseInt(paddingValues[0].trim()) * 3;
        view.setPadding(pad, pad, pad, pad);
      } else {
        throw new IllegalArgumentException("Invalid padding format. Use a single value or 'top,left,bottom,right'");
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid padding value. Ensure values are numbers.");
    }
  }

  // Method to set margin with support for single or four-sided values
  @SimpleFunction(description = "Sets margin for the component. Use format: single value (e.g., '10') or four values (e.g., '10,20,30,40') for top, left, bottom, right.")
  public void SetMargin(Object component, String margin) {
    if (!(component instanceof AndroidViewComponent)) {
      throw new IllegalArgumentException("Invalid component type. Expected an AndroidViewComponent.");
    }

    View view = ((AndroidViewComponent) component).getView();
    ViewGroup.LayoutParams params = view.getLayoutParams();

    if (!(params instanceof LinearLayout.LayoutParams)) {
      throw new IllegalArgumentException("This component does not support margin settings in its current layout.");
    }

    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) params;
    String[] marginValues = margin.split(",");

    try {
      if (marginValues.length == 4) {
        // Four-sided margin (top, left, bottom, right)
        int top = Integer.parseInt(marginValues[0].trim()) * 3;
        int left = Integer.parseInt(marginValues[1].trim()) * 3;
        int bottom = Integer.parseInt(marginValues[2].trim()) * 3;
        int right = Integer.parseInt(marginValues[3].trim()) * 3;
        layoutParams.setMargins(left, top, right, bottom);
      } else if (marginValues.length == 1) {
        // Uniform margin on all sides
        int marginSize = Integer.parseInt(marginValues[0].trim()) * 3;
        layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize);
      } else {
        throw new IllegalArgumentException("Invalid margin format. Use a single value or 'top,left,bottom,right'");
      }
      view.setLayoutParams(layoutParams);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid margin value. Ensure values are numbers.");
    }
  }

  @SimpleFunction(description = "CreateGradientBackgroundTemplate")
  public void CreateGradientBackgroundTemplate(
          final String id,
          final YailList colorsList,
          final int orientation,
          final int shape,
          final YailList cornersRadius,
          final int strokeWidth,
          final int strokeColor
  ) {

    // Create a new GradientBackgroundTemplate and put it in the map
    GradientBackgroundTemplate template = new GradientBackgroundTemplate(colorsList, orientation, shape, cornersRadius, strokeWidth, strokeColor);

    if (GRADIENT_BACKGROUND_TEMPLATES.containsKey(id)){
      GRADIENT_BACKGROUND_TEMPLATES.replace(id, template);
    } else {
      GRADIENT_BACKGROUND_TEMPLATES.put(id, template);
    }

  }

  @SimpleFunction(description = "ApplyGradientBackground")
  public void ApplyGradientBackground(final String id, final AndroidViewComponent component) {
    GradientBackgroundTemplate template = GRADIENT_BACKGROUND_TEMPLATES.get(id);

    if (template != null) {
      MasterGradientBackground(
              component,
              template.getColorsList(),
              template.getOrientation(),
              template.getShape(),
              template.getCornersRadius(),
              template.getStrokeWidth(),
              template.getStrokeColor()
      );
    } else {
      ErrorOccurred("ApplyGradientBackground", "ID does not exist");
    }
  }

  @SimpleFunction(description = "Set the gradient to any view's background")
  public void MasterGradientBackground(

          AndroidViewComponent component,
          YailList colorsList,
          int orientation,
          int shape,
          YailList cornersRadius,
          int strokeWidth,
          int strokeColor

  ){

    android.graphics.drawable.GradientDrawable layoutGradient=new android.graphics.drawable.GradientDrawable();

    //LG_BgColor(colorsList, layoutGradient);
    //LG_Orientation(orientation, layoutGradient);
    //LG_Shape(shape, layoutGradient);
    //LG_CornerRadius(cornersRadius, layoutGradient);
    layoutGradient.setStroke(strokeWidth*5, strokeColor);

    component.getView().setBackgroundColor(16777215);
    component.getView().setBackground(layoutGradient);

  }

  @SimpleFunction(description = "ProcessColor")
  public int ProcessColor(Object color) {
    // Return integer if the color is already an Integer
    if (color instanceof Integer) {
      return (Integer) color; // Directly return the integer color
    }

    // Handle String input
    else if (color instanceof String) {
      String colorStr = ((String) color).trim(); // Trim whitespace

      // Check if the color string starts with "#" (Hexadecimal color code)
      if (colorStr.startsWith("#")) {
        try {
          // Check if the string contains a comma (indicating RGBA format)
          if (colorStr.contains(",")) {
            // Split the string at commas to separate the color and alpha value
            String[] colorValues = colorStr.split(",");
            int colorAlpha = Integer.parseInt(colorValues[1].trim()); // Get the alpha value

            // Parse the hex color to extract RGB values
            int originalColor = ProcessColor(colorValues[0].trim());// Get the hex color part

            // Adjust the alpha using Color.argb() (values for red, green, and blue are extracted from the original color)
            return Color.argb(colorAlpha, Color.red(originalColor), Color.green(originalColor), Color.blue(originalColor));
          }

          // If the string doesn't contain a comma, treat it as a standard hex color
          return Color.parseColor(colorStr); // Parse and return the hex color

        } catch (IllegalArgumentException e) {
          // Handle invalid hex color format (catch exceptions when parsing the color)
          ErrorOccurred("ProcessColor", "Not a valid hex color");
        }
      }


      // Check for RGB/RGBA format (comma-separated)
      String[] rgbValues = colorStr.split(",");
      if (rgbValues.length == 3 || rgbValues.length == 4) {
        try {
          int r = Integer.parseInt(rgbValues[0].trim());
          int g = Integer.parseInt(rgbValues[1].trim());
          int b = Integer.parseInt(rgbValues[2].trim());
          int a = (rgbValues.length == 4) ? Integer.parseInt(rgbValues[3].trim()) : 100; // Default alpha to 100 (fully opaque)

          // Clamp values for RGB (0-255) and Alpha (0-100)
          r = Math.max(0, Math.min(255, r));
          g = Math.max(0, Math.min(255, g));
          b = Math.max(0, Math.min(255, b));
          a = Math.max(0, Math.min(100, a));

          // Convert alpha percentage (0-100) to 0-255
          a = (int) (a * 2.55); // Convert to 0-255

          return Color.argb(a, r, g, b);
        } catch (NumberFormatException e) {
          // Handle parsing error for RGB values
        }
      }
    }

    // If no valid color is found, return white color (default)
    ErrorOccurred("ProcessColor","No valid color found");
    return Color.WHITE; // Return white color as default
  }

  //----------------------------------------------------------------------
  // Private Methods
  //----------------------------------------------------------------------



}
