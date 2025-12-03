package com.watsonllc.chestlock.events.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.player.AddOwner;
import com.watsonllc.chestlock.commands.player.ClaimLock;
import com.watsonllc.chestlock.commands.player.DestroyLock;
import com.watsonllc.chestlock.commands.player.MakePublic;
import com.watsonllc.chestlock.commands.player.RemoveOwner;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.IntrusionAlert;
import com.watsonllc.chestlock.logic.HopperOwnerData;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class PlayerInteract implements Listener {
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
                trackHopperMinecartPlacement(event);

                MakePublic.eventChecker(event);
                AddOwner.eventChecker(event);
                RemoveOwner.eventChecker(event);
		DestroyLock.eventChecker(event);
		ClaimLock.eventChecker(event);
		
                if(PlayerStateManager.isBypassing(event.getPlayer()))
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
		
		if(!lc.hasAccess(interactLocation, player.getName()))	{
			event.setCancelled(true);
			player.sendMessage(Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(interactLocation)));
		}
		
                IntrusionAlert.alert(lc, interactLocation, player);
        }

        private void trackHopperMinecartPlacement(PlayerInteractEvent event) {
                if(event.getHand() != EquipmentSlot.HAND)
                        return;

                if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                if(event.getItem() == null || event.getItem().getType() != Material.HOPPER_MINECART)
                        return;

                Location targetLocation = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
                Player player = event.getPlayer();

                Bukkit.getScheduler().runTaskLater(com.watsonllc.chestlock.Main.instance, () -> {
                        HopperMinecart untagged = null;

                        for(org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(targetLocation, 0.75, 0.75, 0.75)) {
                                if(!(entity instanceof HopperMinecart))
                                        continue;

                                HopperMinecart minecart = (HopperMinecart) entity;

                                if(HopperOwnerData.getOwner(minecart) == null) {
                                        untagged = minecart;
                                        break;
                                }
                        }

                        if(untagged != null) {
                                HopperOwnerData.tagHopperMinecart(untagged, player.getName());
                        }
                }, 1L);
        }
}