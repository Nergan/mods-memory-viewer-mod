package com.modsmemoryviewer.mod.client

import com.modsmemoryviewer.mod.ModsMemoryViewer
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier

object Keys {
    private val CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(ModsMemoryViewer.MOD_ID, "keys"),
    )

    /** Unbound until the player assigns it, so it does not take a vanilla key. */
    val OPEN: KeyMapping = KeyMapping(
        "key.modsmemoryviewer.open",
        InputConstants.UNKNOWN.value,
        CATEGORY,
    )
}
