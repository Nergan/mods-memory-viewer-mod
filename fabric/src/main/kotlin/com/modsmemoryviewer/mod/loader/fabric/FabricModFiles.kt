package com.modsmemoryviewer.mod.loader.fabric

import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.report.ModFile
import com.modsmemoryviewer.mod.report.NestedJar
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import org.slf4j.LoggerFactory

object FabricModFiles {
    private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)

    fun list(): List<ModFile> {
        val mods = FabricLoader.getInstance().allMods.toList()
        val byId = mods.associateBy { it.metadata.id }
        val files = ArrayList<ModFile>()
        var nested = 0
        for (mod in mods) {
            val file = fileFor(mod, byId) ?: continue
            if (file.nestedJars.isNotEmpty()) nested++
            files.add(file)
        }
        LOGGER.info("Mods Memory Viewer listed {} mods, {} from nested jars", files.size, nested)
        return files
    }

    private fun fileFor(mod: ModContainer, byId: Map<String, ModContainer>): ModFile? {
        val origin = mod.origin
        return when (origin.kind) {
            ModOrigin.Kind.PATH -> {
                val paths = origin.paths
                if (paths.isEmpty()) null else ModFile(mod.metadata.id, mod.metadata.name, paths)
            }
            ModOrigin.Kind.NESTED -> {
                val jars = nestedJars(origin, byId)
                if (jars.isEmpty()) null else ModFile(mod.metadata.id, mod.metadata.name, emptyList(), jars)
            }
            else -> null
        }
    }

    private fun nestedJars(origin: ModOrigin, byId: Map<String, ModContainer>): List<NestedJar> {
        val parent = byId[origin.parentModId] ?: return emptyList()
        val parentOrigin = parent.origin
        if (parentOrigin.kind != ModOrigin.Kind.PATH) return emptyList()
        val entry = origin.parentSubLocation
        if (entry.isEmpty()) return emptyList()
        return parentOrigin.paths.map { NestedJar(it, entry) }
    }
}
