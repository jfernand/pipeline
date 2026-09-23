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

# The http4k-ai-mcp-core jar ships one consumer ProGuard rule per message class, each meant to
# keep that class's synthetic "all Kotlin defaults" constructor (used by Kotshi's Moshi codegen
# whenever a field with a default value — e.g. every message's `_meta: Meta = Meta.default` — is
# omitted from the JSON, which is every real client, every request). Those generated rules target
# the wrong class (the *JsonAdapter class instead of the message class itself), so ProGuard reports
# "the configuration refers to the unknown method" for every one of them and keeps nothing.
# Shrinking then strips the real synthetic constructors, and every MCP request fails to parse
# (JSON-RPC -32600 "Invalid Request") regardless of how well-formed the request actually is.
# Keep the whole message/protocol/model surface directly rather than relying on Kotshi's broken
# per-class rules — these are wire-format DTOs with nothing worth shrinking anyway.
-keep class org.http4k.ai.** { *; }
-keep class org.http4k.connect.model.** { *; }

