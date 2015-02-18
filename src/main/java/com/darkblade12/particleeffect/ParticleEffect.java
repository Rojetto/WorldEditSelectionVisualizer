package com.darkblade12.particleeffect;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ParticleEffect {
	
	/**
	 * @return The name of this particle effect
	 */
	public abstract String getName();
	
	/**
	 * Displays a particle effect which is only visible for the specified players
	 * 
	 * @param center Center location of the effect
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param players Receivers of the effect
	 * @see #sendPacket
	 * @see #instantiatePacket
	 */
	public abstract void display(Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Player... players);
	
	/**
	 * Displays a particle effect which is only visible for all players within a certain range in the world of @param center
	 * 
	 * @param center Center location of the effect
	 * @param range Range of the visibility
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param players Receivers of the effect
	 * @throws @IllegalArgumentException if the range is higher than 20
	 * @see #sendPacket
	 * @see #instantiatePacket
	 */
	public abstract void display(Location center, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount);
	
	/**
	 * Displays a particle effect which is only visible for all players within a range of 20 in the world of @param center
	 * 
	 * @param center Center location of the effect
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param players Receivers of the effect
	 * @see #display(Location, double, float, float, float, float, int)
	 */
	public abstract void display(Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount);
	
}