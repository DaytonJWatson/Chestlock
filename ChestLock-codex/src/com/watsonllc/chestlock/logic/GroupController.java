package com.watsonllc.chestlock.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;

import com.watsonllc.chestlock.config.GroupsData;

public class GroupController {

        private static final Map<String, String> groupByPlayer = new ConcurrentHashMap<>();
        private static final Map<String, Set<String>> membersByGroup = new ConcurrentHashMap<>();
        private static final Map<String, String> ownersByGroup = new ConcurrentHashMap<>();

        public static void loadGroupsFromDisk() {
                clearIndexes();
                ConfigurationSection section = GroupsData.getConfiguration().getConfigurationSection("Groups");

                if (section == null)
                        return;

                for (String groupName : section.getKeys(false)) {
                        String normalizedGroup = normalize(groupName);
                        @SuppressWarnings("unchecked")
                        List<String> members = (List<String>) GroupsData.get("Groups." + groupName + ".members");
                        String owner = (String) GroupsData.get("Groups." + groupName + ".owner");

                        if (members == null)
                                members = new ArrayList<>();

                        if (owner != null && !members.contains(owner))
                                members.add(owner);

                        Set<String> memberSet = new LinkedHashSet<>(members);
                        membersByGroup.put(normalizedGroup, memberSet);
                        ownersByGroup.put(normalizedGroup, owner);
                        indexMembers(normalizedGroup, memberSet);
                }

                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
        }

        public static void clearIndexes() {
                groupByPlayer.clear();
                membersByGroup.clear();
                ownersByGroup.clear();
        }

        public boolean createGroup(String groupName, String owner) {
                String normalizedGroup = normalize(groupName);

                if (membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (getGroupForPlayer(owner) != null)
                        return false;

                List<String> members = new ArrayList<>();
                members.add(owner);
                List<String> invites = new ArrayList<>();

                GroupsData.set("Groups." + normalizedGroup + ".owner", owner);
                GroupsData.set("Groups." + normalizedGroup + ".members", members);
                GroupsData.set("Groups." + normalizedGroup + ".invites", invites);
                GroupsData.save();

                Set<String> memberSet = new LinkedHashSet<>(members);
                membersByGroup.put(normalizedGroup, memberSet);
                ownersByGroup.put(normalizedGroup, owner);
                indexMembers(normalizedGroup, memberSet);
                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
                return true;
        }

        public boolean deleteGroup(String groupName, String requester) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                Set<String> members = membersByGroup.getOrDefault(normalizedGroup, Collections.emptySet());
                for (String member : members) {
                        groupByPlayer.remove(normalizePlayer(member));
                }

                membersByGroup.remove(normalizedGroup);
                ownersByGroup.remove(normalizedGroup);

                GroupsData.set("Groups." + normalizedGroup, null);
                GroupsData.save();
                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
                return true;
        }

        public boolean addMember(String groupName, String requester, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                return addMemberInternal(normalizedGroup, target);
        }

        public boolean inviteMember(String groupName, String requester, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                List<String> invites = getInvites(normalizedGroup);

                if (invites.contains(target))
                        return false;

                invites.add(target);
                GroupsData.set("Groups." + normalizedGroup + ".invites", invites);
                GroupsData.save();
                return true;
        }

        public boolean acceptInvite(String groupName, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (getGroupForPlayer(target) != null)
                        return false;

                List<String> invites = getInvites(normalizedGroup);

                if (!invites.contains(target))
                        return false;

                invites.remove(target);
                GroupsData.set("Groups." + normalizedGroup + ".invites", invites);

                boolean added = addMemberInternal(normalizedGroup, target);
                if (!added)
                        return false;

                GroupsData.save();
                return true;
        }

        public boolean declineInvite(String groupName, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                List<String> invites = getInvites(normalizedGroup);

                if (!invites.contains(target))
                        return false;

                invites.remove(target);
                GroupsData.set("Groups." + normalizedGroup + ".invites", invites);
                GroupsData.save();
                return true;
        }

        public boolean removeMember(String groupName, String requester, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                if (!isOwner(groupName, requester))
                        return false;

                Set<String> members = new LinkedHashSet<>(membersByGroup.getOrDefault(normalizedGroup, Collections.emptySet()));

                if (!members.contains(target))
                        return false;

                members.remove(target);
                membersByGroup.put(normalizedGroup, members);
                groupByPlayer.remove(normalizePlayer(target));

                GroupsData.set("Groups." + normalizedGroup + ".members", new ArrayList<>(members));
                GroupsData.save();
                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
                return true;
        }

        public boolean leaveGroup(String groupName, String target) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return false;

                Set<String> members = new LinkedHashSet<>(membersByGroup.get(normalizedGroup));

                if (!members.contains(target))
                        return false;

                members.remove(target);

                String owner = ownersByGroup.get(normalizedGroup);
                if (members.isEmpty() || target.equalsIgnoreCase(owner)) {
                        for (String member : members) {
                                groupByPlayer.remove(normalizePlayer(member));
                        }
                        membersByGroup.remove(normalizedGroup);
                        ownersByGroup.remove(normalizedGroup);
                        groupByPlayer.remove(normalizePlayer(target));
                        GroupsData.set("Groups." + normalizedGroup, null);
                        GroupsData.save();
                        LockController.invalidateSharedMembersCache(null);
                        HopperCache.invalidate();
                        return true;
                }

                membersByGroup.put(normalizedGroup, members);
                groupByPlayer.remove(normalizePlayer(target));
                GroupsData.set("Groups." + normalizedGroup + ".members", new ArrayList<>(members));
                GroupsData.save();
                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
                return true;
        }

        public boolean isOwner(String groupName, String player) {
                String normalizedGroup = normalize(groupName);
                String owner = ownersByGroup.get(normalizedGroup);
                return player != null && owner != null && player.equalsIgnoreCase(owner);
        }

        public boolean groupExists(String groupName) {
                return membersByGroup.containsKey(normalize(groupName));
        }

        public List<String> getGroupMembers(String groupName) {
                String normalizedGroup = normalize(groupName);
                Set<String> members = membersByGroup.get(normalizedGroup);
                return members == null ? new ArrayList<>() : new ArrayList<>(members);
        }

        public List<String> getGroupInvites(String groupName) {
                String normalizedGroup = normalize(groupName);

                if (!membersByGroup.containsKey(normalizedGroup))
                        return new ArrayList<>();

                return getInvites(normalizedGroup);
        }

        public Set<String> getSharedMembers(String player) {
                String groupName = getGroupForPlayer(player);

                if (groupName == null)
                        return Collections.emptySet();

                return new HashSet<>(membersByGroup.getOrDefault(normalize(groupName), Collections.emptySet()));
        }

        public boolean shareGroup(String firstPlayer, String secondPlayer) {
                String firstGroup = getGroupForPlayer(firstPlayer);
                String secondGroup = getGroupForPlayer(secondPlayer);

                return firstGroup != null && firstGroup.equalsIgnoreCase(secondGroup);
        }

        public List<String> getGroupNames() {
                return new ArrayList<>(membersByGroup.keySet());
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
                if (player == null)
                        return null;

                return groupByPlayer.get(normalizePlayer(player));
        }

        public String getOwnedGroup(String owner) {
                if (owner == null)
                        return null;

                String normalizedOwner = normalizePlayer(owner);
                for (Map.Entry<String, String> entry : ownersByGroup.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(normalizedOwner)) {
                                return entry.getKey();
                        }
                }

                return null;
        }

        private static String normalize(String groupName) {
                return groupName == null ? null : groupName.toLowerCase();
        }

        private static String normalizePlayer(String player) {
                return player == null ? null : player.toLowerCase();
        }

        private List<String> getInvites(String normalizedGroup) {
                @SuppressWarnings("unchecked")
                List<String> invites = (List<String>) GroupsData.get("Groups." + normalizedGroup + ".invites");

                return invites == null ? new ArrayList<>() : new ArrayList<>(invites);
        }

        private boolean addMemberInternal(String normalizedGroup, String target) {
                Set<String> members = new LinkedHashSet<>(membersByGroup.getOrDefault(normalizedGroup, Collections.emptySet()));

                if (members.contains(target))
                        return false;

                members.add(target);
                membersByGroup.put(normalizedGroup, members);
                groupByPlayer.put(normalizePlayer(target), normalizedGroup);
                GroupsData.set("Groups." + normalizedGroup + ".members", new ArrayList<>(members));
                GroupsData.save();
                LockController.invalidateSharedMembersCache(null);
                HopperCache.invalidate();
                return true;
        }

        private static void indexMembers(String normalizedGroup, Set<String> members) {
                for (String member : members) {
                        groupByPlayer.put(normalizePlayer(member), normalizedGroup);
                }
        }
}
