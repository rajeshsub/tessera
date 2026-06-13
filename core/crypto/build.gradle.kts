plugins {
    id("tessera.android.library")
}

android {
    namespace = "io.github.rajeshsub.tessera.core.crypto"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.argon2kt)

    testImplementation(libs.junit)
}
