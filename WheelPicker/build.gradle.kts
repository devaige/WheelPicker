plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.aigestudio.wheelpicker"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Matches Kotlin 1.9.24
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)

    // Compose Support
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
}

mavenPublishing {
    coordinates("dev.aige.pub", "WheelPicker", "1.2.0")

    pom {
        name.set("WheelPicker")
        description.set("Simple and fantastic wheel view for Android, support Kotlin and Jetpack Compose.")
        url.set("https://github.com/AigeStudio/WheelPicker")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("devaige")
                name.set("AigeStudio")
                email.set("aigestudio@qq.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/AigeStudio/WheelPicker.git")
            developerConnection.set("scm:git:ssh://github.com/AigeStudio/WheelPicker.git")
            url.set("https://github.com/AigeStudio/WheelPicker/tree/master")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
