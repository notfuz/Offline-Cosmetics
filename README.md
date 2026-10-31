# Labymod Offline Cosmetics
[![Release](https://img.shields.io/github/v/release/Xqedii/Offline-Cosmetics)](https://github.com/Xqedii/Offline-Cosmetics/releases/latest) 
[![Discord](https://img.shields.io/badge/Discord-Join-7289DA?logo=discord&logoColor=white)](https://discord.com/invite/6JQfeQEB4W)

**Restores LabyMod cosmetics and premium skins on cracked servers**

An unofficial LabyMod addon that client-side restores cosmetics, skins, and capes on offline-mode servers by mapping the cracked session UUID to a Premium UUID and retrieving skin data from the Mojang API.

<img width="1121" height="599" alt="123" src="https://github.com/user-attachments/assets/0316f5e6-05bd-4d94-9744-12e1c67c5853" />

-----

# Installation

To install the addon, simply build the JAR file or download the latest version from the Releases, then place it in the Addons folder in LabyMod.
The addon works with Minecraft 1.8.9 through 1.21.8 and with 26.1, 26.1.1, 26.1.2 and 26.3.

> [!IMPORTANT]
> Versions 1.21.10 and 1.21.11 are not supported.
> Versions 1.20.2 and 1.20.5 are no longer built, because the current LabyGradle does not list them.

### How it works on 26.1+

Since Minecraft 1.21.9 `GameProfile` is a record, so the cracked UUID can no longer be replaced on the existing object.
On 26.1+ the addon swaps the profile for a new one with the premium UUID whenever a player or tab list entry is created for you, and
loads your premium skin from Mojang in the background. The code for this lives in `game-runner/src/v26_*`.

-----

# Settings

In the addon settings, you can toggle whether the addon is enabled or disabled.
Additionally, you can choose whether the addon should load only cosmetics, only the skin, or both.

<img width="1049" height="507" alt="x" src="https://github.com/user-attachments/assets/12a857e3-efda-44aa-a61e-f6c00d138357" />

-----

# Building

After cloning the repository, you can build the project with Gradle using `./gradlew build`.
The .jar file will appear in `build/libs`

You need JDK 25 to build the project, because Minecraft 26.1+ runs on Java 25.

The `.github/workflows/build.yml` workflow does the same on every push and pull request and uploads the release jar as an artifact.
Pushing a tag like `v1.1.0` also publishes a GitHub release with the jar attached, using the tag as the addon version.
