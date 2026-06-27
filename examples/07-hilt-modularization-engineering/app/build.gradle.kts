plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.helloandroid.engineering"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.helloandroid.engineering"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://dev.api.hello-android.local/\"")
            buildConfigField("String", "ENVIRONMENT_NAME", "\"dev\"")
            buildConfigField("boolean", "ENABLE_VERBOSE_LOG", "true")
            resValue("string", "app_name", "工程化课程 Dev")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://api.hello-android.local/\"")
            buildConfigField("String", "ENVIRONMENT_NAME", "\"prod\"")
            buildConfigField("boolean", "ENABLE_VERBOSE_LOG", "false")
            resValue("string", "app_name", "工程化课程")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(project(":core-network"))
    implementation(project(":data-course"))
    implementation(project(":feature-course"))

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
