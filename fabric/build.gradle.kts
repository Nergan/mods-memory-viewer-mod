import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy

plugins {
    id("fabric-loom") version "1.9.2"
    kotlin("jvm") version "2.1.20"
}

fun prop(name: String): String = project.property(name) as String

version = prop("mod_version")
group = prop("mod_group_id")

base {
    archivesName.set("${prop("mod_id")}-fabric-${prop("minecraft_version")}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

kotlin {
    jvmToolchain(21)
    sourceSets.named("main") {
        kotlin.srcDir(rootProject.file("src/main/kotlin"))
        kotlin.exclude("**/loader/neoforge/**")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerExecutionStrategy.set(KotlinCompilerExecutionStrategy.IN_PROCESS)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

sourceSets.named("main") {
    resources.srcDir(rootProject.file("src/main/resources"))
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${prop("fabric_kotlin_version")}")
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
