import org.gradle.api.tasks.compile.JavaCompile

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.rqm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rqm"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("org.apache.poi:poi-ooxml:5.2.5")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
