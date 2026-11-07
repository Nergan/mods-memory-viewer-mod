package com.modsmemoryviewer.mod.report

import java.nio.file.Path

data class ModFile(
    val id: String,
    val label: String,
    val paths: List<Path>,
)

data class ClassStat(
    val className: String,
    val instances: Long,
    val bytes: Long,
)

data class ModUsage(
    val id: String,
    val label: String,
    val instances: Long,
    val bytes: Long,
)

data class HeapSnapshot(
    val used: Long,
    val committed: Long,
    val max: Long,
)

data class MemoryReport(
    val heap: HeapSnapshot,
    val rows: List<ModUsage>,
    val histogramBytes: Long,
    val error: String? = null,
) {
    companion object {
        const val MINECRAFT = "minecraft-and-libraries"
    }
}
