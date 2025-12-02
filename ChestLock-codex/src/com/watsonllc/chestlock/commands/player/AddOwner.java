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

public class AddOwner {
	private static HashMap<Player, Boolean> addToggle = new HashMap<>();
	
	public static boolean logic(Player player, String target, boolean toggle) {
		addToggle.put(player, toggle);
		
		if(Commands.usePermissions()) {
			if(!player.hasPermission("chestlock.add")) {
				player.sendMessage(Config.getString("messages.noPermission"));
				return false;
			}
		}
		
		String targetPlayer = target;
		
		if(Main.addingOwner.containsKey(player)) {
			String actionMSG = Config.getString("messages.cancelAction");
			actionMSG = actionMSG.replace("%action%", Config.getString("actions.addOwner"));
			player.sendMessage(actionMSG);
			Main.addingOwner.remove(player);
			addToggle.remove(player);
			return false;
		}
		
		Main.addingOwner.put(player, targetPlayer);
		String shareTipMSG = Config.getString("messages.shareLockTip");
		shareTipMSG = shareTipMSG.replace("%target%", Main.addingOwner.get(player));
		player.sendMessage(shareTipMSG);
        
        commandTimeout(player);
        
		return true;
	}
	
	private static void commandTimeout(Player player) {
		new BukkitRunnable() {
            @Override
            public void run() {
            	if(!Main.addingOwner.containsKey(player))
            		return;
            	
                Main.addingOwner.remove(player);
                addToggle.remove(player);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", Config.getString("actions.addOwner"));
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, 20*15);
	}
	
	public static void eventChecker(PlayerInteractEvent event) {
		if(!Main.addingOwner.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if(!Utils.lockableBlock(event.getClickedBlock())) {
			player.sendMessage(Config.getString("messages.invalidLock"));
			Main.addingOwner.remove(player);
			addToggle.remove(player);
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
		if(lc.getOwner(blockLocation).equals(player.getName()) || Main.bypassLocks.containsKey(player)) {
			event.setCancelled(true);
			lc.addOwner(player, Main.addingOwner.get(player), blockLocation);
			String shareMSG = Config.getString("messages.shareLock");
			shareMSG = shareMSG.replace("%target%", Main.addingOwner.get(player));
			player.sendMessage(shareMSG);
			
			if(addToggle.get(player))
				return;
			
			Main.addingOwner.remove(player);
			addToggle.remove(player);
			return;
		}
		
		String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
		player.sendMessage(invalidOwnerMSG);
		Main.addingOwner.remove(player);
		addToggle.remove(player);
		return;
	}
}