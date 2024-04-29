# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

############################################ datastore ############################################
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

-keepattributes Signature

# Keep all classes that are used for navigation
-keep class androidx.navigation.** { *; }

# Keep all classes that are used for fragment transactions
-keep class androidx.fragment.** { *; }

# Keep all classes that are used for activity transitions
-keep class androidx.activity.** { *; }

# Keep all Parcelable classes used in navigation arguments
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep all classes that are used as arguments or destinations in navigation graph XML files
-keep class com.shakir.weatherzoomer.ui.*.** { *; }

# Keep default constructors for Firebase models
-keepclassmembers class * {
    public <init>();
}

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models.
# Modify this rule to fit the structure of your app.
-keepclassmembers class com.shakir.weatherzoomer.model.** {
    *;
}