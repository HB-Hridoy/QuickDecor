package com.hridoy.quickdecor.helpers;

import com.google.appinventor.components.common.OptionList;

import java.util.HashMap;
import java.util.Map;

public enum GradientType implements OptionList<Integer> {
    Linear(0),
    Radial(1),
    Sweep(2);

    private Integer gradientType;

    GradientType(Integer mGradientType) {
        this.gradientType = mGradientType;
    }

    public Integer toUnderlyingValue() {
        return gradientType;
    }

    private static final Map<Integer, GradientType> lookup = new HashMap<>();

    static {
        for(GradientType mGradientType : GradientType.values()) {
            lookup.put(mGradientType.toUnderlyingValue(), mGradientType);
        }
    }

    public static GradientType fromUnderlyingValue(Integer mGradientType) {
        return lookup.get(mGradientType);
    }
}
