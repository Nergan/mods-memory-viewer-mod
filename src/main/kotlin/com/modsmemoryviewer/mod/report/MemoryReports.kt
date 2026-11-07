package com.modsmemoryviewer.mod.report

import java.lang.management.ManagementFactory

object MemoryReports {
    private var cachedIndex: Map<String, String>? = null
    private var cachedKey: Int? = null

    fun collect(mods: List<ModFile>): MemoryReport {
        val usage = ManagementFactory.getMemoryMXBean().heapMemoryUsage
        val heap = HeapSnapshot(usage.used, usage.committed, usage.max)
        return attribute(index(mods), mods, HistogramSource.read(), heap)
    }

    fun attribute(mods: List<ModFile>, histogram: String, heap: HeapSnapshot): MemoryReport =
        attribute(ClassIndex.build(mods), mods, histogram, heap)

    private fun index(mods: List<ModFile>): Map<String, String> {
        val key = mods.flatMap { mod -> listOf(mod.id) + mod.paths.map { it.toString() } }.hashCode()
        val cached = cachedIndex
        if (cached != null && cachedKey == key) return cached
        return ClassIndex.build(mods).also {
            cachedIndex = it
            cachedKey = key
        }
    }

    private fun attribute(
        index: Map<String, String>,
        mods: List<ModFile>,
        histogram: String,
        heap: HeapSnapshot,
    ): MemoryReport {
        val labels = LinkedHashMap<String, String>()
        for (mod in mods) labels.putIfAbsent(mod.id, mod.label)
        val totals = LinkedHashMap<String, LongArray>()
        var histogramBytes = 0L
        for (stat in HistogramParser.parse(histogram)) {
            histogramBytes += stat.bytes
            val id = ClassIndex.owner(stat.className, index) ?: MemoryReport.MINECRAFT
            val slot = totals.getOrPut(id) { longArrayOf(0L, 0L) }
            slot[0] += stat.bytes
            slot[1] += stat.instances
        }
        val rows = totals.map { (id, slot) ->
            ModUsage(id, labels[id] ?: id, slot[1], slot[0])
        }.sortedByDescending { it.bytes }
        return MemoryReport(heap, rows, histogramBytes)
    }
}
