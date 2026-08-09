plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val pluginAssetDir = layout.buildDirectory.dir("generated/plugin-assets")
val preparePluginAssets by tasks.registering(Copy::class) {
    dependsOn(":sample-plugin:assembleDebug")
    from(project(":sample-plugin").layout.buildDirectory.file("outputs/apk/debug/sample-plugin-debug.apk"))
    into(pluginAssetDir)
    rename { "sample-plugin-debug.apk" }
}

android {
    namespace = "com.helloandroid.codeloading"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.helloandroid.codeloading"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("String", "BUILD_PROFILE", "\"debug\"")
            buildConfigField("Boolean", "MINIFY_ENABLED", "false")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BUILD_PROFILE", "\"release\"")
            buildConfigField("Boolean", "MINIFY_ENABLED", "true")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(pluginAssetDir)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.named("preBuild") {
    dependsOn(preparePluginAssets)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(project(":plugin-contract"))

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
