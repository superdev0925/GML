# GLM400CL Quad Laser Monitor

Android tablet APK source for connecting to four GLM400CL BLE laser distance devices and showing four live panels.

## Tested target
- Galaxy Tab A7 Lite SM-T227U
- Android Studio Panda / recent Android Studio
- Kotlin + Jetpack Compose

## BLE protocol currently used
- Measurement service: `02a6c0d0-0451-4000-b000-fb3210111989`
- Measurement characteristic: `02a6c0d1-0451-4000-b000-fb3210111989`
- CCCD: `00002902-0000-1000-8000-00805f9b34fb`
- Uses **indications**: descriptor value `0x0200`

## How to test
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Connect the Galaxy Tab by USB, or build APK.
4. Run the app.
5. Tap **Scan**.
6. Select slot 1/2/3/4.
7. Tap the GLM400CL device from the list.
8. Repeat for four devices.

## Important
The app currently displays raw BLE packets because the exact distance decoding needs known-distance samples.
Collect samples like:

```text
1.000 m = ?
2.000 m = ?
3.000 m = ?
4.000 m = ?
```

Then update `DistanceParser.kt`.
