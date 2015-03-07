package com.rojel.wesv;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditSelectionVisualizer extends JavaPlugin implements Listener {
    private Configuration config;
    private WorldEditHelper worldEditHelper;
    private ProtocolLibHelper protocolLibHelper;
    private ShapeHelper shapeHelper;
    private ParticleSender particleSender;

    private Map<UUID, Boolean> shown;
    private Map<UUID, Boolean> lastSelectionTooLarge;

    @Override
    public void onEnable() {
        shown = new HashMap<>();
        lastSelectionTooLarge = new HashMap<>();

        config = new Configuration(this);
        config.load();

        worldEditHelper = new WorldEditHelper(this, config);
        protocolLibHelper = new ProtocolLibHelper(this, config);
        shapeHelper = new ShapeHelper(config);
        particleSender = new ParticleSender(this, config, protocolLibHelper);

        new CustomMetrics(this, config).initMetrics();

        getServer().getPluginManager().registerEvents(this, this);

        if (config.useProtocolLib() && !protocolLibHelper.isProtocolLibInstalled())
            getLogger().info("ProtocolLib is enabled in the config but not installed. You need to install ProtocolLib if you want to use certain features.");

        if (config.particleDistance() > 16 && !protocolLibHelper.canUseProtocolLib())
            getLogger().info("Particle distances > 16 only work with ProtocolLib. Set \"useProtocolLib\" in the config to \"true\" and/or install ProtocolLib.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equals("wesv")) {
                if (player.hasPermission("wesv.toggle")) {
                    boolean isEnabled = !config.isEnabled(player);
                    config.setEnabled(player, isEnabled);
                    if (isEnabled) {
                        player.sendMessage(ChatColor.DARK_GREEN + "Your WorldEditSelectionVisualizer has been enabled.");
                        if (shouldShowSelection(player))
                            showSelection(player);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Your WorldEditSelectionVisualizer has been disabled.");
                        hideSelection(player);
                    }
                }
                return true;
            }
        } else {
            sender.sendMessage("Only a player can toggle his WESV.");
            return true;
        }

        return false;
    }

    @EventHandler
    private void onWorldEditSelectionChange(WorldEditSelectionChangeEvent event) {
        Player player = event.getPlayer();

        if (isSelectionShown(player)) {
            showSelection(player);
        }
    }

    @EventHandler
    private void onItemChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (config.checkForAxe() && config.isEnabled(player)) {
            ItemStack item = player.getInventory().getItem(event.getNewSlot());

            if (item != null && item.getType() == config.selectionItem())
                showSelection(player);
            else
                hideSelection(player);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        shown.remove(event.getPlayer().getUniqueId());
        lastSelectionTooLarge.remove(event.getPlayer().getUniqueId());
    }

    public boolean holdsSelectionItem(Player player) {
        ItemStack item = player.getItemInHand();

        return item != null && item.getType() == config.selectionItem();
    }

    public boolean isSelectionShown(Player player) {
        return shown.containsKey(player.getUniqueId()) ? shown.get(player.getUniqueId()) : shouldShowSelection(player);
    }

    public boolean shouldShowSelection(Player player) {
        return config.isEnabled(player) && (!config.checkForAxe() || (config.checkForAxe() && holdsSelectionItem(player)));
    }

    public void showSelection(Player player) {
        if (!player.hasPermission("wesv.use"))
            return;

        Region region = worldEditHelper.getSelectedRegion(player);

        if (region != null && region.getArea() > config.maxSize()) {
            particleSender.setParticlesForPlayer(player, null);
            if (!lastSelectionTooLarge.get(player.getUniqueId()))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "The visualizer only works with selections up to a size of " + config.maxSize() + " blocks.");
            lastSelectionTooLarge.put(player.getUniqueId(), true);
        } else {
            lastSelectionTooLarge.put(player.getUniqueId(), false);
            particleSender.setParticlesForPlayer(player, shapeHelper.getLocationsFromRegion(region));
        }

        shown.put(player.getUniqueId(), true);
    }

    public void hideSelection(Player player) {
        shown.put(player.getUniqueId(), false);
        particleSender.setParticlesForPlayer(player, null);
    }
}
