import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.ksp)
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

dependencies {
    implementation(compose.desktop.currentOs)

    implementation(libs.multisettings.noarg)
    implementation(libs.multisettings.serialization)
    implementation(libs.multisettings.coroutines)
    implementation(libs.gson)
    implementation(libs.jsystemThemeDetector)
    implementation(libs.ksp)
    // Required
    implementation(libs.lyricist.lib)
    // If you want to use @LyricistStrings to generate code for you
    ksp(libs.lyricist.processor)

    implementation(libs.composeIcons.fontAwesome)

    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.datastore.core.okio)
    implementation(libs.jetbrains.navigation.compose)
    implementation(libs.jetbrains.lifecycle.viewmodel)
    implementation(libs.sujanpoudel.multiplatform.paths)
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

// 将这些属性作为额外的属性添加到项目中
extra["packageName"] = compose.desktop.application.nativeDistributions.packageName
extra["packageVersion"] =  compose.desktop.application.nativeDistributions.packageVersion
apply(from = "versionInfo.gradle.kts")