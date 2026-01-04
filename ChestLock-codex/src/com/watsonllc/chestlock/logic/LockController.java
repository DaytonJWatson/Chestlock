package com.watsonllc.chestlock.logic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.config.LocksData;

public class LockController {
        private static final GroupController groupController = new GroupController();
        private static final Map<BlockKey, LockEntry> locksByBlock = new ConcurrentHashMap<>();
        private static final Map<String, LockEntry> locksById = new ConcurrentHashMap<>();
        private static final Map<String, Set<String>> sharedMembersByOwner = new ConcurrentHashMap<>();

        public static void loadLocksFromDisk() {
                locksByBlock.clear();
                locksById.clear();
                sharedMembersByOwner.clear();
                HopperCache.invalidate();

                ConfigurationSection section = LocksData.getConfiguration().getConfigurationSection("Locks");

                if (section == null)
                        return;

                for (String lockId : section.getKeys(false)) {
                        String basePath = "Locks." + lockId;
                        String worldName = (String) LocksData.get(basePath + ".location.world");
                        Integer x = (Integer) LocksData.get(basePath + ".location.x");
                        Integer y = (Integer) LocksData.get(basePath + ".location.y");
                        Integer z = (Integer) LocksData.get(basePath + ".location.z");

                        if (worldName == null || x == null || y == null || z == null)
                                continue;

                        World world = Bukkit.getWorld(worldName);
                        if (world == null)
                                continue;

                        BlockKey blockKey = new BlockKey(world.getUID(), x, y, z);

                        @SuppressWarnings("unchecked")
                        List<String> allowedPlayersList = (List<String>) LocksData.get(basePath + ".allowed");
                        Set<String> allowedPlayers = allowedPlayersList == null ? new LinkedHashSet<>() : new LinkedHashSet<>(allowedPlayersList);

                        boolean isPublic = Boolean.TRUE.equals(LocksData.get(basePath + ".public"));
                        boolean allowHoppers = Boolean.TRUE.equals(LocksData.get(basePath + ".allowHoppers"));
                        String owner = allowedPlayers.isEmpty() ? null : allowedPlayers.iterator().next();
                        String type = (String) LocksData.get(basePath + ".type");

                        LockEntry entry = new LockEntry(lockId, blockKey, owner, allowedPlayers, isPublic, allowHoppers, type);
                        indexLock(entry);
                }
        }

        private static void indexLock(LockEntry entry) {
                if (entry == null || entry.getBlockKey() == null || entry.getLockId() == null)
                        return;

                locksByBlock.put(entry.getBlockKey(), entry);
                locksById.put(entry.getLockId(), entry);
        }

        public static void invalidateSharedMembersCache(String owner) {
                if (owner == null) {
                        sharedMembersByOwner.clear();
                        return;
                }

                sharedMembersByOwner.remove(owner.toLowerCase());
        }

        private static Set<String> getCachedSharedMembers(String owner) {
                if (owner == null)
                        return Collections.emptySet();

                String normalizedOwner = owner.toLowerCase();
                return sharedMembersByOwner.computeIfAbsent(normalizedOwner, key -> groupController.getSharedMembers(owner));
        }

        public String getLockID(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? null : entry.getLockId();
        }

        public boolean naturalBlock(Location location) {
                return !locksByBlock.containsKey(BlockKey.fromLocation(location));
        }

        public List<String> getAllowedPlayers(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                if (entry == null)
                        return null;

                return new ArrayList<>(entry.getAllowedPlayers());
        }

        public String getOwner(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? null : entry.getOwner();
        }

        public boolean hasAccess(Location location, String playerName) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));

                if (entry == null)
                        return false;

                for (String allowed : entry.getAllowedPlayers()) {
                        if (allowed.equalsIgnoreCase(playerName))
                                return true;
                }

                return groupController.shareGroup(entry.getOwner(), playerName);
        }

        public List<String> getAllAccessors(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                Set<String> accessors = new LinkedHashSet<>();

                if (entry != null)
                        accessors.addAll(entry.getAllowedPlayers());

                if (entry != null && entry.getOwner() != null)
                        accessors.addAll(getCachedSharedMembers(entry.getOwner()));

                return new ArrayList<>(accessors);
        }

        public List<String> getLocks(Player player) {
                return new ArrayList<>(locksById.keySet());
        }

        public boolean isPublic(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry != null && entry.isPublic();
        }

        public void changePublicMode(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                if (entry == null)
                        return;

                String lockId = entry.getLockId();
                String path = "Locks." + lockId + ".public";
                boolean newValue = !entry.isPublic();
                LocksData.set(path, newValue);
                LocksData.save();

                LockEntry updated = entry.withPublic(newValue);
                indexLock(updated);
                HopperCache.invalidate();
        }

        public String getLockType(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? null : entry.getType();
        }

        public String getLockWorld(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                if (entry == null || entry.getBlockKey() == null)
                        return null;

                World world = Bukkit.getWorld(entry.getBlockKey().getWorldId());
                return world == null ? null : world.getName();
        }

        public int getLockX(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? 0 : entry.getBlockKey().getX();
        }

        public int getLockY(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? 0 : entry.getBlockKey().getY();
        }

        public int getLockZ(Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                return entry == null ? 0 : entry.getBlockKey().getZ();
        }

        public boolean getAlertMode(Location location) {
                String lockId = getLockID(location);
                if (lockId == null)
                        return false;

                Object alertMode = LocksData.get("Locks." + lockId + ".intrusionAlert");
                return alertMode instanceof Boolean && (boolean) alertMode;
        }

        public void changeAlertMode(Location location) {
                String lockId = getLockID(location);
                if (lockId == null)
                        return;

                boolean newMode = !getAlertMode(location);
                LocksData.set("Locks." + lockId + ".intrusionAlert", newMode);
                LocksData.save();
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

                BlockKey blockKey = new BlockKey(blockLoc.getWorld().getUID(), x, y, z);
                LockEntry entry = new LockEntry(lockID, blockKey, name, new LinkedHashSet<>(allowedUsers), false, false, type);
                indexLock(entry);
                HopperCache.invalidate();
        }

        public void removeLock(String lockID) {
                if (lockID == null)
                        return;

                LockEntry entry = locksById.remove(lockID);
                if (entry != null)
                        locksByBlock.remove(entry.getBlockKey());

                LocksData.set("Locks." + lockID, null);
                LocksData.save();
                HopperCache.invalidate();
        }

        public void moveLockLocation(String lockID, Location location) {
                if (lockID == null || location == null || location.getWorld() == null)
                        return;

                String path = "Locks." + lockID + ".location";
                LocksData.set(path + ".world", location.getWorld().getName());
                LocksData.set(path + ".x", location.getBlockX());
                LocksData.set(path + ".y", location.getBlockY());
                LocksData.set(path + ".z", location.getBlockZ());
                LocksData.save();

                LockEntry entry = locksById.get(lockID);
                BlockKey newKey = BlockKey.fromLocation(location);
                if (entry != null) {
                        locksByBlock.remove(entry.getBlockKey());
                        LockEntry updated = entry.withLocation(newKey);
                        indexLock(updated);
                }
                HopperCache.invalidate();
        }

        public void removeOwner(Player owner, String target, Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                if (entry == null)
                        return;

                Set<String> allowedPlayers = new LinkedHashSet<>(entry.getAllowedPlayers());

                if (allowedPlayers.contains(owner.getName())) {
                        allowedPlayers.remove(target);
                        persistAllowedPlayers(entry.getLockId(), allowedPlayers);
                        LockEntry updated = entry.withAllowedPlayers(allowedPlayers);
                        indexLock(updated);
                        HopperCache.invalidate();
                }
        }

        public void addOwner(Player owner, String target, Location location) {
                LockEntry entry = locksByBlock.get(BlockKey.fromLocation(location));
                if (entry == null)
                        return;

                Set<String> allowedPlayers = new LinkedHashSet<>(entry.getAllowedPlayers());

                if (allowedPlayers.contains(owner.getName())) {
                        if (allowedPlayers.contains(target))
                                return;
                        allowedPlayers.add(target);
                        persistAllowedPlayers(entry.getLockId(), allowedPlayers);
                        LockEntry updated = entry.withAllowedPlayers(allowedPlayers);
                        indexLock(updated);
                        HopperCache.invalidate();
                }
        }

        private void persistAllowedPlayers(String lockId, Set<String> allowedPlayers) {
                LocksData.set("Locks." + lockId + ".allowed", new ArrayList<>(allowedPlayers));
                LocksData.save();
        }

        private static final String CHARACTERS = Config.getString("settings.lockID-characters");
        private static final int LENGTH = Config.getInt("settings.lockID-size");
        private static final SecureRandom random = new SecureRandom();

        private String createLockID() {
                StringBuilder sb = new StringBuilder(LENGTH);
                for (int i = 0; i < LENGTH; i++) {
                        sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
                }
                return sb.toString();
        }

        public Map<BlockKey, LockEntry> getLocksByBlock() {
                return Collections.unmodifiableMap(locksByBlock);
        }
}
