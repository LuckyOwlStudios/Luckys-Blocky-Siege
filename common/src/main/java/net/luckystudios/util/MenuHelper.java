package net.luckystudios.util;

import net.luckystudios.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;

public final class MenuHelper {
    private MenuHelper() {
    }

    /**
     * Opens one of the mod's menus and sends the block position to the client. Does nothing on the client side.
     */
    public static void openMenu(Player player, MenuProvider provider, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.openMenu(serverPlayer, provider, pos);
        }
    }
}
