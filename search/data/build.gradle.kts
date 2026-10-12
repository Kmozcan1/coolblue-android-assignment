plugins {
    alias(libs.plugins.coolblue.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.coolblue.assignment.search.data"
}

dependencies {
    implementation(projects.search.domain)
    implementation(projects.core.network)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit.converter.kotlinx.serialization)
}
