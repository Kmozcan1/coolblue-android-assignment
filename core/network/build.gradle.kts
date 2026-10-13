plugins {
    alias(libs.plugins.coolblue.android.library)
    alias(libs.plugins.coolblue.android.hilt)
}

android {
    namespace = "com.coolblue.assignment.core.network"

    buildFeatures {
        buildConfig = true // for BuildConfig.DEBUG, to log HTTP only in debug builds
    }
}

dependencies {
    // `api` so feature data modules can declare Retrofit interfaces and catch HttpException.
    api(libs.retrofit)
    api(libs.okhttp)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
}
