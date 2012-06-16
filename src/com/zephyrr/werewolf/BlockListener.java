package com.zephyrr.werewolf;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Phoenix
 */
public class BlockListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getWorld() == Werewolf.getWolfWorld())
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getWorld() == Werewolf.getWolfWorld())
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockCombust(BlockIgniteEvent event) {
        if(event.getBlock().getWorld() != Werewolf.getWolfWorld())
            return;
        if(event.getBlock().getType() != Material.NETHERRACK)
            event.setCancelled(true);
    }
}
