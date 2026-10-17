# AutoSpanish: Requirements

Android app for auto-generating Spanish/English vocab, with genders/articles/forms, into flashcards and study lists.

Status: APPROVED. Implementation in progress.

## Core input/translation
- Enter a single word or list of words.
- Auto-detect direction (ES to EN or EN to ES), or translate both ways.
- Nouns: return with correct article and gender (el lápiz, la mesa).
- Adjectives: return masculine and feminine forms (rojo/roja).
- Professions/people nouns: return el/la + masc/fem form (el/la profesor/a).
- Accents applied correctly.
- Words with multiple meanings: return all meanings.
- Full phrase/sentence translation supported, not just single words.
- Offline translation via a bundled dictionary of roughly 79,000 real Spanish headwords (sourced from Kaikki.org's Wiktionary extract, dual-licensed CC BY-SA 4.0 and GFDL, filtered down to single-word entries with clean glosses and a part of speech). Falls back to the on-device ML Kit translator, then the online API, only for words outside that dictionary.
- Online translation capability too (better quality when available).
- When direction is set to Auto and a single word cannot be confidently resolved as Spanish or English (not in the dictionary either way, no Spanish-specific accents or verb/noun endings), ask the user directly ("Is this a Spanish word or an English word?") instead of guessing.
- "Show example sentence" button per word/flashcard, covering different meanings.
- Part of speech shown as a small flag/badge on the card (e.g. "adjective/adjetivo"). Informational only, never quizzed.
- Plural forms available via an on-demand button ("show plural"), not shown by default.
- Synonyms available via on-demand button/menu, not shown by default.
- False-friend warnings shown automatically when relevant (e.g. embarazada does not mean embarrassed). High-value, always visible.
- Regional variants: default to one common form (Mexican Spanish) rather than listing every dialect; a variant can optionally be surfaced via on-demand button if worth flagging.
- Verb conjugation tables available via on-demand button ("show conjugations"), covering major tenses.
- Irregular verb flag: automatically shown (not hidden behind a button) whenever a verb is irregular, labeled with the type of irregularity (e.g. e to ie stem change, g-insertion, orthographic j-change) and which tense(s) it affects.
- Reflexive verb flag (levantarse) shown automatically alongside the irregularity flag, when applicable.
- Ser vs. estar hint shown automatically for adjectives whose meaning changes with the verb (e.g. aburrido = boring vs. bored).
- Preposition pairing note (e.g. soñar con, pensar en) available via on-demand button, not shown by default.
- General display principle: keep the default card view minimal (word, translation, article/gender, irregularity/reflexive/ser-vs-estar flags if applicable). Everything else (plural, conjugations, synonyms, part of speech detail, regional variant, preposition pairing) sits behind a per-word menu/button so the UI never feels overloaded.
- No audio/pronunciation/TTS features anywhere in the app.

## Capture/input
- OCR: photograph text, select which specific word(s) to add. Never bulk-add everything detected. Reachable both from a list's toolbar and directly from the Add Word screen.
- Paste a paragraph and pick out unknown words to add individually.
- Duplicate detection when adding a word already present (in a list/set or globally).

## Study/learn mode (additions)
- Mastery tracking per word (% correct over time); words missed repeatedly (leeches) surface automatically within this same mastery system rather than as a separate feature.
- Smart/dynamic sets, e.g. auto-set of weakest/least-mastered words for review.

## Lists
- A single kind of list: no separate "flashcard set" concept. Studying a list (flip-card or Learn mode) is just an action available on any list, not a different object you build from one.
- Add individual words to a specific list, from the word's own menu, the Add Word screen, or a list's own "Add to this list" searchable picker (which can pull in existing words and/or merge in whole other lists).
- One permanent, undeletable global list containing everything ever added.
- Rename, reorder, delete lists.
- Global search across all lists.
- Sort a list's flashcards by most recent, oldest, or random.

## Study/learn mode
- Quizlet-style "Learn" mode: multiple choice first, then typed answer (EN/ES).
- Standard flip-card study mode.
- Both modes expose the same on-demand grammar detail menu (plural, conjugations, synonyms, regional variant, preposition pairing, example sentence) available while a card is on screen, not just in list view.

## Export/import
- Export a list to PDF or DOCX.
- Export in a format importable into Quizlet.
- Export to CSV.
- Backup/restore to a readable, usable file format (e.g. JSON), for data safety and device migration.
- Merge/import a backup file into existing data without wiping current data.

## UI/UX
- Visual style inspired by Quizlet: clean, card-based.
- Dark mode.
- Undo on deletion (words, lists).
- Default card view stays minimal; extra linguistic info (plural, conjugations, synonyms, POS detail, regional variant) accessed via a per-word button/menu, per the display principle above.

## Platform
- Android app.
- Deliverable as an installable APK.
- Instructions to sideload/test on a physical Android device.
- Instructions to build/run/emulate via Android Studio.
