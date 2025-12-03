package com.watsonllc.chestlock.logic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;

public class IntrusionAlert implements Listener {
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
                if (PlayerStateManager.isBypassing(event.getPlayer()))
                        return;
		
		if(event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if(!player.isSneaking())
			return;
		
		if(player.getInventory().getItemInMainHand().getType() != Material.REDSTONE)
			return;
		
		Location interactLocation = event.getClickedBlock().getLocation();

		LockController lc = new LockController();
		
		if(lc.naturalBlock(interactLocation))
			return;
		
		if(!lc.hasAccess(interactLocation, player.getName()))
			return;
		
		if(!Utils.lockableBlock(event.getClickedBlock()))
			return;
		
		String changeAlertMSG = Config.getString("messages.changeAlertMode");
		changeAlertMSG = changeAlertMSG.replace("%type%", lc.getLockType(interactLocation));
		changeAlertMSG = changeAlertMSG.replace("%mode%", String.valueOf(lc.getAlertMode(interactLocation)));
		player.sendMessage(changeAlertMSG);
		event.setCancelled(true);
		
		lc.changeAlertMode(interactLocation);
		
		return;
	}
	
	public static void alert(LockController lc, Location location, Player player) {
		if (!Config.getBoolean("settings.intrusionAlerts"))
			return;
		
		if(lc.getAlertMode(location) != true)
			return;

                if (lc.hasAccess(location, player.getName()))
                        return;

                List<String> owners = lc.getAllowedPlayers(location);

                if (owners == null || owners.isEmpty())
                        return;

                for (int i = 0; i < owners.size(); i++) {
                        Player owner = Bukkit.getPlayer(owners.get(i));

                        if (owner == null)
                                continue;

                        if (isPlayerWithinRadius(owner, location, Config.getInt("settings.alertRadius")))
                                return;

			String intrusionAlertMSG = Config.getString("messages.intrusionAlert");
			intrusionAlertMSG = intrusionAlertMSG.replace("%player%", player.getName());
			intrusionAlertMSG = intrusionAlertMSG.replace("%type%", lc.getLockType(location));
			owner.sendMessage(intrusionAlertMSG);
		}
	}

	public static boolean isPlayerWithinRadius(Player player, Location center, double radius) {
		return player.getLocation().distanceSquared(center) <= radius * radius;
	}
}