#!/bin/bash

if [ ! -d "/home/circleci/android_sdk/" ]; then
	wget https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip
	unzip sdk-tools-linux-3859397.zip -d /home/circleci/android_sdk
	(yes | /home/circleci/android_sdk/tools/bin/sdkmanager --licenses) || true
	/home/circleci/android_sdk/tools/bin/sdkmanager "platforms;android-27" "build-tools;27.0.3"
fi

