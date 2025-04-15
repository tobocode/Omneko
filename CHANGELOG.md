# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Download quality settings
- German translation

## [1.0.0-alpha.10] - 2025-04-15

### Added

- Settings screen
- Theme setting to override the system dark/light mode
- Setting for limiting the amount of downloaded comments
- Setting for deciding whether to use aria2c for downloads

## [1.0.0-alpha.9] - 2025-04-13

### Changed

- Comments are now downloaded separately after the video to improve loading times
- Videos and comments will now be downloaded using Aria2c

## [1.0.0-alpha.8] - 2025-04-12

### Added

- Comments are now visible in the comments tab of the info sheet

## [1.0.0-alpha.7] - 2025-04-11

### Added

- Button for downloading the viewed video

## [1.0.0-alpha.6] - 2025-04-09

### Added

- Bottom sheet for video information and comments
- Video title, uploader, description, like count and view count are now shown in the about tab of the bottom sheet

## [1.0.0-alpha.5] - 2025-04-08

### Added

- Button for easy access to the link association settings page
- Settings button on the video player activity
- Add disabled video info and comments buttons

### Changed

- Change splash screen background to a dark color when night mode is enabled
- Move PlayerViewModel data into a separate data class

### Fixed

- Fix text not readable in light mode

## [1.0.0-alpha.4] - 2025-04-06

### Added

- New main screen when opening the app from the launcher
- Button for updating YoutubeDL
- Automatic update for YoutubeDL when the app ist launched for the first time

### Fixed

- Fix Instagram Reels not working due to YoutubeDL being outdated

## [1.0.0-alpha.3] - 2025-04-06

### Added

- Show video title and channel on the bottom

### Changed

- Lock screen orientation to portrait mode

### Fixed

- Fix video player being destroyed when the Activity is recreated

## [1.0.0-alpha.2] - 2025-04-04

### Fixed

- Fix crashing when an unsupported URL is shared with the app
- Fix crashing when trying to view a deleted video
- Fix broken release build configuration

## [1.0.0-alpha.1] - 2025-04-03

### Added

- Very simple vertical video player
- Support for opening links for TikTok, Youtube Shorts and Instagram Reels
- Ability to share links with the app in addition to clicking supported links
