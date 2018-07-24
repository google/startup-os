This sample project is intended to be used as a template for Android app which uses Firestore and is
built via Bazel build system.

To get it running:
* Create a new project [here](https://console.firebase.google.com/)
* Download `google-services.json` config file and put it in `src/main/assets/` folder.
* Configure `android_sdk_repository` in WORKSPACE as described there (set `ANDROID_HOME` env variable)
