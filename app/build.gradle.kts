import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

// Firebase reads app/google-services.json, which is not versioned. Without it the app builds and push stays off.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.vitahealth.tata"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vitahealth.tata"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val apiBaseUrl = providers.gradleProperty("TATA_API_BASE_URL").orElse("https://web-services-yzxl.onrender.com/").get()
        require(apiBaseUrl.startsWith("http://") || apiBaseUrl.startsWith("https://"))
        require(apiBaseUrl.endsWith("/"))
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl}\"")
        // Social SDK provider identifiers remain in ignored local.properties and google-services.json.
        // These client identifiers (not Facebook's App Secret) are resources in the installed APK.
        val localProps = Properties()
        val localConfig = rootProject.file("local.properties")
        if (localConfig.exists()) localConfig.inputStream().use { stream -> localProps.load(stream) }
        val facebookAppId = localProps.getProperty("TATA_FACEBOOK_APP_ID", "")
            .takeIf { it.matches(Regex("[0-9]{5,30}")) }.orEmpty()
        val facebookClientToken = localProps.getProperty("TATA_FACEBOOK_CLIENT_TOKEN", "")
            .takeIf { it.matches(Regex("[A-Za-z0-9_-]{5,256}")) }.orEmpty()
        resValue("string", "tata_facebook_app_id", facebookAppId)
        resValue("string", "tata_facebook_client_token", facebookClientToken)
        buildConfigField("boolean", "FACEBOOK_LOGIN_CONFIGURED",
            (facebookAppId.isNotBlank() && facebookClientToken.isNotBlank()).toString())
        buildConfigField("boolean", "GOOGLE_LOGIN_CONFIGURED",
            file("google-services.json").exists().toString())

        // Optional, verified public support destination; absent means disabled contact (#69).
        // The academic prototype does not register FCM device tokens by default.
        val pushRegistrationEnabled = providers.gradleProperty("TATA_PUSH_REGISTRATION_ENABLED")
            .orElse("false").get().equals("true", ignoreCase = true)
        buildConfigField("boolean", "PUSH_REGISTRATION_ENABLED", pushRegistrationEnabled.toString())
        val publicContact = providers.gradleProperty("TATA_PUBLIC_CONTACT_URI").orElse("").get()
        require(publicContact.length <= 1024 &&
            Regex("[A-Za-z0-9:/?&=.%+@_#-]*").matches(publicContact))
        buildConfigField("String", "PUBLIC_CONTACT_URI", "\"" + publicContact + "\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

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
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("com.facebook.android:facebook-login:18.3.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}


// Opt-in local Spring integration; normal JVM tests do not need a running server.
tasks.register<JavaExec>("verifyLocalBackendContracts") {
    group = "verification"
    description = "Checks Android adapters against an isolated local Spring backend."
    dependsOn("compileDebugUnitTestKotlin")
    mainClass.set("com.vitahealth.tata.carelink.integration.LocalBackendContractProbe")
    classpath = tasks.named<Test>("testDebugUnitTest").get().classpath
    val liveContextPath = providers.gradleProperty("tata.liveContext")
    doFirst {
        val contextPath = liveContextPath.orNull
            ?: error("Pass -Ptata.liveContext=<private local fixture file>")
        args(contextPath)
    }
}
