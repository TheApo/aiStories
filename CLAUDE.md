# AIStories - Kindergeschichten-Generator

## Was ist das?
LibGDX Desktop/Android App. Generiert Kindergeschichten per LLM (OpenAI GPT-5-mini, Google Gemini), wandelt sie per ElevenLabs TTS in Audio um und kann sie auf Tonie.box hochladen.

## Prinzipien
- **KISS** — Einfach halten. Keine Over-Engineering, keine unnötigen Abstraktionen.
- **DRY** — Code wiederverwenden. Keine Duplikate. Gemeinsame Logik in Helper-Methoden extrahieren.
- **Kurze Klassen** — Klassen klein und fokussiert halten. Eine Klasse = eine Verantwortung.
- **Bestehenden Code nutzen** — Vor dem Schreiben prüfen, ob es schon etwas Passendes gibt.
- **Keine unnötigen Kommentare/Docs** — Code soll selbsterklärend sein.

## Architektur

### Screen-System
`MainPanel` (extends `GameScreen`) ist der zentrale Controller. Hält alle `ScreenModel`-Instanzen und wechselt zwischen ihnen via `changeModel()`. Screens:
- `Menu` — Hauptmenü
- `CreateStory` — Geschichte konfigurieren (5 Spalten: MainCharacter, SupportingCharacter, Universe, Places, Objectives)
- `ListenStories` — Geschichte anzeigen/abspielen
- `CreativeTonie` — Tonie-Upload
- `CustomEntityEditor` — Editor für Custom-Objekte (Name, Beschreibung, Bild)

### EnumInterface-Pattern
Alle Story-Elemente implementieren `EnumInterface` (getName, getDisplayName, getImage, getEnumByName, ...). Konkrete Enums: `MainCharacter`, `SupportingCharacter`, `Universe`, `Places`, `Objectives` (je 30 Werte). `CustomEntity` implementiert dasselbe Interface für benutzerdefinierte Einträge.

### Input-System (Achtung!)
`GameScreen.keyDown` → `keyPressedArray`, `keyTyped(char)` → `keyReleasedArray`, `keyUp(keycode)` → `keyReleasedArray`. keyTyped und keyUp landen im SELBEN Array! Workaround: `keys[]`-Array in Screens, um keyUp von keyTyped zu unterscheiden.

### LLM-Integration
`ChatGPTIO` handhabt zwei APIs:
- **OpenAI** (gpt-5-mini): Bearer-Token Auth, `messages`/`choices` Format, **`max_completion_tokens`** (NICHT max_tokens!)
- **Gemini** (gemini-3-flash-preview): API-Key in URL, `contents`/`candidates` Format, `systemInstruction` separat

API-Keys in `assets/config.properties`, geladen in `AIStories.java`.

## Projektstruktur
```
core/src/com/apogames/
├── aistories/
│   ├── AIStories.java              — Entry Point, lädt API-Keys
│   ├── Constants.java              — Farben, Größen, Flags
│   └── game/
│       ├── MainPanel.java          — Zentraler Controller, Custom-Entities, Screen-Wechsel
│       ├── ButtonProvider.java     — Alle Button-Definitionen
│       ├── createStory/
│       │   ├── CreateStory.java    — Konfigurations-Screen
│       │   └── ObjectSelection.java — Spalte mit UP/DOWN/Custom-Buttons
│       ├── customEntity/
│       │   ├── CustomEntityEditor.java — Editor (Name, Details, Bild-Grid)
│       │   └── CustomEntityEditorPreferences.java
│       ├── listenStories/          — Story anzeigen/abspielen
│       ├── main/
│       │   ├── ChatGPTIO.java      — LLM API (OpenAI + Gemini)
│       │   ├── ElvenlabIO.java     — TTS API
│       │   ├── Prompt.java         — Prompt-Generierung
│       │   └── ToniesAPI.java      — Tonie-Upload
│       ├── menu/                   — Hauptmenü
│       └── objects/
│           ├── EnumInterface.java  — Basis-Interface für alle Story-Elemente
│           ├── CustomEntity.java   — Benutzerdefiniertes Story-Element
│           ├── MainCharacter.java  — 30 Hauptcharaktere (Enum)
│           ├── SupportingCharacter.java
│           ├── Universe.java
│           ├── Places.java
│           └── Objectives.java
├── asset/AssetLoader.java          — Texturen, Fonts, Audio
├── backend/                        — GameScreen, ScreenModel, Input-Handling
├── common/Localization.java        — I18n (de, en, fr, es, it, tr)
└── entity/                         — UI-Komponenten (Buttons, Textfield, TextArea)

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
