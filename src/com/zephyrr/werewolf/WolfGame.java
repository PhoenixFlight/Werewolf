package com.zephyrr.werewolf;

import com.zephyrr.werewolf.Players.*;
import com.zephyrr.werewolf.enums.HouseLevers;
import com.zephyrr.werewolf.enums.WarpPoint;
import com.zephyrr.werewolf.enums.WolfStage;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Phoenix
 */
public class WolfGame implements CommandExecutor {

    public static int MAXPLAYERS = 16;
    private Werewolf regulator;
    private WolfStage gameStage;
    private Player[] players;
    private WolfPlayer[] wolfPlayers;

    public WolfGame(Werewolf regulator) {
        this.regulator = regulator;
        players = new Player[MAXPLAYERS];
        wolfPlayers = new WolfPlayer[MAXPLAYERS];
        for (HouseLevers hl : HouseLevers.values()) {
            hl.power(false);
        }
        gameStage = WolfStage.JOINING;
    }

    public WolfStage getStage() {
        return gameStage;
    }

    public int playerCount() {
        int count = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (players[i] != null) {
                count++;
            }
        }
        return count;
    }

    public int wolfPlayerCount() {
        int count = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (wolfPlayers[i] != null && wolfPlayers[i].isAlive()) {
                count++;
            }
        }
        return count;
    }

    private void determineRoles() {
        for(int i = 0; i < MAXPLAYERS; i++) {
            if(players[i] == null)
                HouseLevers.valueOf("HOUSE" + (i + 1)).power(false);
        }
        int count = playerCount();
        int wolfCount = 1;
        if (count > 10) {
            wolfCount = 3;
        } else if (count > 5) {
            wolfCount = 2;
        }
        do {
            int index = (int) (Math.random() * count);
            if (wolfPlayers[index] == null) {
                wolfCount--;
                wolfPlayers[index] = new Wolf(players[index], index);
            }
        } while (wolfCount > 0);

        if (regulator.getConfig().getBoolean("enable-priest")) {
            int priestCount = (int) (Math.random() * 3);
            while (priestCount > 0) {
                int index = (int) (Math.random() * count);
                if (wolfPlayers[index] == null) {
                    priestCount--;
                    wolfPlayers[index] = new Priest(players[index], index);
                }
            }
        }
        if (regulator.getConfig().getBoolean("enable-visionary")) {
            int visionCount = (int) (Math.random() * 2);
            while (visionCount > 0) {
                int index = (int) (Math.random() * count);
                if (wolfPlayers[index] == null) {
                    visionCount--;
                    wolfPlayers[index] = new Visionary(players[index], index);
                }
            }
        }
        if (regulator.getConfig().getBoolean("enable-idiot")) {
            int idiotCount = (int) (Math.random() * 2);
            while (idiotCount > 0) {
                int index = (int) (Math.random() * count);
                if (wolfPlayers[index] == null) {
                    idiotCount--;
                    wolfPlayers[index] = new Idiot(players[index], index);
                }
            }
        }
        for (int i = 0; i < count; i++) {
            if (wolfPlayers[i] == null) {
                wolfPlayers[i] = new Villager(players[i], i);
            }
        }
        regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] Roles have been distributed.  Let the games begin!");
        setStage(WolfStage.DISCUSSION);
    }

    public void setStage(WolfStage stage) {
        switch (stage) {
            case DETERMINEROLES:
                determineRoles();
                break;
            case NIGHT:
                regulator.getWolfWorld().setFullTime(14000L);
                for(WolfPlayer wp : wolfPlayers) {
                    if(wp != null && wp.isAlive()) {
                        wp.showInstructions();
                        if(wp instanceof Wolf) {
                            for(WolfPlayer p2 : wolfPlayers) {
                                if(p2 != null && !(p2 instanceof Wolf))
                                    p2.getPlayer().hidePlayer(wp.getPlayer());
                            }
                            wp.getPlayer().teleport(WarpPoint.TOWNSQUARE.getLocation());
                        }
                    }
                }
                break;
            case DISCUSSION:
                for(WolfPlayer wp : wolfPlayers) {
                    if(wp != null && wp instanceof Wolf) {
                        wp.getPlayer().teleport(WarpPoint.valueOf("HOUSE" + (findPlayer(wp) + 1)).getLocation());
                        for(WolfPlayer p2 : wolfPlayers) {
                            if(p2 != null && p2 != wp)
                                p2.getPlayer().showPlayer(wp.getPlayer());
                        }
                    }
                }
                if(isOver() != 0) {
                    end(isOver());
                    return;
                }
                regulator.getWolfWorld().setFullTime(5000L);
                for (WolfPlayer wp : wolfPlayers) {
                    if (wp != null && wp.isAlive()) {
                        wp.freeze(false);
                        wp.enableCmd();
                        wp.setProtected(false);
                        wp.getPlayer().teleport(WarpPoint.TOWNSQUARE.getLocation());
                    }
                }
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] You will be given 60 seconds to discuss who you wish to lynch.");
                regulator.getServer().getScheduler().scheduleAsyncDelayedTask(regulator, new Runnable() {

                    public void run() {
                        setStage(WolfStage.VOTING);
                    }
                }, 1200);
                break;
            case OFF:
                regulator.getWolfWorld().setFullTime(5000L);
                for (int i = 0; i < MAXPLAYERS; i++) {
                    players[i] = null;
                    wolfPlayers[i] = null;
                }
                break;
            case VOTING:
                regulator.getWolfWorld().setFullTime(-1500L);
                lightNetherrack();
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] The voting period has begun.");
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] You have 45 seconds to cast your vote via " + ChatColor.GREEN + "/vote " + ChatColor.RED + "<TARGET_ID>");
                regulator.getServer().getScheduler().scheduleAsyncDelayedTask(regulator, new Runnable() {
                    public void run() {
                        setStage(WolfStage.EXECUTION);
                    }
                }, 900L);
                break;
            case EXECUTION:
                extNetherrack();
                execution();
                break;

        }
        for (WolfPlayer wp : wolfPlayers) {
            if (wp != null && wp.isAlive()) {
                wp.enableCmd();
            }
        }
        gameStage = stage;
    }

    private void lightNetherrack() {
        for (int x = -208; x >= -228; x--) {
            for (int z = 308; z <= 328; z++) {
                Block b = regulator.getWolfWorld().getBlockAt(x, 62, z);
                if (b.getType() == Material.NETHERRACK) {
                    b.getRelative(BlockFace.UP).setType(Material.FIRE);
                }
            }
        }
    }

    private void extNetherrack() {
        for (int x = -208; x >= -228; x--) {
            for (int z = 308; z <= 328; z++) {
                Block b = regulator.getWolfWorld().getBlockAt(x, 62, z);
                if (b.getType() == Material.NETHERRACK) {
                    b.getRelative(BlockFace.UP).setType(Material.AIR);
                }
            }
        }
    }

    private void execution() {
        ArrayList<WolfPlayer> mostVotes = new ArrayList<WolfPlayer>();
        WolfPlayer most = wolfPlayers[0];
        for (WolfPlayer wp : wolfPlayers) {
            if(wp == null)
                continue;
            if (most.getVotes() <= wp.getVotes()) {
                if (most.getVotes() < wp.getVotes()) {
                    mostVotes.clear();
                }
                most = wp;
                mostVotes.add(most);
            }
        }
        final WolfPlayer toKill = mostVotes.get((int) (Math.random() * mostVotes.size()));
        toKill.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + toKill.getPlayer().getName() + " will be killed!");
        for (int i = 0; i < wolfPlayers.length; i++) {
            if (wolfPlayers[i] != null && wolfPlayers[i].isAlive()) {
                if (wolfPlayers[i] != toKill) {
                    wolfPlayers[i].getPlayer().getInventory().clear();
                    wolfPlayers[i].getPlayer().getInventory().addItem(new ItemStack(Material.BOW));
                    wolfPlayers[i].getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 5));
                    wolfPlayers[i].getPlayer().teleport(WarpPoint.valueOf("EXECUTIONER" + (i + 1)).getLocation());
                }
                //wolfPlayers[i].freeze(true);
            }
        }
        toKill.getPlayer().teleport(WarpPoint.EXECUTION_STAND.getLocation());
        toKill.freeze(true);
        regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] Ready!");
        regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] Aim!");
        regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] FIRE!");
        regulator.getServer().getScheduler().scheduleAsyncDelayedTask(regulator, new Runnable() {

            public void run() {
                if (toKill.isAlive()) {
                    toKill.kill();
                }
                for (int i = 0; i < MAXPLAYERS; i++) {
                    if (wolfPlayers[i] != null) {
                        wolfPlayers[i].getPlayer().teleport(WarpPoint.valueOf("HOUSE" + (i + 1)).getLocation());
                        if (wolfPlayers[i].isAlive()) {
                            wolfPlayers[i].freeze(false);
                        }
                    }
                }
                toKill.getPlayer().getServer().broadcastMessage(ChatColor.GOLD + toKill.getPlayer().getName() + " was a(n) " + toKill.getClass().getSimpleName());
                if (toKill instanceof Idiot) {
                    end(2);
                } else if (isOver() != 0) {
                    end(isOver());
                } else {
                    setStage(WolfStage.NIGHT);
                }
            }
        }, 600L);
    }

    public int findPlayer(WolfPlayer wolf) {
        for (int i = 0; i < wolfPlayers.length; i++) {
            if (wolfPlayers[i] == wolf) {
                return i;
            }
        }
        return -1;
    }

    public int findPlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }
        return -1;
    }

    public WolfPlayer getAtIndex(int index) {
        return wolfPlayers[index];
    }

    public void end(int cause) {
        if(cause == 0)
            return;
        for (Player p : players) {
            if (p != null) {
                p.teleport(p.getServer().getWorld("world").getSpawnLocation());
            }
        }
        switch (cause) {
            case 1:
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] The wolves are victorious!");
                break;
            case 2:
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] The Village Idiot was victorious!");
                break;
            case 3:
                regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] The villagers were victorious!");
                break;
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            players[i] = null;
            wolfPlayers[i] = null;
        }
        gameStage = WolfStage.OFF;
    }

    public int isOver() {
        int wolfCount = 0;
        int other = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (wolfPlayers[i] != null && wolfPlayers[i] instanceof Wolf && wolfPlayers[i].isAlive()) {
                wolfCount++;
            } else if (wolfPlayers[i] != null && wolfPlayers[i].isAlive()) {
                other++;
            }
        }
        if (wolfCount >= other) {
            return 1;
        } else if (wolfCount == 0) {
            return 3;
        } else {
            return 0;
        }
    }

    public void addPlayer(Player p) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == p) {
                p.sendMessage(ChatColor.RED + "[Wolf] You are already registered in this game.");
                return;
            }
        }
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = p;
                players[i].teleport(WarpPoint.valueOf("HOUSE" + (i + 1)).getLocation());
                HouseLevers.valueOf("HOUSE" + (i + 1)).power(true);
                regulator.getServer().broadcastMessage(ChatColor.GREEN + "[Wolf] " + p.getDisplayName() + " has joined the game as player #" + (i + 1) + ".");
                return;
            }
        }
        p.sendMessage(ChatColor.RED + "[Wolf] This game is already full.  Please wait for the next game or for someone to quit.");
    }

    public WolfPlayer getPlayer(int index) {
        return wolfPlayers[index];
    }

    public void removePlayer(int index) {
        players[index] = null;
        wolfPlayers[index] = null;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (cmnd.getName().equalsIgnoreCase("listPlayers")) {
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (wolfPlayers[i] != null && wolfPlayers[i].isAlive()) {
                    cs.sendMessage(ChatColor.GOLD + "" + (i + 1) + " - " + players[i].getName());
                }
            }
            return true;
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (wolfPlayers[i].getPlayer() == cs) {
                if (cmnd.getName().equalsIgnoreCase("vote") && gameStage == WolfStage.VOTING && strings.length != 0) {
                    if (wolfPlayers[i].canCastCmd()) {
                        int target = -1;
                        try {
                            target = Integer.parseInt(strings[0]);
                        } catch (Exception e) {
                            for(int q = 0; q < MAXPLAYERS && target == -1; q++) {
                                if(players[q].getName().equalsIgnoreCase(strings[0]))
                                    target = q + 1;
                            }
                        }
                        if(target == -1)
                            return true;
                        if (target <= MAXPLAYERS && target > 0 && wolfPlayers[target-1] != null && wolfPlayers[target-1].isAlive()) {
                            wolfPlayers[i].onCommand(cmnd, strings);
                            wolfPlayers[target-1].setVotes(wolfPlayers[target-1].getVotes() + 1);
                            regulator.getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + wolfPlayers[target - 1].getVotes() + " have been cast for " + players[target-1].getName());
                        }
                    }
                    break;
                } else if (cmnd.getName().equalsIgnoreCase("vision") && gameStage == WolfStage.NIGHT && wolfPlayers[i] instanceof Visionary) {
                    wolfPlayers[i].onCommand(cmnd, strings);
                    break;
                } else if (cmnd.getName().equalsIgnoreCase("priest") && gameStage == WolfStage.NIGHT && wolfPlayers[i] instanceof Priest) {
                    wolfPlayers[i].onCommand(cmnd, strings);
                    break;
                }
            }
        }
        return true;
    }
}
