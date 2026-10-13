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

    // The in-app switch needs both languages on every device, so a Play bundle must not split them off.
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat) // per-app language before Android 13
    implementation(libs.coil.core)
    implementation(libs.coil.network.okhttp)

    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.search.data) // so Hilt sees the repository binding
    implementation(projects.search.ui)
}
