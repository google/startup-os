#!/bin/bash

RED=$(tput setaf 1)
RESET=$(tput sgr0)

if [[ -z "$ANDROID_HOME" ]]; then
  export ANDROID_HOME=$HOME/android_sdk/
fi

DOWNLOAD_COMMAND=wget
if [[ ! -x "$(command -v ${DOWNLOAD_COMMAND})" ]]; then
  DOWNLOAD_COMMAND="curl -LO"
fi

if [[ ! -x "$(command -v ${DOWNLOAD_COMMAND})" ]]; then
  echo "$(tput setaf 1)No wget or curl found$(tput sgr0)"
  exit 1
fi

URL_BASE="https://dl.google.com/android/repository/"

platform=$(uname)
if [[ "$platform" == "Darwin" ]]; then
  FILENAME="sdk-tools-darwin-4333796.zip"
  PROFILE_FILE="$HOME/.bash_profile"
elif [[ "$platform" == "Linux" ]]; then
  FILENAME="sdk-tools-linux-4333796.zip"
  PROFILE_FILE="$HOME/.bashrc"
else
  echo "Android SDK does not have an archive for $platform"
  exit 1
fi

if [[ ! -d "$ANDROID_HOME" ]]; then
  ${DOWNLOAD_COMMAND} ${URL_BASE}/${FILENAME}
  unzip ${FILENAME} -d ${ANDROID_HOME}
  echo y | ${ANDROID_HOME}/tools/bin/sdkmanager "platforms;android-27"
  echo y | ${ANDROID_HOME}/tools/bin/sdkmanager "platform-tools"
  echo y | ${ANDROID_HOME}/tools/bin/sdkmanager "build-tools;27.0.3"
  echo y | ${ANDROID_HOME}/tools/bin/sdkmanager "extras;android;m2repository"
  echo y | ${ANDROID_HOME}/tools/bin/sdkmanager "extras;google;m2repository"
  echo "Removing $(rm -v ${FILENAME})"
fi

echo "Android SDK at $RED$ANDROID_HOME$RESET"
echo "Run$RED 'export ANDROID_HOME=$ANDROID_HOME'$RESET before building"
echo "Run$RED 'echo export ANDROID_HOME=$ANDROID_HOME >> $PROFILE_FILE'$RESET to persist the setting"
