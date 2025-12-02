package com.watsonllc.chestlock.logic;

import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;

public class DebugAction implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Main.bypassLocks.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if(!player.isSneaking())
			return;
		
		if(player.getInventory().getItemInMainHand().getType() != Material.AIR)
			return;
		
		player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
		
		Location interactLocation = event.getClickedBlock().getLocation();

		LockController lc = new LockController();
		
		if(lc.naturalBlock(interactLocation))
			return;
		
		if(!Utils.lockableBlock(event.getClickedBlock()))
			return;
		
		String LockIDMSG = Config.getString("messages.getLockID");
		LockIDMSG = LockIDMSG.replace("%id%", lc.getLockID(interactLocation));
		
		String LockTypeMSG = Config.getString("messages.getLockType");
		LockTypeMSG = LockTypeMSG.replace("%type%", lc.getLockType(interactLocation));
		
		String LockPublicMSG = Config.getString("messages.getLockPublic");
		LockPublicMSG = LockPublicMSG.replace("%public%", String.valueOf(lc.isPublic(interactLocation)));
		
		String LockOwnersMSG = Config.getString("messages.getLockOwners");
		LockOwnersMSG = LockOwnersMSG.replace("%owners%", lc.getAllowedPlayers(interactLocation).stream().collect(Collectors.joining(", ")));

		player.sendMessage(LockIDMSG);
		player.sendMessage(LockTypeMSG);
		player.sendMessage(LockPublicMSG);
		player.sendMessage(LockOwnersMSG);
		
		if(player.getGameMode() == GameMode.CREATIVE)
			event.setCancelled(true);
		
		return;
	}
}
