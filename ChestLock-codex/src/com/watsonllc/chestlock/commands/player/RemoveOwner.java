package com.watsonllc.chestlock.commands.player;

import java.util.HashMap;

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

public class RemoveOwner {
	private static HashMap<Player, Boolean> removeToggle = new HashMap<>();
	
	public static boolean logic(Player player, String target, boolean toggle) {
		removeToggle.put(player, toggle);
		
		if(Commands.usePermissions()) {
			if(!player.hasPermission("chestlock.remove")) {
				player.sendMessage(Config.getString("messages.noPermission"));
				return false;
			}
		}
		
		String targetPlayer = target;
		
		if(Main.removingOwner.containsKey(player)) {
			String actionMSG = Config.getString("messages.cancelAction");
			actionMSG = actionMSG.replace("%action%", Config.getString("actions.removeOwner"));
			player.sendMessage(actionMSG);
			Main.removingOwner.remove(player);
			removeToggle.remove(player);
			return false;
		}
		
		Main.removingOwner.put(player, targetPlayer);
		String unshareLockTipMSG = Config.getString("messages.unshareLockTip");
		unshareLockTipMSG = unshareLockTipMSG.replace("%target%", targetPlayer);
		player.sendMessage(unshareLockTipMSG);
		
		commandTimeout(player);
		
		return false;
	}
	
	private static void commandTimeout(Player player) {
		new BukkitRunnable() {
            @Override
            public void run() {
            	if(!Main.removingOwner.containsKey(player))
            		return;
            	
                Main.removingOwner.remove(player);
                removeToggle.remove(player);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", Config.getString("actions.removeOwner"));
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, 20*15);
	}
	
	public static void eventChecker(PlayerInteractEvent event) {
		if(!Main.removingOwner.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if(!Utils.lockableBlock(event.getClickedBlock())) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			return;
		}
		
		LockController lc = new LockController();
		Location blockLocation = event.getClickedBlock().getLocation();
		
		if(lc.getAllowedPlayers(blockLocation) == null)
			return;
		
		// check if the player owns the lock or can bypass
		if(lc.getOwner(blockLocation).equals(player.getName()) || Main.bypassLocks.containsKey(player)) {
			event.setCancelled(true);
			// check if the target player owns the lock
			if(!lc.getAllowedPlayers(blockLocation).contains(Main.removingOwner.get(player))) {
				String invalidShareOwnerMSG = Config.getString("messages.invalidShareOwner");
				invalidShareOwnerMSG = invalidShareOwnerMSG.replace("%target%", Main.removingOwner.get(player));
				player.sendMessage(invalidShareOwnerMSG);
				Main.removingOwner.remove(player);
				removeToggle.remove(player);
				return;
			}
			
			lc.removeOwner(player, Main.removingOwner.get(player), blockLocation);
			String unshareLockMSG = Config.getString("messages.unshareLock");
			unshareLockMSG = unshareLockMSG.replace("%target%", Main.removingOwner.get(player));
			player.sendMessage(unshareLockMSG);
			
			if(lc.getAllowedPlayers(blockLocation).isEmpty()) {
				String lockID = lc.getLockID(event.getClickedBlock().getLocation());
				String lockType = lc.getLockType(event.getClickedBlock().getLocation());
				String destroyedLockMSG = Config.getString("messages.destroyedLock");
				destroyedLockMSG = destroyedLockMSG.replace("%type%", lockType);
				player.sendMessage(destroyedLockMSG);
				lc.removeLock(lockID);
			}
			
			if(removeToggle.get(player))
				return;
			
			Main.removingOwner.remove(player);
			removeToggle.remove(player);
			return;
		}
		
		String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
		invalidOwnerMSG = invalidOwnerMSG.replace("%target%", Main.removingOwner.get(player));
		player.sendMessage(invalidOwnerMSG);
		Main.removingOwner.remove(player);
		return;
	}

}