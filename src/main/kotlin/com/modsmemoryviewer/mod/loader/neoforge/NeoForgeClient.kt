package com.modsmemoryviewer.mod.loader.neoforge

import com.modsmemoryviewer.mod.client.Keys
import com.modsmemoryviewer.mod.client.MemoryScreen
import com.modsmemoryviewer.mod.client.MemoryViewerClient
import net.minecraft.client.Minecraft
import net.minecraft.commands.Commands
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge

object NeoForgeClient {
    fun init(modBus: IEventBus) {
        modBus.addListener { event: RegisterKeyMappingsEvent ->
            event.register(Keys.OPEN)
        }
        val bus = NeoForge.EVENT_BUS
        bus.addListener { _: ClientTickEvent.Post ->
            MemoryViewerClient.onTick(Minecraft.getInstance())
        }
        bus.addListener { event: RegisterClientCommandsEvent ->
            event.dispatcher.register(
                Commands.literal("modsmemory").executes {
                    val minecraft = Minecraft.getInstance()
                    minecraft.execute { minecraft.setScreen(MemoryScreen.measure("command")) }
                    1
                },
            )
        }
    }
}
