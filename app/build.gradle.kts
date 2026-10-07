plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.vitahealth.tata"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vitahealth.tata"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        val apiBaseUrl = providers.gradleProperty("TATA_API_BASE_URL").orElse("https://web-services-yzxl.onrender.com/").get()
        require(apiBaseUrl.startsWith("http://") || apiBaseUrl.startsWith("https://"))
        require(apiBaseUrl.endsWith("/"))
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":identity"))
    implementation(project(":carelink"))
    implementation(project(":treatment"))
    implementation(project(":intake"))
    implementation(project(":omission"))
    implementation(project(":monitoring"))
    implementation(project(":analytics"))
    implementation(project(":inventory"))
    implementation(project(":preferences"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
