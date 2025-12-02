package com.watsonllc.chestlock.commands.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;
import com.watsonllc.chestlock.logic.ActionState;

public class RemoveOwner {

        public static boolean logic(Player player, String target, boolean toggle) {
                if(Commands.usePermissions()) {
                        if(!player.hasPermission("chestlock.remove")) {
                                player.sendMessage(Config.getString("messages.noPermission"));
                                return false;
                        }
                }

                if(PlayerStateManager.hasAction(player, PlayerActionType.REMOVE_OWNER)) {
                        String actionMSG = Config.getString("messages.cancelAction");
                        actionMSG = actionMSG.replace("%action%", Config.getString("actions.removeOwner"));
                        player.sendMessage(actionMSG);
                        PlayerStateManager.clearAction(player, PlayerActionType.REMOVE_OWNER);
                        return false;
                }

                PlayerStateManager.startAction(player, PlayerActionType.REMOVE_OWNER, toggle, target);
                String unshareLockTipMSG = Config.getString("messages.unshareLockTip");
                unshareLockTipMSG = unshareLockTipMSG.replace("%target%", target);
                player.sendMessage(unshareLockTipMSG);

                PlayerStateManager.scheduleTimeout(player, PlayerActionType.REMOVE_OWNER, Config.getString("actions.removeOwner"));

                return false;
        }
	
        public static void eventChecker(PlayerInteractEvent event) {
                if(!PlayerStateManager.hasAction(event.getPlayer(), PlayerActionType.REMOVE_OWNER))
                        return;

                if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                Player player = event.getPlayer();
                ActionState state = PlayerStateManager.getAction(player, PlayerActionType.REMOVE_OWNER);
                String target = state != null ? state.getTarget() : null;

                if(!Utils.lockableBlock(event.getClickedBlock())) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.REMOVE_OWNER);
                        return;
                }

                LockController lc = new LockController();
                Location blockLocation = event.getClickedBlock().getLocation();

                if(lc.getAllowedPlayers(blockLocation) == null)
                        return;

                // check if the player owns the lock or can bypass
                if(lc.getOwner(blockLocation).equals(player.getName()) || PlayerStateManager.isBypassing(player)) {
                        event.setCancelled(true);
                        // check if the target player owns the lock
                        if(!lc.getAllowedPlayers(blockLocation).contains(target)) {
                                String invalidShareOwnerMSG = Config.getString("messages.invalidShareOwner");
                                invalidShareOwnerMSG = invalidShareOwnerMSG.replace("%target%", target);
                                player.sendMessage(invalidShareOwnerMSG);
                                PlayerStateManager.clearAction(player, PlayerActionType.REMOVE_OWNER);
                                return;
                        }

                        lc.removeOwner(player, target, blockLocation);
                        String unshareLockMSG = Config.getString("messages.unshareLock");
                        unshareLockMSG = unshareLockMSG.replace("%target%", target);
                        player.sendMessage(unshareLockMSG);

                        if(lc.getAllowedPlayers(blockLocation).isEmpty()) {
                                String lockID = lc.getLockID(event.getClickedBlock().getLocation());
                                String lockType = lc.getLockType(event.getClickedBlock().getLocation());
                                String destroyedLockMSG = Config.getString("messages.destroyedLock");
                                destroyedLockMSG = destroyedLockMSG.replace("%type%", lockType);
                                player.sendMessage(destroyedLockMSG);
                                lc.removeLock(lockID);
                        }

                        if(PlayerStateManager.isToggleEnabled(player, PlayerActionType.REMOVE_OWNER))
                                return;

                        PlayerStateManager.clearAction(player, PlayerActionType.REMOVE_OWNER);
                        return;
                }

                String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
                invalidOwnerMSG = invalidOwnerMSG.replace("%target%", target);
                player.sendMessage(invalidOwnerMSG);
                PlayerStateManager.clearAction(player, PlayerActionType.REMOVE_OWNER);
        }

}