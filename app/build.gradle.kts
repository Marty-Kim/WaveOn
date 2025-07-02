import java.util.Properties

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id("com.google.gms.google-services")
}

// local.properties 파일에서 API 키 읽기
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.surfing.inthe.wavepark"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.surfing.inthe.wavepark"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API Keys (local.properties에서 가져옴)
        val openWeatherApiKey: String = localProperties.getProperty("OPENWEATHER_API_KEY") ?: "YOUR_OPENWEATHER_API_KEY"
        val waveparkApiKey: String = localProperties.getProperty("WAVEPARK_API_KEY") ?: "YOUR_WAVEPARK_API_KEY"
        val firebaseApiKey: String = localProperties.getProperty("FIREBASE_API_KEY") ?: "YOUR_FIREBASE_API_KEY"
        
        // API URLs (gradle.properties에서 가져옴)
        val waveparkBaseUrl: String = project.findProperty("WAVEPARK_BASE_URL") as? String ?: "https://wavepark.co.kr"
        val weatherApiBaseUrl: String = project.findProperty("WEATHER_API_BASE_URL") as? String ?: "https://apis.data.go.kr/"
        val temperatureApiBaseUrl: String = project.findProperty("TEMPERATURE_API_BASE_URL") as? String ?: "https://api.example.com/temperature"
        
        // BuildConfig 필드 생성
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherApiKey\"")
        buildConfigField("String", "WAVEPARK_API_KEY", "\"$waveparkApiKey\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
        buildConfigField("String", "WAVEPARK_BASE_URL", "\"$waveparkBaseUrl\"")
        buildConfigField("String", "WEATHER_API_BASE_URL", "\"$weatherApiBaseUrl\"")
        buildConfigField("String", "TEMPERATURE_API_BASE_URL", "\"$temperatureApiBaseUrl\"")
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
        viewBinding = true
        dataBinding = true
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit for REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava2")
    implementation ("org.jsoup:jsoup:1.16.1")
    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.zxing:core:3.5.2")

}