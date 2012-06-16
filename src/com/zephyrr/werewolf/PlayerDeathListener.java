package com.zephyrr.werewolf;

import com.zephyrr.werewolf.Players.Wolf;
import com.zephyrr.werewolf.Players.WolfPlayer;
import com.zephyrr.werewolf.enums.HouseLevers;
import com.zephyrr.werewolf.enums.WolfStage;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Phoenix
 */
public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player) || event.getEntity().getWorld() != Werewolf.getWolfWorld())
            return;

        if(event.getCause() != DamageCause.PROJECTILE)
            event.setCancelled(true);

        if(event.getDamage() != 5000 && Werewolf.getActiveGame().getStage() != WolfStage.EXECUTION)
            event.setCancelled(true);

    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.getFrom().getWorld() != Werewolf.getWolfWorld())
            return;
        int index = Werewolf.getActiveGame().findPlayer(event.getPlayer());
        if(index == -1)
            return;
        WolfPlayer wp = Werewolf.getActiveGame().getAtIndex(index);
        if(wp == null)
            return;
        if(!wp.isFrozen())
            return;
        if(event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ())
            event.setCancelled(true);
    }
    @EventHandler
    public void onLeverClicked(PlayerInteractEvent event) {
        int index = -1;

        if(event.getPlayer().getWorld() != Werewolf.getWolfWorld())
            return;
        if(event.getClickedBlock() == null)
            return;
        Material mat = event.getClickedBlock().getType();
        if(mat == Material.WOODEN_DOOR ||
                mat == Material.WOOD_DOOR ||
                mat == Material.IRON_DOOR ||
                mat == Material.IRON_DOOR_BLOCK)
            event.setCancelled(true);
        for(int i = 0; i < HouseLevers.values().length && index == -1; i++) {
            HouseLevers hl = HouseLevers.values()[i];
            if(hl.getBlock().equals(event.getClickedBlock())) {
                index = i;
            }
        }
        if(index == -1)
            return;
        if(Werewolf.getActiveGame().getStage() != WolfStage.NIGHT)
            event.setCancelled(true);
        WolfPlayer toKill = Werewolf.getActiveGame().getAtIndex(index);
        if(toKill == null || !toKill.isAlive()) {
            event.setCancelled(true);
            return;
        }
        if(toKill instanceof Wolf) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "[Wolf] You can't kill another wolf!");
            event.setCancelled(true);
            return;
        }
        if(toKill.isProtected()) {
            toKill.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] The wolves attempted to kill " + toKill.getPlayer().getName() + ", but the Priest saved them.");
            event.setCancelled(true);
        } else {
            toKill.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + toKill.getPlayer().getName() + " was killed by the wolves!");
            toKill.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + toKill.getPlayer().getName() + " was a " + toKill.getClass().getSimpleName());
            toKill.kill();
        }
        Werewolf.getActiveGame().setStage(WolfStage.DISCUSSION);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        int index = Werewolf.getActiveGame().findPlayer(event.getPlayer());
        if(index == -1)
            return;
        WolfPlayer wp = Werewolf.getActiveGame().getAtIndex(index);
        if(wp == null) {
            Werewolf.getActiveGame().removePlayer(index);
            return;
        }
        wp.kill();
        event.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + event.getPlayer().getName() + " was a " + wp.getClass().getSimpleName());
        Werewolf.getActiveGame().end(Werewolf.getActiveGame().isOver());
    }
    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        int index = Werewolf.getActiveGame().findPlayer(event.getEntity());
        if(index == -1)
            return;
        Werewolf.getActiveGame().getAtIndex(index).kill();
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTalk(PlayerChatEvent event) {
        if(event.getPlayer().getWorld() != Werewolf.getWolfWorld())
            return;
        int index = Werewolf.getActiveGame().findPlayer(event.getPlayer());
        if(index == -1)
            return;
        if(Werewolf.getActiveGame().getStage() != WolfStage.NIGHT)
            return;
        WolfPlayer wp = Werewolf.getActiveGame().getAtIndex(index);
        if(wp == null)
            return;
        if(!(wp instanceof Wolf))
            event.setCancelled(false);
        HashSet<Player> toRemove = new HashSet<Player>();
        for(Player p : event.getRecipients()) {
            int targIndex = Werewolf.getActiveGame().findPlayer(p);
            if(targIndex == -1)
                continue;
            WolfPlayer targ = Werewolf.getActiveGame().getAtIndex(targIndex);
            if(!(targ instanceof Wolf))
                toRemove.add(p);
        }
        event.getRecipients().removeAll(toRemove);
    }
}
