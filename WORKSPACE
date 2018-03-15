git_repository(
    name = 'gmaven_rules',
    remote = 'https://github.com/aj-michael/gmaven_rules',
    commit = '5e89b7cdc94d002c13576fad3b28b0ae30296e55',
)

load('@gmaven_rules//:gmaven.bzl', 'gmaven_rules')
gmaven_rules()

# Uncomment to configure Android SDK. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
# android_sdk_repository(
#    name = "androidsdk",
#    path = "<path to sdk, e.g /Users/vmax/Library/Android/sdk",
#    api_level = "<SDK API version, e.g 27>",
#    build_tools_version = "<SDK Build Tools version, e.g 26.0.2>"
#)

maven_jar(
  name = "com_squareup_okhttp_okhttp_2_7_2",
  artifact = "com.squareup.okhttp:okhttp:jar:2.7.2",
)

maven_jar(
  name = "com_squareup_okio_okio_1_6_0",
  artifact = "com.squareup.okio:okio:jar:1.6.0"
)
