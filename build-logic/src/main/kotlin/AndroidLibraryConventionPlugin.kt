import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Archetype for every Android library module: the shared SDK and JVM levels, nothing else.
 * Modules with Hilt bindings or injection points add `coolblue.android.hilt` on top.
 * AGP 9 built-in Kotlin: no `org.jetbrains.kotlin.android`; jvmTarget follows compileOptions.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

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
            "implementation"(libs.lib("kotlinx-coroutines-core"))
        }
    }
}
