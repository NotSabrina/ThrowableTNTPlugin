package me.supramental.throwabletnt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ThrowableTNT extends JavaPlugin implements Listener {

    private double defaultVelocity = 1.5; // Default velocity multiplier
    private double maxThrowDistance = 20.0; // Default max throw distance
    private String permission = "throwtnt.throw"; // Permission node
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Load config
        config = getConfig();

        // Register events and commands
        getServer().getPluginManager().registerEvents(this, this);

        // Load settings from config
        loadConfigSettings();
    }

    private void loadConfigSettings() {
        // Load velocity from config
        if (config.contains("velocity")) {
            defaultVelocity = config.getDouble("velocity");
        } else {
            config.set("velocity", defaultVelocity);
            saveConfig();
        }

        // Load max throw distance from config
        if (config.contains("max-throw-distance")) {
            maxThrowDistance = config.getDouble("max-throw-distance");
        } else {
            config.set("max-throw-distance", maxThrowDistance);
            saveConfig();
        }

        // Load disabled worlds from config
        if (config.contains("disabled-worlds")) {
            for (String world : config.getStringList("disabled-worlds")) {
                getLogger().info("Disabled world: " + world);
            }
        } else {
            config.set("disabled-worlds", Bukkit.getWorlds().get(0).getName()); // Example: default to first loaded world
            saveConfig();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(permission)) {
            return;
        }

        // Check if player right-clicks with TNT
        if (event.getAction().toString().contains("RIGHT") && event.getItem() != null && event.getItem().getType() == Material.TNT) {
            // Check if player is in a disabled world
            if (config.getStringList("disabled-worlds").contains(player.getWorld().getName())) {
                return;
            }

            // Check if distance to target is within max throw distance
            double distance = player.getLocation().distance(event.getPlayer().getLocation());
            if (distance > maxThrowDistance) {
                player.sendMessage("TNT throw distance exceeded!");
                return;
            }

            // Remove one TNT from player's inventory
            ItemStack item = event.getItem();
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().remove(item);
            }

            // Calculate velocity based on config
            double velocityMultiplier = config.getDouble("velocity", defaultVelocity);
            Vector direction = player.getLocation().getDirection().multiply(velocityMultiplier);

            // Spawn primed TNT entity
            TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);
            tnt.setVelocity(direction);
        }
    }
}
