import org.gradle.api.JavaVersion

/** The SDK and JVM levels every module shares, so they can only change in one place. */
internal object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val TARGET_SDK = 37
    val JAVA: JavaVersion = JavaVersion.VERSION_17
}
