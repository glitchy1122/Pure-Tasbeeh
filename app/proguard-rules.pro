# Pure Tasbeeh — ProGuard rules
# Property of Adrees ul Hassan

-keep class com.adreesulhassan.puretasbeeh.data.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class com.batoulapps.adhan.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
