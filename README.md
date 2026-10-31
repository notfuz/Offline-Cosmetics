# LabyMod Offline Cosmetics

**Restores LabyMod cosmetics and premium skins on offline-mode servers.**

LabyMod Offline Cosmetics is an unofficial client-side addon that restores the visibility of LabyMod cosmetics, premium skins and capes when playing on offline-mode (cracked) servers.

The addon works by mapping your offline session UUID to your Premium Minecraft UUID and retrieving the associated skin information from Mojang.

<img width="1121" height="599" alt="Offline Cosmetics in-game screenshot" src="https://github.com/user-attachments/assets/0316f5e6-05bd-4d94-9744-12e1c67c5853" />

---

## Features

- Restore LabyMod cosmetics and emotes.
- Restore premium Minecraft skins.
- Restore capes.
- Automatically map your offline player profile to your Premium Minecraft profile.

## Supported Minecraft Versions

| Minecraft version | Support |
|---|---|
| 1.8.9 – 1.21.8 | Supported, with listed exceptions |
| 26.1 - 26.3 | Supported |
| 1.21.9 - 1.21.11 | Still thinking if support is really needed |

## Installation

1. Download the latest release from the [Releases page](../../releases/latest).
2. Download `OfflineCosmeticsLabyMod-release.jar`.
3. Open your LabyMod installation's `addons` folder.
4. Place the downloaded JAR inside the folder.
5. Restart LabyMod.

Only the production release JAR should be installed. Intermediate build files are not intended for use in the production client.

## How It Works

### Profile and UUID mapping

Offline-mode servers generate player UUIDs independently of Mojang's authenticated account system.

This prevents LabyMod from automatically associating your offline player profile with your Premium Minecraft account.

Offline Cosmetics addresses this by mapping your offline session UUID to your Premium Minecraft UUID.

### Minecraft 1.21.9 and newer

Minecraft changed `GameProfile` into a record in 1.21.9, meaning the previous profile replacement approach could no longer be used.

For newer Minecraft versions, the addon creates a replacement profile containing the Premium UUID when the relevant player or tab-list entries are created.

Premium skin information is retrieved from Mojang in the background.

### Minecraft 26.x

The 26.x implementations use separate version-specific source directories.

These implementations handle the profile mapping and related compatibility changes required by the newer Minecraft versions.

The version-specific implementations are located in:

`game-runner/src/v26_*/`

## Settings

Offline Cosmetics provides configurable settings that allow you to choose which features are active.

You can configure:

- **Addon enabled:** Enable or disable Offline Cosmetics.
- **Cosmetics:** Restore LabyMod cosmetics.
- **Skin:** Restore your Premium Minecraft skin.

You can choose to use cosmetics only, skin restoration only, or both.

<img width="1049" height="507" alt="Offline Cosmetics settings" src="https://github.com/user-attachments/assets/12a857e3-efda-44aa-a61e-f6c00d138357" />

---

## Building From Source

### Requirements

- JDK 25
- Git
- Internet connection for Gradle dependencies
- Windows, Linux or macOS

Minecraft 26.1 and newer require Java 25. The project also contains older Minecraft implementations, with their respective compilation requirements handled by the Gradle configuration.

### Clone the repository

```bash
git clone https://github.com/notfuz/Offline-Cosmetics.git
cd Offline-Cosmetics
```

### Build the production addon

On Windows PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat createReleaseJar
```

On Linux or macOS:

```bash
chmod +x gradlew
./gradlew build
./gradlew createReleaseJar
```

The finished production addon is generated in:

`build/libs/`

The production JAR ends in `-release.jar`.

For example:

`OfflineCosmeticsLabyMod-release.jar`

**Important:** Do not install the intermediate `api`, `core`, `game-runner`, combined or development JARs. Only use the finished release JAR.

---

## Automated Builds and Releases

This repository uses GitHub Actions to automatically compile and package Offline Cosmetics.

The workflow is located at:

`.github/workflows/build.yml`

### Automatic builds

A build is triggered when:

- Code is pushed to `main` or `master`.
- A pull request is opened against `main` or `master`.
- A workflow is manually started using GitHub Actions.

Every successful build uploads the production release JAR as a downloadable GitHub Actions artifact.

### Publishing a release

To publish a new release, create and push a version tag.

For example:

```bash
git tag v1.1.0
git push origin v1.1.0
```

The workflow will:

1. Read the version from the Git tag.
2. Build the addon using that version.
3. Generate the production release JAR.
4. Upload the JAR as a GitHub Actions artifact.
5. Create a GitHub Release and attach the finished JAR.

The version tag determines the addon version. For example, `v1.1.0` produces addon version `1.1.0`.

---

## Credits and Attribution

**Original project:** [Xqedii/Offline-Cosmetics](https://github.com/Xqedii/Offline-Cosmetics)

Offline Cosmetics is based on the original work by **Xqedii**. This repository is a fork that builds upon the original implementation, with additional development, compatibility work, Minecraft version support and maintenance.

All credit for the original project and its foundational implementation belongs to Xqedii.

Please visit the original repository to support the original author and view the upstream project.

**Xqedii** is very aware that this repository exists! I have dm'ed him personally to know if its ok to continue his work!

---

## Disclaimer

This is an unofficial community-made LabyMod addon. It is not affiliated with, endorsed by or maintained by LabyMod, Mojang or Microsoft.

Minecraft is a trademark of Microsoft Corporation.

Use of this addon is client-side and does not modify the server's authentication system.
