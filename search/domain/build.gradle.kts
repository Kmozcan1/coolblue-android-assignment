plugins {
    alias(libs.plugins.coolblue.kotlin.jvm)
}

dependencies {
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
}
