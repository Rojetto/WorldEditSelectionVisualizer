package com.rojel.wesv;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WorldEditSelectionVisualizer extends JavaPlugin implements Listener {
	private Map<UUID, Integer> runningTasks;
    private Configuration config;
    private WorldEditHelper worldEditHelper;
    private ShapeHelper shapeHelper;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
        runningTasks = new HashMap<>();

        config = new Configuration(this);
        config.load();

        worldEditHelper = new WorldEditHelper(this);
        shapeHelper = new ShapeHelper(this);
		
		new CustomMetrics(this).initMetrics();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (label.equals("wesv")) {
				boolean isEnabled = !config.isEnabled(player);
				config.setEnabled(player, isEnabled);
				if (isEnabled) {
					player.sendMessage(ChatColor.DARK_GREEN + "Your WorldEditSelectionVisualizer has been enabled.");
					displaySelection(player);
				} else {
					player.sendMessage(ChatColor.DARK_RED + "Your WorldEditSelectionVisualizer has been disabled.");
					sendSelection(player, new ArrayList<Vector>());
				}
				
				return true;
			}
		} else {
			sender.sendMessage("Only a player can toggle his WESV.");
			return true;
		}
		
		return false;
	}
	
	public void displaySelection(Player player) {
		Region region = worldEditHelper.getSelectedRegion(player);
		List<Vector> locs = shapeHelper.getLocationsFromSelection(region);
		sendSelection(player, locs);
	}
	
	public void sendSelection(Player player, List<Vector> locs) {
		if (runningTasks.containsKey(player.getUniqueId())) {
			int alreadyRunningTaskId = runningTasks.get(player.getUniqueId());
			getServer().getScheduler().cancelTask(alreadyRunningTaskId);
		}
		
		int newTaskId = new ParticleUpdater(player, locs, config.particle()).runTaskTimer(this, 0, config.updateParticlesInterval()).getTaskId();
		runningTasks.put(player.getUniqueId(), newTaskId);
	}
	
	@EventHandler
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		
		if (config.checkForAxe() && config.isEnabled(player)) {
			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			
			if (item != null && item.getType() == config.selectionItem())
				displaySelection(player);
			else
				sendSelection(player, new ArrayList<Vector>());
		}
	}

    public Configuration config() {
        return config;
    }
}
