
# Android Biometric and Authentication Application

## Project Description
This project provides a robust and user-friendly Android application with the following core features:
- **Biometric Authentication Setup**: Enables users to configure biometric authentication for secure access.
- **Code-based Authentication**: Supports PIN or passcode setups for additional security.
- **Deep Linking and OAuth Integration**: Implements callback handling for authentication flows.
- **Modern UI/UX**: Built with Material Design principles for a clean and intuitive user experience.

## Features
- Secure and fast biometric setup.
- Code-based authentication for fallback or alternative access.
- Seamless integration of OAuth workflows using deep links.
- Adherence to Android best practices and MVVM architecture.

## Prerequisites
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 31
- Android Studio (latest version recommended).

## Installation
1. Clone the repository:  
   ```bash
   git clone [REPOSITORY_URL]
   ```
2. Open the project in Android Studio.
3. Sync Gradle and resolve dependencies.
4. Run the app on an emulator or physical device.

## App Flow
1. Launch the app to land on the `MainActivity`.
2. Navigate to:
   - **Biometric Setup**: Configure secure biometric authentication.
   - **Code Setup**: Set up PIN or passcode-based access.
3. Use OAuth integration for authenticated interactions.

## Project Structure
The app is structured following MVVM architecture:
- **Activities**: Manage user interfaces and interaction logic.
- **ViewModels**: Handle business logic and data binding.
- **Resources**: Store UI elements like layouts and drawables.

## Testing
The application includes unit testing for business logic and integration testing for UI flows. Sample test cases cover:
- Biometric prompt validation.
- Code validation logic.
- Navigation testing for deep links.

## License
This project is licensed under the MIT License.
