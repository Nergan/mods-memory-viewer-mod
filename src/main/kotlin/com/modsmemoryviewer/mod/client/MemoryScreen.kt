package com.modsmemoryviewer.mod.client

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.report.HeapSnapshot
import com.modsmemoryviewer.mod.report.MemoryReport
import com.modsmemoryviewer.mod.report.MemoryReports
import com.modsmemoryviewer.mod.report.ModUsage
import com.modsmemoryviewer.mod.report.TableScroll
import java.util.Locale
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

class MemoryScreen(
    private val parent: Screen?,
    private val report: MemoryReport,
) : Screen(Component.translatable("modsmemoryviewer.screen.title")) {
    private val rows = report.rows.sortedByDescending { it.bytes }
    private var scroll = 0
    private var dragging = false
    private var dragGrab = 0

    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("modsmemoryviewer.screen.close")) { _ ->
                onClose()
            }.bounds(this.width - 22, 4, 16, 16).build(),
        )
        addRenderableWidget(
            Button.builder(Component.translatable("modsmemoryviewer.screen.refresh")) { _ ->
                refresh()
            }.bounds(this.width / 2 - 50, this.height - 28, 100, 20).build(),
        )
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_R) {
            if (!refreshHeld) {
                refreshHeld = true
                refresh()
            }
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_R) {
            refreshHeld = false
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    private fun refresh() {
        val now = System.nanoTime()
        if (now < nextRefreshAt) {
            return
        }
        minecraft?.setScreen(measure(parent, "refresh"))
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, WHITE)
        graphics.drawCenteredString(
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
            graphics.drawString(
                this.font,
                Component.translatable("modsmemoryviewer.screen.error", error),
                12,
                HEADER_Y,
                WHITE,
            )
            return
        }
        val nameX = 12
        val memoryRight = percentRight() - 48
        val nameWidth = (memoryRight - 64 - nameX).coerceAtLeast(40)
        graphics.fill(8, HEADER_Y - 2, barX() - 2, HEADER_Y + HEADER, HEADER_BG)
        drawCell(graphics, Component.translatable("modsmemoryviewer.screen.column.mod").string, nameX, HEADER_Y, nameWidth, false)
        drawRight(graphics, Component.translatable("modsmemoryviewer.screen.column.memory").string, memoryRight, HEADER_Y)
        drawRight(graphics, Component.translatable("modsmemoryviewer.screen.column.share").string, percentRight(), HEADER_Y)
        val listTop = listTop()
        val listBottom = listBottom()
        graphics.enableScissor(0, listTop, this.width, listBottom)
        val total = rows.sumOf { it.bytes }.coerceAtLeast(1L)
        rows.forEachIndexed { index, row ->
            val y = listTop + index * ROW - scroll
            if (y + ROW < listTop || y > listBottom) {
                return@forEachIndexed
            }
            val background = if (index % 2 == 0) ROW_BG else ROW_BG_ALT
            graphics.fill(8, y, barX() - 2, y + ROW, background)
            drawCell(graphics, label(row), nameX, y + 2, nameWidth, true)
            drawRight(graphics, format(row.bytes), memoryRight, y + 2)
            drawRight(graphics, percent(row.bytes, total), percentRight(), y + 2)
        }
        graphics.disableScissor()
        drawScrollbar(graphics)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scrollBy(-(scrollY * ROW).toInt())
        return true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true
        }
        if (button != 0 || report.error != null || !hitBar(mouseX, mouseY)) {
            return false
        }
        val top = thumbTop()
        val height = thumbHeight()
        if (mouseY >= top && mouseY < top + height) {
            dragGrab = (mouseY - top).toInt()
        } else {
            dragGrab = height / 2
            scrollToThumb((mouseY - dragGrab).toInt())
        }
        dragging = true
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (dragging && button == 0) {
            scrollToThumb((mouseY - dragGrab).toInt())
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            dragging = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun drawScrollbar(graphics: GuiGraphics) {
        val top = listTop()
        val bottom = listBottom()
        graphics.fill(barX(), top, barX() + BAR_W, bottom, TRACK)
        val thumb = thumbTop()
        val color = if (dragging) THUMB_DRAG else THUMB
        graphics.fill(barX(), thumb, barX() + BAR_W, thumb + thumbHeight(), color)
    }

    private fun listTop(): Int = HEADER_Y + HEADER + 2

    private fun listBottom(): Int = this.height - 36

    private fun viewHeight(): Int = (listBottom() - listTop()).coerceAtLeast(1)

    private fun contentHeight(): Int = rows.size * ROW

    private fun barX(): Int = this.width - 8 - BAR_W

    private fun percentRight(): Int = barX() - 6

    private fun thumbTop(): Int = TableScroll.thumbTop(scroll, viewHeight(), contentHeight(), listTop())

    private fun thumbHeight(): Int = TableScroll.thumbHeight(viewHeight(), contentHeight())

    private fun hitBar(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= barX() && mouseX < barX() + BAR_W && mouseY >= listTop() && mouseY < listBottom()

    private fun scrollBy(delta: Int) {
        scroll = TableScroll.clamp(scroll + delta, contentHeight(), viewHeight())
    }

    private fun scrollToThumb(thumbTop: Int) {
        scroll = TableScroll.scrollForThumbTop(thumbTop, listTop(), viewHeight(), contentHeight())
    }

    private fun label(row: ModUsage): String {
        if (row.id == MemoryReport.MINECRAFT) {
            return Component.translatable("modsmemoryviewer.row.minecraft").string
        }
        return row.label
    }

    private fun drawCell(graphics: GuiGraphics, text: String, x: Int, y: Int, maxWidth: Int, trim: Boolean) {
        val shown = if (trim) fit(text, maxWidth) else text
        graphics.drawString(this.font, shown, x, y, WHITE)
    }

    private fun drawRight(graphics: GuiGraphics, text: String, right: Int, y: Int) {
        graphics.drawString(this.font, text, right - this.font.width(text), y, WHITE)
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
        private const val BAR_W = 6
        private const val WHITE = -1
        private val HEADER_BG = 0xFF243044.toInt()
        private val ROW_BG = 0xFF1A222C.toInt()
        private val ROW_BG_ALT = 0xFF141A22.toInt()
        private val TRACK = 0xFF0E141C.toInt()
        private val THUMB = 0xFF8AA0B8.toInt()
        private val THUMB_DRAG = 0xFFE7EEF6.toInt()
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
