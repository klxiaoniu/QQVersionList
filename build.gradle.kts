// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
}

buildscript {
    repositories{
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.secrets.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.oss.licenses.plugin)
    }
}