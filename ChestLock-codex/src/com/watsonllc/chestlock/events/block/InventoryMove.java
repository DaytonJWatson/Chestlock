package com.watsonllc.chestlock.events.block;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.logic.LockController;

public class InventoryMove implements Listener {
        private LockController lc = new LockController();

        @EventHandler
        public void onInventoryMove(InventoryMoveItemEvent event) {
                if(!isHopperInventory(event.getInitiator()))
                        return;

                OwnerInfo initiatorInfo = getInventoryOwnerInfo(event.getInitiator());
                OwnerInfo sourceInfo = getInventoryOwnerInfo(event.getSource());
                OwnerInfo destinationInfo = getInventoryOwnerInfo(event.getDestination());

                if(!sourceInfo.hasProtectedLock && !destinationInfo.hasProtectedLock)
                        return;

                if(!initiatorInfo.hasProtectedLock || initiatorInfo.conflictingOwners ||
                                sourceInfo.conflictingOwners || destinationInfo.conflictingOwners) {
                        event.setCancelled(true);
                        return;
                }

                String owner = initiatorInfo.ownerName;

                if((sourceInfo.hasProtectedLock && !owner.equals(sourceInfo.ownerName)) ||
                                (destinationInfo.hasProtectedLock && !owner.equals(destinationInfo.ownerName))) {
                        event.setCancelled(true);
                }
        }

        private boolean isHopperInventory(Inventory inventory) {
                InventoryHolder holder = inventory.getHolder();

                return holder instanceof Hopper || holder instanceof HopperMinecart;
        }

        private OwnerInfo getInventoryOwnerInfo(Inventory inventory) {
                OwnerInfo info = new OwnerInfo();

                List<Location> locations = getInventoryLocations(inventory.getHolder());

                for (Location location : locations) {
                        if(location == null)
                                continue;

                        if(lc.naturalBlock(location))
                                continue;

                        if(!Utils.lockableBlock(location.getBlock()) || lc.isPublic(location))
                                continue;

                        info.hasProtectedLock = true;

                        if(info.ownerName == null) {
                                info.ownerName = lc.getOwner(location);
                                continue;
                        }

                        if(!info.ownerName.equals(lc.getOwner(location)))
                                info.conflictingOwners = true;
                }

                return info;
        }

        private List<Location> getInventoryLocations(InventoryHolder holder) {
                List<Location> locations = new ArrayList<>();

                if(holder instanceof DoubleChest) {
                        DoubleChest doubleChest = (DoubleChest) holder;
                        locations.add(doubleChest.getLeftSide().getLocation());
                        locations.add(doubleChest.getRightSide().getLocation());
                        return locations;
                }

                if(holder instanceof BlockState) {
                        locations.add(((BlockState) holder).getLocation());
                        return locations;
                }

                if(holder instanceof HopperMinecart) {
                        locations.add(((HopperMinecart) holder).getLocation());
                        return locations;
                }

                return locations;
        }

        private static class OwnerInfo {
                private boolean hasProtectedLock;
                private boolean conflictingOwners;
                private String ownerName;
        }
}
