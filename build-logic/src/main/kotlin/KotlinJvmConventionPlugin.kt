import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Archetype for plain Kotlin modules (`search:domain`, `core:test`): no Android, same JVM level as the Android modules. */
class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(AndroidConfig.JAVA.majorVersion))
            }
        }
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = AndroidConfig.JAVA
            targetCompatibility = AndroidConfig.JAVA
        }
    }
}
