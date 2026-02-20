# StoryMagic Kids 🌈 AI-Powered Bedtime Story App

An enchanting Android application that creates personalized bedtime stories for children ages 3-12 using AI. Each story is uniquely crafted based on the child's name, age, gender, interests, and preferred moral themes.

## Features

- **Personalized Stories**: Enter your child's name, age, and gender for stories that feel personal
- **Gender-Aware Narratives**: Stories use appropriate pronouns (he/she) based on child's gender
- **Multiple Genres**: Adventure, Fantasy, Space, Dinosaurs, Magic, Animals, Pirates, Mystery
- **Moral Themes**: Choose from Friendship, Courage, Sharing, Kindness, Honesty, Curiosity, Teamwork
- **Text-to-Speech**: Listen to stories with various voice options and adjustable speeds
- **Word Highlighting**: Follow along as words are highlighted during playback
- **Story Library**: Save, search, and organize your favorite stories
- **Kid-Friendly Design**: Colorful, animated interface with magical mascots
- **Parental Controls**: PIN protection, daily limits, content filters
- **Offline Access**: Saved stories available without internet

## Requirements

- Android 8.0 (API 26) or higher
- OpenRouter API key (get yours at [openrouter.ai/keys](https://openrouter.ai/keys))

## Installation

1. Clone the repository:
```bash
git clone https://github.com/Amacorp/Story-Magic-App-For-Kids.git
```

2. Open in Android Studio

3. Build and run on your device or emulator

4. On first launch, go to Settings and enter your OpenRouter API key

## Configuration

### API Key Setup
1. Open the app
2. Navigate to Settings (gear icon)
3. Enter your OpenRouter API key
4. Click "Save API Key"
5. Test the connection

### Supported AI Models
- Google Gemini 2.0 Flash Lite (default)
- Google Gemini 2.0 Flash
- DeepSeek Chat V3 (free)
- Meta Llama 3.1 8B (free)

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Networking**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines & Flow
- **Preferences**: DataStore + EncryptedSharedPreferences

## Project Structure

```
app/src/main/java/com/storymagic/kids/
├── data/
│   ├── local/          # Room database, entities, DAOs
│   ├── remote/         # API interfaces, network models
│   └── repository/     # Repository implementations
├── di/                 # Dependency injection modules
├── domain/             # Domain models, use cases
├── ui/
│   ├── components/     # Reusable UI components
│   ├── screens/        # Screen composables
│   ├── theme/          # Colors, typography, themes
│   └── viewmodel/      # ViewModels
└── MainActivity.kt
```

## Key Components

### Onboarding Flow
1. **Name & Gender**: Child's name and gender for personalization
2. **Age Selection**: 3-12 years with age-appropriate content
3. **Story Preferences**: Genres, beloved objects, optional pet
4. **Moral Theme**: Select the lesson the story should teach

### Story Generation
- AI generates 400-500 word bedtime stories
- Stories are saved to local database
- Each story gets a unique colorful thumbnail

### Playback Features
- Multiple voice options (Happy Teacher, Friendly Mom, etc.)
- Speed controls (0.6x, 0.8x, 1.0x, 1.2x)
- Word-by-word highlighting
- Progress tracking

## Customization

### Themes
Currently supports Light theme with colorful gradients. The background smoothly transitions between pastel colors.

### Colors
- Primary Pink: #FF6B9D
- Sky Blue: #00D4FF  
- Sunshine Yellow: #FFDD00

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Privacy

- All stories are stored locally on the device
- API keys are encrypted using EncryptedSharedPreferences
- No personal data is collected or transmitted beyond API calls
- No analytics or tracking

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [OpenRouter](https://openrouter.ai/) for AI model access
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI
- Icons from 3D Icons collection

Made with love for children everywhere
