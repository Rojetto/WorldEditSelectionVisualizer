package com.rojel.wesv;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditHelper implements Listener {
    private final JavaPlugin plugin;
    private final WorldEditPlugin we;
    private final Map<UUID, Region> lastSelectedRegions;

    public WorldEditHelper(final JavaPlugin plugin, final Configuration config) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        this.lastSelectedRegions = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (config.isEnabled(player) && player.hasPermission("wesv.use")) {
                        Region lastRegion = lastSelectedRegions.get(player.getUniqueId());
                        Region currentRegion = getSelectedRegion(player);

                        if (!compareRegion(lastRegion, currentRegion)) {
                            if (currentRegion != null)
                                lastSelectedRegions.put(player.getUniqueId(), currentRegion.clone());
                            else
                                lastSelectedRegions.remove(player.getUniqueId());

                            plugin.getServer().getPluginManager().callEvent(new WorldEditSelectionChangeEvent(player, currentRegion));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, config.updateSelectionInterval());
    }

    public Region getSelectedRegion(Player player) {
        LocalSession session = we.getWorldEdit().getSessionManager().findByName(player.getName());

        if (session != null && session.getSelectionWorld() != null) {
            World world = session.getSelectionWorld();
            RegionSelector selector = session.getRegionSelector(world);

            if (selector.isDefined()) {
                try {
                    return selector.getRegion();
                } catch (IncompleteRegionException e) {
                    plugin.getServer().getLogger().info("Region still incomplete.");
                }
            }
        }

        return null;
    }

    public boolean compareRegion(Region r1, Region r2) {
        if (r1 == null && r2 == null)
            return true;
        else if (r1 != null && r2 == null)
            return false;
        else if (r1 == null && r2 != null)
            return false;
        else {
            boolean points = r1.getMinimumPoint().equals(r2.getMinimumPoint()) && r1.getMaximumPoint().equals(r2.getMaximumPoint()) && r1.getCenter().equals(r2.getCenter());
            boolean worlds = r1.getWorld() != null ? r1.getWorld().equals(r2.getWorld()) : r2.getWorld() == null;
            boolean dimensions = r1.getWidth() == r2.getWidth() && r1.getHeight() == r2.getHeight() && r1.getLength() == r2.getLength();
            boolean type = r1.getClass().equals(r2.getClass());
            boolean polyPoints = true;
            if (r1 instanceof Polygonal2DRegion && r2 instanceof Polygonal2DRegion) {
                Polygonal2DRegion r1Poly = (Polygonal2DRegion) r1;
                Polygonal2DRegion r2Poly = (Polygonal2DRegion) r2;

                if (r1Poly.getPoints().size() != r2Poly.getPoints().size()) {
                    polyPoints = false;
                } else {
                    for (int i = 0; i < r1Poly.getPoints().size(); i++)
                        if (!r1Poly.getPoints().get(i).equals(r2Poly.getPoints().get(i)))
                            polyPoints = false;
                }
            }

            return (points && worlds && dimensions && type && polyPoints);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        lastSelectedRegions.remove(event.getPlayer().getUniqueId());
    }
}
