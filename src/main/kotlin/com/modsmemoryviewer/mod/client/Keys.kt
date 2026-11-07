package com.modsmemoryviewer.mod.client

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

object Keys {
    /** Unbound until the player assigns it, so it does not take a vanilla key. */
    val OPEN: KeyMapping = KeyMapping(
        "key.modsmemoryviewer.open",
        InputConstants.UNKNOWN.value,
        "key.categories.modsmemoryviewer",
    )
}
