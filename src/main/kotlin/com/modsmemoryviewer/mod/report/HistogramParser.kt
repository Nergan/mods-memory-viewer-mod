package com.modsmemoryviewer.mod.report

object HistogramParser {
    private val LINE = Regex("""^\s*\d+:\s+(\d+)\s+(\d+)\s+(\S+)""")

    fun parse(histogram: String): List<ClassStat> {
        val stats = ArrayList<ClassStat>()
        for (line in histogram.lineSequence()) {
            val match = LINE.find(line) ?: continue
            val instances = match.groupValues[1].toLong()
            val bytes = match.groupValues[2].toLong()
            stats.add(ClassStat(match.groupValues[3], instances, bytes))
        }
        return stats
    }
}
