# Pure Tasbeeh — ProGuard rules
# Property of Adrees ul Hassan

-keep class com.adreesulhassan.puretasbeeh.data.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
