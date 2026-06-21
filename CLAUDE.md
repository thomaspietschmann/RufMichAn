# RufMichAn — Codebase Guide

## What the app does

Simulates incoming phone calls from user-defined contacts at a scheduled time. Core flow:
1. User creates a contact (name, phone number, optional photo)
2. User schedules a fake call (countdown or clock time)
3. At the scheduled time the phone rings with a full-screen call UI
4. User can answer (in-call screen with timer + proximity blanking) or decline

## Build

```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # release APK
```

- **minSdk 26** (Android 8), **compileSdk/targetSdk 35**
- Kotlin 2.1, AGP 8.7.3, Gradle 8.11.1 — pinned for F-Droid compatibility
- Jetpack Compose + Material 3 (no XML layouts)
- Room 2.6 via KSP, Coil 2.7, Navigation Compose 2.8

## Package layout

```
de.pietschie.rufmichan/
  RufMichAnApp.kt          Application — creates AppContainer, registers notification channel
  AppContainer.kt          Manual DI — holds DB, repositories, CallScheduler singletons
  MainActivity.kt          Compose host — provides LocalAppContainer, handles runtime permissions
  data/
    AppDatabase.kt         Room DB (version 1, fallbackToDestructiveMigration)
    Converters.kt          CallState ↔ String type converter
    contact/               ContactEntity, ContactDao, ContactRepository, PhotoStorage
    call/                  ScheduledCallEntity, CallState, CallWithContact, ScheduledCallDao, CallRepository
    media/                 PhotoStorage — copies Photo Picker URIs to filesDir/contact_photos/
  alarm/
    CallScheduler.kt       AlarmManager.setAlarmClock() — row id = PendingIntent request code
    AlarmReceiver.kt       BroadcastReceiver — fires alarm → starts CallService
    BootReceiver.kt        BroadcastReceiver — BOOT_COMPLETED → re-arms future alarms
    ExactAlarmPermission.kt  Helpers for SCHEDULE_EXACT_ALARM (API 31/32) and USE_FULL_SCREEN_INTENT (API 34+)
  call/
    CallService.kt         Foreground service (specialUse) — owns notification, ringtone, vibration
    CallActivity.kt        Full-screen Activity — shown over lock screen; hosts IncomingCallScreen / InCallScreen
    Ringer.kt              MediaPlayer on STREAM_RING + VibrationEffect; respects ringer mode and DND
    ProximityController.kt PROXIMITY_SCREEN_OFF_WAKE_LOCK for in-call ear-blanking
    CallNotifications.kt   Notification channel + FSI notification builder
    ui/
      IncomingCallScreen.kt  Compose — dark full-screen incoming call UI
      InCallScreen.kt        Compose — in-call timer + hang-up
      ContactAvatar.kt       Reusable circular avatar (Coil or person icon fallback)
  ui/
    theme/                 Color, Theme (static green scheme, no dynamic color), Type
    navigation/Nav.kt      NavHost + BottomNavigationBar (Contacts / Scheduled)
    contacts/              ContactListScreen+VM, ContactEditScreen+VM
    schedule/              ScheduleCallScreen+VM, ScheduledListScreen+VM
```

## DI pattern

No Hilt. `AppContainer` is created in `RufMichAnApp.onCreate()` and exposed as `app.container` for non-Compose code (Receivers, Services). Compose screens access it via `LocalAppContainer.current`.

## Alarm reliability

`AlarmManager.setAlarmClock()` is used (not `setExactAndAllowWhileIdle`) because it fires in deep Doze and is rate-limit exempt. The row `id` from Room is used as the `PendingIntent` request code so individual alarms can be cancelled. `BootReceiver` re-arms all `SCHEDULED` rows with a future `triggerAtEpochMillis` after reboot.

## Call trigger chain

```
AlarmManager fires
  → AlarmReceiver.onReceive()
    → ContextCompat.startForegroundService(CallService)
      → CallService.onStartCommand()
        → startForeground(notification with setFullScreenIntent → CallActivity)
        → Ringer.start()
          → (screen off/locked) CallActivity launches full-screen
          → (screen on)         heads-up banner appears; tap → CallActivity
```

## Photos

System Photo Picker (`PickVisualMedia`) — no storage permission needed. On pick, bytes are immediately copied/compressed to `filesDir/contact_photos/<uuid>.jpg`. The absolute path is stored in Room. Coil loads `File(path)` directly. File is deleted when the contact is deleted.

## Key decisions & constraints

- **F-Droid target** — no proprietary libraries. No Google Play Services. `dynamicColor = false` in Theme to avoid relying on Monet/dynamic-color APIs that behave differently across ROMs.
- **FGS type `specialUse`** — `phoneCall` type would imply real Telecom integration; `specialUse` is honest and avoids Play Store scrutiny (irrelevant for F-Droid but clean).
- **DND not bypassed** — `Ringer` checks both `AudioManager.getRingerMode()` and `NotificationManager.getCurrentInterruptionFilter()`. Silent/DND = no sound.
- **Past clock time → next day** — resolved in `ScheduleCallViewModel.resolveTargetTime()`.
- **Force-stop kills alarms** — OS limitation, cannot be worked around. Documented in README.

## Permissions summary (Manifest)

| Permission | SDK gate | Notes |
|---|---|---|
| `USE_EXACT_ALARM` | 33+ | Auto-granted; no user prompt |
| `SCHEDULE_EXACT_ALARM` | 31–32 (maxSdk=32) | User-revocable; prompt handled in MainActivity |
| `POST_NOTIFICATIONS` | 33+ runtime | Requested on first launch |
| `USE_FULL_SCREEN_INTENT` | 34+ | Auto-granted on emulator/dev; may need settings on real devices |
| `RECEIVE_BOOT_COMPLETED` | all | Static |
| `VIBRATE` / `WAKE_LOCK` | all | Static |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | 28+ / 34+ | Static |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 23+ | Optional; one-time dialog in MainActivity |
