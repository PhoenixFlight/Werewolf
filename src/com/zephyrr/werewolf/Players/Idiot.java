package com.zephyrr.werewolf.Players;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public class Idiot extends WolfPlayer {
    public Idiot(Player p, int id) {
        super(p, id);
        p.sendMessage(ChatColor.GOLD + "[Wolf] You are the " + ChatColor.RED + "Village Idiot!");
        p.sendMessage(ChatColor.GOLD + "[Wolf] Your objective is to be lynched by your fellow villagers.");
        p.sendMessage(ChatColor.GOLD + "[Wolf] If you're killed by a Wolf, however, you lose.");
    }
    public void showInstructions() {}
}
