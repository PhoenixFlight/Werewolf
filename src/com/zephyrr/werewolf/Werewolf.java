package com.zephyrr.werewolf;

import com.zephyrr.werewolf.enums.HouseLevers;
import com.zephyrr.werewolf.enums.WolfStage;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phoenix
 */
public class Werewolf extends JavaPlugin {
    private static WolfGame activeGame;
    private static Werewolf regulator;
    public void onEnable() {
        regulator = this;
        activeGame = new WolfGame(this);
        activeGame.setStage(WolfStage.OFF);
        if(!new File("plugins/Werewolf/config.yml").exists())
            saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
    }

    public static World getWolfWorld() {
        return regulator.getServer().getWorld("werewolf");
    }

    public static Werewolf getPlugin() {
        return regulator;
    }

    public static WolfGame getActiveGame() {
        return activeGame;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("wolf"))
            return false;
        if(args.length == 0)
            return false;
        String cmdName = args[0];
        if(cmdName.equalsIgnoreCase("start")) {
            if(activeGame != null && activeGame.getStage() != WolfStage.OFF) {
                sender.sendMessage(ChatColor.RED + "[Wolf] There is already an active game!");
            } else {
                getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] A new game of Werewolf has begun!");
                getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] Type " + ChatColor.GREEN + "/wolf JOIN" + ChatColor.GOLD + " to join the game!");
                getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] You have 45 seconds to join.");
                getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                    public void run() {
                        if(activeGame.playerCount() < getConfig().getInt("minPlayers")) {
                            getServer().broadcastMessage(ChatColor.GOLD + "[Wolf] " + ChatColor.RED + "There are not enough people to begin a game.");
                            for(int i = 0; i < WolfGame.MAXPLAYERS; i++)
                                HouseLevers.valueOf("HOUSE" + (i + 1)).power(false);
                            activeGame.setStage(WolfStage.OFF);
                        } else {
                            activeGame.setStage(WolfStage.DETERMINEROLES);
                            getCommand("listPlayers").setExecutor(activeGame);
                            getCommand("vision").setExecutor(activeGame);
                            getCommand("vote").setExecutor(activeGame);
                            getCommand("priest").setExecutor(activeGame);
                        }
                    }
                }, 900L);
                activeGame = new WolfGame(this);
            }
            return true;
        } else if(cmdName.equalsIgnoreCase("join")) {
            if(activeGame.getStage() == WolfStage.JOINING) {
                if(sender instanceof Player) {
                    activeGame.addPlayer((Player)sender);
                } else sender.sendMessage(ChatColor.RED + "[Wolf] You must be a player in order to join.");
            } else if(activeGame.getStage() == WolfStage.OFF) {
                sender.sendMessage(ChatColor.RED + "[Wolf] There are no games of Werewolf running right now.  You can start one with /wolf START");
            } else sender.sendMessage(ChatColor.RED + "[Wolf] There is already a game of Werewolf running.");
            return true;
        }
        return false;
    }

    public void onDisable() {}
}
