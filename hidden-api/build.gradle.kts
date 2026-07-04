plugins {
    id("com.android.library")
}

android {
    namespace = "io.github.xtrlumen.hidden.api"
    buildToolsVersion = "36.0.0"
    compileSdk = 36
    defaultConfig {
        minSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}