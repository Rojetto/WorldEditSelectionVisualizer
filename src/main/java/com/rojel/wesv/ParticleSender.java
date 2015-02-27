package com.rojel.wesv;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleSender {
    private JavaPlugin plugin;
    private Configuration config;
    private Map<UUID, Collection<Location>> playerParticleMap;

    public ParticleSender(JavaPlugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
        playerParticleMap = new HashMap<>();

        startSending();
    }

    public void setParticlesForPlayer(Player player, Collection<Location> locations) {
        playerParticleMap.put(player.getUniqueId(), locations);
        if (locations == null || locations.size() == 0)
            playerParticleMap.remove(player.getUniqueId());
    }

    private void startSending() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playerParticleMap.keySet()) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    for (Location loc : playerParticleMap.get(uuid)) {
                        if (loc.getWorld().equals(player.getLocation().getWorld()) && loc.distance(player.getLocation()) <= 16)
                            config.particle().display(0, 0, 0, 0, 1, loc, player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, config.updateParticlesInterval());
    }
}
