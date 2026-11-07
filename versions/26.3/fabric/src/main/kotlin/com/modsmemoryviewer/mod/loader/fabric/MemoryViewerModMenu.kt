package com.modsmemoryviewer.mod.loader.fabric

import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.client.MemoryScreen
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import org.slf4j.LoggerFactory

class MemoryViewerModMenu : ModMenuApi {
    init {
        LOGGER.info("Mods Memory Viewer registered a Mods button config screen.")
    }

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> MemoryScreen.measure(parent, "mods") }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)
    }
}
