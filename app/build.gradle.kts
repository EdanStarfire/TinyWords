plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.edanstarfire.tinywords"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.edanstarfire.tinywords"
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}
// ADD or MODIFY the 'kotlin' block like this:
kotlin {
    jvmToolchain(21) // Use the integer value for the JDK version
}

// Test logging configuration
tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed", "standard_out", "standard_error")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation(libs.javapoet) // Add this line if using version catalog
    implementation(libs.datastore.preferences)
    ksp(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.serialization.json)
    // Core library
    testImplementation(libs.junit) // Or testImplementation "junit:junit:4.13.2"
    androidTestImplementation(libs.androidx.junit)

    // AndroidX Test Core
    testImplementation(libs.androidx.test.core) // Or testImplementation "androidx.test:core-ktx:1.5.0"
    androidTestImplementation(libs.androidx.test.core)

    // Robolectric
    testImplementation(libs.robolectric) // Or testImplementation "org.robolectric:robolectric:4.11.1"
    androidTestImplementation(libs.robolectric)

    // Truth (for more readable assertions - optional but recommended)
    testImplementation(libs.truth) // Or testImplementation "com.google.truth:truth:1.1.5"
    androidTestImplementation(libs.truth)

    // Truth (for more readable assertions - optional but recommended)
    testImplementation(libs.guava) // Or testImplementation "com.google.truth:truth:1.1.5"
    androidTestImplementation(libs.guava)

    // Mockito (for mocking if needed, though we might not need it heavily for this class if we control JSON)
    testImplementation(libs.mockito.core) // Or testImplementation "org.mockito:mockito-core:5.4.0"
    testImplementation(libs.mockito.kotlin) // Or testImplementation "org.mockito.kotlin:mockito-kotlin:5.1.0"
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    // For kotlinx.coroutines.test if you add coroutines later
    // testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test) // Use the latest stable version
    androidTestImplementation(libs.kotlinx.coroutines.test) // Use the latest stable version

    androidTestImplementation(kotlin("test"))
}