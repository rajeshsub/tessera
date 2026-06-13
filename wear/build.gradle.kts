plugins {
    id("tessera.android.application")
    id("tessera.android.compose")
}

android {
    namespace = "io.github.rajeshsub.tessera.wear"

    defaultConfig {
        // Companion shares the phone applicationId so the watch app is paired as its
        // Wear extension. Wear OS 3 baseline is API 30, so override the module minSdk.
        applicationId = "io.github.rajeshsub.tessera"
        minSdk = 30
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)

    debugImplementation(libs.compose.ui.tooling)
}
