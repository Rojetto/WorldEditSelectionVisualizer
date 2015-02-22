package com.rojel.wesv;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditHelper {
    private final WorldEditSelectionVisualizer plugin;
    private final WorldEditPlugin we;
    private final Map<UUID, Region> lastSelectedRegions;

    public WorldEditHelper(final WorldEditSelectionVisualizer plugin) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        lastSelectedRegions = new HashMap<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.config().isEnabled(player) && player.hasPermission("wesv.use")) {
                        Region lastRegion = lastSelectedRegions.get(player.getUniqueId());
                        Region currentRegion = getSelectedRegion(player);

                        if (!compareRegion(lastRegion, currentRegion)) {
                            if (currentRegion != null)
                                lastSelectedRegions.put(player.getUniqueId(), currentRegion.clone());
                            else
                                lastSelectedRegions.remove(player.getUniqueId());

                            plugin.displaySelection(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, plugin.config().updateSelectionInterval());
    }

    public Region getSelectedRegion(Player player) {
        LocalSession session = we.getWorldEdit().getSessionManager().findByName(player.getDisplayName());

        if (session != null) {
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
        else
            return (r1.getMinimumPoint().equals(r2.getMinimumPoint()) && r1.getMaximumPoint().equals(r2.getMaximumPoint()) && r1.getCenter().equals(r2.getCenter()) && r1.getWorld().equals(r2.getWorld()) && r1.getWidth() == r2.getWidth() && r1.getHeight() == r2.getHeight() && r1.getLength() == r2.getLength() && r1.getClass().equals(r2.getClass()));
    }
}
