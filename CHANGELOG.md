# Changelog

All notable changes to this project will be documented in this file.

## [0.1.5] - 2026-04-28

### Added
- **Backup & Restore**: Comprehensive backup and restore functionality, including manual exports and imports with password protection.
- **Auto-Export**: Periodic automatic backups in the background using WorkManager.
- **Swipe Gestures**: Implemented swipe-to-edit and bidirectional swipe gestures for message items.
- **SmartText**: New component for enhanced text rendering in messages with `maxLines` support.
- **F-Droid Flavor**: Added a dedicated F-Droid build flavor with update checking disabled.
- **Custom Theming**: Custom Material 3 color schemes and a comprehensive typography scale.
- **KDoc Documentation**: Added extensive KDoc to domain use cases, preference constants, and Koin modules.
- **Manual Update Check**: Added settings to manually check for updates (non-F-Droid builds).

### Changed
- **UI Improvements**:
    - Backup progress indicator moved to the top bar in Settings.
    - Backup and restore dialogs refactored for better UX, including visibility toggles for passwords.
    - Updated authentication locked title string.
- **Refactoring**:
    - Standardized and reorganized string resource naming.
    - Renamed `lockOnCreateEnabled` to `isAuthRequired` for clarity.
    - Extracted `SettingsUiEvent` handling in `SettingsViewModel`.
    - Cleaned up WorkManager imports and used KTX `toUri`.
- **Build & Maintenance**:
    - Bumped `versionCode` to 10.
    - Removed custom sourceSet for GitHub variant.
    - Disabled dependency information in APK and Bundle for privacy.

### Fixed
- Cleaned up document and URI handling when cancelling export dialogs.
- Sorted copied messages by timestamp in domain layer.

### Removed
- Unused UI and state components.
- `foojay-resolver-convention` plugin from build scripts.

---

## [0.1.4] - 2026-04-28

### Changed
- Downgraded version name to 0.1.4 from 0.1.5 in a previous build correction.
- Added Android Fastlane metadata and assets.
