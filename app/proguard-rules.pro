# ProGuard & R8 rules for Marklify

# Preserve OpenCV native interfaces and loaders
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve Room database runtime, schemas and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Preserve Data and Backup Models used in JSON serialization/deserialization
-keep class com.example.data.entity.** { *; }
-keep class com.example.data.backup.** { *; }
-keep class com.example.omr.processing.** { *; }
-keep class com.example.omr.spec.** { *; }

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
-dontwarn androidx.camera.**

# Preserve line numbers and attributes for clean debugging and stack traces
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod
