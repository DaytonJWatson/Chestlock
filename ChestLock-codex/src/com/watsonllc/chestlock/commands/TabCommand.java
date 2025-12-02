package com.watsonllc.chestlock.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TabCommand implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		// chestlock add
		// chestlock remove
		// chestlock public
		// chestlock bypass
		if (args.length == 1) {
			List<String> subCommands = Arrays.asList("add","remove","claim","destroy","public","bypass");
			for (String subCommand : subCommands) {
				if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(subCommand);
				}
			}
		}
		
		// chestlock add <player>
		// chestlock remove <player>
		if (args.length == 2) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				completions.add(player.getName());
			}
		}
		
		// chestlock bypass
		if (args.length == 2 && args[0].equalsIgnoreCase("bypass"))
			return null;

		// chestlock public [toggle]
		if (args.length == 2 && args[0].equalsIgnoreCase("public")) {
			List<String> subCommand1Options = Arrays.asList("toggle");
			for (String option : subCommand1Options) {
				if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(option);
				}
			}
		}
		
		// chestlock claim [toggle]
		if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
			List<String> subCommand1Options = Arrays.asList("toggle");
			for (String option : subCommand1Options) {
				if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(option);
				}
			}
		}
		
		// chestlock destroy [toggle]
		if (args.length == 2 && args[0].equalsIgnoreCase("destroy")) {
			List<String> subCommand1Options = Arrays.asList("toggle");
			for (String option : subCommand1Options) {
				if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(option);
				}
			}
		}
		
		if (args.length == 3) {
			List<String> subCommand1Options = Arrays.asList("toggle");
			for (String option : subCommand1Options) {
				if (option.toLowerCase().startsWith(args[2].toLowerCase())) {
					completions.add(option);
				}
			}
		}

		return completions;
	}

}
