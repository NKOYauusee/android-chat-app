plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.example.mychatapp"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    dataBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.mychatapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":common"))
    implementation(project(":api"))
    implementation(project(":database"))

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    //viewpager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.14.2")
    //annotationProcessor("com.github.bumptech.glide:compiler:4.14.0")
    kapt("com.github.bumptech.glide:compiler:4.14.0")
    // RxAndroid 2
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    // Okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")

    // websocket
    implementation("org.java-websocket:Java-WebSocket:1.5.1")

    // PictureSelector 基础 (必须)
    implementation("io.github.lucksiege:pictureselector:v3.11.2")
    implementation("io.github.lucksiege:compress:v3.11.2")
    implementation("io.github.lucksiege:ucrop:v3.11.2")

    implementation("me.rosuh:AndroidFilePicker:1.0.1")
    // 图片预览
    implementation("io.github.FlyJingFish.OpenImage:OpenImageFullLib:2.2.6")
    implementation("com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v9.0.0-release-jitpack")


    //imagePreview 不适配androidx
    //implementation("com.ycjiang:ImagePreview:2.3.5")
    //implementation("com.github.chrisbanes:PhotoView:1.3.1")
}