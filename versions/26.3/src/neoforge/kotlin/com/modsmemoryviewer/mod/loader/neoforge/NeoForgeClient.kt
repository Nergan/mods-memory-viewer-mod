package com.modsmemoryviewer.mod.loader.neoforge

import com.modsmemoryviewer.mod.client.MemoryScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge

object NeoForgeClient {
    private var openedFromProperty = false

    fun init(modBus: IEventBus, container: ModContainer) {
        container.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _, parent -> MemoryScreen.measure(parent, "mods") },
        )
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            openFromProperty(Minecraft.getInstance())
        }
    }

    private fun openFromProperty(minecraft: Minecraft) {
        if (openedFromProperty || System.getProperty("modsmemoryviewer.open") != "true") {
            return
        }
        if (minecraft.gui.screen() !is TitleScreen) {
            return
        }
        openedFromProperty = true
        minecraft.gui.setScreen(MemoryScreen.measure(minecraft.gui.screen(), "mods"))
    }
}
