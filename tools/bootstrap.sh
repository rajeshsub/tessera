#!/usr/bin/env bash
# One-step bootstrap for Tessera (POSIX / CI). Windows users: use bootstrap.ps1.
set -euo pipefail

echo ">> Tessera bootstrap"

# 1. Verify a usable JDK (17 through 23; Gradle 8.11 does not support newer yet).
if ! command -v java >/dev/null 2>&1; then
  echo "!! No 'java' on PATH. Install a JDK 17 (LTS) and retry." >&2
  exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | head -n1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ "$JAVA_MAJOR" -lt 17 ] || [ "$JAVA_MAJOR" -gt 23 ]; then
  echo "!! JDK $JAVA_MAJOR found. Need 17 through 23. Point JAVA_HOME at a JDK 17." >&2
  exit 1
fi
echo ".. JDK $JAVA_MAJOR ok"

# 2. Install pre-commit hooks if pre-commit is available.
if command -v pre-commit >/dev/null 2>&1; then
  pre-commit install
  echo ".. pre-commit hooks installed"
else
  echo "!! pre-commit not found. Install it (pip install pre-commit) to enable local hooks." >&2
fi

# 3. Seed keystore.properties from the example if absent.
if [ ! -f keystore.properties ]; then
  cp keystore.properties.example keystore.properties
  echo ".. created keystore.properties from example (edit before release signing)"
fi

# 4. Generate a debug keystore if absent (AGP also auto-creates one on first debug build).
DEBUG_KS="${HOME}/.android/debug.keystore"
if [ ! -f "$DEBUG_KS" ] && command -v keytool >/dev/null 2>&1; then
  mkdir -p "${HOME}/.android"
  keytool -genkeypair -v -keystore "$DEBUG_KS" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1 || true
  echo ".. generated debug keystore"
fi

# 5. Prove a clean FOSS debug build.
echo ">> assembleFossDebug"
./gradlew assembleFossDebug
echo ">> bootstrap complete"
