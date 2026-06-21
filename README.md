# RufMichAn

An Android app that simulates incoming phone calls from user-defined contacts — useful for escaping awkward situations, pranks, or testing.

## Features

- **Own contact book** — add contacts with name, phone number and photo (no access to the system address book)
- **Schedule a fake call** — either as a countdown (e.g. in 5 minutes) or at a specific clock time
- **Multiple calls** can be scheduled in parallel and cancelled individually
- **Realistic incoming-call screen** — full-screen overlay with contact photo, name, ringtone and vibration, shown over the lock screen
- **In-call screen** — running call duration timer, proximity sensor blanks the screen when held to the ear
- **Respects Do Not Disturb** — silent/vibrate mode and DND are honoured, never overridden
- **Reliable scheduling** — uses `AlarmManager.setAlarmClock()` which fires even in deep Doze mode; alarms survive a device reboot

## Requirements

- Android 8.0 (API 26) or higher
- No Google Play Services required — fully compatible with [F-Droid](https://f-droid.org)

## Permissions

| Permission | Why |
|---|---|
| `USE_EXACT_ALARM` / `SCHEDULE_EXACT_ALARM` | Fire the fake call at exactly the right time |
| `POST_NOTIFICATIONS` | Show the incoming-call notification / full-screen overlay |
| `USE_FULL_SCREEN_INTENT` | Display the call screen over the lock screen |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after a reboot |
| `VIBRATE` / `WAKE_LOCK` | Ringtone vibration and proximity-sensor screen blanking |
| `FOREGROUND_SERVICE` | Keep the call presentation alive while ringing |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional — prevents OEM battery killers from deferring alarms |

## Known limitations

- **Force-stop cancels all alarms** — if the user force-stops the app via Android settings, all pending alarms are removed by the OS. This cannot be worked around.
- **OEM battery killers** — on Xiaomi, Huawei, Samsung and similar devices the app should be added to the "protected apps" or "auto-start" list for best reliability.
- **Heads-up notification** — when the screen is already on and unlocked, Android shows a heads-up banner first rather than launching the full-screen call UI directly. Tapping the banner opens the full-screen view.

## Building

```bash
./gradlew assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Room (local database), Coil (image loading)
- `AlarmManager.setAlarmClock()` + Foreground Service + Full-Screen Intent
- No proprietary dependencies

## License

MIT
