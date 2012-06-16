package com.zephyrr.werewolf.Players;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public class Wolf extends WolfPlayer {
    private Player player;
    public Wolf(Player p, int id) {
        super(p, id);
        p.sendMessage(ChatColor.GOLD + "[Wolf] You are a " + ChatColor.RED + "Wolf!");
    }

    @Override
    public void showInstructions() {
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Venture forth into the dark night, and seek out your prey!");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Climb up the ladder behind your victim's house, and flip the switch to bring their death!");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] Worry not about being seen, as only your kin and Visionaries can see you!");
        getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] However, not even Visionaries can hear you conversing with your fellow wolves.");
    }
}
