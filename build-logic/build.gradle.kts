plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "coolblue.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "coolblue.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidUi") {
            id = "coolblue.android.ui"
            implementationClass = "AndroidUiConventionPlugin"
        }
        register("kotlinJvm") {
            id = "coolblue.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
    }
}
