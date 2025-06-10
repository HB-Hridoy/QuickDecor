package com.hridoy.quickdecor.helpers;

import com.google.appinventor.components.common.OptionList;

import java.util.HashMap;
import java.util.Map;

public enum StrokeType implements OptionList<Integer> {
    Solid(0),
    Dashed(1),
    Dotted(2),
    DashDot(3);

    private Integer strokeType;

    StrokeType(Integer mStrokeType) {
        this.strokeType = mStrokeType;
    }

    public Integer toUnderlyingValue() {
        return strokeType;
    }

    private static final Map<Integer, StrokeType> lookup = new HashMap<>();

    static {
        for(StrokeType mStrokeType : StrokeType.values()) {
            lookup.put(mStrokeType.toUnderlyingValue(), mStrokeType);
        }
    }

    public static StrokeType fromUnderlyingValue(Integer mStrokeType) {
        return lookup.get(mStrokeType);
    }
}
