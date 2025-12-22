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
                        player.sendMessage(Utils.color("&8/&6chestlock &7group create &8<&7group&8>"));

                if(player.hasPermission("chestlock.group.delete") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group delete"));

                if(player.hasPermission("chestlock.group.invite") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group invite &8<&7player&8>"));

                if(player.hasPermission("chestlock.group.remove") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group remove &8<&7player&8>"));

                if(player.hasPermission("chestlock.group.accept") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group accept &8[&7group&8]"));

                if(player.hasPermission("chestlock.group.decline") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group decline &8[&7group&8]"));

                if(player.hasPermission("chestlock.group.invites") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group invites"));

                if(player.hasPermission("chestlock.group.leave") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group leave"));

                if(player.hasPermission("chestlock.group.list") || !usePermissions())
                        player.sendMessage(Utils.color("&8/&6chestlock &7group list"));
		
		if(usePermissions()) {
                        List<String> permissions = Arrays.asList("chestlock.add","chestlock.remove","chestlock.claim","chestlock.destroy","chestlock.public","chestlock.bypass","chestlock.group.create","chestlock.group.delete","chestlock.group.invite","chestlock.group.remove","chestlock.group.accept","chestlock.group.decline","chestlock.group.invites","chestlock.group.leave","chestlock.group.list");
                        int totalPerms = 0;
                        for(int i=0; i<permissions.size(); i++) {
                                if(!player.hasPermission(permissions.get(i))) {
                                        totalPerms++;
                                }
                        }

                        // make sure to increase this when you add commands
                        if(totalPerms == 15) {
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

                if(args[0].equalsIgnoreCase("group")) {
                        return handleGroup(player, args);
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
                        default:
                                return helpMenu(player);
                        }
                }

                if(args.length == 3) {
                        switch(args[0]) {
                        case "add":
                                if(args[2].equalsIgnoreCase("toggle"))
                                        return AddOwner.logic(player, args[1], true);
                        case "remove":
                                if(args[2].equalsIgnoreCase("toggle"))
                                        return RemoveOwner.logic(player, args[1], true);
                        }
                }

                return helpMenu(player);
        }

        private boolean handleGroup(Player player, String[] args) {
                if(args.length == 1) {
                        return groupHelp(player);
                }

                String action = args[1].toLowerCase();

                switch(action) {
                case "create":
                        if(args.length < 3) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group create <group>"));
                                return true;
                        }
                        return GroupCommands.create(player, args[2]);
                case "delete":
                        if(args.length < 2) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group delete"));
                                return true;
                        }
                        return GroupCommands.delete(player);
                case "add":
                case "invite":
                        if(args.length < 3) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group invite <player>"));
                                return true;
                        }
                        return GroupCommands.invite(player, args[2]);
                case "remove":
                        if(args.length < 3) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group remove <player>"));
                                return true;
                        }
                        return GroupCommands.remove(player, args[2]);
                case "accept":
                        if(args.length > 3) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group accept [group]"));
                                return true;
                        }
                        return GroupCommands.accept(player, args.length > 2 ? args[2] : null);
                case "decline":
                        if(args.length > 3) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group decline [group]"));
                                return true;
                        }
                        return GroupCommands.decline(player, args.length > 2 ? args[2] : null);
                case "invites":
                        if(args.length > 2) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group invites"));
                                return true;
                        }
                        return GroupCommands.invites(player);
                case "leave":
                        if(args.length < 2) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group leave"));
                                return true;
                        }
                        return GroupCommands.leave(player);
                case "list":
                        if(args.length < 2) {
                                player.sendMessage(Utils.color("&cUsage: /chestlock group list"));
                                return true;
                        }
                        return GroupCommands.list(player);
                default:
                        return groupHelp(player);
                }
        }

        private boolean groupHelp(Player player) {
                player.sendMessage(Utils.color("&8======== &6Group Help &8========"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group create &8<&7group&8>"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group delete"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group invite &8<&7player&8>"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group remove &8<&7player&8>"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group accept &8[&7group&8]"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group decline &8[&7group&8]"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group invites"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group leave"));
                player.sendMessage(Utils.color("&8/&6chestlock &7group list"));
                return true;
        }
}
