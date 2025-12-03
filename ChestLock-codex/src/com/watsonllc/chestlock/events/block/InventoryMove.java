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
import com.watsonllc.chestlock.logic.HopperOwnerData;
import com.watsonllc.chestlock.logic.LockController;

public class InventoryMove implements Listener {
        private LockController lc = new LockController();

        @EventHandler
        public void onInventoryMove(InventoryMoveItemEvent event) {
                if(!isHopperInventory(event.getInitiator()))
                        return;

                LockInfo sourceInfo = getInventoryLockInfo(event.getSource());
                LockInfo destinationInfo = getInventoryLockInfo(event.getDestination());

                if(!sourceInfo.hasProtectedLock && !destinationInfo.hasProtectedLock)
                        return;

                if(sourceInfo.conflictingOwners || destinationInfo.conflictingOwners) {
                        event.setCancelled(true);
                        return;
                }

                InventoryHolder initiatorHolder = event.getInitiator().getHolder();
                String hopperOwner = null;

                if(initiatorHolder instanceof Hopper) {
                        hopperOwner = HopperOwnerData.getOwner((Hopper) initiatorHolder);
                }

                if(hopperOwner == null && initiatorHolder instanceof HopperMinecart) {
                        hopperOwner = HopperOwnerData.getOwner((HopperMinecart) initiatorHolder);
                }

                if(hopperOwner == null) {
                        event.setCancelled(true);
                        return;
                }

                if((sourceInfo.hasProtectedLock && !sourceInfo.allowedPlayers.contains(hopperOwner)) ||
                                (destinationInfo.hasProtectedLock && !destinationInfo.allowedPlayers.contains(hopperOwner))) {
                        event.setCancelled(true);
                }
        }

        private boolean isHopperInventory(Inventory inventory) {
                InventoryHolder holder = inventory.getHolder();

                return holder instanceof Hopper || holder instanceof HopperMinecart;
        }

        private LockInfo getInventoryLockInfo(Inventory inventory) {
                LockInfo info = new LockInfo();

                List<Location> locations = getInventoryLocations(inventory.getHolder());

                for (Location location : locations) {
                        if(location == null)
                                continue;

                        if(lc.naturalBlock(location))
                                continue;

                        if(!Utils.lockableBlock(location.getBlock()) || lc.isPublic(location))
                                continue;

                        info.hasProtectedLock = true;

                        List<String> allowedPlayers = lc.getAllAccessors(location);

                        if(info.allowedPlayers.isEmpty()) {
                                info.allowedPlayers = new ArrayList<>(allowedPlayers);
                                continue;
                        }

                        if(!allowedPlayers.isEmpty() && !info.allowedPlayers.equals(allowedPlayers))
                                info.conflictingOwners = true;
                }

                return info;
        }

        private List<Location> getInventoryLocations(InventoryHolder holder) {
                List<Location> locations = new ArrayList<>();

                if(holder instanceof DoubleChest) {
                        DoubleChest doubleChest = (DoubleChest) holder;
                        addHolderLocation(doubleChest.getLeftSide(), locations);
                        addHolderLocation(doubleChest.getRightSide(), locations);
                        return locations;
                }

                addHolderLocation(holder, locations);

                return locations;
        }

        private void addHolderLocation(InventoryHolder holder, List<Location> locations) {
                if(holder instanceof BlockState) {
                        locations.add(((BlockState) holder).getLocation());
                }

                if(holder instanceof HopperMinecart) {
                        locations.add(((HopperMinecart) holder).getLocation());
                }
        }

        private static class LockInfo {
                private boolean hasProtectedLock;
                private boolean conflictingOwners;
                private List<String> allowedPlayers = new ArrayList<>();
        }
}
