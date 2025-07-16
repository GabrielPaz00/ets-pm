plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.e2.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.e2.android"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        // Activa Jetpack Compose si lo usas
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}




dependencies {
    implementation(projects.shared)

    // Compose UI y Material3
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compiler)

    // Camera
    val cameraxVersion = "1.1.0"
    implementation ("androidx.camera:camera-core:$cameraxVersion")
    implementation ("androidx.camera:camera-camera2:$cameraxVersion")
    implementation ("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation ("androidx.camera:camera-view:$cameraxVersion")


    // Compose y Accompanist
    implementation("androidx.compose.ui:ui:1.3.0")
    implementation("androidx.compose.material:material:1.3.0")
    implementation("androidx.compose.foundation:foundation:1.3.0")
    implementation("com.google.accompanist:accompanist-permissions:0.25.1")

    // Debugging dependencies
    debugImplementation(libs.compose.ui.tooling)

    //
    implementation("io.coil-kt:coil-compose:2.2.2")

}
configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:30.0-jre")  // Forzar la versión 30.0-jre
    }
}