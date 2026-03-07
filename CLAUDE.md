# AIStories - Kindergeschichten- und Liedergenerator

## Was ist das?
LibGDX Desktop/Android App. Generiert Kindergeschichten per LLM (OpenAI GPT-5-mini, Google Gemini) und Kinderlieder per Suno API. Geschichten werden per ElevenLabs TTS in Audio umgewandelt und koennen auf Tonie.box hochgeladen werden. Bietet KI-Bildgenerierung fuer Custom-Charaktere/Objekte.

## Prinzipien
- **KISS** — Einfach halten. Keine Over-Engineering, keine unnötigen Abstraktionen.
- **DRY** — Code wiederverwenden. Keine Duplikate. Gemeinsame Logik in Helper-Methoden extrahieren.
- **Kurze Klassen** — Klassen klein und fokussiert halten. Eine Klasse = eine Verantwortung.
- **Bestehenden Code nutzen** — Vor dem Schreiben prüfen, ob es schon etwas Passendes gibt.
- **Keine unnötigen Kommentare/Docs** — Code soll selbsterklärend sein.

## Architektur

### Screen-System
`MainPanel` (extends `GameScreen`) ist der zentrale Controller. Hält alle `ScreenModel`-Instanzen und wechselt zwischen ihnen via `changeModel()`. `songMode`-Flag steuert ob Song- oder Story-Modus aktiv ist. Screens:
- `Menu` — Hauptmenü (3 Buttons: neue Geschichte, erstellte Geschichten, neues Lied). Lied-Button nur sichtbar wenn Suno-API-Key vorhanden.
- `CreateStory` — Geschichte/Lied konfigurieren (5 Spalten: MainCharacter, SupportingCharacter, Universe, Places, Objectives). Hat Settings-Button (Zahnrad), LLM-Switch und Toggle-Buttons zum Aktivieren/Deaktivieren einzelner Spalten. Im Song-Modus wird `SunoApiIO` statt `ChatGPTIO` aufgerufen.
- `ListenStories` — Geschichte als Buch anzeigen/abspielen bzw. Song-Lyrics anzeigen mit Audio-Wiedergabe und Versions-Wechsel
- `CreativeTonie` — Tonie-Upload (Kapitel-Übersicht, Upload-Funktion)
- `CustomEntityEditor` — Editor für Custom-Objekte mit 3 Modi: GRID (Bildauswahl), GENERATE (KI-Bildgenerierung), MANAGE (Custom-Bilder verwalten/löschen)
- `StorySettingsScreen` — Einstellungen für Story-Typ/Altersgruppe/Länge/Prompt-Template. Im Song-Modus zeigt es SongSettings (Musikstil/Altersgruppe/Länge).

### EnumInterface-Pattern
Alle Story-Elemente implementieren `EnumInterface` (getName, getDisplayName, getDisplayDetails, getImage, getEnumByName, ...). Konkrete Enums: `MainCharacter`, `SupportingCharacter` (je 60 Werte: 30 eigene + 30 aus der anderen Kategorie), `Universe`, `Places`, `Objectives` (je 30 Werte). `CustomEntity` implementiert dasselbe Interface für benutzerdefinierte Einträge mit CustomImageManager-Support.

### ObjectSelection mit Toggle
`ObjectSelection` zeigt eine Spalte mit UP/DOWN-Navigation, optionalem Custom-Button (Stift-Icon) und Toggle-Button (Haken-Icon, `ApoButtonCheckIcon`). Der Toggle deaktiviert die Spalte: UI wird ausgegraut (alpha 0.2), Buttons versteckt, und `GameObjectives` bekommt `null` für deaktivierte Spalten. **Wichtig:** Alle Stellen die GameObjectives-Felder lesen muessen null-safe sein (Prompt, SongPrompt, ListenStories, Dateinamen-Generierung).

### Settings-System
**StorySettings** speichert via LibGDX Preferences ("AIStoriesSettings"):
- **StoryType**: BEDTIME, FRIENDSHIP, DETECTIVE, ADVENTURE, FAIRYTALE
- **AgeGroup**: AGE_0_1, AGE_2_4, AGE_5_7, AGE_8_12, AGE_12_16, AGE_16_PLUS
- **StoryLength**: SHORT (2000 Zeichen, 2-3 Kapitel), MEDIUM (3500, 3-4), LONG (5000, 5)
- **PromptTemplate**: Editierbarer Prompt-Text

**SongSettings** speichert via LibGDX Preferences ("AIStoriesSongSettings"):
- **MusicStyle**: POP, ROCK, COUNTRY, HIPHOP, LULLABY, FOLK, ELECTRONIC, MUSICAL
- **AgeGroup**: Gleiche AgeGroup wie StorySettings
- **SongLength**: SHORT (~2 min), MEDIUM (~3 min), LONG (~4 min)
- **PromptTemplate**: Editierbarer Prompt-Text

`StorySettingsScreen` zeigt je nach `songMode` die passenden Settings.

### Song-Erstellung (Suno API)
`SunoApiIO` sendet Prompts an die Suno API (`api.sunoapi.org`), pollt den Status alle 20 Sekunden und laedt bei Erfolg die MP3s herunter. Suno generiert immer 2 Varianten pro Song.

**Zwei-Prompt-System** in `SongPrompt`:
- `buildFullPrompt()` — ausfuehrlicher Prompt fuer Anzeige (kein Zeichenlimit)
- `buildSunoPrompt()` — kompakter Prompt fuer Suno API (max 490 Zeichen, API-Limit ist 500). Verwendet absteigendes Kuerzungsverfahren: 1) alles komplett, 2) Details pro Element kuerzen, 3) Details weglassen, 4) Notfall-Truncate.

**Altersgerechte Prompts**: Jede AgeGroup hat eigene Tonanweisungen (Themen, Sprachkomplexitaet, emotionaler Ton) und Strukturvorgaben (Strophenanzahl, Refrainform). Musikstile haben ausfuehrliche Beschreibungen. AABB-Reimschema wird immer gefordert.

**Ablauf**: Menu → CreateStory (songMode=true) → Spalten konfigurieren/togglen → "Generiere Song" → ListenStories zeigt Lyrics + Audio. `SunoApiIO` zeigt Polling-Fortschritt mit vergangener Zeit "(20s)", "(40s)" etc. im Loading-Overlay.

**Dateiformat Songs**: `{datum}_{titel}_song.txt` und `{datum}_{titel}_song_v1.mp3` / `_v2.mp3`. TXT-Header: `song;main;support;universe;places;objectives;!;!{lyrics}`. Beim Loeschen werden TXT + beide MP3s entfernt.

### Song-Varianten in ListenStories
Songs haben einen Versions-Button (`FUNCTION_SONG_VARIANT`) der zwischen v1 und v2 MP3 wechselt. Button nur sichtbar wenn ein Song geladen ist (nicht bei Geschichten mit MP3).

### KI-Bildgenerierung
`ImageGenerationIO` generiert Bilder über zwei APIs:
- **OpenAI**: Model `gpt-image-1-mini`, 1024x1024, low quality, Base64-Rückgabe
- **Gemini**: Model `gemini-3.1-flash-image-preview`, responseModalities: image+text, inlineData Base64

`ImageStyle` enum definiert 12 Stile mit Prompt-Fragment und Farbe:
CLAY, WATERCOLOR, CRAYON, DISNEY, ANIME, COMIC, PIXEL, REALISTIC, PAPERCUT, PIXAR, MINECRAFT, LEGO

`CustomImageManager` speichert generierte Bilder als 200x200 PNG in `custom_images/<entityType>/`. Lädt beim Start alle existierenden Bilder. Characters teilen sich einen gemeinsamen Manager ("characters").

`CustomEntity` hat jetzt Built-in-Bilder (aus Sprite-Sheets) + Custom-Bilder (aus CustomImageManager). `getTextureByIndex()` mappt den Index auf das richtige Bild.

### Buch-Rendering (ListenStories)
`BookRenderer` rendert ein aufgeschlagenes Buch:
- Brauner Ledereinband, Pergament-Seiten, Buchrücken mit Schatten
- Linke/rechte Seite mit Text, Seitenzahlen, Kapitelüberschriften
- Clipping für Text innerhalb der Seitengrenzen

`PageTurnAnimation` — 800ms Seitenumblätter-Animation (vorwärts/rückwärts) mit Smoothstep-Easing. Knicklinie wandert über beide Buchseiten.

`WordHighlighter` — Markiert das aktuelle Wort während der Audio-Wiedergabe mit einem Unterstrich. Mappt TTS-Wort-Timings auf Display-Zeilen.

`WordTimingData` — Speichert Wort-Level-Timing aus ElevenLabs-Alignment. Binary Search für aktuelles Wort. Persistenz als JSON.

`FontSize` — 5 Schriftgrößen (FONT_15 bis FONT_40) mit Zeilenhöhe. Plus/Minus Buttons zum Umschalten.

### Input-System (Achtung!)
`GameScreen.keyDown` → `keyPressedArray`, `keyTyped(char)` → `keyReleasedArray`, `keyUp(keycode)` → `keyReleasedArray`. keyTyped und keyUp landen im SELBEN Array! Workaround: `keys[]`-Array in Screens, um keyUp von keyTyped zu unterscheiden. In Settings-Screens nur `keyTyped` fuer Texteingabe nutzen, `keyUp` nur fuer Navigation.

### LLM-Integration
`ChatGPTIO` handhabt zwei APIs:
- **OpenAI** (gpt-5-mini): Bearer-Token Auth, `messages`/`choices` Format, **`max_completion_tokens`** (NICHT max_tokens!)
- **Gemini** (gemini-3-flash-preview): API-Key in URL, `contents`/`candidates` Format, `systemInstruction` separat

Default-LLM: Gemini wenn Key vorhanden, sonst OpenAI. Umschaltbar per ApoButtonSwitch in CreateStory und CustomEntityEditor.

API-Keys in `assets/config.properties`, geladen in `AIStories.java`.

### Hilfsklassen
- `GameObjectives` — Hält die 5 ausgewählten Story-Elemente (alle nullable wenn Spalte deaktiviert). `shuffleWithCustoms()` bezieht Custom-Entities ein. `refresh()` ist null-safe.
- `Running` — Enum für Async-Zustände: NONE, CREATE_STORY, CREATE_AUDIO, CREATE_SONG
- `MainInterface` — Callback-Interface (setRunning, setStatusText, setTextForTextArea, onImageReceived, getPrompt)
- `LanguageEnum` — 6 Sprachen (DE, EN, ES, FR, IT, TR) mit Locale und Localization-Key
- `FontSize` — 5 Schriftgrößen mit Font-Referenz und Zeilenhöhe
- `ObjectSelectionInterface` — Interface für ObjectSelection-Rendering

### UI-Komponenten (entity/)
- `ApoButton` — Basis-Button-Klasse
- `ApoButtonMovement` — Button mit Pfeil-Icon (UP/DOWN)
- `ApoButtonEditIcon` — Runder Button mit Stift-Icon (Custom-Entity bearbeiten)
- `ApoButtonCheckIcon` — Runder Button mit Haken-Icon (Spalte aktivieren/deaktivieren). Gleicher Stil wie EditIcon (Spalten-Hintergrundfarbe + schwarzer Haken).
- `ApoButtonSwitch` — Toggle-Button mit zwei Labels (z.B. GPT-5-mini / Gemini-3)
- `Textfield` — Einzeiliges Textfeld mit Ctrl+A/C/X/V Clipboard-Support
- `TextArea` — Mehrzeiliges Textfeld

## Projektstruktur
```
core/src/com/apogames/
├── aistories/
│   ├── AIStories.java              — Entry Point, lädt API-Keys (inkl. Suno)
│   ├── Constants.java              — Farben, Größen, Flags
│   └── game/
│       ├── MainPanel.java          — Zentraler Controller, songMode, Custom-Entities, Screen-Wechsel
│       ├── ButtonProvider.java     — Alle Button-Definitionen (inkl. Settings, AI-Generate, Manage, Song-Variant)
│       ├── createStory/
│       │   ├── CreateStory.java    — Konfigurations-Screen (5 Spalten + Toggle + Settings)
│       │   ├── ObjectSelection.java — Spalte mit UP/DOWN/Custom/Toggle-Buttons
│       │   ├── ObjectSelectionInterface.java — Rendering-Interface
│       │   └── CreateStoryPreferences.java
│       ├── customEntity/
│       │   ├── CustomEntityEditor.java — Editor (3 Modi: GRID/GENERATE/MANAGE)
│       │   ├── CustomEntityEditorPreferences.java
│       │   ├── ImageGenerationIO.java  — KI-Bildgenerierung (OpenAI + Gemini)
│       │   ├── ImageStyle.java         — 12 Bildstile (Clay, Watercolor, etc.)
│       │   └── CustomImageManager.java — Speichert/lädt Custom-Bilder als PNG
│       ├── settings/
│       │   ├── StorySettings.java      — Datenmodell (StoryType, AgeGroup, StoryLength, PromptTemplate)
│       │   ├── SongSettings.java       — Datenmodell (MusicStyle, AgeGroup, SongLength, PromptTemplate)
│       │   ├── StorySettingsScreen.java — UI für Story-Einstellungen
│       │   └── SongSettingsScreen.java  — UI für Song-Einstellungen
│       ├── listenStories/
│       │   ├── ListenStories.java      — Story/Song anzeigen/abspielen, Polling-Statusanzeige
│       │   ├── BookRenderer.java       — Buch-Rendering (Seiten, Einband, Schatten)
│       │   ├── PageTurnAnimation.java  — Seitenumblätter-Animation
│       │   ├── WordHighlighter.java    — Wort-Hervorhebung bei Wiedergabe
│       │   ├── WordTimingData.java     — Wort-Timing aus ElevenLabs
│       │   ├── FontSize.java           — 5 Schriftgrößen-Enum
│       │   └── ListenStoriesPreferences.java
│       ├── main/
│       │   ├── ChatGPTIO.java      — LLM API (OpenAI + Gemini) + alte Bild-API
│       │   ├── ElvenlabIO.java     — TTS API (ElevenLabs)
│       │   ├── SunoApiIO.java      — Song-Generierung (Suno API V5, Polling mit Fortschritt)
│       │   ├── Prompt.java         — Story-Prompt-Generierung aus StorySettings
│       │   ├── SongPrompt.java     — Song-Prompt-Generierung (Full + Compact/Suno ≤490 Zeichen)
│       │   ├── MainInterface.java  — Callback-Interface (setRunning, setStatusText, etc.)
│       │   ├── Running.java        — Async-Status-Enum (NONE, CREATE_STORY, CREATE_AUDIO, CREATE_SONG)
│       │   ├── ToniesAPI.java      — Tonie-Upload
│       │   └── tonie/              — Tonie-Datenklassen (Login, Household, Chapter, etc.)
│       ├── menu/
│       │   ├── Menu.java           — Hauptmenü (Story + Song + Listen)
│       │   ├── MenuPreferences.java
│       │   └── LanguageEnum.java   — 6 Sprachen (DE, EN, ES, FR, IT, TR)
│       ├── creativeTonie/
│       │   ├── CreativeTonie.java  — Tonie-Upload-Screen
│       │   ├── ChapterTile.java    — Kapitel-Darstellung
│       │   └── CreativeToniePreferences.java
│       └── objects/
│           ├── EnumInterface.java  — Basis-Interface für alle Story-Elemente
│           ├── CustomEntity.java   — Benutzerdefiniertes Element (mit CustomImageManager)
│           ├── GameObjectives.java — Hält ausgewählte Story-Elemente (nullable, null-safe refresh)
│           ├── MainCharacter.java  — 30 Hauptcharaktere (Enum)
│           ├── SupportingCharacter.java
│           ├── Universe.java
│           ├── Places.java
│           └── Objectives.java
├── asset/AssetLoader.java          — Texturen, Fonts, Audio
├── backend/                        — GameScreen, ScreenModel, Input-Handling
├── common/Localization.java        — I18n (de, en, fr, es, it, tr)
└── entity/                         — UI-Komponenten (Buttons inkl. ApoButtonCheckIcon, Textfield mit Clipboard, TextArea)

assets/
├── config.properties               — API-Keys (NICHT committen!)
├── i18n/aistories_*.properties     — Übersetzungen
└── images/                         — Sprite-Sheets
```

## Build
```bash
./gradlew :core:compileJava        # Nur kompilieren
./gradlew :desktop:run             # Desktop starten
```
