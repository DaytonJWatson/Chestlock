package com.watsonllc.chestlock.commands.admin;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.config.Config;

public class Bypass {
	public static boolean logic(Player player) {
		if(!player.hasPermission("chestlock.bypass")) {
			player.sendMessage(Config.getString("messages.noPermission"));
			return false;
		}

		if (Main.bypassLocks.containsKey(player)) {
			Main.bypassLocks.remove(player);
			Main.bypassWarning.remove(player);
			player.sendMessage(Config.getString("messages.bypassOff"));
			return false;
		}

		Main.bypassLocks.put(player, true);
		Main.bypassWarning.put(player, false);
		player.sendMessage(Config.getString("messages.bypassOn"));

		return false;
	}
}