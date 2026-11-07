plugins {
    alias(libs.plugins.android.application)
    // Compilador de Jetpack Compose: transforma las funciones @Composable.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.ghibliapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.ghibliapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        // ViewBinding: genera una clase *Binding por cada layout XML
        // (ej: item_film.xml -> ItemFilmBinding). Evita usar findViewById.
        viewBinding = true
        // Compose: habilitado para la pantalla de detalle (DetailActivity).
        compose = true
    }
}

dependencies {
    // AndroidX base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material) // BottomNavigationView, MaterialCardView y TextInputLayout

    // Fragments + MVVM (ViewModel y LiveData) - versiones de la consigna
    implementation(libs.androidx.fragment.ktx)              // 1.8.6
    implementation(libs.androidx.lifecycle.viewmodel.ktx)   // 2.8.7
    implementation(libs.androidx.lifecycle.livedata.ktx)    // 2.8.7

    // Red: Retrofit + Gson - versiones de la consigna
    implementation(libs.retrofit)                // 2.11.0
    implementation(libs.retrofit.converter.gson) // 2.11.0

    // Imágenes: Picasso - versión de la consigna (se usa en el Adapter y en Compose)
    implementation(libs.picasso) // 2.71828

    // Corrutinas (Dispatchers.IO para red y base de datos)
    implementation(libs.kotlinx.coroutines.android)

    // Jetpack Compose (pantalla de detalle). La BOM define las versiones de cada librería.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)       // setContent { } en una Activity
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview) // @Preview
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)    // Render de @Preview en Android Studio

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
