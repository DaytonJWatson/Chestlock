package com.watsonllc.chestlock.events.block;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class BlockBreak implements Listener {
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		Location blockLocation = event.getBlock().getLocation();
		
		LockController lc = new LockController();
		
		if(lc.naturalBlock(blockLocation))
			return;
		
		if(!Utils.lockableBlock(block))
			return;
		
		if(lc.getOwner(blockLocation).equals(player.getName()) || PlayerStateManager.isBypassing(event.getPlayer()))	{
			player.sendMessage(Config.getString("messages.removeLock"));

			Set<String> lockIds = new LinkedHashSet<>();
			for (Block targetBlock : Utils.getConnectedChestBlocks(block)) {
				if (lc.naturalBlock(targetBlock.getLocation()))
					continue;
				String lockId = lc.getLockID(targetBlock.getLocation());
				if (lockId != null) {
					lockIds.add(lockId);
				}
			}

			for (String lockId : lockIds) {
				lc.removeLock(lockId);
			}
			return;
		} else {
			event.setCancelled(true);
			player.sendMessage(Config.getString("messages.invalidOwner").replace("%player%", lc.getAllowedPlayers(blockLocation).get(0)));
			return;
		}
	}
}
