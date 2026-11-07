Client mod for Minecraft 1.21.1 and 26.3. Open **Mods** on the main menu and choose this mod. The config screen is a table of how much heap each loaded mod is using right now: MiB, percent, heaviest first.

The number is the HotSpot class histogram summed by the mod jar that contains each class. A nested `META-INF/jars` entry belongs to the nested mod when that mod has its own id, and stays with the parent otherwise. Classes outside mod jars are the row **Minecraft and libraries**. Refresh runs the histogram again and can stall the game for a moment. This does not measure native memory.

The key starts unbound. `/modsmemory` opens the same screen. Fabric loads the mod on the client only and needs Mod Menu for the Mods button. On NeoForge the Mods button is built in. The same NeoForge jar can sit on a dedicated server and does nothing there.

This is an original mod under MPL-2.0. It is not a port of MemoryCatcher.
