plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("kotlin-kapt")
}

android {
    namespace = "com.persianesricart.mismedidas"
    compileSdk = 34  //36

    defaultConfig {
        applicationId  = "com.persianesricart.mismedidas"
        minSdk = 24
        targetSdk = 34  //36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        //kotlinCompilerExtensionVersion = "1.6.7"
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.compose.ui:ui:1.6.7")
    implementation ("androidx.compose.material:material-icons-extended:1.6.7") // Use the latest version
    //implementation (libs.material3)
    //implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // Room + Coroutine
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    // Lifecycle ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    //Preview
    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.3")
}
