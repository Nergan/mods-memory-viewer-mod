pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
        maven {
            name = "Kotlin for Forge"
            url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "modsmemoryviewer-26.3"
include("fabric")
