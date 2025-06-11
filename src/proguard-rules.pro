# Repackage and simplify
-repackageclasses com.hridoy.quickdecor.repacked
-flattenpackagehierarchy

# Show detailed output
-verbose

# Optimization settings
-optimizationpasses 10
-allowaccessmodification
-mergeinterfacesaggressively
-dontpreverify

# Keep main extension class and all public methods except Debug
-keep public class com.hridoy.quickdecor.QuickDecor {
    public *;
}

# Keep required Kawa/AI2 runtime classes
-keeppackagenames gnu.kawa**
-keeppackagenames gnu.expr**

# Optional: Keep OptionList interfaces if used in enums like Orientation
-keep interface com.google.appinventor.components.common.OptionList
-keep class com.google.appinventor.components.common.OptionList {
    *;
}
