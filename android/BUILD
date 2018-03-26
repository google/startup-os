android_library(
    name = "firestore_config_manager_impl",
    deps = ["@com_google_firebase_firebase_common_11_8_0//aar"],
    visibility = ["//visibility:public"],
    srcs = ["src/main/java/com/google/startupos/android/FirestoreConfigLoader.java"]
)

android_library(
    name = "firestore",
    visibility = ["//visibility:public"],
    exports = [
        "@com_android_support_support_annotations_25_2_0//jar",
        "@com_android_support_support_compat_25_2_0//aar",
        "@com_android_support_support_core_ui_25_2_0//aar",
        "@com_android_support_support_core_utils_25_2_0//aar",
        "@com_android_support_support_fragment_25_2_0//aar",
        "@com_android_support_support_media_compat_25_2_0//aar",
        "@com_android_support_support_v4_25_2_0//aar",
        "@com_google_android_gms_play_services_basement_11_8_0//aar",
        "@com_google_android_gms_play_services_basement_license_11_8_0//aar",
        "@com_google_android_gms_play_services_tasks_11_8_0//aar",
        "@com_google_android_gms_play_services_tasks_license_11_8_0//aar",
        "@com_google_firebase_firebase_common_11_8_0//aar",
        "@com_google_firebase_firebase_common_license_11_8_0//aar",
        "@com_google_firebase_firebase_firestore_11_8_0//aar",
        "@com_squareup_okhttp_okhttp_2_7_2//jar",
        "@com_squareup_okio_okio_1_6_0//jar",
    ],
)

android_library(
    name = "firestore_config_manager",
    exports = [":firestore_config_manager_impl"],
    data = ["src/main/assets/google-services.json"],
)

android_binary(
    name = "android_app",
    srcs = glob([
        "src/main/java/com/google/startupos/android/MainApplication.java",
        "src/main/java/com/google/startupos/android/activities/*.java",
    ]),
    custom_package = "com.google.startupos.android",
    manifest = "src/main/java/com/google/startupos/android/AndroidManifest.xml",
    resource_files = glob(["src/main/java/com/google/startupos/android/res/**"]),
    visibility = ["//visibility:public"],
    assets = ["src/main/assets/google-services.json"],
    assets_dir = "src/main/assets/",
    deps = [":firestore", ":firestore_config_manager"],
)
