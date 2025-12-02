package com.watsonllc.chestlock.events;

import org.bukkit.plugin.PluginManager;

import com.watsonllc.chestlock.ErrorBuild;
import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.events.block.BlockBreak;
import com.watsonllc.chestlock.events.block.BlockExplode;
import com.watsonllc.chestlock.events.block.InventoryMove;
import com.watsonllc.chestlock.events.block.BlockPlace;
import com.watsonllc.chestlock.events.block.EntityExplode;
import com.watsonllc.chestlock.events.player.InventoryClick;
import com.watsonllc.chestlock.events.player.PlayerInteract;
import com.watsonllc.chestlock.events.player.PlayerJoin;
import com.watsonllc.chestlock.logic.DebugAction;
import com.watsonllc.chestlock.logic.IntrusionAlert;

public class Events {
	private static PluginManager pm = Main.instance.getServer().getPluginManager();
	
	public static void invalidConfig() {
		pm.registerEvents(new ErrorBuild(), Main.instance);
	}
	
	public static void setup() {
                pm.registerEvents(new BlockBreak(), Main.instance);
                pm.registerEvents(new BlockExplode(), Main.instance);
                pm.registerEvents(new InventoryMove(), Main.instance);
                pm.registerEvents(new BlockPlace(), Main.instance);
                pm.registerEvents(new EntityExplode(), Main.instance);
                pm.registerEvents(new InventoryClick(), Main.instance);
		pm.registerEvents(new PlayerInteract(), Main.instance);
		pm.registerEvents(new PlayerJoin(), Main.instance);
		
		//pm.registerEvents(new AddOwner(), Main.instance);
		//pm.registerEvents(new MakePublic(), Main.instance);
		//pm.registerEvents(new RemoveOwner(), Main.instance);
		pm.registerEvents(new DebugAction(), Main.instance);
		pm.registerEvents(new IntrusionAlert(), Main.instance);
	}
}
