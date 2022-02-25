plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")

}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.zoho.vtouch.logging_agent.android"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":logging_agent"))
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation ("com.google.code.gson:gson:2.8.5")
}