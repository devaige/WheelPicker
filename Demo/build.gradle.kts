plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.aigestudio.wheelpicker.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aigestudio.wheelpicker.demo"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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
    implementation(project(":WheelPicker"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
}
