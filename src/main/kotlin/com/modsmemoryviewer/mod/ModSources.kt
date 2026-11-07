package com.modsmemoryviewer.mod

import com.modsmemoryviewer.mod.report.ModFile

/** Each loader fills this before a screen can open. An empty list means the histogram stays unattributed. */
object ModSources {
    @Volatile
    var provider: () -> List<ModFile> = { emptyList() }
}
