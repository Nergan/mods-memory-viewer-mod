package com.modsmemoryviewer.mod.loader.fabric

import com.modsmemoryviewer.mod.report.ModFile
import net.fabricmc.loader.api.FabricLoader

object FabricModFiles {
    fun list(): List<ModFile> =
        FabricLoader.getInstance().allMods.map { mod ->
            ModFile(mod.metadata.id, mod.metadata.name, mod.origin.paths)
        }
}
