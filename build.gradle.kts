import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
}

group = "io.github.jixiaoyong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
}

ksp {
    arg("lyricist.generateStringsProperty", "true")

}

val multiplatformSettings = "1.1.1"
val lyricist = "1.6.2-1.8.20"
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
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
    // Required
    implementation("cafe.adriel.lyricist:lyricist:$lyricist")
    // If you want to use @LyricistStrings to generate code for you
    ksp("cafe.adriel.lyricist:lyricist-processor:$lyricist")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ApkSigner"
            packageVersion = "1.2.0"

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