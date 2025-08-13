plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // This should resolve to kotlin 2.0.0 or higher
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.simtag.androidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.simtag.androidapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime.v261)
    implementation(libs.androidx.room.ktx.v261)
    implementation(libs.androidx.security.crypto)
    ksp(libs.androidx.room.compiler.v261)
    testImplementation(libs.androidx.room.testing)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)

    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.pager)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.ui)
    implementation(libs.androidx.foundation)

    implementation(libs.firebase.bom)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.core)
}
