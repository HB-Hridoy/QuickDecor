package com.hridoy.quickdecor.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum Orientation implements OptionList<Integer> {
    LeftToRight(10),
    RightToLeft(11),
    TopToBottom(12),
    BottomToTop(13),
    BottomLeftToTopRight(14),
    BottomRightToTopLeft(15),
    TopLeftToBottomRight(16),
    TopRightToBottomLeft(17);

    private Integer orientation;

    Orientation(Integer mOrientation) {
        this.orientation = mOrientation;
    }

    public Integer toUnderlyingValue() {
        return orientation;
    }

    private static final Map<Integer, Orientation> lookup = new HashMap<>();

    static {
        for(Orientation mOrientation : Orientation.values()) {
            lookup.put(mOrientation.toUnderlyingValue(), mOrientation);
        }
    }

    public static Orientation fromUnderlyingValue(Integer mOrientation) {
        return lookup.get(mOrientation);
    }
}
