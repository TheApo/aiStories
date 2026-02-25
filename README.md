# AIStories

A cross-platform app that lets children create their own bedtime stories. Pick characters, worlds, and themes — then either let an AI write the story or get creative yourself. No API keys required to use the app.

With optional API keys you can unlock additional features step by step:

- **ChatGPT or Gemini API Key** — The AI generates a unique story based on your selections
- **+ ElevenLabs API Key** — The story is read aloud with a configurable voice
- **+ Tonies Cloud Login** — Upload the audio directly to your Creative Tonie figure

## Features

- **Works Without API Keys** — Children can pick story elements, get creative, and write their own stories
- **AI-Powered Story Generation** — Generate unique children's stories using OpenAI (GPT-5-mini) or Google Gemini
- **5 Customizable Story Elements** — Pick from 30 options each for Main Character, Supporting Character, Universe, Place, and Object — or create your own custom entries with name, description, and image
- **Text-to-Speech** — Convert stories to audio via ElevenLabs with configurable voice and model
- **Tonie.box Integration** — Upload generated audio directly to a Creative Tonie
- **Multi-Language UI** — Available in: 🇩🇪 Deutsch, 🇬🇧 English, 🇫🇷 Français, 🇪🇸 Español, 🇮🇹 Italiano, 🇹🇷 Türkçe
- **Cross-Platform** — Runs on Desktop (Windows, macOS, Linux) and Android

## Screenshots

<p>
  <a href="screenshots/aiStories_1.png"><img src="screenshots/aiStories_1.png" width="30%"></a>
  <a href="screenshots/aiStories_2.png"><img src="screenshots/aiStories_2.png" width="30%"></a>
  <a href="screenshots/aiStories_3.png"><img src="screenshots/aiStories_3.png" width="30%"></a>
  <a href="screenshots/aiStories_4.png"><img src="screenshots/aiStories_4.png" width="30%"></a>
  <a href="screenshots/aiStories_5.png"><img src="screenshots/aiStories_5.png" width="30%"></a>
</p>

## Prerequisites

- **Java 8** or higher
- **Android SDK** (API 27+) for Android builds
- API keys are **optional** — see [Configuration](#configuration) for details

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/TheApo/aiStories.git
cd aiStories
```

### 2. Configuration

All API keys are optional. Add only the ones for the features you want to use:

```bash
cp assets/config.properties.sample assets/config.properties
```

Edit `assets/config.properties`:

```properties
CHATGPT_API_KEY=your-openai-api-key
GEMINI_API_KEY=your-gemini-api-key
ELEVENLABS_API_KEY=your-elevenlabs-api-key
ELEVENLABS_VOICE_ID=your-elevenlabs-voice-id
ELEVENLABS_MODEL_ID=eleven_multilingual_v2
TONIES_USERNAME=your-tonies-username
TONIES_PASSWORD=your-tonies-password
```

| Key | Required | Description |
|-----|----------|-------------|
| `CHATGPT_API_KEY` | For OpenAI stories | [OpenAI API Key](https://platform.openai.com/api-keys) |
| `GEMINI_API_KEY` | For Gemini stories | [Google AI Studio API Key](https://aistudio.google.com/apikey) |
| `ELEVENLABS_API_KEY` | For TTS | [ElevenLabs API Key](https://elevenlabs.io/) |
| `ELEVENLABS_VOICE_ID` | For TTS | Voice ID from your ElevenLabs account |
| `ELEVENLABS_MODEL_ID` | No (default: `eleven_multilingual_v2`) | ElevenLabs TTS model |
| `TONIES_USERNAME` | For Tonie upload | Tonies Cloud login email |
| `TONIES_PASSWORD` | For Tonie upload | Tonies Cloud login password |

> **Note:** `config.properties` is in `.gitignore` and will not be committed. Never share your API keys.

### 3. Build & Run

**Desktop:**

```bash
./gradlew :desktop:run
```

**Android:**

```bash
./gradlew :android:installDebug
```

**Build Desktop JAR:**

```bash
./gradlew :desktop:dist
```

The JAR will be created in `desktop/build/libs/`.

## Tech Stack

- [LibGDX](https://libgdx.com/) 1.13.1 — Cross-platform game framework
- [Gson](https://github.com/google/gson) — JSON serialization
- [OkHttp](https://square.github.io/okhttp/) — HTTP client
- [Lombok](https://projectlombok.org/) — Boilerplate reduction
- [toniebox-api](https://github.com/maximilianvoss/toniebox-api) — Tonie.box cloud connection (Apache License 2.0)
- [ElevenLabs API](https://elevenlabs.io/) — Text-to-Speech
- [OpenAI API](https://platform.openai.com/) — Story generation (GPT-5-mini)
- [Google Gemini API](https://ai.google.dev/) — Story generation (Gemini)

## Project Structure

```
AIStories/
├── core/           # Shared game logic (story generation, UI, entities)
├── desktop/        # Desktop launcher (LWJGL3)
├── android/        # Android launcher
├── assets/         # Images, fonts, translations, config
│   ├── config.properties.sample
│   ├── i18n/       # Localization files
│   └── images/     # Sprite sheets and textures
├── data/           # Generated story files
└── save/           # Save data
```

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

### Third-Party Licenses

This project uses [toniebox-api](https://github.com/maximilianvoss/toniebox-api) by Maximilian Voss, licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
