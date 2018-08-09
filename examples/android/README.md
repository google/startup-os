# Android sample project

This sample project is intended to be used as a template for an Android app that uses Firestore.

To get it running:
* Configure Android SDK (see below).
* Create a new project [here](https://console.firebase.google.com/).
* Download `google-services.json` config file and put it in `src/main/assets/` folder.

## Configuring Android SDK
There are 2 options for configuring the Android SDK.
### Downloading an Android SDK
Set `ANDROID_HOME` to your desired SDK location, then run:
`./tools/get_android_sdk.sh`

### Working with an existing Android SDK
If you already have Android SDK on your machine (i.e. you've installed Android Studio), and you want to use it, then
set `ANDROID_HOME` to it (e.g `export ANDROID_HOME=/your/sdk/location`). Typical locations for the SDK are `$HOME/Library/Android/sdk/` for macOS, and `$HOME/Android/Sdk` for Linux.

You might need to set `api_level` and `build_tools_version` in the `android_sdk_repository` rule in `WORKSPACE`, to match those you have in the SDK folder. For more details on `android_sdk_repository`, see [Bazel's documentation](https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository).
