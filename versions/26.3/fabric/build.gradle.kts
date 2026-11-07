import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy
import java.util.Properties
import org.gradle.api.tasks.Sync

plugins {
    id("net.fabricmc.fabric-loom") version "1.18.4"
    kotlin("jvm") version "2.4.0"
}

fun prop(name: String): String = project.property(name) as String

fun parentProp(name: String): String {
    val props = Properties()
    rootProject.file("../../gradle.properties").reader(Charsets.UTF_8).use { props.load(it) }
    return props.getProperty(name) ?: error("Missing $name in the 1.21.1 gradle.properties")
}

version = parentProp("mod_version")
group = parentProp("mod_group_id")

base {
    archivesName.set("modsmemoryviewer-fabric-${prop("minecraft_version")}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

val sharedKotlin by tasks.registering(Sync::class) {
    from(rootProject.file("../../src/main/kotlin")) {
        exclude("com/modsmemoryviewer/mod/client/**")
        exclude("com/modsmemoryviewer/mod/loader/**")
    }
    from(rootProject.file("../../fabric/src/main/kotlin")) {
        exclude("**/FabricClient.kt")
        exclude("**/MemoryViewerModMenu.kt")
    }
    from(rootProject.file("src/main/kotlin"))
    into(layout.buildDirectory.dir("shared-kotlin"))
}

kotlin {
    jvmToolchain(25)
    sourceSets.named("main") {
        kotlin.srcDir(sharedKotlin)
        kotlin.srcDir(file("src/main/kotlin"))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerExecutionStrategy.set(KotlinCompilerExecutionStrategy.IN_PROCESS)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

sourceSets.named("main") {
    resources.srcDir("src/main/resources")
    resources.srcDir(rootProject.file("src/main/resources"))
    resources.srcDir(rootProject.file("../../src/main/resources"))
}

repositories {
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${prop("fabric_kotlin_version")}")
    implementation("com.terraformersmc:modmenu:${prop("modmenu_version")}")
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching("fabric.mod.json") {
        filter { line -> line.replace("\${version}", version) }
    }
}

loom {
    runs {
        named("client") {
            vmArg("-Xms256m")
            vmArg("-Xmx768m")
            vmArg("-XX:+UseSerialGC")
            if (project.hasProperty("openMemory")) {
                vmArg("-Dmodsmemoryviewer.open=true")
            }
        }
        named("server") {
            programArg("--nogui")
            programArg("--port")
            programArg("25567")
            vmArg("-Xms128m")
            vmArg("-Xmx512m")
            vmArg("-XX:+UseSerialGC")
        }
    }
}
