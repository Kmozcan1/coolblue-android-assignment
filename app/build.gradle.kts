plugins {
    alias(libs.plugins.coolblue.android.application)
}

android {
    namespace = "com.coolblue.assignment"

    defaultConfig {
        applicationId = "com.coolblue.assignment"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.network.okhttp)

    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.search.data) // so Hilt sees the repository binding
    implementation(projects.search.ui)
}
