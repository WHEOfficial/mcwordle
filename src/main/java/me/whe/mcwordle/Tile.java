package me.whe.mcwordle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.util.Random;

public class Tile {

    private final Location center;
    private final int width;
    private final int height;
    private final int zOffset;
    private final int yOffset;

    private final World world = Bukkit.getWorlds().get(0);


    public Tile(Location center, int width, int height) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.zOffset = width / 2;
        this.yOffset = height / 2;
    }

    public void createTile(char c) {
        fillTile(Material.BLACK_CONCRETE);
        setLetter(c);
    }

    public void destroyTile() {
        fillTile(Material.AIR);
        setLetter('\0');
    }

    public void fillTile(Material material) {
        for (int z = (int) center.z() - zOffset; z <= center.z() + zOffset; z++) {
            for (int y = (int) center.y() - yOffset; y <= center.y() + zOffset; y++) {
                Location location = new Location(world, center.x(), y, z);
                world.getBlockAt(location).setType(material);
            }
        }
    }

    public void setLetter(char c) {
        if (c == '\0') {
            for (int z = (int) center.z() - zOffset; z <= center.z() + zOffset; z++) {
                for (int y = (int) center.y() - yOffset; y <= center.y() + zOffset; y++) {
                    Location location = new Location(world, center.x() - 1, y, z);
                    world.getBlockAt(location).setType(Material.AIR);
                }
            }
        } else {
            Structure structure = WordleController.getLetterStructures().get(c);
            Location letterLocation = center.clone().subtract(new Vector(1, 1, 1));
            structure.place(letterLocation, false, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
    }

}
