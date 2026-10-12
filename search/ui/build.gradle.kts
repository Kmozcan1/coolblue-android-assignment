plugins {
    alias(libs.plugins.coolblue.android.ui)
}

android {
    namespace = "com.coolblue.assignment.search.ui"
}

dependencies {
    implementation(projects.search.domain)
}
