import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val application = extensions.findByType(ApplicationExtension::class.java)
        if (application != null) {
            configureCompose(application)
        } else {
            extensions.configure<LibraryExtension> { configureCompose(this) }
        }
    }

    private fun configureCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
        commonExtension.buildFeatures.compose = true
    }
}
