# Tessera

A free, open-source TOTP and HOTP authenticator for Android. Privacy-absolute: zero data
collection, zero trackers, zero unnecessary permissions. Your secrets stay on your device,
encrypted with a key only you hold.

Tessera is a Roman token used as proof of identity or a pass. That is the product thesis:
a small token that proves who you are.

## Status

Early development. Phase 0 (scaffold) is in place: multi-module Gradle build, FOSS and Play
flavors, linting, coverage, and CI. The OTP and crypto cores land next.

## Principles

- On-device only. No accounts, no servers, no analytics, no ads.
- Single encrypted vault. Secrets live in memory only while the vault is unlocked.
- Passphrase is the root of trust (Argon2id). Optional biometric unlock is a convenience
  layer wrapped by the Android Keystore; the passphrase always works as a fallback.
- You own your data. Encrypted backup to storage you choose, plus a gated plaintext export
  so you can leave at any time.

## F-Droid build

Please note that Tessera ships in two flavors:

- `foss`: no Google Play Services. This is the F-Droid build.
- `play`: includes `play-services-wearable` and the Wear OS companion.

Full Wear OS support needs the Play Services Wearable Data Layer, which is not F-Droid clean.
Therefore the foss build app comes without the Wear companion. 

If you want the wear companion (to use in wearable devices like a smart watch), please use the 
play build. Please note that the watch is a tethered display and holds no secrets at rest. It 
shows codes computed by the phone over the Data Layer. When the phone is unreachable, the 
watch shows an "open phone" state.

## Dev Requirements (for building this repo on your machine)

- JDK 17 (LTS). JDK 17 through 23 work; newer JDKs are not yet supported by the build.
- Android SDK with platform 35 and build-tools 35.x.

## Getting started

Windows:

```
powershell -ExecutionPolicy Bypass -File .\bootstrap.ps1
```

macOS or Linux:

```
make bootstrap
```

Bootstrap verifies the JDK, installs the pre-commit hooks, seeds `keystore.properties`,
generates a debug keystore if needed, and proves a clean `assembleFossDebug` build.

## Common tasks

```
./gradlew assembleFossDebug assemblePlayDebug   # build both flavors
./gradlew ktlintCheck detekt                    # lint
./gradlew testFossDebugUnitTest koverXmlReport  # unit tests + coverage
```

## License

MIT. See [LICENSE](LICENSE).
