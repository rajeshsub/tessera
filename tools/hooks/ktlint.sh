#!/usr/bin/env sh
# Run ktlint through the Gradle wrapper so the engine version matches the build.
set -e
if [ -x "./gradlew" ]; then
  exec ./gradlew --quiet ktlintCheck
else
  exec ./gradlew.bat --quiet ktlintCheck
fi
