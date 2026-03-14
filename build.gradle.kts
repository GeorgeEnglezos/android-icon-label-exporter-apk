import java.util.Properties

plugins {
    id("com.android.application") version "8.1.0"
    kotlin("android") version "1.9.0"
}

// Read and auto-increment patch version on each build
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply { load(versionPropsFile.inputStream()) }
val patch = versionProps.getProperty("patch").toInt() + 1
versionProps.setProperty("patch", patch.toString())
versionProps.store(versionPropsFile.outputStream(), null)

val appVersionName = "1.0.$patch.beta"

android {
    namespace = "com.george.iconhelper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.george.iconhelper"
        minSdk = 26
        targetSdk = 34
        versionCode = patch
        versionName = appVersionName
        buildConfigField("String", "APP_VERSION", "\"$appVersionName\"")
    }

    buildFeatures {
        buildConfig = true
    }

    applicationVariants.all {
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "icon-helper-v$appVersionName-${name}.apk"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("com.google.android.material:material:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}
