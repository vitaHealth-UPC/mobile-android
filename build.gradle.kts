plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Declared here and not in the version catalog; :app applies it only when google-services.json exists.
    id("com.google.gms.google-services") version "4.4.2" apply false
}
