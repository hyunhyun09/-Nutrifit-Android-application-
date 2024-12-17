plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nutrifit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nutrifit"
        minSdk = 28
        targetSdk = 34
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
}

dependencies {
    val FOODLENS_SDK_VERSION by extra("1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation ("com.doinglab.foodlens:FoodLens:2.6.6")
    implementation("com.doinglab.foodlens:FoodLens:$FOODLENS_SDK_VERSION") {
        exclude(group = "com.android.support", module = "appcompat")
        exclude(group = "com.android.support", module = "design")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support.constraint", module = "constraint-layout")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}