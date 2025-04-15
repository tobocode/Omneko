# Omneko

[![Build and Release APK](https://github.com/tobocode/Omneko/actions/workflows/build_release.yaml/badge.svg)](https://github.com/tobocode/Omneko/actions/workflows/build_release.yaml)

## What is this?

Omneko is a simple video player for Android, which allows you to watch vertical short-form videos of various popular platforms, without having to install their proprietary apps.
That way, when a friend sends you a video, you can watch it without the hassle of watching it in a web browser, as most of these platforms heavily try to push their apps if you use the mobile web page.

### What does the name mean?

Omneko is a portmanteau of omniscient/omnipresent/omnipotent and neko, the japanese word for cat.
The omni* part comes from it's ability to watch videos from numerous popular platforms.
The cat part was chosen because cats have vertical pupils.

## Features

Currently, links to any of these platforms are supported:

- TikTok
- Youtube Shorts
- Instagram Reels

## Installation and usage

You can download the app from the [releases page](https://github.com/tobocode/Omneko/releases).
To install it manually, you need to download either the latest apk for your processor architecture or the universal apk if you're unsure about which to choose.
But keep in mind that the universal apk has the largest size, as it supports each possible architecture.

However, I recommend installing it with [Obtainium](https://github.com/ImranR98/Obtainium) to automatically get new updates.
Installing it manually means that you need to download a new apk file each time an update is available, without getting notified about new updates.
Obtainium automates all of that.

> [!NOTE]
> As the app is still in early development, there are currently only pre-release versions available.
> To make sure Obtainium can find the apk files, you need to enable **"Include prereleases"** in the app configuration

### Setup link handling

Due to security related changes in Android 12, [any links will be opened in the browser](https://developer.android.com/training/app-links/#web-links), unless the domain verifies that an app is authorized to handle them.
As I don't control the domains whose links should be handled, the app cannot automatically register itself for those links.

Luckily, you can manually approve apps to handle certain links.
To do that, you can open the app and click the **Configure link handling** button, which will automatically navigate you to the correct settings page, where you can add all the links which you want the app to handle.
Alternatively, you can also manually navigate to `Settings > Apps > Omneko > Open by default > Links to open in this app`.
Now, clicking on any approved links should open the app instead of your default web browser.

As some apps seem to not like opening links in their dedicated apps, you can use Androids sharing functionality as an alternative and share a link with the app.
This will play the video in the same way as clicking on the link would.

### Updating YoutubeDL

The first time the app is started, it will automatically update YoutubeDL to the latest stable version.
If you encounter problems watching videos from platforms that previously worked, you can manually update YoutubeDL again in the main screen.

## Donate

If you like this project, please consider donating a little to one of these addresses :)

Monero: 84pqAhMwbXWV7jjnA7zpNULYtWrYdJjoCPgrzXbW7AbtDDF6FWY2zJbK3nfA28PRUrfEHPV84VaCg4TW3E3VkXsL5w4dJNm
Solana: 4TUA4GP6sp682HLbmcgL6oY9SyxBcaKXj9NCwqVDSAUp
