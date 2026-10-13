import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Archetype for the `app` module: Android application + Hilt via KSP + Compose. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("coolblue.android.hilt")

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
            // R8 for release: Compose is markedly slower unshrunk. The libraries ship their own keep rules.
            buildTypes.getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            }
        }
    }
}
