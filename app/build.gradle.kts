plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.ikoro.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ikoro.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val breezApiKey: String = project.findProperty("breezApiKey") as? String
            ?: project.findProperty("breez_api_key") as? String
            ?: ""
        buildConfigField("String", "BREEZ_API_KEY", "\"$breezApiKey\"")

        val rootstockRpc: String = project.findProperty("rootstockRpc") as? String
            ?: project.findProperty("rootstock_rpc") as? String
            ?: "https://public-node.rsk.co"
        buildConfigField("String", "ROOTSTOCK_RPC", "\"$rootstockRpc\"")

        val etherscanApiKey: String = project.findProperty("etherscanApiKey") as? String ?: ""
        buildConfigField("String", "ETHERSCAN_API_KEY", "\"$etherscanApiKey\"")

        val agabraNpub: String = project.findProperty("agabraNpub") as? String
            ?: "npub13ufag8855wayvsf0kzu9ml3dh8yc55pp0z89fd3pnswdxp28gfsqch86wq"
        buildConfigField("String", "AGABRA_NPUB", "\"$agabraNpub\"")

        buildConfigField("String", "AGABRA_INVITE_ENDPOINT", "\"https://smp.ugogbe.info/agbara/invite\"")
        buildConfigField("String", "SMP_SERVER_URI", "\"smp://kG8TDZb0A1r1MCy5do5eVfQTInWPliSI7XOjJObSy1E=:ikoro-smp-pass-2026@smp.ugogbe.info:5223,443\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("ikoro-release.keystore")
            storePassword = System.getenv("IKORO_KEYSTORE_PASSWORD") ?: "dummy-keystore-password"
            keyAlias = "ikoro"
            keyPassword = System.getenv("IKORO_KEY_PASSWORD") ?: "dummy-key-password"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if ((System.getenv("IKORO_KEYSTORE_PASSWORD") ?: "").isNotBlank()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }
        debug {
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf("**/*.so")
            keepDebugSymbols += listOf("**/*.so")
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // Phase 1: Stable Shell only.
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Compose BOM
    // Nostr SDK for cross-platform secure DMs to Agbara
    implementation("org.rust-nostr:nostr-sdk-kmp-android:0.44.3")

    // QR codes (generate + scan)
    implementation("com.google.zxing:core:3.5.3")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Database (placeholder for later phases)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Networking / crypto
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.zxing:core:3.5.3")

    // Bitcoin / Lightning (Breez Liquid)
    implementation("breez_sdk_liquid:bindings-android:0.12.3-dev1") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }

    // Bitcoin / BIP-39
    implementation("org.bitcoinj:bitcoinj-core:0.17-alpha1") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }

    // EVM (web3j)
    implementation("org.web3j:core:4.10.3")
    implementation("org.web3j:contracts:4.10.3")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
