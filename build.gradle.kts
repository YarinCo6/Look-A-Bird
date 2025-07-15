buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1") // For compileSdk 36
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.0")
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
