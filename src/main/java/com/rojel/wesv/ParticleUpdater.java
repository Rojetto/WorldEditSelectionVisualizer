package com.rojel.wesv;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.darkblade12.particleeffect.ParticleEffect;
import com.sk89q.worldedit.Vector;

public class ParticleUpdater extends BukkitRunnable {
	private Player player;
	private List<Vector> locs;
	private ParticleEffect effect;
	
	public ParticleUpdater(Player player, List<Vector> locs, ParticleEffect effect) {
		this.player = player;
		this.locs = locs;
		this.effect = effect;
	}
	
	@Override
	public void run() {
		for (Vector vec : locs) {
			Location loc = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
			if (loc.distance(player.getLocation()) <= 20)
                effect.display(0, 0, 0, 0, 1, loc, player);
		}
	}
}
