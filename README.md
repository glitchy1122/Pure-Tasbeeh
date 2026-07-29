# Pure Tasbeeh

**Property of Adrees ul Hassan — Not to be shared without permission.**

Minimalist, privacy-focused, offline-first Islamic app (Java / XML).

## Features

- Claymorphism / Khak-e-Shifa UI (`#D0B49F` / `#C49A7C`)
- Fiqh preference: Jafriya, Hanfiya, or Both (SharedPreferences)
- Room DB: Duas/Munajat, Ahadith, Books (pre-seeded)
- Offline prayer times (adhan-java)
- Tasbeeh e Zehra (s.a) guided counter
- Custom Tasbeeh + Gemini zikr suggestions
- Islamic books via DownloadManager

## Setup

1. Open the project in Android Studio.
2. Put your Gemini key in `local.properties` (gitignored):

```properties
GEMINI_API_KEY=your_key_here
```

3. Sync Gradle and run the `app` configuration.

Secrets are injected via Google’s Secrets Gradle Plugin into `BuildConfig.GEMINI_API_KEY`.

## License

Proprietary. Copyright © 2026 Adrees ul Hassan. All rights reserved.
