package com.watsonllc.chestlock.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.logic.GroupController;

public class TabCommand implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
                List<String> completions = new ArrayList<>();
                GroupController groupController = new GroupController();

		// chestlock add
		// chestlock remove
		// chestlock public
		// chestlock bypass
                if (args.length == 1) {
                        List<String> subCommands = Arrays.asList("add","remove","claim","destroy","public","bypass","group");
                        for (String subCommand : subCommands) {
                                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                                        completions.add(subCommand);
                                }
                        }
                }

                if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("group")) {
                                List<String> groupSubCommands = Arrays.asList("create", "delete", "invite", "add", "remove", "accept", "decline", "invites", "leave", "list");
                                for (String sub : groupSubCommands) {
                                        if (sub.startsWith(args[1].toLowerCase())) {
                                                completions.add(sub);
                                        }
                                }
                                return completions;
                        }

                        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                        completions.add(player.getName());
                                }
                        }

                        if (args[0].equalsIgnoreCase("bypass"))
                                return null;

                        if (args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("destroy")) {
                                List<String> subCommand1Options = Arrays.asList("toggle");
                                for (String option : subCommand1Options) {
                                        if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
                                                completions.add(option);
                                        }
                                }
                        }
                }
		
                if (args.length == 3) {
                        if (args[0].equalsIgnoreCase("group")) {
                                if (args[1].equalsIgnoreCase("invite") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                                        for (Player player : Bukkit.getOnlinePlayers()) {
                                                completions.add(player.getName());
                                        }
                                        return completions;
                                }

                                if ((args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("decline")) && sender instanceof Player) {
                                        completions.addAll(groupController.getInviteGroupsForPlayer(((Player) sender).getName()));
                                        return completions;
                                }
                        }

                        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                                List<String> subCommand1Options = Arrays.asList("toggle");
                                for (String option : subCommand1Options) {
                                        if (option.toLowerCase().startsWith(args[2].toLowerCase())) {
                                                completions.add(option);
                                        }
                                }
                        }

                }

                return completions;
        }

}
