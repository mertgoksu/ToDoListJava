plugins {
    alias(libs.plugins.android.application)
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.mertg.yapilacaklarlistesijava"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mertg.yapilacaklarlistesijava"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.2.1") // RecyclerView bağımlılığı
    implementation (platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation( "com.google.firebase:firebase-auth:21.0.6")
    implementation ("com.google.firebase:firebase-firestore:24.0.2")
    implementation ("com.google.firebase:firebase-analytics")

}