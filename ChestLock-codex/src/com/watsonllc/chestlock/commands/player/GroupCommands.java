package com.watsonllc.chestlock.commands.player;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.GroupController;
import org.bukkit.Bukkit;

public class GroupCommands {

        private static final GroupController GROUP_CONTROLLER = new GroupController();

        public static boolean create(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.create"))
                        return false;

                String existingGroup = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                if (existingGroup != null) {
                        player.sendMessage(Config.getString("messages.groupAlreadyIn").replace("%group%", existingGroup));
                        return false;
                }

                if (GROUP_CONTROLLER.createGroup(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupCreated").replace("%group%", groupName));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupExists").replace("%group%", groupName));
                return false;
        }

        public static boolean delete(Player player) {
                if (!hasPermission(player, "chestlock.group.delete"))
                        return false;

                String ownedGroup = GROUP_CONTROLLER.getOwnedGroup(player.getName());
                if (ownedGroup == null) {
                        String memberGroup = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                        if (memberGroup != null) {
                                player.sendMessage(Config.getString("messages.groupNotOwner"));
                                return false;
                        }
                        player.sendMessage(Config.getString("messages.groupNotInGroup"));
                        return false;
                }

                if (GROUP_CONTROLLER.deleteGroup(ownedGroup, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupDeleted").replace("%group%", ownedGroup));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", ownedGroup));
                return false;
        }

        public static boolean invite(Player player, String target) {
                if (!hasPermission(player, "chestlock.group.invite"))
                        return false;

                String ownedGroup = GROUP_CONTROLLER.getOwnedGroup(player.getName());
                if (ownedGroup == null) {
                        String memberGroup = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                        if (memberGroup != null) {
                                player.sendMessage(Config.getString("messages.groupNotOwner"));
                                return false;
                        }
                        player.sendMessage(Config.getString("messages.groupNotInGroup"));
                        return false;
                }

                String targetGroup = GROUP_CONTROLLER.getGroupForPlayer(target);
                if (targetGroup != null) {
                        String alreadyIn = Config.getString("messages.groupTargetAlreadyIn");
                        alreadyIn = alreadyIn.replace("%target%", target);
                        alreadyIn = alreadyIn.replace("%group%", targetGroup);
                        player.sendMessage(alreadyIn);
                        return false;
                }

                if (GROUP_CONTROLLER.inviteMember(ownedGroup, player.getName(), target)) {
                        String sent = Config.getString("messages.groupInviteSent");
                        sent = sent.replace("%target%", target);
                        sent = sent.replace("%group%", ownedGroup);
                        player.sendMessage(sent);

                        Player targetPlayer = Bukkit.getPlayerExact(target);
                        if (targetPlayer != null) {
                                String received = Config.getString("messages.groupInviteReceived");
                                received = received.replace("%group%", ownedGroup);
                                received = received.replace("%player%", player.getName());
                                targetPlayer.sendMessage(received);
                        }
                        return true;
                }

                String alreadyInvited = Config.getString("messages.groupInviteAlreadySent");
                alreadyInvited = alreadyInvited.replace("%target%", target);
                player.sendMessage(alreadyInvited);
                return false;
        }

        public static boolean remove(Player player, String target) {
                if (!hasPermission(player, "chestlock.group.remove"))
                        return false;

                String ownedGroup = GROUP_CONTROLLER.getOwnedGroup(player.getName());
                if (ownedGroup == null) {
                        String memberGroup = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                        if (memberGroup != null) {
                                player.sendMessage(Config.getString("messages.groupNotOwner"));
                                return false;
                        }
                        player.sendMessage(Config.getString("messages.groupNotInGroup"));
                        return false;
                }

                if (target.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(Config.getString("messages.groupCannotRemoveSelf"));
                        return false;
                }

                if (GROUP_CONTROLLER.removeMember(ownedGroup, player.getName(), target)) {
                        String removed = Config.getString("messages.groupMemberRemoved");
                        removed = removed.replace("%target%", target);
                        removed = removed.replace("%group%", ownedGroup);
                        player.sendMessage(removed);
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMemberMissing").replace("%target%", target));
                return false;
        }

        public static boolean leave(Player player) {
                if (!hasPermission(player, "chestlock.group.leave"))
                        return false;

                String groupName = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                if (groupName == null) {
                        player.sendMessage(Config.getString("messages.groupNotInGroup"));
                        return false;
                }

                if (GROUP_CONTROLLER.leaveGroup(groupName, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupLeft").replace("%group%", groupName));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupMissing").replace("%group%", groupName));
                return false;
        }

        public static boolean list(Player player) {
                if (!hasPermission(player, "chestlock.group.list"))
                        return false;

                String groupName = GROUP_CONTROLLER.getGroupForPlayer(player.getName());
                if (groupName == null) {
                        player.sendMessage(Config.getString("messages.groupNotInGroup"));
                        return false;
                }

                String members = String.join(", ", GROUP_CONTROLLER.getGroupMembers(groupName));

                String message = Config.getString("messages.groupMembers");
                message = message.replace("%group%", groupName);
                message = message.replace("%members%", members.isEmpty() ? Config.getString("messages.groupNoMembers") : members);
                player.sendMessage(message);
                return true;
        }

        public static boolean invites(Player player) {
                if (!hasPermission(player, "chestlock.group.invites"))
                        return false;

                String invites = String.join(", ", GROUP_CONTROLLER.getInviteGroupsForPlayer(player.getName()));
                String message = Config.getString("messages.groupInvites");
                message = message.replace("%invites%", invites.isEmpty() ? Config.getString("messages.groupNoInvites") : invites);
                player.sendMessage(message);
                return true;
        }

        public static boolean accept(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.accept"))
                        return false;

                String resolvedGroup = resolveInviteGroup(player, groupName);
                if (resolvedGroup == null)
                        return false;

                if (GROUP_CONTROLLER.acceptInvite(resolvedGroup, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupInviteAccepted").replace("%group%", resolvedGroup));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupInviteMissing").replace("%group%", resolvedGroup));
                return false;
        }

        public static boolean decline(Player player, String groupName) {
                if (!hasPermission(player, "chestlock.group.decline"))
                        return false;

                String resolvedGroup = resolveInviteGroup(player, groupName);
                if (resolvedGroup == null)
                        return false;

                if (GROUP_CONTROLLER.declineInvite(resolvedGroup, player.getName())) {
                        player.sendMessage(Config.getString("messages.groupInviteDeclined").replace("%group%", resolvedGroup));
                        return true;
                }

                player.sendMessage(Config.getString("messages.groupInviteMissing").replace("%group%", resolvedGroup));
                return false;
        }

        private static String resolveInviteGroup(Player player, String groupName) {
                if (GROUP_CONTROLLER.getGroupForPlayer(player.getName()) != null) {
                        player.sendMessage(Config.getString("messages.groupAlreadyIn").replace("%group%", GROUP_CONTROLLER.getGroupForPlayer(player.getName())));
                        return null;
                }

                if (groupName != null && !groupName.isEmpty())
                        return groupName;

                java.util.List<String> inviteGroups = GROUP_CONTROLLER.getInviteGroupsForPlayer(player.getName());
                if (inviteGroups.isEmpty()) {
                        player.sendMessage(Config.getString("messages.groupInviteNone"));
                        return null;
                }

                if (inviteGroups.size() > 1) {
                        player.sendMessage(Config.getString("messages.groupInviteMultiple"));
                        return null;
                }

                return inviteGroups.get(0);
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
