package com.watsonllc.chestlock.events.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.player.AddOwner;
import com.watsonllc.chestlock.commands.player.ClaimLock;
import com.watsonllc.chestlock.commands.player.DestroyLock;
import com.watsonllc.chestlock.commands.player.MakePublic;
import com.watsonllc.chestlock.commands.player.RemoveOwner;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.IntrusionAlert;
import com.watsonllc.chestlock.logic.LockController;

public class PlayerInteract implements Listener {
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		MakePublic.eventChecker(event);
		AddOwner.eventChecker(event);
		RemoveOwner.eventChecker(event);
		DestroyLock.eventChecker(event);
		ClaimLock.eventChecker(event);
		
		if(Main.bypassLocks.containsKey(event.getPlayer()))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		Location interactLocation = event.getClickedBlock().getLocation();
		
		LockController lc = new LockController();
		
		if(lc.naturalBlock(interactLocation))
			return;
		
		if(lc.isPublic(interactLocation))
			return;
		
		if(!Utils.lockableBlock(event.getClickedBlock()))
			return;
		
		if(!lc.getAllowedPlayers(interactLocation).contains(player.getName()))	{
			event.setCancelled(true);
			player.sendMessage(Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(interactLocation)));
		}
		
		IntrusionAlert.alert(lc, interactLocation, player);
	}
}