pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        id("com.google.devtools.ksp").version(extra["ksp.version"] as String)
    }
}

rootProject.name = "ApkSigner"
