# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep BouncyCastle and bitcoinj classes
-keep class org.bouncycastle.** { *; }
-keep class org.bitcoinj.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn org.bitcoinj.**

# Keep Web3j
-keep class org.web3j.** { *; }
-dontwarn org.web3j.**

# Keep SimpleX if integrated via reflection
-keep class chat.simplex.** { *; }
-dontwarn chat.simplex.**

# Keep LiveKit
-keep class io.livekit.** { *; }
-dontwarn io.livekit.**

# Keep Breez SDK
-keep class breez_sdk_liquid.** { *; }
-dontwarn breez_sdk_liquid.**

# Keep Timber
-dontwarn timber.log.Timber

# General Compose/Serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
