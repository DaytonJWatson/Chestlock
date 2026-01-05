package com.watsonllc.chestlock.events.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.BlockKey;
import com.watsonllc.chestlock.logic.HopperCache;
import com.watsonllc.chestlock.logic.HopperDecisionKey;
import com.watsonllc.chestlock.logic.HopperDecisionSimulator;
import com.watsonllc.chestlock.logic.HopperOwnerData;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.LockEntry;
import com.watsonllc.chestlock.logic.UntaggedHopperPolicy;

public class InventoryMove implements Listener {
        private static final LockController LOCK_CONTROLLER = new LockController();
        private static final AtomicLong handlerNanos = new AtomicLong();
        private static final AtomicLong lockLookupNanos = new AtomicLong();
        private static final AtomicLong groupLookupNanos = new AtomicLong();

        @EventHandler
        public void onInventoryMove(InventoryMoveItemEvent event) {
                long handlerStart = System.nanoTime();

                try {
                        if (!Config.getBoolean("settings.hopper-protection"))
                                return;

                        if (!isHopperInventory(event.getInitiator()))
                                return;

                        InventoryHolder initiatorHolder = event.getInitiator().getHolder();
                        if (initiatorHolder instanceof HopperMinecart && !Config.getBoolean("settings.hopper-minecart-protection"))
                                return;

                        boolean checkSource = Config.getBoolean("settings.hopper-check-source");
                        boolean checkDestination = Config.getBoolean("settings.hopper-check-destination");

                        if (!checkSource && !checkDestination)
                                return;

                        InventoryContext sourceContext = checkSource ? InventoryContext.fromInventory(event.getSource()) : InventoryContext.empty();
                        InventoryContext destinationContext = checkDestination ? InventoryContext.fromInventory(event.getDestination()) : InventoryContext.empty();

                        if (!sourceContext.hasLockableBlocks() && !destinationContext.hasLockableBlocks())
                                return;

                        Map<BlockKey, LockEntry> lockIndex = LOCK_CONTROLLER.getLocksByBlock();

                        if (!hasAnyLock(lockIndex, sourceContext.getBlockKeys()) && !hasAnyLock(lockIndex, destinationContext.getBlockKeys()))
                                return;

                        HopperDecisionKey decisionKey = new HopperDecisionKey(sourceContext.getBlockKeys(), destinationContext.getBlockKeys(), buildInitiatorId(initiatorHolder));

                        Boolean cachedDecision = HopperCache.get(decisionKey);
                        if (cachedDecision != null) {
                                if (!cachedDecision)
                                        event.setCancelled(true);
                                return;
                        }

                        long lockStart = System.nanoTime();
                        LockInfo sourceInfo = resolveLockInfo(sourceContext, lockIndex);
                        LockInfo destinationInfo = resolveLockInfo(destinationContext, lockIndex);
                        lockLookupNanos.addAndGet(System.nanoTime() - lockStart);

                        boolean hasProtectedLock = sourceInfo.hasProtectedLock || destinationInfo.hasProtectedLock;
                        UntaggedHopperPolicy policy = UntaggedHopperPolicy
                                        .fromConfig(Config.getStringRaw("settings.untagged-hoppers"));

                        HopperDecisionSimulator.DecisionResult decision = HopperDecisionSimulator.evaluate(policy,
                                        resolveHopperOwner(initiatorHolder), sourceInfo.hasProtectedLock,
                                        destinationInfo.hasProtectedLock,
                                        sourceInfo.conflictingOwners || destinationInfo.conflictingOwners,
                                        sourceInfo.getAllowedPlayers(), destinationInfo.getAllowedPlayers(),
                                        hasProtectedLock ? () -> tagHopperOnUse(initiatorHolder, sourceInfo,
                                                        destinationInfo) : null);

                        if (!decision.allowed)
                                event.setCancelled(true);

                        if (decision.tagged)
                                HopperCache.invalidate();

                        HopperCache.put(decisionKey, decision.allowed);
                } finally {
                        long elapsed = System.nanoTime() - handlerStart;
                        handlerNanos.addAndGet(elapsed);
                        // Timing counters retained for profiling but no console output to avoid spam
                        // handlerNanos accumulates total handler time
                        // lockLookupNanos accumulates lock map resolution time
                        // groupLookupNanos accumulates group/permissions time
                        // Remove or export as needed during profiling
                }
        }

        private boolean isHopperInventory(Inventory inventory) {
                InventoryHolder holder = inventory.getHolder();

                return holder instanceof Hopper || holder instanceof HopperMinecart;
        }

        private LockInfo resolveLockInfo(InventoryContext context, Map<BlockKey, LockEntry> lockIndex) {
                LockInfo info = new LockInfo();

                for (BlockKey key : context.getBlockKeys()) {
                        LockEntry entry = lockIndex.get(key);

                        if (entry == null)
                                continue;

                        if (entry.isAllowHoppers() || entry.isPublic())
                                continue;

                        Location location = locationFromKey(key);
                        if (location == null)
                                continue;

                        info.hasProtectedLock = true;

                        long accessStart = System.nanoTime();
                        List<String> allowedPlayers = LOCK_CONTROLLER.getAllAccessors(location);
                        groupLookupNanos.addAndGet(System.nanoTime() - accessStart);

                        Set<String> allowedLower = new LinkedHashSet<>();
                        for (String allowedPlayer : allowedPlayers) {
                                allowedLower.add(allowedPlayer.toLowerCase());
                        }

                        if (info.allowedPlayers.isEmpty()) {
                                info.allowedPlayers.addAll(allowedLower);
                                continue;
                        }

                        if (!allowedLower.isEmpty() && !info.allowedPlayers.equals(allowedLower))
                                info.conflictingOwners = true;
                }

                return info;
        }

        private boolean hasAnyLock(Map<BlockKey, LockEntry> lockIndex, List<BlockKey> keys) {
                for (BlockKey key : keys) {
                        if (lockIndex.containsKey(key))
                                return true;
                }

                return false;
        }

        private String resolveHopperOwner(InventoryHolder holder) {
                if (holder instanceof Hopper) {
                        return HopperOwnerData.getOwner((Hopper) holder);
                }

                if (holder instanceof HopperMinecart) {
                        return HopperOwnerData.getOwner((HopperMinecart) holder);
                }

                return null;
        }

        private Location locationFromKey(BlockKey key) {
                if (key == null)
                        return null;

                World world = Bukkit.getWorld(key.getWorldId());
                if (world == null)
                        return null;

                return new Location(world, key.getX(), key.getY(), key.getZ());
        }

        private String buildInitiatorId(InventoryHolder holder) {
                if (holder instanceof HopperMinecart) {
                        return "minecart:" + ((HopperMinecart) holder).getUniqueId().toString();
                }

                if (holder instanceof Hopper) {
                        BlockKey key = BlockKey.fromLocation(((Hopper) holder).getLocation());
                        return key == null ? "hopper:unknown" : "hopper:" + key.toString();
                }

                return holder == null ? "unknown" : holder.getClass().getSimpleName();
        }

        private String tagHopperOnUse(InventoryHolder holder, LockInfo sourceInfo, LockInfo destinationInfo) {
                String fallbackOwner = sourceInfo.getAnyAllowed();
                if (fallbackOwner == null)
                        fallbackOwner = destinationInfo.getAnyAllowed();
                if (fallbackOwner == null)
                        fallbackOwner = "SERVER";

                if (holder instanceof Hopper) {
                        HopperOwnerData.tagHopper(((Hopper) holder).getLocation().getBlock(), fallbackOwner);
                        return fallbackOwner;
                }

                if (holder instanceof HopperMinecart) {
                        HopperOwnerData.tagHopperMinecart((HopperMinecart) holder, fallbackOwner);
                        return fallbackOwner;
                }

                return fallbackOwner;
        }

        private static class InventoryContext {
                private final List<BlockKey> blockKeys;

                private InventoryContext(List<BlockKey> blockKeys) {
                        this.blockKeys = blockKeys;
                }

                public static InventoryContext fromInventory(Inventory inventory) {
                        if (inventory == null || inventory.getHolder() == null)
                                return new InventoryContext(Collections.emptyList());

                        List<BlockKey> keys = new ArrayList<>();
                        collectHolderKeys(inventory.getHolder(), keys);
                        return new InventoryContext(keys);
                }

                public static InventoryContext empty() {
                        return new InventoryContext(Collections.emptyList());
                }

                public List<BlockKey> getBlockKeys() {
                        return blockKeys;
                }

                public boolean hasLockableBlocks() {
                        return !blockKeys.isEmpty();
                }

                private static void collectHolderKeys(InventoryHolder holder, List<BlockKey> keys) {
                        if (holder instanceof DoubleChest) {
                                DoubleChest doubleChest = (DoubleChest) holder;
                                collectHolderKeys(doubleChest.getLeftSide(), keys);
                                collectHolderKeys(doubleChest.getRightSide(), keys);
                                return;
                        }

                        if (holder instanceof BlockState) {
                                Block block = ((BlockState) holder).getBlock();
                                if (Utils.lockableBlock(block)) {
                                        keys.add(BlockKey.fromLocation(((BlockState) holder).getLocation()));
                                }
                                return;
                        }
                }
        }

        private static class LockInfo {
                private boolean hasProtectedLock;
                private boolean conflictingOwners;
                private Set<String> allowedPlayers = new LinkedHashSet<>();

                private String getAnyAllowed() {
                        if (allowedPlayers.isEmpty())
                                return null;

                        return allowedPlayers.iterator().next();
                }

                private Set<String> getAllowedPlayers() {
                        return Collections.unmodifiableSet(allowedPlayers);
                }
        }
}
