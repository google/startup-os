git_repository(
    name = 'gmaven_rules',
    remote = 'https://github.com/aj-michael/gmaven_rules',
    commit = '5e89b7cdc94d002c13576fad3b28b0ae30296e55',
)

load('@gmaven_rules//:gmaven.bzl', 'gmaven_rules')
gmaven_rules()

# Set ANDROID_HOME environment variable to location of your Android SDK
# i.e. on macOS with Android Studio this would be "$HOME/Library/Android/sdk"
android_sdk_repository(
    name = "androidsdk",
    # specify params to have reproducible builds
    # by default bazel uses the latest available versions
    # api_level = <…>,
    # build_tools_version = <…>,
)

maven_jar(
  name = "com_squareup_okhttp_okhttp_2_7_2",
  artifact = "com.squareup.okhttp:okhttp:jar:2.7.2",
)

maven_jar(
  name = "com_squareup_okio_okio_1_6_0",
  artifact = "com.squareup.okio:okio:jar:1.6.0"
)
