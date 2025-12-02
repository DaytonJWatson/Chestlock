package com.watsonllc.chestlock.commands.player;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;

public class DestroyLock {
	
	public static boolean logic(Player player, boolean toggle) {
		if(Commands.usePermissions()) {
			if(!player.hasPermission("chestlock.destroy")) {
				player.sendMessage(Config.getString("messages.noPermission"));
				return false;
			}
		}
		
		if(Main.destroyLock.containsKey(player)) {
			String actionMSG = Config.getString("messages.cancelAction");
			actionMSG = actionMSG.replace("%action%", Config.getString("actions.destroyLock"));
			player.sendMessage(actionMSG);
			Main.destroyLock.remove(player);
			return false;
		}
		
		Main.destroyLock.put(player, toggle);
		String destroyLockMSG = Config.getString("messages.destroyLockTip");
		player.sendMessage(destroyLockMSG);
		
		commandTimeout(player);
		
		return false;
	}
	
	private static void commandTimeout(Player player) {
		new BukkitRunnable() {
            @Override
            public void run() {
            	if(!Main.destroyLock.containsKey(player))
            		return;
            	
                Main.destroyLock.remove(player);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", Config.getString("actions.destroyLock"));
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, 20*15);
	}
	
	public static void eventChecker(PlayerInteractEvent event) {
		if(!Main.destroyLock.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		Location blockLocation = block.getLocation();
		
		event.setCancelled(true);
		
		LockController lc = new LockController();
		
		if(lc.naturalBlock(blockLocation)) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.destroyLock.remove(player);
			return;
		}
		
		// Check if the interacted block is a valid lock
		if(!Utils.lockableBlock(block)) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.destroyLock.remove(player);
			return;
		}
		
		// check if the player owns the lock or can bypass
		if(!lc.getOwner(blockLocation).equals(player.getName())) {
			String lockID = lc.getLockID(event.getClickedBlock().getLocation());
			String lockType = lc.getLockType(event.getClickedBlock().getLocation());
			String destroyedLockMSG = Config.getString("messages.destroyedLock");
			destroyedLockMSG = destroyedLockMSG.replace("%type%", lockType);
			player.sendMessage(destroyedLockMSG);
			lc.removeLock(lockID);
			
			if(Main.destroyLock.get(player))
				return;
			else
				Main.destroyLock.remove(player);
			return;
		}
		
		String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
		player.sendMessage(invalidOwnerMSG);
		Main.destroyLock.remove(player);
		return;
	}
}