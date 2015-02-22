package com.rojel.wesv;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditSelectionVisualizer extends JavaPlugin implements Listener {
    private Configuration config;
    private WorldEditHelper worldEditHelper;
    private ShapeHelper shapeHelper;
    private ParticleSender particleSender;

    private Map<UUID, Boolean> shown;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
        shown = new HashMap<>();

        config = new Configuration(this);
        config.load();

        worldEditHelper = new WorldEditHelper(this, config);
        shapeHelper = new ShapeHelper(config);
        particleSender = new ParticleSender(this, config);

        new CustomMetrics(this, config).initMetrics();
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
    public void onWorldEditSelectionChange(WorldEditSelectionChangeEvent event) {
        Player player = event.getPlayer();

        if (isSelectionShown(player))
            showSelection(player);
    }
	
	@EventHandler
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		
		if (config.checkForAxe() && config.isEnabled(player)) {
			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			
			if (item != null && item.getType() == config.selectionItem())
				showSelection(player);
			else
				hideSelection(player);
		}
	}

    public boolean holdsSelectionItem(Player player) {
        ItemStack item = player.getItemInHand();

        return item != null && item.getType() == config.selectionItem();
    }

    public boolean isSelectionShown(Player player) {
        return shown.containsKey(player.getUniqueId()) ? shown.get(player.getUniqueId()) : config.isEnabled(player) && holdsSelectionItem(player);
    }

    public void showSelection(Player player) {
        if (!player.hasPermission("wesv.use"))
            return;
        shown.put(player.getUniqueId(), true);
        particleSender.setParticlesForPlayer(player, shapeHelper.getLocationsFromRegion(worldEditHelper.getSelectedRegion(player)));
    }

    public void hideSelection(Player player) {
        shown.put(player.getUniqueId(), false);
        particleSender.setParticlesForPlayer(player, null);
    }
}
