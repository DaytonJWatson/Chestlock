package com.watsonllc.chestlock.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.watsonllc.chestlock.config.GroupsData;

public class GroupController {

        public boolean createGroup(String groupName, String owner) {
                String path = "Groups." + normalize(groupName);

                if (GroupsData.contains(path))
                        return false;

                if (getGroupForPlayer(owner) != null)
                        return false;

                List<String> members = new ArrayList<>();
                members.add(owner);
                List<String> invites = new ArrayList<>();

                GroupsData.set(path + ".owner", owner);
                GroupsData.set(path + ".members", members);
                GroupsData.set(path + ".invites", invites);
                GroupsData.save();
                return true;
        }

        public boolean deleteGroup(String groupName, String requester) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                GroupsData.set(path, null);
                GroupsData.save();
                return true;
        }

        public boolean addMember(String groupName, String requester, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                return addMemberInternal(path, target);
        }

        public boolean inviteMember(String groupName, String requester, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                List<String> invites = getInvites(path);

                if (invites.contains(target))
                        return false;

                invites.add(target);
                GroupsData.set(path + ".invites", invites);
                GroupsData.save();
                return true;
        }

        public boolean acceptInvite(String groupName, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                List<String> invites = getInvites(path);

                if (!invites.contains(target))
                        return false;

                invites.remove(target);
                GroupsData.set(path + ".invites", invites);

                boolean added = addMemberInternal(path, target);
                if (!added)
                        return false;

                GroupsData.save();
                return true;
        }

        public boolean declineInvite(String groupName, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                List<String> invites = getInvites(path);

                if (!invites.contains(target))
                        return false;

                invites.remove(target);
                GroupsData.set(path + ".invites", invites);
                GroupsData.save();
                return true;
        }

        public boolean removeMember(String groupName, String requester, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                @SuppressWarnings("unchecked")
                List<String> members = (List<String>) GroupsData.get(path + ".members");

                if (members == null)
                        members = new ArrayList<>();

                if (!members.contains(target))
                        return false;

                members.remove(target);
                GroupsData.set(path + ".members", members);
                GroupsData.save();
                return true;
        }

        public boolean leaveGroup(String groupName, String target) {
                String path = "Groups." + normalize(groupName);

                if (!GroupsData.contains(path))
                        return false;

                @SuppressWarnings("unchecked")
                List<String> members = (List<String>) GroupsData.get(path + ".members");

                if (members == null)
                        members = new ArrayList<>();

                if (!members.contains(target))
                        return false;

                members.remove(target);

                if (members.isEmpty() || target.equalsIgnoreCase((String) GroupsData.get(path + ".owner"))) {
                        GroupsData.set(path, null);
                        GroupsData.save();
                        return true;
                }

                GroupsData.set(path + ".members", members);
                GroupsData.save();
                return true;
        }

        public boolean isOwner(String groupName, String player) {
                String path = "Groups." + normalize(groupName) + ".owner";
                return player.equalsIgnoreCase((String) GroupsData.get(path));
        }

        public boolean groupExists(String groupName) {
                return GroupsData.contains("Groups." + normalize(groupName));
        }

        public List<String> getGroupMembers(String groupName) {
                String path = "Groups." + normalize(groupName) + ".members";

                if (!GroupsData.contains(path))
                        return new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<String> members = (List<String>) GroupsData.get(path);

                return members == null ? new ArrayList<>() : new ArrayList<>(members);
        }

        public List<String> getGroupInvites(String groupName) {
                String path = "Groups." + normalize(groupName) + ".invites";

                if (!GroupsData.contains(path))
                        return new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<String> invites = (List<String>) GroupsData.get(path);

                return invites == null ? new ArrayList<>() : new ArrayList<>(invites);
        }

        public Set<String> getSharedMembers(String player) {
                Set<String> members = new HashSet<>();

                String groupName = getGroupForPlayer(player);

                if (groupName == null)
                        return members;

                members.addAll(getGroupMembers(groupName));

                return members;
        }

        public boolean shareGroup(String firstPlayer, String secondPlayer) {
                String firstGroup = getGroupForPlayer(firstPlayer);
                String secondGroup = getGroupForPlayer(secondPlayer);

                return firstGroup != null && firstGroup.equalsIgnoreCase(secondGroup);
        }

        public List<String> getGroupNames() {
                List<String> groupNames = new ArrayList<>();

                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return groupNames;

                groupNames.addAll(section.getKeys(false));

                return groupNames;
        }

        public List<String> getInviteGroupsForPlayer(String player) {
                List<String> groupNames = new ArrayList<>();
                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return groupNames;

                for (String groupName : section.getKeys(false)) {
                        @SuppressWarnings("unchecked")
                        List<String> invites = (List<String>) GroupsData.get("Groups." + groupName + ".invites");

                        if (invites == null)
                                continue;

                        for (String invite : invites) {
                                if (invite.equalsIgnoreCase(player)) {
                                        groupNames.add(groupName);
                                        break;
                                }
                        }
                }

                return groupNames;
        }

        public String getGroupForPlayer(String player) {
                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return null;

                for (String groupName : section.getKeys(false)) {
                        @SuppressWarnings("unchecked")
                        List<String> members = (List<String>) GroupsData.get("Groups." + groupName + ".members");

                        if (members == null)
                                continue;

                        for (String member : members) {
                                if (member.equalsIgnoreCase(player))
                                        return groupName;
                        }
                }

                return null;
        }

        public String getOwnedGroup(String owner) {
                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return null;

                for (String groupName : section.getKeys(false)) {
                        String groupOwner = (String) GroupsData.get("Groups." + groupName + ".owner");

                        if (groupOwner != null && groupOwner.equalsIgnoreCase(owner))
                                return groupName;
                }

                return null;
        }

        private String normalize(String groupName) {
                return groupName.toLowerCase();
        }

        private List<String> getInvites(String path) {
                @SuppressWarnings("unchecked")
                List<String> invites = (List<String>) GroupsData.get(path + ".invites");

                return invites == null ? new ArrayList<>() : new ArrayList<>(invites);
        }

        private boolean addMemberInternal(String path, String target) {
                @SuppressWarnings("unchecked")
                List<String> members = (List<String>) GroupsData.get(path + ".members");

                if (members == null)
                        members = new ArrayList<>();

                if (members.contains(target))
                        return false;

                members.add(target);
                GroupsData.set(path + ".members", members);
                GroupsData.save();
                return true;
        }
}
