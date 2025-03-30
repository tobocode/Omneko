# Omneko

## What is this?

Omneko is a simple video player for Android, which allows you to watch vertical short-form videos of various popular platforms, without having to install their proprietary apps.
That way, when a friend sends you a video, you can watch it without the hassle of watching it in a web browser, as most of these platforms heavily try to push their apps if you use the mobile web page.

### What does the name mean?

Omneko ist a portmanteau of omniscient/omnipresent/omnipotent and neko, the japanese word for cat.
The omni* part comes from it's ability to watch videos from numerous popular platforms.
The cat part was chosen because cats have vertical pupils.

## Features

Currently, links to any of these platforms are supported:

- TikTok
- Youtube Shorts
- Instagram Reels

## Installation and usage

> [!NOTE]
> As the app is still in early development, there are no releases yet.
> For now, you have to build it yourself.

Due to security related changes in Android 12, [any links will be opened in the browser](https://developer.android.com/training/app-links/#web-links), unless the domain verifies that an app is authorized to handle them.
As I don't control the domains whose links should be handled, the app cannot automatically register itself for those links.

Luckily, the user can manually approve apps to handle certain links.
To do that, after installing the app, you need to navigate to `Settings > Apps > Omneko > Open by default > Links to open in this app` and add all links which you want the app to handle.
Now, clicking on any approved links should open the app instead of your default web browser.

As some apps seem to not like opening links in their dedicated apps, you can use Androids sharing functionality as an alternative and share a link with the app.
This will play the video in the same way as clicking on the link would.
