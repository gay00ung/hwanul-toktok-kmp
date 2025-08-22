import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
    }
    
    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.play.services.ads)
            implementation(libs.guava)

            // Firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:34.0.0"))
            implementation(libs.firebase.analytics)

            // Glance
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // DateTime
            implementation(libs.kotlinx.datetime)
            
            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.androidx.compose)
            
            // Database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sqldelight {
    databases {
        create("HwanulDatabase") {
            packageName.set("net.ifmain.hwanultoktok.kmp.database")
        }
    }
}

android {
    namespace = "net.ifmain.hwanultoktok.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.ifmain.hwanultoktok.kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 5
        versionName = "1.0.4"
        
        // Load API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val apiKey = localProperties.getProperty("KOREAEXIM_API_KEY") ?: "YOUR_API_KEY_HERE"
        buildConfigField("String", "KOREAEXIM_API_KEY", "\"$apiKey\"")
        
        // AdMob App ID from local.properties
        val admobAppId = localProperties.getProperty("ADMOB_APP_ID") ?: "YOUR_ADMOB_APP_ID_HERE"
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        manifestPlaceholders["admobAppId"] = admobAppId
        
        // AdMob Banner Unit ID from local.properties
        val admobBannerId = localProperties.getProperty("ADMOB_BANNER_ID") ?: "ca-app-pub-3940256099942544/9214589741"
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerId\"")

        // Korea Holiday API keys from local.properties
        val holidayApiKeyEncoding = localProperties.getProperty("KOREA_HOLIDAY_API_KEY_ENCODING") ?: localProperties.getProperty("KOREA_HOLIDAY_API_KEY_DECODING")
        buildConfigField("String", "KOREA_HOLIDAY_API_KEY_ENCODING", "\"$holidayApiKeyEncoding\"")
        val holidayApiKeyDecoding = localProperties.getProperty("KOREA_HOLIDAY_API_KEY_DECODING") ?: localProperties.getProperty("KOREA_HOLIDAY_API_KEY_ENCODING")
        buildConfigField("String", "KOREA_HOLIDAY_API_KEY_DECODING", "\"$holidayApiKeyDecoding\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            isDebuggable = false
            setProperty("archivesBaseName", "hwanultoktok_${project.version}")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
