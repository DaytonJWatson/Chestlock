package com.watsonllc.chestlock.commands.admin;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class Bypass {
        public static boolean logic(Player player) {
                if(!player.hasPermission("chestlock.bypass")) {
                        player.sendMessage(Config.getString("messages.noPermission"));
                        return false;
                }

                if (PlayerStateManager.isBypassing(player)) {
                        PlayerStateManager.setBypassing(player, false);
                        player.sendMessage(Config.getString("messages.bypassOff"));
                        return false;
                }

                PlayerStateManager.setBypassing(player, true);
                PlayerStateManager.resetBypassWarning(player);
                player.sendMessage(Config.getString("messages.bypassOn"));

                return false;
        }
}