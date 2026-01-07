package com.watsonllc.chestlock.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class HelpService {
    private static final List<String> TOPICS = Arrays.asList("locks", "owners", "groups", "admin");

    private HelpService() {
    }

    public static boolean handleHelp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("ChestLock help is available in-game.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            sendHelpPage(player, 1);
            return true;
        }

        String arg = args[1].toLowerCase();
        if (isNumeric(arg)) {
            int page = Integer.parseInt(arg);
            sendHelpPage(player, page);
            return true;
        }

        sendTopicHelp(player, arg);
        return true;
    }

    public static void sendQuickHelp(Player player) {
        player.sendMessage(Utils.color("&6ChestLock &7Quick Help"));
        sendHelpLine(player, "/cl claim", "Enter claim mode to lock a container.", true);
        sendHelpLine(player, "/cl add <player>", "Share a lock with another player.", false);
        sendHelpLine(player, "/cl group", "Open group management.", true);
        player.sendMessage(Utils.color("&7More: &e/cl help"));
    }

    public static void sendUsage(Player player, String syntax, List<String> examples) {
        player.sendMessage(Utils.color("&cUsage: &e" + syntax));
        for (String example : examples) {
            sendHelpLine(player, example, "Click to suggest.", false);
        }
    }

    private static void sendHelpPage(Player player, int page) {
        if (page <= 1) {
            player.sendMessage(Utils.color("&6ChestLock Help &7(Page 1/2)"));
            sendHelpLine(player, "/cl help <topic>", "Help topics: " + String.join(", ", TOPICS), false);
            sendHelpLine(player, "/cl status", "Show your current mode and settings.", true);
            sendHelpLine(player, "/cl cancel", "Cancel any active mode.", true);
            sendHelpLine(player, "/cl claim", "Claim a container lock.", true);
            sendHelpLine(player, "/cl public", "Make a lock public.", true);
            sendHelpLine(player, "/cl add <player>", "Add a co-owner to a lock.", false);
            sendHelpLine(player, "/cl group", "Manage player groups.", true);
            player.sendMessage(Utils.color("&7Next page: &e/cl help 2"));
            return;
        }

        player.sendMessage(Utils.color("&6ChestLock Help &7(Page 2/2)"));
        sendHelpLine(player, "/cl destroy", "Remove a lock you own.", true);
        sendHelpLine(player, "/cl remove <player>", "Remove a co-owner from a lock.", false);
        sendHelpLine(player, "/cl group invites", "View pending group invites.", true);
        sendHelpLine(player, "/cl group select <group>", "Select a group for invites.", false);
        sendHelpLine(player, "/cl bypass", "Toggle bypass mode (admin).", true);
        sendHelpLine(player, "/cl taghoppers <SERVER|player> <radius|world>", "Tag hoppers in scope (admin).", false);
        player.sendMessage(Utils.color("&7Topics: &e/cl help <topic>"));
    }

    private static void sendTopicHelp(Player player, String topic) {
        switch (topic) {
        case "locks":
            player.sendMessage(Utils.color("&6ChestLock Help &7- Locks"));
            sendHelpLine(player, "/cl claim [on|off|toggle]", "Toggle claim mode.", true);
            sendHelpLine(player, "/cl destroy [on|off|toggle]", "Toggle destroy mode.", true);
            sendHelpLine(player, "/cl public [on|off|toggle]", "Toggle public mode.", true);
            return;
        case "owners":
            player.sendMessage(Utils.color("&6ChestLock Help &7- Owners"));
            sendHelpLine(player, "/cl add <player> [on|off|toggle]", "Add a co-owner.", false);
            sendHelpLine(player, "/cl remove <player> [on|off|toggle]", "Remove a co-owner.", false);
            return;
        case "groups":
            player.sendMessage(Utils.color("&6ChestLock Help &7- Groups"));
            sendHelpLine(player, "/cl group create <name>", "Create a group.", false);
            sendHelpLine(player, "/cl group delete", "Delete your owned group.", true);
            sendHelpLine(player, "/cl group invite <player> [group]", "Invite a player.", false);
            sendHelpLine(player, "/cl group remove <player> [group]", "Remove a player.", false);
            sendHelpLine(player, "/cl group accept [group]", "Accept an invite.", true);
            sendHelpLine(player, "/cl group decline [group]", "Decline an invite.", true);
            sendHelpLine(player, "/cl group leave [group]", "Leave a group.", true);
            sendHelpLine(player, "/cl group list", "List group members.", true);
            sendHelpLine(player, "/cl group invites", "List pending invites.", true);
            sendHelpLine(player, "/cl group select <group>", "Select a group.", false);
            return;
        case "admin":
            player.sendMessage(Utils.color("&6ChestLock Help &7- Admin"));
            sendHelpLine(player, "/cl bypass [on|off|toggle]", "Toggle bypass mode.", true);
            sendHelpLine(player, "/cl taghoppers <SERVER|player> <radius|world> [minecarts]", "Tag hopper owners.", false);
            return;
        default:
            player.sendMessage(Utils.color("&cUnknown help topic. Try &e/cl help"));
        }
    }

    private static void sendHelpLine(Player player, String command, String description, boolean run) {
        String baseText = Utils.color("&6• &e" + command + " &7- " + description + " ");
        TextComponent base = new TextComponent();
        for (TextComponent part : TextComponent.fromLegacyText(baseText)) {
            base.addExtra(part);
        }

        TextComponent action = new TextComponent(Utils.color(run ? "&a[Run]" : "&e[Suggest]"));
        action.setClickEvent(new ClickEvent(run ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command));
        action.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.color(description)).create()));
        base.addExtra(action);
        player.spigot().sendMessage(base);
    }

    private static boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
