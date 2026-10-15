import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Archetype for `ui` modules: Android library + Hilt + Compose + Coil + the shared test stack, Compose UI tests on Robolectric included. */
class AndroidUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("coolblue.android.library")
        pluginManager.apply("coolblue.android.hilt")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
                // Robolectric reaches into a JDK-internal package that JDK 17 and up keep closed.
                unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
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
            "testImplementation"(platform(libs.lib("androidx-compose-bom")))
            "testImplementation"(libs.lib("androidx-compose-ui-test-junit4"))
            "testImplementation"(libs.lib("androidx-compose-ui-test-manifest"))
            "testImplementation"(libs.lib("robolectric"))
            // The Compose test rule waits for idle through Espresso; the 3.5 it brings calls an InputManager method SDK 34+ no longer has.
            "testImplementation"(libs.lib("androidx-test-espresso-core"))
        }
    }
}
