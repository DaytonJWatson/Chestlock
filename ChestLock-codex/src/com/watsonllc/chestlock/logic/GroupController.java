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

                List<String> members = new ArrayList<>();
                members.add(owner);

                GroupsData.set(path + ".owner", owner);
                GroupsData.set(path + ".members", members);
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

        public Set<String> getSharedMembers(String player) {
                Set<String> members = new HashSet<>();

                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return members;

                for (String groupName : section.getKeys(false)) {
                        @SuppressWarnings("unchecked")
                        List<String> groupMembers = (List<String>) GroupsData.get("Groups." + groupName + ".members");

                        if (groupMembers == null || !groupMembers.contains(player))
                                continue;

                        members.addAll(groupMembers);
                }

                return members;
        }

        public boolean shareGroup(String firstPlayer, String secondPlayer) {
                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return false;

                for (String groupName : section.getKeys(false)) {
                        @SuppressWarnings("unchecked")
                        List<String> members = (List<String>) GroupsData.get("Groups." + groupName + ".members");

                        if (members == null)
                                continue;

                        if (members.contains(firstPlayer) && members.contains(secondPlayer))
                                return true;
                }

                return false;
        }

        public List<String> getGroupNames() {
                List<String> groupNames = new ArrayList<>();

                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return groupNames;

                groupNames.addAll(section.getKeys(false));

                return groupNames;
        }

        private String normalize(String groupName) {
                return groupName.toLowerCase();
        }
}
