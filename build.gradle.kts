import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "io.github.jixiaoyong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
}
val multiplatformSettings = "1.0.0"
dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("com.russhwolf:multiplatform-settings-no-arg:$multiplatformSettings")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.russhwolf:multiplatform-settings-serialization:$multiplatformSettings")
    implementation("com.russhwolf:multiplatform-settings-coroutines:$multiplatformSettings")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.6")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ApkSigner"
            packageVersion = "1.0.1"

            buildTypes.release {
                proguard {
                    configurationFiles.from("compose.desktop.pro")
                }
            }

            macOS {
                iconFile.set(project.file("src/main/resources/imgs/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/main/resources/imgs/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/imgs/icon.png"))
            }
        }
    }
}