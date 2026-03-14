# Keep our main activity
-keep class com.george.iconhelper.MainActivity { *; }
-keep class com.george.iconhelper.extraction.** { *; }
-keep class com.george.iconhelper.storage.** { *; }

# Keep Kotlin reflection
-keep class kotlin.Metadata { *; }

