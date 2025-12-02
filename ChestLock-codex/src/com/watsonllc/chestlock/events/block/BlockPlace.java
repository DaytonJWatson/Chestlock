package com.watsonllc.chestlock.events.block;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;

public class BlockPlace implements Listener {
	
	public static final boolean autoLock = Config.getBoolean("settings.autoLock");
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
	    Block block = event.getBlock();
	    Player player = event.getPlayer();
	    LockController lc = new LockController();

	    if (!Utils.lockableBlock(block))
	        return;

	    // Don't allow placement if it's touching a lock and the player can't bypass
	    if (adjacentToLock(lc, event, player) && !Main.bypassLocks.containsKey(player)) {
	        return;
	    }

	    if (!autoLock)
	        return;

	    // If the player is bypassing lock controls, don't create a lock
	    if (Main.bypassLocks.containsKey(player)) {
	        if (!Main.bypassWarning.get(player)) {
	            player.sendMessage(Utils.color(Config.getString("messages.bypassWarning")));
	            Main.bypassWarning.replace(player, true);
	        }
	        return;
	    }

	    Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
	        Block placedBlock = block.getWorld().getBlockAt(block.getLocation());

	        // Ensure the block still exists AND is not AIR
	        if (placedBlock.getType() == block.getType() && placedBlock.getType() != Material.AIR) {
	            lc.createLock(player, block);
	            player.sendMessage(Config.getString("messages.createLock"));
	        }
	    }, 10L);
	}
	
	private boolean adjacentToLock(LockController lc, BlockPlaceEvent event, Player player) {
		Block placedBlock = event.getBlockPlaced();

		// Get the block faces
		BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
				BlockFace.UP, BlockFace.DOWN };

		for (BlockFace face : faces) {
			Block neighbor = placedBlock.getRelative(face);

			if (lc.naturalBlock(neighbor.getLocation()))
				continue;

			if (lc.getOwner(neighbor.getLocation()).equals(player.getName()) || Main.bypassLocks.containsKey(player))
				continue;

			if (!Utils.lockableBlock(placedBlock))
				continue;

			player.sendMessage(Config.getString("messages.adjacentToLock"));
			event.setCancelled(true);
			return true;
		}
		// allow the player to place the block
		return false;
	}
}
