package com.modsmemoryviewer.mod.report

import java.lang.management.ManagementFactory
import javax.management.ObjectName

/** HotSpot `gcClassHistogram`. It stops the JVM for a collection, so the screen calls it only on open and refresh. */
object HistogramSource {
    fun read(): String {
        val server = ManagementFactory.getPlatformMBeanServer()
        val result = server.invoke(
            ObjectName("com.sun.management:type=DiagnosticCommand"),
            "gcClassHistogram",
            arrayOf<Any>(emptyArray<String>()),
            arrayOf(String::class.java.arrayType().name),
        )
        return result as? String ?: result.toString()
    }
}
