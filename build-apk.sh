#!/usr/bin/env bash
# Rebuilds the debug APK using the project-local toolchain and copies it to the repo root.
set -e
cd "$(dirname "$0")"

export JAVA_HOME="$(pwd)/.toolchain/jdk-17.0.20.1+1"
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew assembleDebug --console=plain

cp app/build/outputs/apk/debug/app-debug.apk ./AutoSpanish-debug.apk
echo "APK ready: $(pwd)/AutoSpanish-debug.apk"
