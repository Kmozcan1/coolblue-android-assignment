import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Archetype for `data` modules and `core:network`: Android library + Hilt via KSP.
 * AGP 9 built-in Kotlin: no `org.jetbrains.kotlin.android`; jvmTarget follows compileOptions.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        extensions.configure<LibraryExtension> {
            compileSdk = AndroidConfig.COMPILE_SDK
            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
            }
            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA
                targetCompatibility = AndroidConfig.JAVA
            }
        }

        dependencies {
            "implementation"(libs.lib("hilt-android"))
            "ksp"(libs.lib("hilt-compiler"))
            "implementation"(libs.lib("kotlinx-coroutines-core"))
        }
    }
}
