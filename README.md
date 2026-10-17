# AutoSpanish

Android app that turns a word, phrase, or sentence (Spanish or English) into a fully-annotated
flashcard: correct article and gender for nouns, masculine/feminine forms for adjectives and
professions, verb conjugations with irregularity flags, false-friend warnings, and more. Organized
into reusable lists, each of which can be studied directly with flip cards or Quizlet-style Learn
mode: there's no separate "flashcard set" concept to manage.

See `REQUIREMENTS.md` for the full feature spec this was built against.

A pre-built debug APK is already included at the project root: `app-debug.apk`.

## Opening the project in Android Studio

1. Install Android Studio (Giraffe/Koala or newer) if you don't have it.
2. `File > Open`, select the `AutoSpanish` folder (this folder, the one with `settings.gradle.kts`).
3. Let Android Studio sync Gradle on first open. It uses its own bundled JDK 17 and Android SDK.
   Android Studio regenerates `local.properties` to point at its own SDK automatically.
4. If prompted to install a missing SDK platform/build-tools version, accept. The project targets
   compileSdk/targetSdk 34, minSdk 26.

## Running on the Android emulator (from Android Studio)

1. `Tools > Device Manager > Create Device`, pick any phone profile, and download a system image
   (API 26+; API 34 recommended) if you don't have one yet.
2. Select the emulator from the device dropdown in the toolbar and press the green Run ▶ button
   (or `Shift+F10`). Android Studio builds and installs the app on the emulator automatically.

## Installing the APK on a physical Android device

**Option A: copy and tap to install:**
1. Copy `app-debug.apk` onto your phone (USB transfer, email to yourself, cloud drive, etc.).
2. On the phone, open the file. Android will prompt to allow installs from that source
   (Settings > Apps > Special access > Install unknown apps: enable it for whichever app opened
   the file, e.g. Files or Chrome).
3. Confirm the install prompt.

**Option B: install via `adb` over USB:**
1. On the phone: Settings > About phone > tap "Build number" 7 times to enable Developer Options,
   then Settings > Developer Options > enable USB debugging.
2. Connect the phone via USB and accept the "Allow USB debugging" prompt on the phone.
3. From a terminal with `adb` on PATH (or via Android Studio's `Terminal` panel, or
   `.toolchain/sdk/platform-tools/adb.exe` in this project):
   ```
   adb install app-debug.apk
   ```

The app requests camera permission the first time you use the "scan text" (OCR) feature. Grant it
when prompted. Everything else works fully offline once the curated dictionary is bundled; go to
Settings inside the app to optionally download the offline ML Kit translation models for words
outside the curated dictionary, or leave that step for later and use the app online (it works either
way).

## Building a release APK (once you're ready to distribute)

1. In Android Studio: `Build > Generate Signed Bundle / APK`, choose APK.
2. Create a new keystore (or use an existing one) when prompted. Keep it and its passwords safe:
   you'll need the same keystore for every future update.
3. Choose the `release` build variant and finish. The signed APK lands in
   `app/release/app-release.apk`.

## The built-in dictionary

`app/src/main/assets/dictionary.json` holds ~78,900 real Spanish headwords, generated directly from
Kaikki.org's machine-readable extract of English Wiktionary
(https://kaikki.org/dictionary/Spanish/), dual-licensed under Creative Commons
Attribution-ShareAlike 4.0 and the GNU Free Documentation License. Every grammatical field is
extracted straight from that source, never hand-authored: English glosses and part of speech
(78,936 entries); noun gender (42,318); plural forms (41,786); gendered noun-pair forms such as
profesor/profesora or actor/actriz, generalized to every noun the source pairs, not just a curated
profession list (16,583 feminine, 238 masculine counterparts, 2,333 flagged as paired); adjective
invariability and comparative forms such as mejor/peor/mayor/menor (7,764); full verb conjugation
tables covering indicative, subjunctive, conditional, imperative, gerund and participle, including
regional vos-forms, for every verb the source documents a table for (7,856); real usage example
sentences with real English translations (11,203); and synonyms/antonyms (15,045 / 1,665) where
Wiktionary editors recorded them. Words the source didn't document one of these fields for fall back
to this app's own suffix/rule-based heuristics for that field only (see `GrammarEnricher.kt`) --
never the reverse. A rare handful of identically-spelled Spanish homonyms with different genders per
sense (e.g. "el radio" vs. "la radio") collapse to whichever sense Wiktionary documents more
thoroughly, since the app stores one entry per spelling.

The other files under `app/src/main/assets/*.json` (irregular verbs, false friends, ser/estar
adjectives, preposition pairings, regional variants, sentence frames) are curated grammar-rule
fallbacks, not vocabulary: they only kick in for the minority of words/fields the sourced dictionary
above doesn't cover. There is no separate gender-exceptions or adjective-exceptions file -- both are
sourced directly from the dictionary entry, with the suffix heuristic only as a last resort. They're
plain JSON and can be edited directly; rebuild afterward to pick up changes.

A word not found in the dictionary falls back to the on-device ML Kit translator, then (if online)
the MyMemory API, with the same grammar rules applied automatically. When the direction is set to
Auto and a single typed word can't be confidently placed in either language, the app asks directly
rather than guessing.
