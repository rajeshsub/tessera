plugins {
    id("tessera.kotlin.library")
}

dependencies {
    implementation(project(":core:model"))

    testImplementation(libs.junit)
}
