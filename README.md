# My Planner

A personal productivity app for Android, built from scratch with Kotlin and Jetpack Compose. My Planner combines a day-by-day task timeline with a backlog for unscheduled tasks, repeatable multi-step "scenarios", and a lightweight personal finance tracker.

> **Note:** the app's UI is in Russian (its primary audience). This README and the codebase documentation are in English.

## Features

- **Day timeline** — plan your day hour by hour, with tasks that can be dragged and reordered, marked complete, or swapped with one another.
- **Backlog** — tasks without a fixed time live in a separate list until you decide when to schedule ("plant") them into a day.
- **Recurring tasks** — repeat by interval, day of week, month, or year, with support for pausing or permanently stopping a series.
- **Scenarios** — save a sequence of steps with relative time offsets (e.g. a laundry routine) and run the whole sequence into your day with one tap. The app also suggests a matching scenario while you type a task name.
- **Reminders** — per-task notifications with configurable lead time, including exact alarms and reboot-safe rescheduling.
- **Sleep schedule** — optionally account for a sleep window when calculating free time in the day.
- **Finance tracker** — accounts, income/expense transactions with categories, and savings goals ("piggy banks") with progress tracking.
- **Onboarding** — a guided first-run walkthrough of the app's main features.
- **Custom categories** — user-defined task and expense categories.

## Tech stack

- **Kotlin**
- **Jetpack Compose** (Material 3) for the UI
- **Navigation Compose** for in-app navigation
- **Room** for local persistence
- **DataStore Preferences** for settings
- **Gson** for serialization
- **AlarmManager** + `BroadcastReceiver` for scheduled reminders

Architecture follows an MVVM pattern, with per-screen ViewModels and a shared `ViewModelFactory`.

## Requirements

- Android Studio (Koala or newer recommended)
- JDK 17
- Android SDK with `compileSdk = 34`
- minSdk = 26 (Android 8.0+)

## Getting started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/my-planner.git
   ```
2. Open the project in Android Studio and let it sync Gradle.
3. Run the `app` configuration on an emulator or a device running Android 8.0+.

Alternatively, build from the command line:

```bash
./gradlew assembleDebug
```

The debug APK will be produced under `app/build/outputs/apk/debug/`.

## Project structure

```
app/src/main/java/com/uliana/myplanner/
├── data/            # Room entities, DAOs, repositories
├── domain/          # Business logic (scheduling, recurrence rules, etc.)
├── notifications/   # Reminder scheduling and BroadcastReceivers
└── ui/
    ├── planner/     # Day timeline screen
    ├── backlog/     # Unscheduled task list
    ├── taskedit/    # Task creation/editing
    ├── scenario/    # Scenario list, editor, and run dialog
    ├── settings/    # App settings, sleep schedule, categories
    ├── categories/  # Category management
    ├── onboarding/  # First-run walkthrough
    ├── components/  # Shared composables
    ├── navigation/  # Navigation graph
    └── theme/       # Colors, typography, theming
```

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
