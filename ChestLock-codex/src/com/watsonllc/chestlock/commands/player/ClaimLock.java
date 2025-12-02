package com.watsonllc.chestlock.commands.player;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;

public class ClaimLock {
	
	public static boolean logic(Player player, boolean toggle) {
		if(Commands.usePermissions()) {
			if(!player.hasPermission("chestlock.claim")) {
				player.sendMessage(Config.getString("messages.noPermission"));
				return false;
			}
		}
		
		if(Main.claimingLock.containsKey(player)) {
			String actionMSG = Config.getString("messages.cancelAction");
			actionMSG = actionMSG.replace("%action%", Config.getString("actions.claimLock"));
			player.sendMessage(actionMSG);
			Main.claimingLock.remove(player);
			return false;
		}
		
		Main.claimingLock.put(player, toggle);
		String claimLockTipMSG = Config.getString("messages.claimLockTip");
		player.sendMessage(claimLockTipMSG);
		
		commandTimeout(player);
		
		return false;
	}
	
	private static void commandTimeout(Player player) {
		new BukkitRunnable() {
            @Override
            public void run() {
            	if(!Main.claimingLock.containsKey(player))
            		return;
            	
                Main.claimingLock.remove(player);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", Config.getString("actions.claimLock"));
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, 20*15);
	}
	
	public static void eventChecker(PlayerInteractEvent event) {
		if(!Main.claimingLock.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		event.setCancelled(true);
		
		if(!Utils.lockableBlock(event.getClickedBlock())) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.claimingLock.remove(player);
			return;
		}
		
		LockController lc = new LockController();
		
		if(!lc.naturalBlock(event.getClickedBlock().getLocation())) {
			player.sendMessage(Config.getString("messages.invalidClaimType"));
			Main.claimingLock.remove(player);
			return;
		}
		
		lc.createLock(player, event.getClickedBlock());
		String lockType = lc.getLockType(event.getClickedBlock().getLocation());
		String claimLockMSG = Config.getString("messages.claimLock");
		claimLockMSG = claimLockMSG.replace("%type%", lockType);
		player.sendMessage(claimLockMSG);
		
		if(Main.claimingLock.get(player))
			return;
		else
			Main.claimingLock.remove(player);
		return;
	}
}
