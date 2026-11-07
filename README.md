# Mods Memory Viewer

Client mod for Minecraft 1.21.1 and 26.3, Fabric and NeoForge. It shows how much of the JVM heap belongs to classes that live in each mod jar.

This is an original mod. MemoryCatcher is All Rights Reserved and has no public source, so it is not ported or republished here.

The screen reads HotSpot's class histogram (`gcClassHistogram`) and adds up the bytes of live instances. A class counts toward a mod when that mod's jar contains the `.class` file. A jar nested in `META-INF/jars` counts toward the nested mod when Fabric lists that jar as its own mod; otherwise it stays with the parent. Classes that are not in any mod jar are listed as **Minecraft and libraries**. A generated class such as `Foo$$Lambda` is counted with `Foo` when that class is in a mod jar. The histogram is a pause: it runs when the screen opens and when you press R or Refresh, not every tick. A scrollbar on the right follows the cursor. Native memory and off-heap buffers are not included. The heap line above the table is the JVM heap (used, committed, max) and is a different number from the sum of the rows.

## How to open it

On the main menu, open **Mods**, then this mod's config. That is the only way to open the table. The screen lists the mods loaded right now, heaviest first. Each row has the mod name, the memory in MiB, and that row's share of the measured total.

A cross at the top right closes the screen. Esc closes it too. Drag the scrollbar thumb, or click its track. The wheel still scrolls.

## Loaders

| Page | Required | Optional |
| --- | --- | --- |
| NeoForge 1.21.1 | `modsmemoryviewer-neoforge-1.21.1-1.0.3.jar`, Kotlin for Forge 5.8.0 | |
| Fabric 1.21.1 | `modsmemoryviewer-fabric-1.21.1-1.0.3.jar`, Fabric API `0.116.17+1.21.1`, Fabric Language Kotlin `1.13.2+kotlin.2.1.20`, Mod Menu 11.0.4 | |
| NeoForge 26.3 | `modsmemoryviewer-neoforge-26.3-1.0.3.jar`, Kotlin for Forge 6.4.0 | |
| Fabric 26.3 | `modsmemoryviewer-fabric-26.3-1.0.3.jar`, Fabric API `0.162.0+26.3`, Fabric Language Kotlin `1.14.2+kotlin.2.4.21`, Mod Menu 21.0.0 | |

Fabric marks the mod `environment: client`. A Fabric dedicated server does not load it. A NeoForge dedicated server can load the jar: it logs that measurement happens on the client and does not open a screen. NeoForge 21.1 has no client-only flag in `neoforge.mods.toml`; the Mods button is registered only when the game is a client.

License: MPL-2.0. Source: https://github.com/Nergan/mods-memory-viewer-mod

This repository does not publish to McnCDN. CDN packages are published from the modpacks repository.
