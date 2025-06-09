package com.hridoy.quickdecor.helpers;

import com.google.appinventor.components.common.OptionList;

import java.util.HashMap;
import java.util.Map;

public enum Shape implements OptionList<Integer> {
    Rectangle(0),
    Circle(1);

    private Integer shape;

    Shape(Integer mShape) {
        this.shape = mShape;
    }

    public Integer toUnderlyingValue() {
        return shape;
    }

    private static final Map<Integer, Shape> lookup = new HashMap<>();

    static {
        for(Shape mShape : Shape.values()) {
            lookup.put(mShape.toUnderlyingValue(), mShape);
        }
    }

    public static Shape fromUnderlyingValue(Integer mShape) {
        return lookup.get(mShape);
    }
}
