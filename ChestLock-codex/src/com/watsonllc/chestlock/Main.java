package com.watsonllc.chestlock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.config.LocksData;
import com.watsonllc.chestlock.events.Events;
import com.watsonllc.chestlock.config.GroupsData;
import com.watsonllc.chestlock.logic.GroupController;
import com.watsonllc.chestlock.logic.HopperCache;
import com.watsonllc.chestlock.logic.LockController;

public class Main extends JavaPlugin {

        public static Main instance;

        public static boolean checkForUpdates;
	
	@Override
	public void onEnable() {
		instance = this;
        
		String currentVersion = this.getDescription().getVersion();
		Object currentConfigVersion = (Object) this.getConfig().get("configVersion");
                Object correctConfigVersion = "1.3.0";
		
		Config.setup();
		
                if(!correctConfigVersion.equals(currentConfigVersion) || currentConfigVersion == null) {
                        Events.invalidConfig();
                        getLogger().severe("#########################################################");
                        getLogger().severe("#        ChestLock found conflicting configurations     #");
                        getLogger().severe("#            You NEED to delete your config.yml         #");
                        getLogger().severe("#                 and GENERATE a NEW one!               #");
                        getLogger().severe("#            need help? Discord: unsocial6136           #");
                        getLogger().severe("#########################################################");
                        return;
                }

                LocksData.create();
                GroupsData.create();
                LockController.loadLocksFromDisk();
                GroupController.loadGroupsFromDisk();
                HopperCache.configureTtl(Config.getInt("settings.hopper-cache-ttl-ticks"));
                HopperCache.startTickCounter();
                Commands.setup();
                Events.setup();
		
		checkForUpdates = Config.getBoolean("settings.updateChecker");
		
		if(checkForUpdates) {
	        try {
	        	getLogger().info("Checking for updates...");
	            UpdateChecker updater = new UpdateChecker(this, 81204);
	            if(updater.checkForUpdates()) {
	            	String versionsCheck = "#                   %1.1.1% ----> %0.0.0%                   #"
	            			.replace("%0.0.0%", updater.getNewVersion())
	            			.replace("%1.1.1%", currentVersion);
	            	getLogger().warning("#########################################################");
	                getLogger().warning("#           ChestLock has an update available!          #");
	                getLogger().warning(versionsCheck);
	                getLogger().warning("#             Download the latest update at             #");
	                getLogger().warning("#  https://www.spigotmc.org/resources/chestlock.81204/  #");
	                getLogger().warning("#            need help? Discord: unsocial6136           #");
	                getLogger().warning("#########################################################");
	            }else{
	                getLogger().info("ChestLock is up to date!");
	                getLogger().info("Please report bugs on discord: unsocial6136");
	            }
	        }catch(Exception e1) {
	            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not proceed update-checking!");
	        }
		}
	}
}
