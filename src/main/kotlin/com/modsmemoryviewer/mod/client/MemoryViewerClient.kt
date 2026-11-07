package com.modsmemoryviewer.mod.client

import com.modsmemoryviewer.mod.ModsMemoryViewer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import org.slf4j.LoggerFactory

object MemoryViewerClient {
    private var openedFromProperty = false
    private var loggedKey = false
    private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)

    fun onTick(minecraft: Minecraft) {
        if (!loggedKey) {
            loggedKey = true
            LOGGER.info("Mods Memory Viewer key unbound={}", Keys.OPEN.isUnbound)
        }
        var pressed = false
        while (Keys.OPEN.consumeClick()) {
            pressed = true
        }
        if (pressed && !Keys.OPEN.isUnbound && minecraft.screen !is MemoryScreen) {
            minecraft.setScreen(MemoryScreen.measure("key"))
        }
        if (!openedFromProperty && System.getProperty("modsmemoryviewer.open") == "true" && minecraft.screen is TitleScreen) {
            openedFromProperty = true
            minecraft.setScreen(MemoryScreen.measure("property"))
        }
    }
}
