package com.zephyrr.werewolf.enums;

import com.zephyrr.werewolf.Werewolf;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;

/**
 *
 * @author Phoenix
 */
public enum HouseLevers {

    HOUSE1(-261, 69, 305),
    HOUSE2(-261, 69, 294),
    HOUSE3(-273, 69, 300),
    HOUSE4(-275, 69, 290),
    HOUSE5(-275, 69, 279),
    HOUSE6(-275, 70, 279),
    HOUSE7(-264, 70, 317),
    HOUSE8(-274, 70, 330),
    HOUSE9(-248, 69, 379),
    HOUSE10(-246, 69, 362),
    HOUSE11(-266, 69, 365),
    HOUSE12(-253, 69, 362),
    HOUSE13(-254, 70, 349),
    HOUSE14(-267, 70, 351),
    HOUSE15(-235, 69, 370),
    HOUSE16(-235, 69, 379);
    private Block lever;

    HouseLevers(double x, double y, double z) {
        lever = Werewolf.getWolfWorld().getBlockAt(new Location(Werewolf.getWolfWorld(), x, y, z));
    }

    public boolean isPowered() {
        return (lever.getData() & 0x8) > 0;
    }

    public void power(boolean toPower) {
        if (toPower != isPowered()) {
            net.minecraft.server.Block nmsBlock = net.minecraft.server.Block.byId[Material.LEVER.getId()];
            net.minecraft.server.World nmsWorld = ((CraftWorld) lever.getWorld()).getHandle();
            nmsBlock.interact(nmsWorld, lever.getX(), lever.getY(), lever.getZ(), null);
            BlockFace dir = null;
            for(BlockFace val : BlockFace.values()) {
                if(lever.getRelative(val).getType() == Material.REDSTONE_LAMP_OFF ||
                        lever.getRelative(val).getType() == Material.REDSTONE_LAMP_ON) {
                    dir = val;
                    break;
                }
            }
            Block center = lever.getRelative(dir).getRelative(dir);
            for (BlockFace b : BlockFace.values()) {
                if (center.getRelative(b).getType() == Material.REDSTONE_LAMP_ON && !toPower) {
                    center.getRelative(b).setType(Material.REDSTONE_LAMP_OFF);
                } else if (center.getRelative(b).getType() == Material.REDSTONE_LAMP_OFF && toPower) {
                    center.getRelative(b).setType(Material.REDSTONE_LAMP_ON);
                }
            }
        }
    }

    public Block getBlock() {
        return lever;
    }
}
