package com.modsmemoryviewer.mod.loader.fabric

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import org.slf4j.LoggerFactory

class FabricClient : ClientModInitializer {
    private var openedFromProperty = false

    override fun onInitializeClient() {
        ModSources.provider = { FabricModFiles.list() }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            openFromProperty(client)
        }
        LOGGER.info("Mods Memory Viewer client ready. Open the table from this mod's settings in the Mods button.")
    }

    private fun openFromProperty(client: Minecraft) {
        if (openedFromProperty || System.getProperty("modsmemoryviewer.open") != "true") {
            return
        }
        if (client.screen !is TitleScreen) {
            return
        }
        openedFromProperty = true
        client.setScreen(MemoryViewerModMenu().getModConfigScreenFactory().create(client.screen))
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)
    }
}
