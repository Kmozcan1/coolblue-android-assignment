plugins {
    alias(libs.plugins.coolblue.kotlin.jvm)
}

dependencies {
    api(projects.core.domain) // AppResult is part of SearchRepository's signature
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
}
