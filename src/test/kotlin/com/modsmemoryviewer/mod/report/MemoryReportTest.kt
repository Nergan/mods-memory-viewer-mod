package com.modsmemoryviewer.mod.report

import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MemoryReportTest {
    @Test
    fun parsesHistogramLinesAndSkipsTheHeader() {
        val stats = HistogramParser.parse(HISTOGRAM)
        assertEquals(listOf("com.example.Foo", "[Lcom.example.Foo;", "com.example.Bar\$Inner", "com.example.Nested", "java.lang.String", "[I"), stats.map { it.className })
        assertEquals(100L, stats[0].bytes)
        assertEquals(2L, stats[0].instances)
    }

    @Test
    fun attributesClassesArraysInnersAndNestedJars() {
        val root = Files.createTempDirectory("memory-viewer")
        val classes = root.resolve("classes")
        Files.createDirectories(classes.resolve("com/example"))
        Files.write(classes.resolve("com/example/Foo.class"), byteArrayOf(0xCA.toByte(), 0xFE.toByte()))
        val jar = root.resolve("bar.jar")
        val nested = zipBytes("com/example/Nested.class" to byteArrayOf(1))
        ZipOutputStream(Files.newOutputStream(jar)).use { zip ->
            zip.putNextEntry(ZipEntry("com/example/Bar.class"))
            zip.write(byteArrayOf(2))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("com/example/Bar\$Inner.class"))
            zip.write(byteArrayOf(3))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("META-INF/jars/inner.jar"))
            zip.write(nested)
            zip.closeEntry()
        }
        val mods = listOf(
            ModFile("example", "Example", listOf(classes)),
            ModFile("bar", "Bar", listOf(jar)),
        )
        val report = MemoryReports.attribute(mods, HISTOGRAM, HeapSnapshot(10, 20, 30))
        val byId = report.rows.associateBy { it.id }
        assertEquals(150L, byId.getValue("example").bytes)
        assertEquals(5L, byId.getValue("example").instances)
        assertEquals(30L, byId.getValue("bar").bytes)
        assertEquals(48L, byId.getValue(MemoryReport.MINECRAFT).bytes)
        assertEquals(228L, report.histogramBytes)
        assertTrue(report.rows.first().bytes >= report.rows.last().bytes)
    }

    @Test
    fun liveHistogramIncludesJavaLangString() {
        val text = HistogramSource.read()
        val stats = HistogramParser.parse(text)
        val string = stats.find { it.className == "java.lang.String" || it.className.endsWith("/java.lang.String") }
        assertTrue(string != null && string.bytes > 0, text.lineSequence().take(8).joinToString("\n"))
    }

    private fun zipBytes(vararg entries: Pair<String, ByteArray>): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zip ->
            for ((name, bytes) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return buffer.toByteArray()
    }

    companion object {
        private val HISTOGRAM = """
            num     #instances         #bytes  class name (module)
            -------------------------------------------------------
               1:             2            100  com.example.Foo (app)
               2:             3             50  [Lcom.example.Foo;
               3:             1             10  com.example.Bar${'$'}Inner
               4:             4             20  com.example.Nested
               5:             4             40  java.lang.String (java.base@21.0.3)
               6:             1              8  [I
            Total             15            228
        """.trimIndent()
    }
}
