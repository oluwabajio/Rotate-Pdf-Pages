plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "rotate.pdf.pages"
    compileSdk = 36

    defaultConfig {
        applicationId = "rotate.pdf.pages"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue(
                "string", "admob_banner", "ca-app-pub-9295514865119591/4036992869"
            )
            resValue("string", "admob_interstitial", "ca-app-pub-9295514865119591/9045776967")
            resValue("string", "admob_rewarded", "")
            resValue("string", "admob_app_open", "")
        }

        debug {
            resValue("string", "admob_banner", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_interstitial", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_rewarded", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admob_app_open", "ca-app-pub-3940256099942544/3419835294")
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
        viewBinding = true
        buildConfig = true
    }
}

    dependencies {

        implementation("androidx.core:core-ktx:1.17.0")
        implementation("androidx.appcompat:appcompat:1.7.1")
        implementation("com.google.android.material:material:1.13.0")
        implementation("androidx.constraintlayout:constraintlayout:2.2.1")
        implementation("androidx.navigation:navigation-fragment-ktx:2.9.3")
        implementation("androidx.navigation:navigation-ui-ktx:2.9.3")
        implementation("com.google.android.gms:play-services-ads-lite:24.3.0")

        implementation("com.itextpdf:itext7-core:9.3.0")
    }