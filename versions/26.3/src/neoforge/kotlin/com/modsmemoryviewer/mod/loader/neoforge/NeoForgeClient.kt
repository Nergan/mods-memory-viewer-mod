package com.modsmemoryviewer.mod.loader.neoforge

import com.modsmemoryviewer.mod.client.Keys
import com.modsmemoryviewer.mod.client.MemoryScreen
import com.modsmemoryviewer.mod.client.MemoryViewerClient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.commands.Commands
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge

object NeoForgeClient {
    private var openedFromProperty = false

    fun init(modBus: IEventBus, container: ModContainer) {
        modBus.addListener { event: RegisterKeyMappingsEvent ->
            event.register(Keys.OPEN)
        }
        container.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _, parent -> MemoryScreen.measure(parent, "mods") },
        )
        val bus = NeoForge.EVENT_BUS
        bus.addListener { _: ClientTickEvent.Post ->
            val minecraft = Minecraft.getInstance()
            MemoryViewerClient.onTick(minecraft)
            openFromProperty(minecraft)
        }
        bus.addListener { event: RegisterClientCommandsEvent ->
            event.dispatcher.register(
                Commands.literal("modsmemory").executes {
                    val minecraft = Minecraft.getInstance()
                    val parent = minecraft.gui.screen()
                    minecraft.gui.setScreen(MemoryScreen.measure(parent, "command"))
                    1
                },
            )
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
