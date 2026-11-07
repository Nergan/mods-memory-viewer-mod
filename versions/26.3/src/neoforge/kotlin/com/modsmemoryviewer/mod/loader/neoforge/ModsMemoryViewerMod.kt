package com.modsmemoryviewer.mod.loader.neoforge

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import org.slf4j.LoggerFactory

@Mod(ModsMemoryViewer.MOD_ID)
class ModsMemoryViewerMod(modBus: IEventBus, container: ModContainer) {
    init {
        ModSources.provider = { NeoForgeModFiles.list() }
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClient.init(modBus, container)
            LOGGER.info("Mods Memory Viewer client ready. Open it from the Mods button. The key is unbound until you set it.")
        } else {
            LOGGER.info("Mods Memory Viewer measures memory on the client. This dedicated server loaded the jar and does not open a screen.")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ModsMemoryViewer.MOD_ID)
    }
}
