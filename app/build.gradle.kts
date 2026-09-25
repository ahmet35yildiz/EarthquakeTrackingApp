import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.android.junit)
}

android {
    namespace = "com.ahmetyildiz.quakealert"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ahmetyildiz.quakealert"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Languages for the in-app language picker, generated from the res/values-* folders.
        buildConfigField(
            "String[]",
            "SUPPORTED_LANGUAGE_TAGS",
            findSupportedLanguageTags().joinToString(prefix = "{", postfix = "}") { "\"$it\"" },
        )
    }

    androidResources {
        // Generates locales_config.xml (system per-app language settings, Android 13+) from the same folders.
        generateLocaleConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

/**
 * Returns the default language (from res/resources.properties) followed by the language of every
 * `values-<language>/strings.xml`, e.g. ["en", "tr"]. Adding a translation folder is all it takes to add a language.
 */
fun findSupportedLanguageTags(): List<String> {
    val resDirectory: File = file("src/main/res")
    val resourceProperties = Properties().apply {
        File(resDirectory, "resources.properties").inputStream().use(::load)
    }
    val defaultTag: String = resourceProperties.getProperty("unqualifiedResLocale")
    val languageQualifier = Regex("^[a-z]{2,3}(-r[A-Z]{2})?$")
    val translatedTags: List<String> = resDirectory.listFiles().orEmpty()
        .filter { it.name.startsWith("values-") && File(it, "strings.xml").exists() }
        .map { it.name.removePrefix("values-") }
        .filter { languageQualifier.matches(it) }
        .map { it.replace("-r", "-") }
        .sorted()
    return listOf(defaultTag) + translatedTags
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
