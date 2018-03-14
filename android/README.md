This sample project is intended to be used as a template
for Android app which uses Firestore (or Firebase) and is
built via Bazel build system.

To get it functioning:
* create new project [here](https://console.firebase.google.com/) and 
download `google-services.json` config file and put it in `android` folder.
* configure `android_sdk_repository` in WORKSPACE as described
[here](https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository)
