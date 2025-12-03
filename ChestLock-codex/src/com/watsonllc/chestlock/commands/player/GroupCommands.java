package com.watsonllc.chestlock.commands.player;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.GroupController;

public class GroupCommands {

        private static final GroupController GROUP_CONTROLLER = new GroupController();

        public static boolean create(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.create"))
                        return false;

                if (GROUP_CONTROLLER.createGroup(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupCreated").replace("%group%", groupName));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupExists").replace("%group%", groupName));
                return false;
        }

        public static boolean delete(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.delete"))
                        return false;

                if (!GROUP_CONTROLLER.groupExists(groupName)) {
                        player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                        return false;
                }

                if (!GROUP_CONTROLLER.isOwner(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupNotOwner"));
                        return false;
                }

                if (GROUP_CONTROLLER.deleteGroup(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupDeleted").replace("%group%", groupName));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                return false;
        }

        public static boolean add(Player player, String target, String groupName) {
                if (!hasPermission(player, "chestlock.group.add"))
                        return false;

                if (!GROUP_CONTROLLER.groupExists(groupName)) {
                        player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                        return false;
                }

                if (!GROUP_CONTROLLER.isOwner(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupNotOwner"));
                        return false;
                }

                if (GROUP_CONTROLLER.addMember(groupName, player.getName(), target)) {
                        String added = Config.getString("messages.groupMemberAdded");
                        added = added.replace("%target%", target);
                        added = added.replace("%group%", groupName);
                        player.sendMessage(added);
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMemberExists").replace("%target%", target));
                return false;
        }

        public static boolean remove(Player player, String target, String groupName) {
                if (!hasPermission(player, "chestlock.group.remove"))
                        return false;

                if (!GROUP_CONTROLLER.groupExists(groupName)) {
                        player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                        return false;
                }

                if (!GROUP_CONTROLLER.isOwner(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupNotOwner"));
                        return false;
                }

                if (target.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(Config.getString("messages.groupCannotRemoveSelf"));
                        return false;
                }

                if (GROUP_CONTROLLER.removeMember(groupName, player.getName(), target)) {
                        String removed = Config.getString("messages.groupMemberRemoved");
                        removed = removed.replace("%target%", target);
                        removed = removed.replace("%group%", groupName);
                        player.sendMessage(removed);
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMemberMissing").replace("%target%", target));
                return false;
        }

        public static boolean leave(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.leave"))
                        return false;

                if (!GROUP_CONTROLLER.groupExists(groupName)) {
                        player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                        return false;
                }

                if (!GROUP_CONTROLLER.getGroupMembers(groupName).contains(player.getName())) {
                        player.sendMessage(Config.getString("messages.groupMemberMissing").replace("%target%", player.getName()));
                        return false;
                }

                if (GROUP_CONTROLLER.leaveGroup(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupLeft").replace("%group%", groupName));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                return false;
        }

        public static boolean list(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.list"))
                        return false;

                if (!GROUP_CONTROLLER.groupExists(groupName)) {
                        player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                        return false;
                }

                if (!GROUP_CONTROLLER.getGroupMembers(groupName).contains(player.getName())) {
                        player.sendMessage(Config.getString("messages.groupMemberMissing").replace("%target%", player.getName()));
                        return false;
                }

                String members = String.join(", ", GROUP_CONTROLLER.getGroupMembers(groupName));

                String message = Config.getString("messages.groupMembers");
                message = message.replace("%group%", groupName);
                message = message.replace("%members%", members.isEmpty() ? Config.getString("messages.groupNoMembers") : members);
                player.sendMessage(message);
                return true;
        }

        private static boolean hasPermission(Player player, String permission) {
                if (!Commands.usePermissions())
                        return true;

                if (player.hasPermission(permission))
                        return true;

                player.sendMessage(Config.getString("messages.noPermission"));
                return false;
        }
}
