
# Development Documentation

## Project Overview
**Purpose:**  
The project is designed to manage biometric and code-based authentication features along with a home screen for displaying key user information.  
**Features:**  
- Biometric Setup  
- Code Authentication  
- OAuth Callback Support  
- Modern UI using Material Components  

## Code Structure
The project follows an MVVM (Model-View-ViewModel) architecture. The key components are:  
- **Activities**: Manages UI and user interactions.
- **ViewModels**: Handles the business logic and provides data to the UI.

### Key Files and Responsibilities:
1. **BiometricSetupActivity.kt**: Handles biometric authentication setup.
2. **CodeSetupActivity.kt**: Manages code-based authentication.
3. **HomeActivity.kt**: Displays the home screen with user-specific information.
4. **MainActivity.kt**: The app's entry point, includes deep linking configuration.
5. **BaseActivity.kt and BaseViewModel.kt**: Base classes providing shared functionality.

## API Integration
The project integrates an OAuth authentication mechanism using deep linking for callback management.

## UI/UX Design
- The app is designed with a modern material theme (`Theme.GuardianApp`).
- Navigation uses Android's Jetpack Navigation for seamless transitions.

## Testing
- **Unit Tests**: Focus on ViewModels and business logic.
- **UI Tests**: Validate user flows and interaction (e.g., Login, Biometric Setup).

## Deployment
- **Pre-requisites**: Android 11 (API 30) or above.
- **Build Process**: Use Android Studio and Gradle wrapper to build the project.
