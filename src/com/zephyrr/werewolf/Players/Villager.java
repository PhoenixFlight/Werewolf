package com.zephyrr.werewolf.Players;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public class Villager extends WolfPlayer {
    public Villager(Player p, int id) {
        super(p, id);
        p.sendMessage(ChatColor.GOLD + "[Wolf] You are a " + ChatColor.RED + "Villager!");
    }
    @Override
    public void showInstructions() {}

}
