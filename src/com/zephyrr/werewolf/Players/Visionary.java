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
public class Visionary extends WolfPlayer {
    public Visionary(Player p, int id) {
        super(p, id);
        p.sendMessage(ChatColor.GOLD + "[Wolf] You are a " + ChatColor.RED + "Visionary!");
    }

    @Override
    public void showInstructions() {
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Night has fallen... now is your chance to even the odds.");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Type " + ChatColor.GREEN + "/vision " + ChatColor.RED + "<TARGET_ID>" + ChatColor.GOLD + " to see a player's role.");
    }

    public void onCommand(Command cmnd, String[] args) {
        if(cmnd.getName().equalsIgnoreCase("vision")) {
            if(args.length == 0)
                return;
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
            if(target <= 0 || target > WolfGame.MAXPLAYERS)
                return;
            WolfPlayer toSee = Werewolf.getActiveGame().getPlayer(target-1);
            if(toSee == null || toSee == this || !toSee.isAlive())
                return;
            getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] " + toSee.getPlayer().getName() + " has the role of " + ChatColor.RED + toSee.getClass().getSimpleName());
        }
        super.onCommand(cmnd, args);
    }
}
