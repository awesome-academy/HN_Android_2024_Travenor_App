class Versions {
    static final String coreKtx = "1.8.0"
    static final String appCompat = "1.4.1"
    static final String material = "1.5.0"
    static final String constraintLayout = "2.1.4"
    static final String espressoCore = "3.4.0"

    static final String jUnit = "4.13.2"
    static final String junitVersion = "1.1.3"
    static final String ktlint = "11.5.1"

    //UI
    static final String flexBoxLayout = "3.0.0"
    static final String viewPager2 = "1.0.0"

    static final String gson = "2.8.8"

    //Glide
    static final String glide = "4.16.0"

    //Play service map
    static final String playServiceLocation = "21.2.0"
    static final String playServiceMap = "18.2.0"
}

class ClassPath {
    static final String ktLint = "org.jlleitschuh.gradle:ktlint-gradle:${Versions.ktlint}"
}

class Plugins {
    static final String ktLint = "org.jlleitschuh.gradle.ktlint"
}

class Deps {
    static final String core_ktx = "androidx.core:core-ktx:${Versions.coreKtx}"
    static final String appcompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    static final String material = "com.google.android.material:material:${Versions.material}"
    static final String constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    static final String androidx_espresso_core = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"

    // Testing
    static final String junit = "junit:junit:${Versions.jUnit}"
    static final String androidx_junit = "androidx.test.ext:junit:${Versions.junitVersion}"

    //UI
    static final String flexBoxlayout = "com.google.android.flexbox:flexbox:${Versions.flexBoxLayout}"
    static final String viewPager2 = "androidx.viewpager2:viewpager2:${Versions.viewPager2}"

    static final String gson = "com.google.code.gson:gson:${Versions.gson}"

    //Glide
    static final String glide = "com.github.bumptech.glide:glide:${Versions.glide}"

    //Play service map
    static final String playServiceLocation = "com.google.android.gms:play-services-location:$Versions.playServiceLocation"
    static final String playServiceMap = "com.google.android.gms:play-services-maps:$Versions.playServiceMap"
}

class AppConfigs {
    static final String application_id = "com.example.travenor"
    static final String app_name = "Travenor"
    static final String app_name_debug = "Travenor dev"
    static final int compile_sdk_version = 33
    static final int min_sdk_version = 24
    static final int target_sdk_version = 33
    static final int version_code = 1
    static final String version_name = "1.0"
    static final int version_code_release = 1
    static final String version_name_release = "1.0"
}
