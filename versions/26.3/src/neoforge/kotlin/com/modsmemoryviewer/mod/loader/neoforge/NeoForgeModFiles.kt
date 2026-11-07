package com.modsmemoryviewer.mod.loader.neoforge

import com.modsmemoryviewer.mod.report.ModFile
import java.nio.file.Path
import net.neoforged.fml.ModList

object NeoForgeModFiles {
    fun list(): List<ModFile> {
        val seen = HashSet<String>()
        val files = ArrayList<ModFile>()
        for (info in ModList.get().mods) {
            val file = info.owningFile.file
            val path: Path = try {
                file.filePath
            } catch (_: RuntimeException) {
                continue
            }
            if (!seen.add(path.toAbsolutePath().normalize().toString())) continue
            val infos = file.modInfos
            val id = infos.joinToString("+") { it.modId }.ifEmpty { info.modId }
            val label = infos.joinToString(", ") { it.displayName }.ifEmpty { info.displayName }
            files.add(ModFile(id, label, listOf(path)))
        }
        return files
    }
}
