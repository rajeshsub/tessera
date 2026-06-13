plugins {
    id("tessera.android.library")
    id("tessera.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.rajeshsub.tessera.data.vault"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:otp"))
    implementation(project(":core:crypto"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
