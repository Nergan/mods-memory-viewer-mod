package com.modsmemoryviewer.mod.report

import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * Maps a binary class name (`com.example.Foo`) to the mod id whose jar contains that file.
 * Classes generated at runtime are not in any jar; [owner] still peels `$$` and `$` so a lambda
 * can land on the class it was compiled against.
 */
object ClassIndex {
    private const val MAX_NESTED_BYTES = 32L * 1024L * 1024L

    fun build(mods: List<ModFile>): Map<String, String> {
        val index = LinkedHashMap<String, String>()
        for (mod in mods.sortedBy { it.id }) {
            for (path in mod.paths) {
                read(path) { className -> index.putIfAbsent(className, mod.id) }
            }
        }
        return index
    }

    fun owner(histogramName: String, index: Map<String, String>): String? {
        var name = normalize(histogramName) ?: return null
        val generated = name.indexOf("$$")
        if (generated > 0) {
            name = name.substring(0, generated)
        }
        while (true) {
            index[name]?.let { return it }
            val dollar = name.lastIndexOf('$')
            if (dollar <= 0) return null
            name = name.substring(0, dollar)
        }
    }

    fun className(entry: String): String? {
        if (!entry.endsWith(".class") || entry.startsWith("META-INF/")) return null
        val binary = entry.removeSuffix(".class")
        if (binary.isEmpty()) return null
        return binary.replace('/', '.')
    }

    internal fun normalize(raw: String): String? {
        var name = raw.trim()
        if (name.isEmpty()) return null
        if ('/' in name) {
            val after = name.substringAfterLast('/')
            if ('.' in after) name = after
        }
        var dimensions = 0
        while (name.startsWith("[")) {
            dimensions++
            name = name.substring(1)
        }
        if (dimensions > 0) {
            if (name.startsWith("L") && name.endsWith(";")) {
                name = name.substring(1, name.length - 1)
            } else {
                return null
            }
        }
        return name.replace('/', '.')
    }

    private fun read(path: Path, accept: (String) -> Unit) {
        if (!Files.exists(path)) return
        try {
            if (Files.isDirectory(path)) {
                Files.walk(path).use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".class") }.forEach { file ->
                        val relative = path.relativize(file).toString().replace('\\', '/')
                        className(relative)?.let(accept)
                    }
                }
                return
            }
            scanZip(path, accept)
        } catch (_: Exception) {
            // A mod file that is not a zip still leaves the rest of the report usable.
        }
    }

    private fun scanZip(path: Path, accept: (String) -> Unit) {
        ZipFile(path.toFile()).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                acceptEntry(entry.name, entry, accept) {
                    if (entry.size in 0..MAX_NESTED_BYTES) {
                        zip.getInputStream(entry).use { input -> scanBytes(input.readBytes(), accept) }
                    }
                }
            }
        }
    }

    private fun scanBytes(bytes: ByteArray, accept: (String) -> Unit) {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                className(entry.name)?.let(accept)
            }
        }
    }

    private fun acceptEntry(name: String, entry: ZipEntry, accept: (String) -> Unit, nested: () -> Unit) {
        className(name)?.let(accept)
        if (!entry.isDirectory && name.startsWith("META-INF/jars/") && name.endsWith(".jar")) {
            nested()
        }
    }
}
