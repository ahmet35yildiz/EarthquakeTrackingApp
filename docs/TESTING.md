# QuakeAlert — Test Results

| Check | Result |
|---|---|
| `assembleDebug`, `lintDebug` | pass; lint: 0 errors, 14 warnings — 12 libraries have newer versions available and 1 older target SDK, 1 plural suggestion for the metrics detail text |
| `testDebugUnitTest` | 552 tests, 0 failures, 0 skipped |
| `connectedDebugAndroidTest` on `Pixel_6` (API 31) | 135 tests, 0 failures |
| `connectedDebugAndroidTest` on `Pixel_7_API_34` (API 34) | 135 tests, 0 failures |
| Clean clone (`git clone`, no `local.properties`, only `ANDROID_HOME` set) | `assembleDebug testDebugUnitTest lintDebug` pass (552 unit tests run again, 0 failures); `installDebug` installs on both emulators and the app starts on the first onboarding step |
