package com.modsmemoryviewer.mod.client

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.report.HeapSnapshot
import com.modsmemoryviewer.mod.report.MemoryReport
import com.modsmemoryviewer.mod.report.MemoryReports
import com.modsmemoryviewer.mod.report.ModUsage
import com.mojang.blaze3d.platform.InputConstants
import java.util.Locale
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory

class MemoryScreen(
    private val parent: Screen?,
    private val report: MemoryReport,
) : Screen(Component.translatable("modsmemoryviewer.screen.title")) {
    private val rows = report.rows.sortedByDescending { it.bytes }
    private var scroll = 0

    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("modsmemoryviewer.screen.refresh")) { _ ->
                refresh()
            }.bounds(this.width / 2 - 50, this.height - 28, 100, 20).build(),
        )
    }

    override fun onClose() {
        minecraft?.gui?.setScreen(parent)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == InputConstants.KEY_R) {
            if (!refreshHeld) {
                refreshHeld = true
                refresh()
            }
            return true
        }
        return super.keyPressed(event)
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        if (event.key() == InputConstants.KEY_R) {
            refreshHeld = false
        }
        return super.keyReleased(event)
    }

    private fun refresh() {
        val now = System.nanoTime()
        if (now < nextRefreshAt) {
            return
        }
        minecraft?.gui?.setScreen(measure(parent, "refresh"))
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.extractTransparentBackground(graphics)
        graphics.centeredText(this.font, this.title, this.width / 2, 8, WHITE)
        graphics.centeredText(
            this.font,
            Component.translatable(
                "modsmemoryviewer.screen.heap",
                format(report.heap.used),
                format(report.heap.committed),
                format(report.heap.max),
            ),
            this.width / 2,
            22,
            WHITE,
        )
        val error = report.error
        if (error != null) {
            graphics.text(this.font, Component.translatable("modsmemoryviewer.screen.error", error), 12, HEADER_Y, WHITE)
            super.extractRenderState(graphics, mouseX, mouseY, partialTick)
            return
        }
        val nameX = 12
        val memoryRight = this.width - 72
        val percentRight = this.width - 12
        val nameWidth = (memoryRight - 64 - nameX).coerceAtLeast(40)
        graphics.fill(8, HEADER_Y - 2, this.width - 8, HEADER_Y + HEADER, HEADER_BG)
        drawCell(graphics, Component.translatable("modsmemoryviewer.screen.column.mod").string, nameX, HEADER_Y, nameWidth, false)
        drawRight(graphics, Component.translatable("modsmemoryviewer.screen.column.memory").string, memoryRight, HEADER_Y)
        drawRight(graphics, Component.translatable("modsmemoryviewer.screen.column.share").string, percentRight, HEADER_Y)
        val listTop = HEADER_Y + HEADER + 2
        val listBottom = this.height - 36
        graphics.enableScissor(0, listTop, this.width, listBottom)
        val total = rows.sumOf { it.bytes }.coerceAtLeast(1L)
        rows.forEachIndexed { index, row ->
            val y = listTop + index * ROW - scroll
            if (y + ROW < listTop || y > listBottom) {
                return@forEachIndexed
            }
            graphics.fill(8, y, this.width - 8, y + ROW, if (index % 2 == 0) ROW_BG else ROW_BG_ALT)
            drawCell(graphics, label(row), nameX, y + 2, nameWidth, true)
            drawRight(graphics, format(row.bytes), memoryRight, y + 2)
            drawRight(graphics, percent(row.bytes, total), percentRight, y + 2)
        }
        graphics.disableScissor()
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val view = (this.height - 36) - (HEADER_Y + HEADER + 2)
        val max = (rows.size * ROW - view).coerceAtLeast(0)
        scroll = (scroll - (scrollY * ROW).toInt()).coerceIn(0, max)
        return true
    }

    private fun label(row: ModUsage): String {
        if (row.id == MemoryReport.MINECRAFT) {
            return Component.translatable("modsmemoryviewer.row.minecraft").string
        }
        return row.label
    }

    private fun drawCell(graphics: GuiGraphicsExtractor, text: String, x: Int, y: Int, maxWidth: Int, trim: Boolean) {
        val shown = if (trim) fit(text, maxWidth) else text
        graphics.text(this.font, shown, x, y, WHITE)
    }

    private fun drawRight(graphics: GuiGraphicsExtractor, text: String, right: Int, y: Int) {
        graphics.text(this.font, text, right - this.font.width(text), y, WHITE)
    }

    private fun fit(text: String, maxWidth: Int): String {
        if (this.font.width(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && this.font.width(text.substring(0, end) + "...") > maxWidth) {
            end--
        }
        return text.substring(0, end) + "..."
    }

    companion object {
        private const val ROW = 12
        private const val HEADER = 14
        private const val HEADER_Y = 36
        private const val WHITE = -1
        private val HEADER_BG = 0xFF243044.toInt()
        private val ROW_BG = 0xFF1A222C.toInt()
        private val ROW_BG_ALT = 0xFF141A22.toInt()
        private const val REFRESH_GAP_NANOS = 2_000_000_000L
        private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)
        private var refreshHeld = false
        private var nextRefreshAt = 0L

        fun measure(parent: Screen?, reason: String): MemoryScreen {
            val report = try {
                MemoryReports.collect(ModSources.provider())
            } catch (error: Throwable) {
                MemoryReport(HeapSnapshot(0, 0, 0), emptyList(), 0, error.message ?: error.javaClass.name)
            }
            LOGGER.info(
                "Mods Memory Viewer measured {} rows and {} histogram bytes ({})",
                report.rows.size,
                report.histogramBytes,
                reason,
            )
            nextRefreshAt = System.nanoTime() + REFRESH_GAP_NANOS
            return MemoryScreen(parent, report)
        }

        private fun format(bytes: Long): String {
            if (bytes < 0) return "?"
            val mib = bytes / (1024.0 * 1024.0)
            if (mib >= 10.0) {
                return String.format(Locale.ROOT, "%.1f", mib)
            }
            return String.format(Locale.ROOT, "%.2f", mib)
        }

        private fun percent(bytes: Long, total: Long): String =
            String.format(Locale.ROOT, "%.1f%%", bytes * 100.0 / total)
    }
}
