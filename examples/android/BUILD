package(default_visibility = ["//visibility:public"])

android_library(
    name = "firestore_config_manager_impl",
    srcs = ["examples/android/FirestoreInitializer.java"],
    deps = ["@com_google_firebase_firebase_common_11_8_0//aar"],
)

android_library(
    name = "firestore",
    exports = [
        "@com_android_support_support_annotations_27_0_2//jar",
        "@com_android_support_support_compat_27_0_2//aar",
        "@com_android_support_support_core_ui_27_0_2//aar",
        "@com_android_support_support_core_utils_27_0_2//aar",
        "@com_android_support_support_fragment_27_0_2//aar",
        "@com_android_support_support_media_compat_27_0_2//aar",
        "@com_android_support_support_v4_27_0_2//aar",
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
    data = ["src/main/assets/google-services.json"],
    exports = [":firestore_config_manager_impl"],
)

android_binary(
    name = "android_app",
    srcs = glob([
        "examples/android/MainApplication.java",
        "examples/android/activities/*.java",
    ]),
    assets = ["src/main/assets/google-services.json"],
    assets_dir = "src/main/assets/",
    custom_package = "com.google.startupos.examples.android",
    manifest = "examples/android/AndroidManifest.xml",
    resource_files = glob(["examples/android/res/**"]),
    deps = [
        ":firestore",
        ":firestore_config_manager",
    ],
)
