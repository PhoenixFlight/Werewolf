package com.zephyrr.werewolf.Players;

import com.zephyrr.werewolf.enums.HouseLevers;
import com.zephyrr.werewolf.enums.WarpPoint;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public abstract class WolfPlayer {
    private Player player;
    private int id;
    private int votes;
    private boolean isProtected;
    private boolean isAlive;
    private boolean canCastCmd;
    private boolean isFrozen;
    public WolfPlayer(Player p, int id) {
        player = p;
        isAlive = true;
        isFrozen = false;
        isProtected = false;
        canCastCmd = false;
    }
    public boolean isProtected() {
        return isProtected;
    }
    public void setProtected(boolean val) {
        isProtected = val;
    }
    public boolean canCastCmd() {
        return canCastCmd;
    }
    public void enableCmd() {
        canCastCmd = true;
    }
    public Player getPlayer() {
        return player;
    }
    public boolean isAlive() {
        return isAlive;
    }
    public int getVotes() {
        return votes;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
    public void freeze(boolean val) {
        isFrozen = val;
    }
    public boolean isFrozen() {
        return isFrozen;
    }
    public void kill() {
        isAlive = false;
        HouseLevers.valueOf("HOUSE" + (id + 1)).power(false);
        player.damage(5000, player.getWorld().spawnArrow(player.getLocation(), player.getVelocity(), 0, 0));
        player.teleport(WarpPoint.valueOf("HOUSE" + (id + 1)).getLocation());
    }
    public void onCommand(Command cmnd, String[] strings) {
        canCastCmd = false;
    }

    public abstract void showInstructions();
}
