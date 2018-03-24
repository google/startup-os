java_binary(
    name = "firestore_genrule_java_impl",
    srcs = ["FirestoreGenRuleImpl.java"],
    data = ["google-services.json"],
    deps = ["@org_json_json_jar_20180130//jar"],
    main_class = "com.google.startupos.android.FirestoreGenRuleImpl"
)

genrule(
    name = "firestore-genrule",
    outs = ["FirestoreConfig.java"],
    cmd = "java -jar ./bazel-out/host/bin/android/firestore_genrule_java_impl_deploy.jar > $(@)",
    tools = [
        "google-services.json",
        ":firestore_genrule_java_impl_deploy.jar",
    ],
)

android_library(
    name = "firestore",
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
    name = "android_app",
    srcs = glob([
        "src/main/java/com/google/startupos/android/MainApplication.java",
        "src/main/java/com/google/startupos/android/activities/*.java",
    ]) + [
        ":firestore-genrule",
    ],
    custom_package = "com.google.startupos.android",
    manifest = "src/main/java/com/google/startupos/android/AndroidManifest.xml",
    resource_files = glob(["src/main/java/com/google/startupos/android/res/**"]),
    visibility = ["//visibility:public"],
    deps = [":firestore"],
)
