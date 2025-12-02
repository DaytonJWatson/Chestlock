package com.watsonllc.chestlock.commands.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;

public class MakePublic {

	public static boolean logic(Player player, boolean toggle) {
		if (Commands.usePermissions()) {
			if (!player.hasPermission("chestlock.public")) {
				player.sendMessage(Config.getString("messages.noPermission"));
				return false;
			}
		}

		if (Main.makingPublic.containsKey(player)) {
			String actionMSG = Config.getString("messages.cancelAction");
			actionMSG = actionMSG.replace("%action%", Config.getString("actions.makePublic"));
			player.sendMessage(actionMSG);
			Main.makingPublic.remove(player);
			return false;
		}

		Main.makingPublic.put(player, toggle);
		String makePublicTip = Config.getString("messages.makePublicTip");
		player.sendMessage(makePublicTip);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!Main.makingPublic.containsKey(player))
					return;

				Main.makingPublic.remove(player);
				String commandTimeout = Config.getString("messages.commandTimeout");
				commandTimeout = commandTimeout.replace("%action%", Config.getString("actions.makePublic"));
				player.sendMessage(commandTimeout);
			}
		}.runTaskLater(Main.instance, 20 * 15);

		return true;
	}

	public static void eventChecker(PlayerInteractEvent event) {
		if (!Main.makingPublic.containsKey(event.getPlayer()))
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();

		event.setCancelled(true);

		// Check if the interacted block is a valid lock
		if (!Utils.lockableBlock(event.getClickedBlock())) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.makingPublic.remove(player);
			return;
		}

		LockController lc = new LockController();
		Location blockLocation = event.getClickedBlock().getLocation();

		if (lc.naturalBlock(event.getClickedBlock().getLocation())) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.makingPublic.remove(player);
			return;
		}

		// check if the player owns the lock or can bypass
		if (lc.getOwner(blockLocation).equals(player.getName()) || Main.bypassLocks.containsKey(player)) {
			if (!lc.isPublic(event.getClickedBlock().getLocation())) {
				String madePublicMSG = Config.getString("messages.makePublic");
				player.sendMessage(madePublicMSG);
			} else {
				String makePrivateMSG = Config.getString("messages.makePrivate");
				player.sendMessage(makePrivateMSG);
			}

			lc.changePublicMode(event.getClickedBlock().getLocation());
			if (Main.makingPublic.get(player))
				return;
			else
				Main.makingPublic.remove(player);
			return;
		}

		String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%",lc.getOwner(blockLocation));
		player.sendMessage(invalidOwnerMSG);
		Main.makingPublic.remove(player);
		return;
	}
}