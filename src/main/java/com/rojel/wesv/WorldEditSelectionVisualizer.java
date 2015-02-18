package com.rojel.wesv;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.darkblade12.particleeffect.ParticleEffect;
import com.darkblade12.particleeffect.ParticleEffect16;
import com.darkblade12.particleeffect.ParticleEffect17;
import com.rojel.wesv.Metrics.Graph;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;

public class WorldEditSelectionVisualizer extends JavaPlugin implements Listener {
	private WorldEditPlugin we;
	private Map<UUID, Integer> runningTasks;
	private Map<UUID, Region> lastSelectedRegions;
	private ParticleEffect particle;
	private double gapBetweenPoints;
	private double verticalGap;
	private int updateParticlesInterval;
	private int updateSelectionInterval;
	private boolean cuboidLines;
	private boolean polygonLines;
	private boolean cylinderLines;
	private boolean ellipsoidLines;
	private boolean checkForAxe;
	private Material selectionItem;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
		we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
		runningTasks = new HashMap<UUID, Integer>();
		lastSelectedRegions = new HashMap<UUID, Region>();
		
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		
		particle = getParticleEffect(getConfig().getString("particleEffect"));
		gapBetweenPoints = getConfig().getDouble("gapBetweenPoints");
		verticalGap = getConfig().getDouble("verticalGap");
		updateParticlesInterval = getConfig().getInt("updateParticlesInterval");
		updateSelectionInterval = getConfig().getInt("updateSelectionInterval");
		
		cuboidLines = getConfig().getBoolean("horizontalLinesForCuboid");
		polygonLines = getConfig().getBoolean("horizontalLinesForPolygon");
		cylinderLines = getConfig().getBoolean("horizontalLinesForCylinder");
		ellipsoidLines = getConfig().getBoolean("horizontalLinesForEllipsoid");
		
		checkForAxe = getConfig().getBoolean("checkForAxe");
		selectionItem = Material.getMaterial(getConfig().getString("selectionItem"));
		if (selectionItem == null)
			selectionItem = Material.WOOD_AXE;
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : getServer().getOnlinePlayers()) {
					if (isEnabled(player) && player.hasPermission("wesv.use")) {
						Region lastRegion = lastSelectedRegions.get(player.getUniqueId());
						Region currentRegion = getSelectedRegion(player);
						
						if (!compareRegion(lastRegion, currentRegion)) {
							if (currentRegion != null)
								lastSelectedRegions.put(player.getUniqueId(), currentRegion.clone());
							else
								lastSelectedRegions.remove(player.getUniqueId());
							
							displaySelection(player);
						}
					}
				}
			}
		}, 0, updateSelectionInterval);
		
		initMetrics();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (label.equals("wesv")) {
				boolean isEnabled = !isEnabled(player);
				getConfig().set("players." + player.getUniqueId().toString(), isEnabled);
				if (isEnabled) {
					player.sendMessage(ChatColor.DARK_GREEN + "Your WorldEditSelectionVisualizer has been enabled.");
					displaySelection(player);
				} else {
					player.sendMessage(ChatColor.DARK_RED + "Your WorldEditSelectionVisualizer has been disabled.");
					sendSelection(player, new ArrayList<Vector>());
				}
				
				saveConfig();
				
				return true;
			}
		} else {
			sender.sendMessage("Only a player can toggle his WESV.");
			return true;
		}
		
		return false;
	}
	
	public void displaySelection(Player player) {
		Region region = getSelectedRegion(player);
		List<Vector> locs = getLocationsFromSelection(region);
		sendSelection(player, locs);
	}
	
	public Region getSelectedRegion(Player player) {
		LocalSession session = we.getWorldEdit().getSession(player.getName());
		LocalWorld world = BukkitUtil.getLocalWorld(player.getWorld());
		
		if (session != null) {
			RegionSelector selector = session.getRegionSelector(world);
			
			if (selector.isDefined()) {
				try {
					return selector.getRegion();
				} catch (IncompleteRegionException e) {
					getServer().getLogger().info("Region still incomplete.");
				}
			}
		}
		
		return null;
	}
	
	public List<Vector> getLocationsFromSelection(Region region) {
		List<Vector> locs = new ArrayList<Vector>();
		
		if (region != null) {
			Vector min = region.getMinimumPoint();
			Vector max = region.getMaximumPoint().add(1, 1, 1);
			
			int width = region.getWidth();
			int length = region.getLength();
			int height = region.getHeight();
			
			if (region instanceof CuboidRegion) {
				List<Vector> bottomCorners = new ArrayList<Vector>();
				bottomCorners.add(new Vector(min.getX(), min.getY(), min.getZ()));
				bottomCorners.add(new Vector(max.getX(), min.getY(), min.getZ()));
				bottomCorners.add(new Vector(max.getX(), min.getY(), max.getZ()));
				bottomCorners.add(new Vector(min.getX(), min.getY(), max.getZ()));
				
				for (int i = 0; i < bottomCorners.size(); i++) {
					Vector p1 = bottomCorners.get(i);
					Vector p2;
					
					if (i + 1 < bottomCorners.size())
						p2 = bottomCorners.get(i + 1);
					else
						p2 = bottomCorners.get(0);
					
					Vector p3 = p1.add(0, height, 0);
					Vector p4 = p2.add(0, height, 0);
					
					locs.addAll(plotLine(p1, p2));
					locs.addAll(plotLine(p3, p4));
					locs.addAll(plotLine(p1, p3));
					
					if (cuboidLines) {
						for (double offset = verticalGap; offset < height; offset += verticalGap) {
							Vector p5 = p1.add(0, offset, 0);
							Vector p6 = p2.add(0, offset, 0);
							
							locs.addAll(plotLine(p5, p6));
						}
					}
				}
			} else if (region instanceof Polygonal2DRegion) {
				Polygonal2DRegion polyRegion = (Polygonal2DRegion) region;
				
				List<Vector> bottomCorners = new ArrayList<Vector>();
				
				for (BlockVector2D vec2D : polyRegion.getPoints()) {
					bottomCorners.add(new Vector(vec2D.getX() + 0.5, min.getY(), vec2D.getZ() + 0.5));
				}
				
				for (int i = 0; i < bottomCorners.size(); i++) {
					Vector p1 = bottomCorners.get(i);
					Vector p2;
					
					if (i + 1 < bottomCorners.size())
						p2 = bottomCorners.get(i + 1);
					else
						p2 = bottomCorners.get(0);
					
					Vector p3 = p1.add(0, height, 0);
					Vector p4 = p2.add(0, height, 0);
					
					locs.addAll(plotLine(p1, p2));
					locs.addAll(plotLine(p3, p4));
					locs.addAll(plotLine(p1, p3));
					
					if (polygonLines) {
						for (double offset = verticalGap; offset < height; offset += verticalGap) {
							Vector p5 = p1.add(0, offset, 0);
							Vector p6 = p2.add(0, offset, 0);
							
							locs.addAll(plotLine(p5, p6));
						}
					}
				}
			} else if (region instanceof CylinderRegion) {
				CylinderRegion cylRegion = (CylinderRegion) region;
				Vector center = new Vector(cylRegion.getCenter().getX() + 0.5, min.getY(), cylRegion.getCenter().getZ() + 0.5);
				
				double rx = width / 2.0;
				double rz = length / 2.0;
				
				List<Vector> bottomCorners = plotEllipse(center, new Vector(rx, 0, rz));
				locs.addAll(bottomCorners);
				
				for (Vector vec : bottomCorners) {
					locs.add(vec.add(0, height, 0));
				}
				
				Vector p1 = new Vector((max.getX() + min.getX()) / 2.0, min.getY(), min.getZ());
				Vector p2 = new Vector((max.getX() + min.getX()) / 2.0, min.getY(), max.getZ());
				Vector p3 = new Vector(min.getX(), min.getY(), (max.getZ() + min.getZ()) / 2.0);
				Vector p4 = new Vector(max.getX(), min.getY(), (max.getZ() + min.getZ()) / 2.0);
				
				locs.addAll(plotLine(p1, p1.add(0, height, 0)));
				locs.addAll(plotLine(p2, p2.add(0, height, 0)));
				locs.addAll(plotLine(p3, p3.add(0, height, 0)));
				locs.addAll(plotLine(p4, p4.add(0, height, 0)));
				
				if (cylinderLines) {
					for (double offset = verticalGap; offset < height; offset += verticalGap) {
						for (Vector vec : bottomCorners) {
							locs.add(vec.add(0, offset, 0));
						}
					}
				}
			} else if (region instanceof EllipsoidRegion) {
				EllipsoidRegion ellRegion = (EllipsoidRegion) region;
				Vector ellRadius = ellRegion.getRadius().add(0.5, 0.5, 0.5);
				
				Vector center = new Vector(min.getX() + width / 2.0, min.getY() + height / 2.0, min.getZ() + length / 2.0);
				locs.addAll(plotEllipse(center, new Vector(0, ellRadius.getY(), ellRadius.getZ())));
				locs.addAll(plotEllipse(center, new Vector(ellRadius.getX(), 0, ellRadius.getZ())));
				locs.addAll(plotEllipse(center, new Vector(ellRadius.getX(), ellRadius.getY(), 0)));
				
				if (ellipsoidLines) {
					for (double offset = verticalGap; offset < ellRadius.getY(); offset += verticalGap) {
						Vector center1 = new Vector(center.getX(), center.getY() - offset, center.getZ());
						Vector center2 = new Vector(center.getX(), center.getY() + offset, center.getZ());
						double difference = Math.abs(center1.getY() - center.getY());
						double radiusRatio = Math.cos(Math.asin(difference / ellRadius.getY()));
						
						double rx = ellRadius.getX() * radiusRatio;
						double rz = ellRadius.getZ() * radiusRatio;
						
						locs.addAll(plotEllipse(center1, new Vector(rx, 0, rz)));
						locs.addAll(plotEllipse(center2, new Vector(rx, 0, rz)));
					}
				}
			}
		}
		
		return locs;
	}
	
	public void sendSelection(Player player, List<Vector> locs) {
		if (runningTasks.containsKey(player.getUniqueId())) {
			int alreadyRunningTaskId = runningTasks.get(player.getUniqueId());
			getServer().getScheduler().cancelTask(alreadyRunningTaskId);
		}
		
		int newTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new ParticleUpdater(player, locs, particle), 0, updateParticlesInterval);
		runningTasks.put(player.getUniqueId(), newTaskId);
	}
	
	public List<Vector> plotLine(Vector p1, Vector p2) {
		List<Vector> locs = new ArrayList<Vector>();
		int points = (int) (p1.distance(p2) / gapBetweenPoints) + 1;
		
		double length = p1.distance(p2);
		double gap = length / (points - 1);
		
		Vector gapVector = p2.subtract(p1).normalize().multiply(gap);
		
		for (int i = 0; i < points; i++) {
			Vector currentPoint = p1.add(gapVector.multiply(i));
			locs.add(currentPoint);
		}
		
		return locs;
	}
	
	public List<Vector> plotEllipse(Vector center, Vector radius) {
		List<Vector> locs = new ArrayList<Vector>();
		
		double biggestR = Math.max(radius.getX(), Math.max(radius.getY(), radius.getZ()));
		double circleCircumference = 2 * biggestR * Math.PI;
		double deltaTheta = gapBetweenPoints / circleCircumference;
		
		for (double i = 0; i < 1; i += deltaTheta) {
			double x = center.getX();
			double y = center.getY();
			double z = center.getZ();
			
			if (radius.getX() == 0) {
				y = center.getY() + Math.cos(i * 2 * Math.PI) * radius.getY();
				z = center.getZ() + Math.sin(i * 2 * Math.PI) * radius.getZ();
			} else if (radius.getY() == 0) {
				x = center.getX() + Math.cos(i * 2 * Math.PI) * radius.getX();
				z = center.getZ() + Math.sin(i * 2 * Math.PI) * radius.getZ();
			} else if (radius.getZ() == 0) {
				x = center.getX() + Math.cos(i * 2 * Math.PI) * radius.getX();
				y = center.getY() + Math.sin(i * 2 * Math.PI) * radius.getY();
			}
			
			Vector loc = new Vector(x, y, z);
			locs.add(loc);
		}
		
		return locs;
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
	
	public boolean isEnabled(Player player) {
		String path = "players." + player.getUniqueId().toString();
		getConfig().addDefault(path, true);
		
		return getConfig().getBoolean(path);
	}
	
	public ParticleEffect getParticleEffect(String name) {
		Class<? extends ParticleEffect> particleEffectClass = ParticleEffect17.class;
		
		if (isOlderThanMinecraftVersion(Bukkit.getVersion(), "1.6.4"))
			particleEffectClass = ParticleEffect16.class;
		
		Field[] fields = particleEffectClass.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.getName().replaceAll("[^a-zA-Z0-9]", "").equalsIgnoreCase(name.replaceAll("[^a-zA-Z0-9]", ""))) {
				Object fieldContent = null;
				try {
					fieldContent = field.get(particleEffectClass);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				if (fieldContent != null && fieldContent instanceof ParticleEffect) {
					return (ParticleEffect) fieldContent;
				}
			}
		}
		
		getLogger().warning("The particle effect set in the configuration file is invalid.");
		
		if (isOlderThanMinecraftVersion(Bukkit.getVersion(), "1.6.4"))
			return ParticleEffect16.RED_DUST;
		
		return ParticleEffect17.RED_DUST;
	}
	
	public boolean isOlderThanMinecraftVersion(String bukkitVersion, String minecraftVersion) {
		String[] strings = bukkitVersion.split(" ");
		String mc = strings[2].substring(0, strings[2].length() - 1);
		
		String[] subVersionsStrings = mc.split("\\.");
		int[] subVersions = new int[subVersionsStrings.length];
		
		for (int i = 0; i < subVersions.length; i++) {
			subVersions[i] = Integer.parseInt(subVersionsStrings[i]);
		}
		
		String[] mcSubVersionsStrings = minecraftVersion.split("\\.");
		int[] mcSubVersions = new int[mcSubVersionsStrings.length];
		
		for (int i = 0; i < mcSubVersions.length; i++) {
			mcSubVersions[i] = Integer.parseInt(mcSubVersionsStrings[i]);
		}
		
		for (int i = 0; i < Math.max(mcSubVersions.length, subVersions.length); i++) {
			
			if (i >= subVersions.length && i < mcSubVersions.length)
				return true;
			if (i >= mcSubVersions.length && i < subVersions.length)
				return false;
			
			if (subVersions[i] > mcSubVersions[i])
				return false;
		}
		
		return true;
	}
	
	private void initMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			
			Graph cuboidGraph = metrics.createGraph("Horizontal lines for cuboid selections");
			cuboidGraph.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					if (cuboidLines)
						return 1;
					else
						return 0;
				}
			});
			cuboidGraph.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					if (!cuboidLines)
						return 1;
					else
						return 0;
				}
			});
			
			Graph polygonGraph = metrics.createGraph("Horizontal lines for polygon selections");
			polygonGraph.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					if (polygonLines)
						return 1;
					else
						return 0;
				}
			});
			polygonGraph.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					if (!polygonLines)
						return 1;
					else
						return 0;
				}
			});
			
			Graph cylinderGraph = metrics.createGraph("Horizontal lines for cylinder selections");
			cylinderGraph.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					if (cylinderLines)
						return 1;
					else
						return 0;
				}
			});
			cylinderGraph.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					if (!cylinderLines)
						return 1;
					else
						return 0;
				}
			});
			
			Graph ellipsoidGraph = metrics.createGraph("Horizontal lines for ellipsoid selections");
			ellipsoidGraph.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					if (ellipsoidLines)
						return 1;
					else
						return 0;
				}
			});
			ellipsoidGraph.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					if (!ellipsoidLines)
						return 1;
					else
						return 0;
				}
			});
			
			Graph pointGapGraph = metrics.createGraph("Gap between points");
			pointGapGraph.addPlotter(new Metrics.Plotter(gapBetweenPoints + "") {
				@Override
				public int getValue() {
					return 1;
				}
			});
			
			Graph verticalGapGraph = metrics.createGraph("Vertical gap between horizontal filling lines");
			verticalGapGraph.addPlotter(new Metrics.Plotter(verticalGap + "") {
				@Override
				public int getValue() {
					return 1;
				}
			});
			
			Graph particleIntervalGraph = metrics.createGraph("Particle update interval");
			particleIntervalGraph.addPlotter(new Metrics.Plotter(updateParticlesInterval + "") {
				@Override
				public int getValue() {
					return 1;
				}
			});
			
			Graph selectionIntervalGraph = metrics.createGraph("Selection update interval");
			selectionIntervalGraph.addPlotter(new Metrics.Plotter(updateSelectionInterval + "") {
				@Override
				public int getValue() {
					return 1;
				}
			});
			
			metrics.start();
		} catch (IOException e) {
			getLogger().info("Unable to submit statistics to MCStats :(");
		}
	}
	
	@EventHandler
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		
		if (checkForAxe && isEnabled(player)) {
			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			
			if (item != null && item.getType() == selectionItem)
				displaySelection(player);
			else
				sendSelection(player, new ArrayList<Vector>());
		}
	}
}