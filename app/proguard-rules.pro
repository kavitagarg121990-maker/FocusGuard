# Room ProGuard Rules
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# DataStore & Protobuf
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
