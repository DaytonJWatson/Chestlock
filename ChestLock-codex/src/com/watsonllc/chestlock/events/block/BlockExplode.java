package com.watsonllc.chestlock.events.block;

import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;

public class BlockExplode implements Listener {
	
	@EventHandler
	public void onEntityExplode(BlockExplodeEvent event) {
		if(!Config.getBoolean("settings.tntProof"))
			return;
		
		// Get the list of blocks affected by the explosion
		Iterator<Block> iterator = event.blockList().iterator();

		// Iterate through the blocks
		while (iterator.hasNext()) {
			Block block1 = iterator.next();
			
			// Check if the block is lockable
			if(!Utils.lockableBlock(block1))
				continue;

			// Removes lockable blocks from explosion list
			iterator.remove();
		}
	}
}
