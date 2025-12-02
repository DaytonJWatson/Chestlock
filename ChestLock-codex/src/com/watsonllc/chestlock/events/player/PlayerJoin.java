package com.watsonllc.chestlock.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.UpdateChecker;
import com.watsonllc.chestlock.Utils;

public class PlayerJoin implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!Main.checkForUpdates)
			return;

		Player player = event.getPlayer();

		if (!player.hasPermission("chestlock.updatechecker"))
			return;
		
		try {
			UpdateChecker updater = new UpdateChecker(Main.instance, 81204);
			if (updater.checkForUpdates()) {
				String currentVersion = Main.instance.getDescription().getVersion();
				String newVersion = updater.getNewVersion();
				player.sendMessage(Utils.color("&8[&6ChestLock&7-&aUpdate&8] &a"+ currentVersion +" > "+ newVersion 
						+ "&7 Download the latest version from &7https://www.spigotmc.org/resources/chestlock.81204/"));
			} else
				return;
		} catch (Exception e1) {
			return;
		}
	}
}