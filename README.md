# Game Manager API Samples for Google Cast

This is a collection of samples using the [Google Cast Game Manager API](https://developers.google.com/cast/docs/gaming). The Google Cast Game Manager allows native Android, iOS, and Chrome games to connect with a Google Cast device and have communication and synchronization of player and game states across devices.

* **SpriteDemo**: A hello world application. The simplest sample using the Game Manager APIs that lets devices add sprites on the TV.

* **StarCast**: A simple multiplayer shoot-em-up. The Android and iOS versions can control a spaceship. The Chrome version can control up to 4 spaceships using the keyboard.

* **SpellCast**: Team up with up to 3 friends and draw arcane symbols on your device to cast spells and defeat monsters!  This sample shows examples of the [Cast Game UX guidelines (PDF)](https://developers.google.com/cast/downloads/GoogleCastGameUXguidelinesV0.9.pdf), such as setting up a multiplayer lobby.

* **GameDebugger**: A utility that lets you see game state, view the messages being passed around, and connect new players. Works with the [Game Manager Debug UI](https://developers.google.com/cast/docs/debugging#game) on the TV.

## Dependencies

### Android

* google-play-services_lib library from Android SDK (at least version 7.5.71)
* android-support-v7-appcompat (version 21 or above)
* android-support-v7-mediarouter (version 20 or above)

### iOS

* google-cast-sdk (at least version 2.7.0)
* google-cast-games-sdk (at least version 2.7.0)

These can be installed using [Cocoapods](https://guides.cocoapods.org/using/getting-started.html#getting-started).

### Chrome

* Google Cast extension (at least version 15.605.1.0)

## Setup Instructions

Each sample includes code for the Android sender, iOS sender, Chrome sender, and the receiver.  Note SpellCast and GameDebugger only have minimal Chrome senders that let you interact with the receiver using the [Chrome devtools console](https://developer.chrome.com/devtools/docs/console).

Review the [Google Cast SDK Get Started Guide](https://developers.google.com/cast/docs/developers) before setting up these samples.

### Android

* Check out code from GitHub and go to a sample subfolder under `android-sender` with a `build.gradle` file.
* In Android Studio, select `Open Existing Android Studio Project` and select the `build.gradle` file.
* You may need to provide the path to your local gradle installation. You can select the gradle binary inside your Android Studio installation directory.
* You can now run the app normally from within Android Studio, or use `gradlew build` from the command line.

If you prefer to use your local gradle installation to generate the gradle wrapper, type`gradle wrapper` from the project directory.

### iOS

* Check out code from GitHub and go to a sample subfolder under `ios-sender` with a Podfile.
* Run `pod install` in the sample folder with the Podfile (don't have Cocoapods? Learn more at https://developers.google.com/ios/cocoapods).
* Follow the instructions from running `pod install` to open the project.

### Chrome

* Try out the Chrome samples hosted here:
  * [SpriteDemo](https://googlecast.github.io/GameManagerSamples/spritedemo/index.html)
  * [StarCast](https://googlecast.github.io/GameManagerSamples/starcast/index.html)
  * [SpellCast](https://googlecast.github.io/GameManagerSamples/spellcast/index.html)
  * [GameDebugger](https://googlecast.github.io/GameManagerSamples/game_debugger/index.html)
* Check out code from GitHub and go to a sample folder under `chrome-sender` with index.html.
* Every Chrome sample has a `bin` subfolder with a compiled version ready to be served by a web server.
* Every Chrome sample exposes global functions and a global `gameManagerClient` object to let you experiment with the Game Manager API using the [Chrome devtools console](https://developer.chrome.com/devtools/docs/console).
* If you want to modify and build your own version, you can use [Closure Tools](https://developers.google.com/closure/), or stub out the `goog.*` method calls and concatenate the js files into a single file called `all.js`.

### Receiver

* Check out code from GitHub and go to a sample folder under `receiver` with index.html.
* Every receiver sample has a `bin` subfolder with a compiled version ready to be served by a web server.
* Every receiver sample exposes a global `game` object that includes a `debugUi` field to let you call `game.debugUi.open()` to open the Game Manager Debug UI. You can do this programmatically or using the [Chrome Remote Debugger](https://developers.google.com/cast/docs/debugging).
* The global `game` object also has a `gameManager_` field that you can access in if you want to try changing the game state directly on the receiver.
* Create a new app ID for your receiver sample using the Google Cast SDK Developer Console: https://cast.google.com/publish/
* Update app IDs in the Android, iOS, and Chrome senders so they will launch your receiver sample and not the default public sample receiver.
* If you want to modify and build your own version, you can use [Closure Tools](https://developers.google.com/closure/), or stub out the `goog.*` method calls and concatenate the js files into a single file called `all.js`.

## References and How to report bugs

* [Cast APIs](http://developers.google.com/cast/)
* [Game Manager API](https://developers.google.com/cast/docs/gaming)
* [Design Checklist](http://developers.google.com/cast/docs/design_checklist)
* [Cast Game UX
  Guidelines (PDF)](https://developers.google.com/cast/downloads/GoogleCastGameUXguidelinesV0.9.pdf)
* If you find any issues, please open a bug here on GitHub.

## How to make contributions?

Please read and follow the steps in the CONTRIBUTING.md

## License

See LICENSE

## Google+

Google Cast Developers Community on Google+ [http://goo.gl/TPLDxj](http://goo.gl/TPLDxj)
