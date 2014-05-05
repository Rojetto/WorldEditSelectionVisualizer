package com.rojel.worldeditsv;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.Vector;

public class ParticleUpdater extends BukkitRunnable {
	private Player player;
	private List<Vector> locs;
	
	public ParticleUpdater(Player player, List<Vector> locs) {
		this.player = player;
		this.locs = locs;
	}
	
	@Override
	public void run() {
		for (Vector vec : locs) {
			Location loc = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
			if (loc.distance(player.getLocation()) <= 20)
				WorldEditSV.PARTICLE.display(loc, 0, 0, 0, 0, 1, player);
		}
	}
}
