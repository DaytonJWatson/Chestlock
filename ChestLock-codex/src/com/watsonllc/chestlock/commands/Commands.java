package com.watsonllc.chestlock.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.admin.Bypass;
import com.watsonllc.chestlock.commands.player.AddOwner;
import com.watsonllc.chestlock.commands.player.ClaimLock;
import com.watsonllc.chestlock.commands.player.DestroyLock;
import com.watsonllc.chestlock.commands.player.GroupCommands;
import com.watsonllc.chestlock.commands.player.MakePublic;
import com.watsonllc.chestlock.commands.player.RemoveOwner;
import com.watsonllc.chestlock.config.Config;

public class Commands implements CommandExecutor {
	
	public static void setup() {
		Main.instance.getCommand("chestlock").setExecutor(new Commands());
		Main.instance.getCommand("chestlock").setTabCompleter(new TabCommand());
	}	
	
	private static boolean helpMenu(Player player) {
		player.sendMessage(Utils.color("&8======== &6Chestlock Help &8========"));
		
		if(player.hasPermission("chestlock.add") || !usePermissions())
			player.sendMessage(Utils.color("&8/&6chestlock &7add &8<&7player&8> [&7toggle&8]"));
		
		if(player.hasPermission("chestlock.remove") || !usePermissions())
			player.sendMessage(Utils.color("&8/&6chestlock &7remove &8<&7player&8> [&7toggle&8]"));
		
                if(player.hasPermission("chestlock.claim") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7claim &8[&7toggle&8]"));
		
                if(player.hasPermission("chestlock.destroy") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7destroy &8[&7toggle&8]"));
		
                if(player.hasPermission("chestlock.public") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7public &8[&7toggle&8]"));
		
                if(player.hasPermission("chestlock.bypass") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7bypass"));

                if(player.hasPermission("chestlock.group.create") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7groupcreate &8<&7group&8>"));

                if(player.hasPermission("chestlock.group.delete") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7groupdelete &8<&7group&8>"));

                if(player.hasPermission("chestlock.group.add") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7groupadd &8<&7player&8> <&7group&8>"));

                if(player.hasPermission("chestlock.group.remove") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7groupremove &8<&7player&8> <&7group&8>"));

                if(player.hasPermission("chestlock.group.leave") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7groupleave &8<&7group&8>"));

                if(player.hasPermission("chestlock.group.list") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7grouplist &8<&7group&8>"));
		
		if(usePermissions()) {
                        List<String> permissions = Arrays.asList("chestlock.add","chestlock.remove","chestlock.claim","chestlock.destroy","chestlock.public","chestlock.bypass","chestlock.group.create","chestlock.group.delete","chestlock.group.add","chestlock.group.remove","chestlock.group.leave","chestlock.group.list");
                        int totalPerms = 0;
                        for(int i=0; i<permissions.size(); i++) {
                                if(!player.hasPermission(permissions.get(i))) {
                                        totalPerms++;
                                }
                        }

                        // make sure to increase this when you add commands
                        if(totalPerms == 12) {
                                player.sendMessage(Utils.color("&cYou need a permission manager plugin to use commands! If you dont have a permission manager, you can disable 'usePermissions' in the config.yml"));
                                player.sendMessage(Utils.color("&6Available permissions&7: &f" + permissions.toString()));
                        }
		}
		return true;
	}
	
	public static boolean usePermissions() {
		return Config.getBoolean("settings.usePermissions");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(!(sender instanceof Player)) {
			Main.instance.getLogger().warning(Config.getString("messages.invalidInstance"));
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length == 0) {
			return helpMenu(player);
		}
		
		// chestlock public
		// chestlock bypass
		// chestlock claim
		// chestlock destroy
		if(args.length == 1) {
			switch(args[0]) {
			case "public":
				return MakePublic.logic(player, false);
			case "bypass":
				return Bypass.logic(player);
			case "claim":
				return ClaimLock.logic(player, false);
			case "destroy":
				return DestroyLock.logic(player, false);
			default:
				return helpMenu(player);
			}
		}
		
                // chestlock claim toggle
                // chestlock destroy toggle
                // chestlock public toggle
                // chestlock add <player>
                // chestlock remove <player>
                // chestlock groupcreate <group>
                // chestlock groupdelete <group>
                // chestlock groupleave <group>
                // chestlock grouplist <group>
                if(args.length == 2) {
                        switch(args[0]) {
                        case "claim":
                                if(args[1].equalsIgnoreCase("toggle"))
                                        return ClaimLock.logic(player, true);
			case "destroy":
				if(args[1].equalsIgnoreCase("toggle"))
					return DestroyLock.logic(player, true);
			case "public":
				if(args[1].equalsIgnoreCase("toggle"))
					return MakePublic.logic(player, true);
                        case "add":
                                return AddOwner.logic(player, args[1], false);
                        case "remove":
                                return RemoveOwner.logic(player, args[1], false);
                        case "groupcreate":
                                return GroupCommands.create(player, args[1]);
                        case "groupdelete":
                                return GroupCommands.delete(player, args[1]);
                        case "groupleave":
                                return GroupCommands.leave(player, args[1]);
                        case "grouplist":
                                return GroupCommands.list(player, args[1]);
                        default:
                                return helpMenu(player);
                        }
                }

                // chestlock add <player> toggle
                // chestlock remove <player> toggle
                // chestlock groupadd <player> <group>
                // chestlock groupremove <player> <group>
                if(args.length == 3) {
                        switch(args[0]) {
                        case "add":
                                if(args[2].equalsIgnoreCase("toggle"))
                                        return AddOwner.logic(player, args[1], true);
                        case "remove":
                                if(args[2].equalsIgnoreCase("toggle"))
                                        return RemoveOwner.logic(player, args[1], true);
                        case "groupadd":
                                return GroupCommands.add(player, args[1], args[2]);
                        case "groupremove":
                                return GroupCommands.remove(player, args[1], args[2]);
                        }
                }
		
		return helpMenu(player);
	}	
}