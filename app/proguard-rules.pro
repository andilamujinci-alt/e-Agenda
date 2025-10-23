# Supabase
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.suratapp.**$$serializer { *; }
-keepclassmembers class com.example.suratapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.suratapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Signature Pad
-keep class com.github.gcacace.signaturepad.** { *; }

# Data Models
-keep class com.example.suratapp.data.models.** { *; }