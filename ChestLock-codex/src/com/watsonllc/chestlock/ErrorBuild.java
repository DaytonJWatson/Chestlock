package com.watsonllc.chestlock;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ErrorBuild implements Listener {

	private String errorMessage = Utils.color("&c ChestLock was disabled because of conflicting configuration files. "
			+ "Please delete your 'config.yml' found in your &7/path/to/server/plugins/ChestLock &cfolder and reload the plugin. "
			+ "If the issue persists, contact &funsocial6136 &con Discord.");
	
	private static HashMap<UUID, Boolean> gotWarning = new HashMap<>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if(!player.isOp() || !player.hasPermission("chestlock.bypass"))
			return;
		
		sendWarning(player, false);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if(!player.isOp() || !player.hasPermission("chestlock.bypass"))
			return;
		
		if (!Utils.lockableBlock(block))
			return;
		
		sendWarning(player, true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if(!player.isOp() || !player.hasPermission("chestlock.bypass"))
			return;
		
		if (!Utils.lockableBlock(block))
			return;
		
		sendWarning(player, true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(!player.isOp() || !player.hasPermission("chestlock.bypass"))
			return;
		
		if(block == null)
			return;
		
		if (!Utils.lockableBlock(block))
			return;
		
		sendWarning(player, true);
	}
	
	private void sendWarning(Player player, boolean warning) {
		if(warning)
			if(gotWarning.get(player.getUniqueId()) != null && gotWarning.get(player.getUniqueId()) == true)
				return;
		
		player.sendMessage(Utils.color("&8#####################################################"));
		player.sendMessage(Utils.color(""));
		player.sendMessage(Utils.color(errorMessage));
		player.sendMessage(Utils.color(""));
		player.sendMessage(Utils.color("&8#####################################################"));
		if(warning) {
			player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
			gotWarning.put(player.getUniqueId(), true);
		}
	}
}