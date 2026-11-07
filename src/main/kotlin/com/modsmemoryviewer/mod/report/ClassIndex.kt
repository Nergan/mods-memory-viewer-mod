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
        val claimed = HashSet<String>()
        for (mod in mods) {
            for (nested in mod.nestedJars) {
                claimed.add(claimKey(nested.container, nested.entry))
            }
        }
        for (mod in mods.sortedBy { it.id }) {
            for (nested in mod.nestedJars) {
                readNested(nested.container, nested.entry) { className ->
                    index.putIfAbsent(className, mod.id)
                }
            }
            for (path in mod.paths) {
                read(path, claimed) { className -> index.putIfAbsent(className, mod.id) }
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

    private fun read(path: Path, claimed: Set<String>, accept: (String) -> Unit) {
        if (!Files.exists(path)) return
        try {
            if (Files.isDirectory(path)) {
                Files.walk(path).use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".class") }.forEach { file ->
                        val relative = path.relativize(file).toString().replace('\\', '/')
                        className(relative)?.let(accept)
                    }
                }
                val jarsDir = path.resolve("META-INF").resolve("jars")
                if (Files.isDirectory(jarsDir)) {
                    Files.list(jarsDir).use { stream ->
                        stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }.forEach { jar ->
                            val entry = "META-INF/jars/${jar.fileName}"
                            if (claimKey(path, entry) !in claimed) {
                                scanZip(jar, emptySet(), accept, nestedAllowed = false)
                            }
                        }
                    }
                }
                return
            }
            scanZip(path, claimed, accept, nestedAllowed = true)
        } catch (_: Exception) {
            // A mod file that is not a zip still leaves the rest of the report usable.
        }
    }

    private fun readNested(container: Path, entryName: String, accept: (String) -> Unit) {
        val wanted = normalizeEntry(entryName)
        try {
            if (Files.isDirectory(container)) {
                val file = container.resolve(wanted)
                if (Files.isRegularFile(file)) {
                    scanZip(file, emptySet(), accept, nestedAllowed = false)
                }
                return
            }
            if (!Files.isRegularFile(container)) return
            ZipFile(container.toFile()).use { zip ->
                val entry = zip.getEntry(wanted) ?: return
                if (entry.size !in 0..MAX_NESTED_BYTES) return
                zip.getInputStream(entry).use { input -> scanBytes(input.readBytes(), accept) }
            }
        } catch (_: Exception) {
        }
    }

    private fun scanZip(path: Path, claimed: Set<String>, accept: (String) -> Unit, nestedAllowed: Boolean) {
        ZipFile(path.toFile()).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                acceptEntry(path, entry.name, entry, claimed, accept, nestedAllowed) {
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

    private fun acceptEntry(
        container: Path,
        name: String,
        entry: ZipEntry,
        claimed: Set<String>,
        accept: (String) -> Unit,
        nestedAllowed: Boolean,
        nested: () -> Unit,
    ) {
        val normalized = normalizeEntry(name)
        className(normalized)?.let(accept)
        if (!nestedAllowed || entry.isDirectory) return
        if (normalized.startsWith("META-INF/jars/") && normalized.endsWith(".jar") && claimKey(container, normalized) !in claimed) {
            nested()
        }
    }

    private fun claimKey(container: Path, entry: String): String =
        container.toAbsolutePath().normalize().toString() + "!" + normalizeEntry(entry)

    private fun normalizeEntry(name: String): String = name.replace('\\', '/').removePrefix("/")
}
