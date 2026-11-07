package com.modsmemoryviewer.mod.report

/** Pixel math for the memory table scrollbar. Content and view sizes are in pixels. */
object TableScroll {
    fun maxScroll(content: Int, view: Int): Int = (content - view).coerceAtLeast(0)

    fun clamp(scroll: Int, content: Int, view: Int): Int = scroll.coerceIn(0, maxScroll(content, view))

    fun thumbHeight(view: Int, content: Int): Int {
        if (view <= 0) return 0
        if (content <= view) return view
        return (view.toLong() * view / content).toInt().coerceIn(12, view)
    }

    fun thumbTop(scroll: Int, view: Int, content: Int, trackTop: Int): Int {
        val thumb = thumbHeight(view, content)
        val travel = (view - thumb).coerceAtLeast(0)
        val max = maxScroll(content, view)
        val offset = if (max == 0 || travel == 0) 0 else (clamp(scroll, content, view).toLong() * travel / max).toInt()
        return trackTop + offset
    }

    fun scrollForThumbTop(thumbTop: Int, trackTop: Int, view: Int, content: Int): Int {
        val thumb = thumbHeight(view, content)
        val travel = (view - thumb).coerceAtLeast(1)
        val max = maxScroll(content, view)
        val pos = (thumbTop - trackTop).coerceIn(0, travel)
        if (max == 0) return 0
        return (pos.toLong() * max / travel).toInt().coerceIn(0, max)
    }
}
