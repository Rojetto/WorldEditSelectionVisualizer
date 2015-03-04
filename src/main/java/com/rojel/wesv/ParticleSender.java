package com.rojel.wesv;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleSender implements Listener {
    private final JavaPlugin plugin;
    private final Configuration config;
    private final ProtocolLibHelper protocolLibHelper;
    private final Map<UUID, Collection<Location>> playerParticleMap;

    public ParticleSender(JavaPlugin plugin, Configuration config, ProtocolLibHelper protocolLibHelper) {
        this.plugin = plugin;
        this.config = config;
        this.protocolLibHelper = protocolLibHelper;
        this.playerParticleMap = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

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
                int particleDistance = config.particleDistance();
                if (particleDistance > 16 && !protocolLibHelper.canUseProtocolLib())
                    particleDistance = 16;

                for (UUID uuid : playerParticleMap.keySet()) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    for (Location loc : playerParticleMap.get(uuid)) {
                        if (loc.getWorld().equals(player.getLocation().getWorld()) && loc.distance(player.getLocation()) <= particleDistance) {
                            if (protocolLibHelper.canUseProtocolLib())
                                protocolLibHelper.sendParticle(player, loc);
                            else
                                config.particle().display(0, 0, 0, 0, 1, loc, player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, config.updateParticlesInterval());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        playerParticleMap.remove(event.getPlayer().getUniqueId());
    }
}
