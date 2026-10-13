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

    androidResources {
        // English and Dutch only, also for the libraries' strings; Android 13+ lists them under App languages.
        localeFilters += listOf("en", "nl")
        generateLocaleConfig = true
    }

    // Android 13+ lets users pick the app's language in system settings: ship both, so the pick finds its strings.
    bundle {
        language {
            enableSplit = false
        }
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
