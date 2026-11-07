# Mods Memory Viewer

Client mod for Minecraft 1.21.1, Fabric and NeoForge. It shows how much of the JVM heap belongs to classes that live in each mod jar.

This is an original mod. MemoryCatcher is All Rights Reserved and has no public source, so it is not ported or republished here.

The screen reads HotSpot's class histogram (`gcClassHistogram`) and adds up the bytes of live instances. A class counts toward a mod when that mod's jar, or a jar nested in `META-INF/jars`, contains the `.class` file. Classes that are not in any mod jar are listed as **Minecraft and libraries**. A generated class such as `Foo$$Lambda` is counted with `Foo` when that class is in a mod jar. The histogram is a pause: it runs when the screen opens and when you press R or Refresh, not every tick. Native memory and off-heap buffers are not included. The heap line above the list is the JVM heap (used, committed, max) and is a different number from the histogram total.

## How to open it

The key is unbound, so it does not take a vanilla key. Bind **Open Mods Memory Viewer** under Options → Controls → Mods Memory Viewer, or run the client command `/modsmemory`. A development launch can pass `-Dmodsmemoryviewer.open=true` to open the screen once on the title screen.

## Loaders

| Page | Required | Optional |
| --- | --- | --- |
| NeoForge 1.21.1 | `modsmemoryviewer-neoforge-1.21.1-1.0.0.jar`, Kotlin for Forge 5.8.0 | |
| Fabric 1.21.1 | `modsmemoryviewer-fabric-1.21.1-1.0.0.jar`, Fabric API `0.116.17+1.21.1`, Fabric Language Kotlin `1.13.2+kotlin.2.1.20` | |

Fabric marks the mod `environment: client`. A Fabric dedicated server does not load it. A NeoForge dedicated server can load the jar: it logs that measurement happens on the client and does not open a screen.

License: MPL-2.0. Source: https://github.com/Nergan/mods-memory-viewer-mod

This repository does not publish to McnCDN. CDN packages are published from the modpacks repository.
