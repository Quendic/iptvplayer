# Room
-keep class * extends androidx.room.RoomDatabase

# Ktor
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # workaround for https://youtrack.jetbrains.com/issue/KT-46731
-keep,allowobfuscation,allowoptimization class kotlinx.serialization.internal.** { *; }
-keep,allowobfuscation,allowoptimization class * extends kotlinx.serialization.internal.** { *; }
