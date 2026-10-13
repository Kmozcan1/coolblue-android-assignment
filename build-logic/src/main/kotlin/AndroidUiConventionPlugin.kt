import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Archetype for `ui` modules: Android library + Hilt + Compose + Coil + the shared test stack. */
class AndroidUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("coolblue.android.library")
        pluginManager.apply("coolblue.android.hilt")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            "implementation"(platform(libs.lib("androidx-compose-bom")))
            "implementation"(libs.lib("androidx-compose-ui"))
            "implementation"(libs.lib("androidx-compose-ui-tooling-preview"))
            "implementation"(libs.lib("androidx-compose-material3"))
            "implementation"(libs.lib("androidx-compose-material-icons-core"))
            "debugImplementation"(libs.lib("androidx-compose-ui-tooling"))
            "implementation"(libs.lib("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.lib("androidx-lifecycle-viewmodel-compose"))
            "implementation"(libs.lib("hilt-lifecycle-viewmodel-compose"))
            "implementation"(libs.lib("coil-compose"))
            "implementation"(project(":core:ui"))

            "testImplementation"(libs.lib("junit"))
            "testImplementation"(libs.lib("kotlinx-coroutines-test"))
            "testImplementation"(libs.lib("mockk"))
            "testImplementation"(libs.lib("turbine"))
            "testImplementation"(libs.lib("truth"))
            "testImplementation"(project(":core:test"))
        }
    }
}
