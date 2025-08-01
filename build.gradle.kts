// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

// Ensure module hierarchy is properly established
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}