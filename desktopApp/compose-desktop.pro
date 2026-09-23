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

# compose-desktop.pro

# Tell ProGuard to stop breaking the build over optional, compile-time-only dependencies
-dontwarn **

-dontoptimize

# Unlike Android's default AGP/R8 rules, plain ProGuard doesn't preserve an enum's values()/
# valueOf() reflection machinery by default. Without this, http4k-server-netty's
# Method.valueOf(methodName) (used to turn every inbound Netty request into an http4k Request)
# throws for every request; the caller swallows that via runCatching { }.getOrNull() and responds
# 501 Not Implemented, unconditionally, for every method and every path.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

