import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Archetype for the `app` module: Android application + Hilt via KSP + Compose. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        extensions.configure<ApplicationExtension> {
            compileSdk = AndroidConfig.COMPILE_SDK
            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
                targetSdk = AndroidConfig.TARGET_SDK
            }
            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA
                targetCompatibility = AndroidConfig.JAVA
            }
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            "implementation"(libs.lib("hilt-android"))
            "ksp"(libs.lib("hilt-compiler"))
        }
    }
}
