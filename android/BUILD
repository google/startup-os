py_binary(
    name = "firestore-genrule-py-impl",
    srcs = ["firestore-genrule-py-impl.py"],
    data = ["google-services.json"],
)

genrule(
    name = "firestore-genrule",
    outs = ["FirestoreConfig.java"],
    cmd = "python android/firestore-genrule-py-impl.py > $(@)",
    tools = [
        "google-services.json",
        ":firestore-genrule-py-impl",
    ],
)

android_library(
    name = "firebase_integration",
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

android_binary(
    name = "android",
    srcs = glob([
        "src/main/java/com/google/bazel/example/android/MainApplication.java",
        "src/main/java/com/google/bazel/example/android/activities/*.java",
    ]) + [
        ":firestore-genrule",
    ],
    custom_package = "com.google.bazel.example.android",
    manifest = "src/main/java/com/google/bazel/example/android/AndroidManifest.xml",
    resource_files = glob(["src/main/java/com/google/bazel/example/android/res/**"]),
    visibility = ["//visibility:public"],
    deps = [":firebase_integration"],
)
