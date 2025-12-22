package com.watsonllc.chestlock.commands.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class ClaimLock {
	
        public static boolean logic(Player player, boolean toggle) {
                if(Commands.usePermissions()) {
                        if(!player.hasPermission("chestlock.claim")) {
                                player.sendMessage(Config.getString("messages.noPermission"));
                                return false;
                        }
                }

                if(PlayerStateManager.hasAction(player, PlayerActionType.CLAIM_LOCK)) {
                        String actionMSG = Config.getString("messages.cancelAction");
                        actionMSG = actionMSG.replace("%action%", Config.getString("actions.claimLock"));
                        player.sendMessage(actionMSG);
                        PlayerStateManager.clearAction(player, PlayerActionType.CLAIM_LOCK);
                        return false;
                }

                PlayerStateManager.startAction(player, PlayerActionType.CLAIM_LOCK, toggle, null);
                String claimLockTipMSG = Config.getString("messages.claimLockTip");
                player.sendMessage(claimLockTipMSG);

                PlayerStateManager.scheduleTimeout(player, PlayerActionType.CLAIM_LOCK, Config.getString("actions.claimLock"));

                return false;
        }
	
        public static void eventChecker(PlayerInteractEvent event) {
                if(!PlayerStateManager.hasAction(event.getPlayer(), PlayerActionType.CLAIM_LOCK))
                        return;

                if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                Player player = event.getPlayer();

                event.setCancelled(true);

                if(!Utils.lockableBlock(event.getClickedBlock())) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.CLAIM_LOCK);
                        return;
                }

                LockController lc = new LockController();

                if(!lc.naturalBlock(event.getClickedBlock().getLocation())) {
                        player.sendMessage(Config.getString("messages.invalidClaimType"));
                        PlayerStateManager.clearAction(player, PlayerActionType.CLAIM_LOCK);
                        return;
                }

                for (Block targetBlock : Utils.getConnectedChestBlocks(event.getClickedBlock())) {
                        if (!lc.naturalBlock(targetBlock.getLocation()))
                                continue;
                        lc.createLock(player, targetBlock);
                }
                String lockType = lc.getLockType(event.getClickedBlock().getLocation());
                String claimLockMSG = Config.getString("messages.claimLock");
                claimLockMSG = claimLockMSG.replace("%type%", lockType);
                player.sendMessage(claimLockMSG);

                if(PlayerStateManager.isToggleEnabled(player, PlayerActionType.CLAIM_LOCK))
                        return;
                PlayerStateManager.clearAction(player, PlayerActionType.CLAIM_LOCK);
        }
}
