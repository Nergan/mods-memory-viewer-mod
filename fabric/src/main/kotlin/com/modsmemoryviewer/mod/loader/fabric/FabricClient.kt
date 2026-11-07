package com.modsmemoryviewer.mod.loader.fabric

import com.modsmemoryviewer.mod.ModSources
import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.modsmemoryviewer.mod.client.Keys
import com.modsmemoryviewer.mod.client.MemoryScreen
import com.modsmemoryviewer.mod.client.MemoryViewerClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import org.slf4j.LoggerFactory

class FabricClient : ClientModInitializer {
    private var openedFromProperty = false

    override fun onInitializeClient() {
        ModSources.provider = { FabricModFiles.list() }
        KeyBindingHelper.registerKeyBinding(Keys.OPEN)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            MemoryViewerClient.onTick(client)
            openFromProperty(client)
        }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("modsmemory").executes {
                    val client = Minecraft.getInstance()
                    val parent = client.screen
                    client.execute { client.setScreen(MemoryScreen.measure(parent, "command")) }
                    1
                },
            )
        }
        LOGGER.info("Mods Memory Viewer client ready. Open it from the Mods button. The key is unbound until you set it.")
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
