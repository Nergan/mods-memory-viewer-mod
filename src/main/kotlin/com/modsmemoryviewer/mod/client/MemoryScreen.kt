package com.modsmemoryviewer.mod.client

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.report.HeapSnapshot
import com.modsmemoryviewer.mod.report.MemoryReport
import com.modsmemoryviewer.mod.report.MemoryReports
import com.modsmemoryviewer.mod.report.ModUsage
import java.util.Locale
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

class MemoryScreen(private val report: MemoryReport) : Screen(Component.translatable("modsmemoryviewer.screen.title")) {
    private var scroll = 0

    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("modsmemoryviewer.screen.refresh")) { _ ->
                refresh()
            }.bounds(this.width / 2 - 50, this.height - 28, 100, 20).build(),
        )
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

    private fun refresh() {
        val now = System.nanoTime()
        if (now < nextRefreshAt) {
            return
        }
        minecraft?.setScreen(measure("refresh"))
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_R) {
            refreshHeld = false
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        var y = 12 - scroll
        fun draw(component: Component) {
            if (y in 8 until this.height - 36) {
                graphics.drawString(this.font, component, 12, y, WHITE)
            }
            y += LINE
        }
        draw(
            Component.translatable(
                "modsmemoryviewer.screen.heap",
                format(report.heap.used),
                format(report.heap.committed),
                format(report.heap.max),
            ),
        )
        draw(Component.translatable("modsmemoryviewer.screen.total", format(report.histogramBytes)))
        val error = report.error
        if (error != null) {
            draw(Component.translatable("modsmemoryviewer.screen.error", error))
        }
        val total = report.rows.sumOf { it.bytes }.coerceAtLeast(1L)
        for (row in report.rows) {
            draw(rowLine(row, total))
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val max = (report.rows.size * LINE).coerceAtLeast(0)
        scroll = (scroll - (scrollY * LINE).toInt()).coerceIn(0, max)
        return true
    }

    private fun rowLine(row: ModUsage, total: Long): Component {
        val name = if (row.id == MemoryReport.MINECRAFT) {
            Component.translatable("modsmemoryviewer.row.minecraft")
        } else {
            Component.literal(row.label)
        }
        val percent = String.format(Locale.ROOT, "%.1f%%", row.bytes * 100.0 / total)
        return Component.translatable(
            "modsmemoryviewer.screen.row",
            name,
            format(row.bytes),
            percent,
            row.instances,
        )
    }

    companion object {
        private const val LINE = 11
        private const val WHITE = -1
        private const val REFRESH_GAP_NANOS = 2_000_000_000L
        private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)
        private var refreshHeld = false
        private var nextRefreshAt = 0L

        fun measure(reason: String = "command"): MemoryScreen {
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
            return MemoryScreen(report)
        }

        private fun format(bytes: Long): String {
            if (bytes < 0) return "?"
            return String.format(Locale.ROOT, "%.1f MiB", bytes / (1024.0 * 1024.0))
        }
    }
}
