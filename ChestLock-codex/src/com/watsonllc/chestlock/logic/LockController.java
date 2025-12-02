package com.watsonllc.chestlock.logic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.config.LocksData;

public class LockController {
	public String getLockID(Location location) {
		List<String> activeLocks = LocksData.retrieveSubSections("Locks");

		for (int i = 0; i < activeLocks.size(); i++) {
			String lockID = activeLocks.get(i);
			String path = "Locks." + lockID;
			String world = (String) LocksData.get(path + ".location.world");
			int x = (int) LocksData.get(path + ".location.x");
			int y = (int) LocksData.get(path + ".location.y");
			int z = (int) LocksData.get(path + ".location.z");
			Location loc = new Location(Bukkit.getWorld(world), x, y, z);
			if (location.equals(loc))
				return lockID;
		}
		return null;
	}

	public boolean naturalBlock(Location location) {
		List<String> activeLocks = LocksData.retrieveSubSections("Locks");
		List<Location> activeLocations = new ArrayList<>();

		for (int i = 0; i < activeLocks.size(); i++) {
			String lockID = activeLocks.get(i);
			String path = "Locks." + lockID;

			String world = (String) LocksData.get(path + ".location.world");
			int x = (int) LocksData.get(path + ".location.x");
			int y = (int) LocksData.get(path + ".location.y");
			int z = (int) LocksData.get(path + ".location.z");
			Location loc = new Location(Bukkit.getWorld(world), x, y, z);

			activeLocations.add(loc);
		}

		if (activeLocations.contains(location)) {
			activeLocations.clear();
			return false;
		} else {
			activeLocations.clear();
			return true;
		}

	}
	
	public List<String> getAllowedPlayers(Location location) {
		List<String> activeLocks = LocksData.retrieveSubSections("Locks");
		
		for (int i = 0; i < activeLocks.size(); i++) {
			String lockID = activeLocks.get(i);
			String path = "Locks." + lockID;

			String world = (String) LocksData.get(path + ".location.world");
			int x = (int) LocksData.get(path + ".location.x");
			int y = (int) LocksData.get(path + ".location.y");
			int z = (int) LocksData.get(path + ".location.z");
			Location loc = new Location(Bukkit.getWorld(world), x, y, z);

			@SuppressWarnings("unchecked")
			List<String> allowedPlayers = (List<String>) LocksData.get(path + ".allowed");

			if(location.equals(loc))
				return allowedPlayers;
		}
		
		return null;
	}

	public String getOwner(Location location){
		return getAllowedPlayers(location).get(0).toString();
	}

	public List<String> getLocks(Player player) {
		return LocksData.retrieveSubSections("Locks");
	}
	
	public boolean isPublic(Location location) {
		String LockID = getLockID(location);
		boolean isPublic = (boolean) LocksData.get("Locks." + LockID + ".public");
		
		return isPublic;
	}
	
	public void changePublicMode(Location location) {
		String LockID = getLockID(location);
		
		if(!isPublic(location)) {
			LocksData.set("Locks." + LockID + ".public", true);
			LocksData.save();
		} else {
			LocksData.set("Locks." + LockID + ".public", false);
			LocksData.save();
		}
	}
	
	public String getLockType(Location location) {
		String LockID = getLockID(location);
		String itemType = (String) LocksData.get("Locks." + LockID + ".type");
		
		return itemType;
	}
	
	public String getLockWorld(Location location) {
		String LockID = getLockID(location);
		String lockWorld = (String) LocksData.get("Locks." + LockID + ".world");
		
		return lockWorld;
	}
	
	public int getLockX(Location location) {
		String LockID = getLockID(location);
		int lockX = (int) LocksData.get("Locks." + LockID + ".x");
		
		return lockX;
	}
	
	public int getLockY(Location location) {
		String LockID = getLockID(location);
		int lockY = (int) LocksData.get("Locks." + LockID + ".y");
		
		return lockY;
	}
	
	public int getLockZ(Location location) {
		String LockID = getLockID(location);
		int lockZ = (int) LocksData.get("Locks." + LockID + ".z");
		
		return lockZ;
	}
	
	public boolean getAlertMode(Location location) {
		String LockID = getLockID(location);
		boolean alertMode = (boolean) LocksData.get("Locks." + LockID + ".intrusionAlert");
		
		return alertMode;
	}
	
	public void changeAlertMode(Location location) {
		String LockID = getLockID(location);
		
		if(getAlertMode(location) == false) {
			LocksData.set("Locks." + LockID + ".intrusionAlert", true);
			LocksData.save();
		} else {
			LocksData.set("Locks." + LockID + ".intrusionAlert", false);
			LocksData.save();
		}
	}

	public void createLock(Player player, Block block) {
		String lockID = createLockID();

		String name = player.getName();

		List<String> allowedUsers = new ArrayList<>();
		allowedUsers.add(name);

		String type = block.getType().toString();
		Location blockLoc = block.getLocation();
		String world = blockLoc.getWorld().getName();
		int x = blockLoc.getBlockX();
		int y = blockLoc.getBlockY();
		int z = blockLoc.getBlockZ();

		String path = "Locks." + lockID;

		LocksData.set(path + ".public", false);
		LocksData.set(path + ".allowed", allowedUsers);
		LocksData.set(path + ".type", type);
		LocksData.set(path + ".intrusionAlert", false);
		LocksData.set(path + ".location.world", world);
		LocksData.set(path + ".location.x", x);
		LocksData.set(path + ".location.y", y);
		LocksData.set(path + ".location.z", z);

		LocksData.save();
	}

	public void removeLock(String lockID) {
		LocksData.set("Locks." + lockID, null);
		LocksData.save();
	}
	
	public void removeOwner(Player owner, String target, Location location) {
		String LockID = getLockID(location);
		String path = "Locks." + LockID;
		
		if(getAllowedPlayers(location).contains(owner.getName())) {
			List<String> newOwners = getAllowedPlayers(location);
			newOwners.remove(target);
			LocksData.set(path + ".allowed", newOwners);
			LocksData.save();
		}
	}
	
	public void addOwner(Player owner, String target, Location location) {
		String LockID = getLockID(location);
		String path = "Locks." + LockID;
		
		if(getAllowedPlayers(location).contains(owner.getName())) {
			if(getAllowedPlayers(location).contains(target))
				return;
			List<String> newOwners = getAllowedPlayers(location);
			newOwners.add(target);
			LocksData.set(path + ".allowed", newOwners);
			LocksData.save();
		}
	}

	private static final String CHARACTERS = Config.getString("settings.lockID-characters");
	private static final int LENGTH = Config.getInt("settings.lockID-size");
	private static SecureRandom random = new SecureRandom();

	private String createLockID() {
		StringBuilder sb = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
		}
		return sb.toString();
	}
}