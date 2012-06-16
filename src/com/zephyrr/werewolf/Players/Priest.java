package com.zephyrr.werewolf.Players;

import com.zephyrr.werewolf.Werewolf;
import com.zephyrr.werewolf.WolfGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public class Priest extends WolfPlayer {

    public Priest(Player p, int index) {
        super(p, index);
        p.sendMessage(ChatColor.GOLD + "[Wolf] You are a " + ChatColor.RED + "Priest!");
    }

    @Override
    public void showInstructions() {
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Night has fallen... the wolves are hunting.");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Quickly!  Choose someone to protect, before they take another life!");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Surely you, as a man of God, will choose to put the lives of your brethren above your own.");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Type " + ChatColor.GREEN + "/priest " + ChatColor.RED + "<TARGET_ID>" + ChatColor.GOLD + " to protect someone.");
    }

    @Override
    public void onCommand(Command cmnd, String[] args) {
        if (cmnd.getName().equalsIgnoreCase("priest")) {
            if (args.length == 0) {
                return;
            }
            int target = -1;
            try {
                target = Integer.parseInt(args[0]);
            } catch (Exception e) {
                for (int q = 0; q < WolfGame.MAXPLAYERS && target == -1; q++) {
                    if (Werewolf.getActiveGame().getAtIndex(q).getPlayer().getName().equalsIgnoreCase(args[0])) {
                        target = q + 1;
                    }
                }
            }
            if (target == -1) {
                return;
            }
            if (target <= 0 || target > WolfGame.MAXPLAYERS) {
                return;
            }
            WolfPlayer toSee = Werewolf.getActiveGame().getPlayer(target - 1);
            if (toSee == null || toSee == this || !toSee.isAlive()) {
                return;
            }
            toSee.setProtected(true);
            getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] You have protected " + toSee.getPlayer().getName());
            toSee.getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] You have been protected by a priest!");
        }
        super.onCommand(cmnd, args);
    }
}
