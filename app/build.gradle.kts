plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")

}

android {
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }

    namespace = "com.example.cropchecker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cropchecker"
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
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation (libs.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.ktx)

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.tensorflow.lite.metadata)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation ("nl.joery.animatedbottombar:library:1.1.0")

    implementation("org.tensorflow:tensorflow-lite:2.14.0") // Core TensorFlow Lite library
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
        exclude(group = "com.google.ai.edge.litert")
    }// Optional: For preprocessing/postprocessing
    implementation("androidx.recyclerview:recyclerview:1.4.0") // RecyclerView
    implementation("androidx.viewpager2:viewpager2:1.1.0")     // ViewPager2
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.airbnb.android:lottie:6.3.0")
    // Retrofit for API calls
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

// For location (if you're using FusedLocationProviderClient)
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

// Coroutine support
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
