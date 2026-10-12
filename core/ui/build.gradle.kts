plugins {
    alias(libs.plugins.coolblue.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.coolblue.assignment.core.ui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
}
