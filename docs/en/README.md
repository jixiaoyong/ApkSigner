<div align="center">

![icon](./src/main/resources/imgs/icon.png)

# ApkSigner

![GitHub release (with filter)](https://img.shields.io/github/v/release/jixiaoyong/ApkSigner) ![GitHub Release Date - Published_At](https://img.shields.io/github/release-date/jixiaoyong/ApkSigner) ![Github All Releases](https://img.shields.io/github/downloads/jixiaoyong/apksigner/total.svg)

A tool for Android developers to sign their applications with GUI software.

一款供 Android 开发者用来可视化签名的工具。

Based on [JetBrains Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)

![Support Mac](https://img.shields.io/badge/Mac-grey?logo=apple)
![Support Windows](https://img.shields.io/badge/Windows-blue?logo=windows)
![Static Badge](https://img.shields.io/badge/Ubuntu-%23E95420?logo=Ubuntu&logoColor=white)

Supported Languages: Chinese, English

[![download button](docs/screenshort/download.svg)](https://github.com/jixiaoyong/ApkSigner/releases)

> 💬 Thank you all for your love for this small tool. Due to my work adjustment, I will temporarily not have much energy to invest in subsequent development and maintenance.
>
> 💬 感谢大家对这个小工具的喜爱，由于本人工作调整，我将暂时没有太多精力投入到后续的开发维护中。
>
> The functions of the current version are relatively stable and can meet basic usage needs. I hope this tool continues to provide some help to all of you in daily work. Thank you.
>
> 目前版本的各功能已经相对稳定，可以满足基本使用需求，希望这个工具继续在日常工作中对各位同仁提供些许帮助，谢谢。
</div>

---

**[中文 README](README.md)**

Some APKs packaged during work would prompt "No signature" when hardened by 360, but the signing process provided by 360 hardening is too cumbersome. Therefore, this small tool was developed to sign APK files using a graphical interface.

This software is developed based on JetBrains Compose Multiplatform and supports **macOS, Windows, and Linux**.

My daily development is mainly based on macOS, so there may be some undiscovered compatibility issues on Windows and Linux. If you find such problems, please [let me know](https://github.com/jixiaoyong/ApkSigner/issues). ~~I will try my best to adapt~~. I also welcome you to submit [PRs](https://github.com/jixiaoyong/ApkSigner/pulls).

## 📦 Key Features

This APP is designed to facilitate APK signing through a graphical interface, supporting the management and switching of multiple signatures, one-click APK signing, and more.
The tool does not provide the functionality to generate signature files. To use this tool, you need to prepare the following in advance:

* **Signature File**: Usually in `.keystore` or `.jks` format, used to sign the APK.
* **apksigner** and **zipalign** files: Used for signing and aligning the APK, generally found in a specific version folder within the Android SDK's `build-tools` directory.
* **Java Development Environment**: The apksigner used to sign the APK requires reading the `JAVA_HOME` configuration.

🚧 The interface functions are still continuously being improved, but basic features are available:

* [x] Add/Delete/Switch signature information (only modifies the APP's configuration, not your files)
* [x] Sign APK files, supporting V1, V2, V3, and V4 schemes
* [x] Specify the output directory for the signed APK
* [x] View the existing signature information of the APK
* [x] Support Light and Dark themes, with automatic switching
* [x] Optional alignment
* [x] Support multi-file signing and viewing signatures
* [ ] ~~Import multiple signature files~~
* [x] Automatically save/match the corresponding signature information for the APK
* [x] Optimize signature configuration
* [x] Beautify the theme
* [x] Add log viewing functionality
* [x] Support internationalization, automatically identifying system language
* [x] Customization of JAVA_HOME path (optional)

For privacy reasons, this APP will not include online update checking. If you wish to get the latest version information, you can **star** or **watch** this project on [GitHub](https://github.com/jixiaoyong/ApkSigner), and GitHub will notify you via the homepage or email when the APP is updated.

## 🖼️ Interface Preview

| Sign Info | Sign App | Settings | Dark Theme |
| :---: | :---: | :---: | :---: |
| ![sign_info](docs/screenshort/sign_info.png) | ![sign_app](docs/screenshort/sign_app.png) | ![sign_settings](docs/screenshort/sign_settings.png) | ![sign_app_dark](docs/screenshort/sign_app_dark.png) |

## 🚀 Usage

Choose your preferred method below:

### 1. Run system-specific packages like .dmg or .msi [Recommended 👍]

* **Directly run** the corresponding package provided in this repository.

  Click to download the [📦 Latest Release Packages (.dmg/.msi/.deb)](https://github.com/jixiaoyong/ApkSigner/releases)

  **Note for macOS users**: Please download the corresponding software based on your computer chip:
    * Apple Chip: Download `*-arm64.dmg`
    * Intel Chip: Download `*-x86_64.dmg`

  For Windows or Linux users, I apologize that I have not fully tested the software's availability on corresponding machines. If you encounter any issues during use, feel free to give feedback, and I will try my best to fix them. You are also welcome to download the source code and package it yourself using the method below.

* **Or download the source code and package it yourself**:

    ```groovy
    packageReleaseDeb // Suitable for Ubuntu and other Linux systems
    packageReleaseDmg // Suitable for macOS systems
    packageReleaseMsi // Suitable for Windows systems
    packageReleaseDistributionForCurrentOS // Automatically package the suitable format for the current OS
    ```

### 2. Compile from Source

1.  After downloading the source code, open it with IDEA and run the `Main.kt` file to start the graphical interface.
2.  The recommended Java version for development is **zulu 17**.

> For Windows users, if you encounter `Process finished with exit code -1073741819 (0xC0000005)`, you can run `./gradlew run`.
>
> You can find this command under `gradle > ApkSigner > compose desktop > run`;
>
> Or you can add `run` in the `Edit Configurations > Gradle > Add New Run Configuration... > Run` field.

### 3. Run the Jar Package

1.  Package it yourself using the jar packaging command `./gradlew packageReleaseUberJarForCurrentOS`.
2.  Run `java -jar xxx.jar` to launch the software.

For MAC users, you can consider using [jar2app](https://github.com/dante-biase/jar2app) to package the jar into an app for use, avoiding the tedious macOS system signing process.

---

## 📄 License

Copyright (C) JI,XIAOYONG

This software ([ApkSigner](https://github.com/jixiaoyong/ApkSigner)) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either [version 3 of the License](./LICENSE), or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

### Additional Terms

Without violating the above terms, if you wish to create and distribute your own software version based on this project, you must also comply with the following terms:

* **Retention of Author Information**: When using, copying, modifying, or distributing the source code of this project, the name and contact information of the original author must be retained.
* **Logo Use Restriction**: The Logo of this project may not be used, copied, modified, or distributed without the explicit written permission of the original author.
* **Source Code Link**: When using, copying, modifying, or distributing the source code or software works of this project, a link pointing to the original project source code URL must be provided in a prominent location (e.g., at the bottom of the software's "Settings Information" page).

  The text content should be: "This project is developed based on JI,XIAOYONG's open-source project ApkSigner. You can obtain the project source code for free at <https://github.com/jixiaoyong/ApkSigner>."