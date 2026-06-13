#!/usr/bin/env sh
# Run detekt through the Gradle wrapper.
set -e
if [ -x "./gradlew" ]; then
  exec ./gradlew --quiet detekt
else
  exec ./gradlew.bat --quiet detekt
fi
