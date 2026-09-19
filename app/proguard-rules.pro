# androidx.security-crypto pulls in Tink, which references optional annotation-only
# dependencies (error-prone / javax.annotation) that are never present or needed at runtime.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlinx serialization models used for backup/export
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.luxwallet.app.**$$serializer { *; }
-keepclassmembers class com.luxwallet.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.luxwallet.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep domain/backup model classes (data survives obfuscation across app updates)
-keep class com.luxwallet.app.core.model.** { *; }
-keep class com.luxwallet.app.core.database.** { *; }
-keep class com.luxwallet.app.data.BackupPayload { *; }
